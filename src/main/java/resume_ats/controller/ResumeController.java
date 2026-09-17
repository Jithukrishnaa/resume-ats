package resume_ats.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import resume_ats.entity.Resume;
import resume_ats.repository.ResumeRepository;
import resume_ats.service.ResumeService;
import resume_ats.service.SupabaseStorageService;
import resume_ats.util.parser.ResumeParser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.UUID;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

        // =========================================================
        // DEPENDENCIES
        // =========================================================

        private final ResumeRepository resumeRepository;
        private final ResumeService resumeService;
        private final SupabaseStorageService supabaseStorageService;

        // =========================================================
        // CONSTRUCTOR
        // =========================================================

        public ResumeController(
                        ResumeRepository resumeRepository,
                        ResumeService resumeService,
                        SupabaseStorageService supabaseStorageService) {

                this.resumeRepository = resumeRepository;
                this.resumeService = resumeService;
                this.supabaseStorageService = supabaseStorageService;
        }

        // =========================================================
        // UPLOAD RESUME
        // =========================================================
        //
        // POST /api/resumes/upload
        //
        // Flow:
        //
        // 1. Validate PDF
        // 2. Read uploaded bytes
        // 3. Generate SHA-256 hash
        // 4. Check duplicate
        // 5. Create temporary local file
        // 6. Extract resume text
        // 7. Parse resume
        // 8. Upload PDF to Supabase
        // 9. Save Supabase path + hash in PostgreSQL
        // 10. Delete temporary local file
        //
        // =========================================================

        @PostMapping("/upload")
        public ResponseEntity<?> uploadResume(
                        @RequestParam("file") MultipartFile file) {

                File tempFile = null;
                String uploadedObjectPath = null;

                try {

                        // =====================================================
                        // 1. VALIDATE FILE
                        // =====================================================

                        if (file == null || file.isEmpty()) {

                                return ResponseEntity
                                                .badRequest()
                                                .body("Please upload a resume file.");
                        }

                        String originalFilename = file.getOriginalFilename();

                        if (originalFilename == null
                                        || originalFilename.isBlank()) {

                                return ResponseEntity
                                                .badRequest()
                                                .body("Invalid file name.");
                        }

                        // =====================================================
                        // 2. ONLY PDF FOR NOW
                        // =====================================================

                        String fileName = sanitizeFileName(originalFilename);

                        if (!fileName.toLowerCase().endsWith(".pdf")) {

                                return ResponseEntity
                                                .badRequest()
                                                .body("Only PDF resumes are supported.");
                        }

                        // =====================================================
                        // 3. SUPABASE CURRENT BUCKET LIMIT = 50 MB
                        // =====================================================

                        long maxFileSize = 50L * 1024L * 1024L;

                        if (file.getSize() > maxFileSize) {

                                return ResponseEntity
                                                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                                                .body(
                                                                "Resume file is too large. "
                                                                                + "Maximum allowed size is 50 MB.");
                        }

                        System.out.println("----------------------------------");
                        System.out.println("Resume upload started");
                        System.out.println("Original file : " + fileName);
                        System.out.println("File size     : " + file.getSize());
                        System.out.println("----------------------------------");

                        // =====================================================
                        // 4. READ FILE BYTES
                        // =====================================================

                        byte[] fileBytes = file.getBytes();

                        if (fileBytes.length == 0) {

                                return ResponseEntity
                                                .badRequest()
                                                .body("Uploaded file is empty.");
                        }

                        // =====================================================
                        // 5. GENERATE SHA-256 HASH
                        // =====================================================

                        String resumeHash = generateSHA256(fileBytes);

                        System.out.println(
                                        "Resume SHA-256 : " + resumeHash);

                        // =====================================================
                        // 6. CHECK EXACT FILE DUPLICATE
                        // =====================================================

                        if (resumeRepository.existsByResumeHash(resumeHash)) {

                                System.out.println(
                                                "Duplicate resume detected using SHA-256.");

                                return ResponseEntity
                                                .ok(
                                                                "Resume already exists. "
                                                                                + "The same resume file has already been uploaded.");
                        }

                        // =====================================================
                        // 7. CREATE TEMPORARY FILE
                        // =====================================================
                        //
                        // We still use your existing ResumeService, which
                        // expects a java.io.File.
                        //
                        // This file is ONLY temporary.
                        //
                        // It is NOT used as permanent storage.
                        //
                        // =====================================================

                        Path tempDirectory = Files.createTempDirectory("resume-ats-");

                        Path tempFilePath = tempDirectory.resolve(fileName);

                        Files.write(
                                        tempFilePath,
                                        fileBytes);

                        tempFile = tempFilePath.toFile();

                        System.out.println(
                                        "Temporary file : "
                                                        + tempFile.getAbsolutePath());

                        // =====================================================
                        // 8. EXTRACT RESUME TEXT
                        // =====================================================

                        String extractedText = resumeService.extractText(tempFile);

                        if (extractedText == null
                                        || extractedText.isBlank()) {

                                deleteTempFile(tempFile);

                                return ResponseEntity
                                                .badRequest()
                                                .body(
                                                                "Could not extract text from the resume. "
                                                                                + "Please upload a readable PDF.");
                        }

                        // =====================================================
                        // 9. LIMIT EXTRACTED TEXT
                        // =====================================================

                        if (extractedText.length() > 50000) {

                                extractedText = extractedText.substring(0, 50000);
                        }

                        // =====================================================
                        // 10. PARSE RESUME
                        // =====================================================

                        Resume resume = ResumeParser.parse(
                                        extractedText,
                                        fileName);

                        if (resume == null) {

                                deleteTempFile(tempFile);

                                return ResponseEntity
                                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .body(
                                                                "Unable to parse the uploaded resume.");
                        }

                        // =====================================================
                        // 11. EMAIL DUPLICATE CHECK
                        // =====================================================

                        if (resume.getEmail() != null
                                        && !resume.getEmail().isBlank()
                                        && resumeRepository.existsByEmailIgnoreCase(
                                                        resume.getEmail())) {

                                System.out.println(
                                                "Duplicate resume detected using email : "
                                                                + resume.getEmail());

                                deleteTempFile(tempFile);

                                return ResponseEntity
                                                .ok(
                                                                "Resume already exists for : "
                                                                                + resume.getEmail());
                        }

                        // =====================================================
                        // 12. SET RESUME INFORMATION
                        // =====================================================

                        resume.setFileName(fileName);

                        resume.setResumeHash(resumeHash);

                        resume.setRawText(extractedText);

                        // =====================================================
                        // 13. CREATE UNIQUE SUPABASE OBJECT PATH
                        // =====================================================
                        //
                        // Example:
                        //
                        // resumes/550e8400-e29b-41d4-a716-446655440000.pdf
                        //
                        // The UUID prevents filename collisions.
                        //
                        // =====================================================

                        String uniqueFileName = UUID.randomUUID()
                                        + ".pdf";

                        uploadedObjectPath = "resumes/" + uniqueFileName;

                        System.out.println(
                                        "Supabase object : "
                                                        + uploadedObjectPath);

                        // =====================================================
                        // 14. UPLOAD PDF TO SUPABASE
                        // =====================================================

                        supabaseStorageService.uploadBytes(
                                        fileBytes,
                                        uploadedObjectPath,
                                        "application/pdf");

                        System.out.println(
                                        "Resume uploaded to Supabase successfully.");

                        // =====================================================
                        // 15. STORE SUPABASE PATH IN DATABASE
                        // =====================================================
                        //
                        // IMPORTANT:
                        //
                        // filePath no longer contains:
                        //
                        // Uploads/extracted/resume.pdf
                        //
                        // It now contains:
                        //
                        // resumes/UUID.pdf
                        //
                        // =====================================================

                        resume.setFilePath(
                                        uploadedObjectPath);

                        // =====================================================
                        // 16. SAVE RESUME TO POSTGRESQL
                        // =====================================================

                        Resume savedResume = resumeRepository.save(resume);

                        System.out.println(
                                        "Resume saved to PostgreSQL.");
                        System.out.println(
                                        "Resume ID : "
                                                        + savedResume.getId());

                        // =====================================================
                        // 17. DELETE TEMPORARY FILE
                        // =====================================================

                        deleteTempFile(tempFile);

                        // =====================================================
                        // 18. SUCCESS RESPONSE
                        // =====================================================

                        StringBuilder response = new StringBuilder();

                        response.append(
                                        "Resume uploaded and parsed successfully.");

                        response.append(
                                        "\nCandidate Name : "
                                                        + nullSafe(savedResume.getCandidateName()));

                        response.append(
                                        "\nEmail : "
                                                        + nullSafe(savedResume.getEmail()));

                        response.append(
                                        "\nSkills : "
                                                        + nullSafe(savedResume.getSkills()));

                        response.append(
                                        "\nExperience : "
                                                        + nullSafe(
                                                                        savedResume.getExperienceYears())
                                                        + " years");

                        response.append(
                                        "\nEducation : "
                                                        + nullSafe(savedResume.getEducation()));

                        response.append(
                                        "\nCertifications : "
                                                        + nullSafe(
                                                                        savedResume.getCertifications()));

                        response.append(
                                        "\nProjects : "
                                                        + nullSafe(
                                                                        savedResume.getProjectCount()));

                        response.append(
                                        "\nStored in Supabase : YES");

                        response.append(
                                        "\nResume ID : "
                                                        + savedResume.getId());

                        return ResponseEntity
                                        .ok(response.toString());

                } catch (IOException e) {

                        e.printStackTrace();

                        // =====================================================
                        // CLEANUP
                        // =====================================================

                        deleteTempFile(tempFile);

                        // If Supabase upload succeeded but something failed
                        // afterward, remove the orphaned cloud file.
                        if (uploadedObjectPath != null) {

                                try {

                                        supabaseStorageService
                                                        .deleteFile(uploadedObjectPath);

                                } catch (Exception cleanupException) {

                                        cleanupException.printStackTrace();
                                }
                        }

                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(
                                                        "Unable to process resume: "
                                                                        + e.getMessage());

                } catch (Exception e) {

                        e.printStackTrace();

                        // =====================================================
                        // CLEANUP
                        // =====================================================

                        deleteTempFile(tempFile);

                        if (uploadedObjectPath != null) {

                                try {

                                        supabaseStorageService
                                                        .deleteFile(uploadedObjectPath);

                                } catch (Exception cleanupException) {

                                        cleanupException.printStackTrace();
                                }
                        }

                        // =====================================================
                        // HANDLE DATABASE UNIQUE HASH ERROR
                        // =====================================================

                        String errorMessage = e.getMessage() != null
                                        ? e.getMessage().toLowerCase()
                                        : "";

                        if (errorMessage.contains("resume_hash")
                                        || errorMessage.contains("duplicate key")
                                        || errorMessage.contains("unique constraint")) {

                                return ResponseEntity
                                                .ok(
                                                                "Resume already exists. "
                                                                                + "The same resume file has already been uploaded.");
                        }

                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(
                                                        "Unable to process resume: "
                                                                        + e.getMessage());
                }
        }

        // =========================================================
        // GET ALL RESUMES
        // =========================================================

        @GetMapping
        public ResponseEntity<?> getAllResumes() {

                try {

                        return ResponseEntity.ok(
                                        resumeRepository.findAll());

                } catch (Exception e) {

                        e.printStackTrace();

                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(
                                                        "Unable to fetch resumes.");
                }
        }

        // =========================================================
        // SHA-256 GENERATOR
        // =========================================================

        private String generateSHA256(byte[] data)
                        throws Exception {

                MessageDigest digest = MessageDigest.getInstance("SHA-256");

                byte[] hash = digest.digest(data);

                StringBuilder hexString = new StringBuilder();

                for (byte b : hash) {

                        String hex = Integer.toHexString(
                                        0xff & b);

                        if (hex.length() == 1) {
                                hexString.append('0');
                        }

                        hexString.append(hex);
                }

                return hexString.toString();
        }

        // =========================================================
        // TEMPORARY FILE CLEANUP
        // =========================================================

        private void deleteTempFile(File file) {

                if (file == null) {
                        return;
                }

                try {

                        if (file.exists()) {

                                Files.deleteIfExists(
                                                file.toPath());

                                System.out.println(
                                                "Temporary file deleted : "
                                                                + file.getAbsolutePath());
                        }

                        // Delete temporary parent directory if empty
                        Path parent = file.toPath().getParent();

                        if (parent != null) {

                                try {

                                        Files.deleteIfExists(parent);

                                } catch (Exception ignored) {
                                        // Directory may already be removed.
                                }
                        }

                } catch (Exception e) {

                        System.out.println(
                                        "Unable to delete temporary file: "
                                                        + e.getMessage());
                }
        }

        // =========================================================
        // FILE NAME SANITIZATION
        // =========================================================

        private String sanitizeFileName(String fileName) {

                if (fileName == null
                                || fileName.isBlank()) {

                        return "resume.pdf";
                }

                fileName = fileName.replace("\\", "/");

                int lastSlash = fileName.lastIndexOf("/");

                if (lastSlash >= 0) {

                        fileName = fileName.substring(
                                        lastSlash + 1);
                }

                fileName = fileName
                                .replace("\"", "")
                                .replace("\r", "")
                                .replace("\n", "");

                if (fileName.isBlank()) {

                        return "resume.pdf";
                }

                return fileName;
        }

        // =========================================================
        // NULL SAFE VALUE
        // =========================================================

        private String nullSafe(Object value) {

                return value == null
                                ? ""
                                : String.valueOf(value);
        }
}