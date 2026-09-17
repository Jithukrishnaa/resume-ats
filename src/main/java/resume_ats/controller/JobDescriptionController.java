package resume_ats.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import resume_ats.entity.ATSResult;
import resume_ats.entity.JobDescription;
import resume_ats.repository.ATSResultRepository;
import resume_ats.repository.JobDescriptionRepository;
import resume_ats.service.ATSMatchingService;
import resume_ats.service.ResumeService;
import resume_ats.service.SupabaseStorageService;
import resume_ats.util.parser.JobDescriptionParser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobdescriptions")
public class JobDescriptionController {

        private static final long MAX_FILE_SIZE = 50L * 1024L * 1024L;

        private final JobDescriptionRepository jobDescriptionRepository;
        private final ATSResultRepository atsResultRepository;
        private final ResumeService resumeService;
        private final ATSMatchingService atsMatchingService;
        private final SupabaseStorageService supabaseStorageService;

        public JobDescriptionController(
                        JobDescriptionRepository jobDescriptionRepository,
                        ATSResultRepository atsResultRepository,
                        ResumeService resumeService,
                        ATSMatchingService atsMatchingService,
                        SupabaseStorageService supabaseStorageService) {

                this.jobDescriptionRepository = jobDescriptionRepository;
                this.atsResultRepository = atsResultRepository;
                this.resumeService = resumeService;
                this.atsMatchingService = atsMatchingService;
                this.supabaseStorageService = supabaseStorageService;
        }

        // =========================================================
        // UPLOAD JD
        // =========================================================

        @PostMapping("/upload")
        public ResponseEntity<?> uploadJobDescription(
                        @RequestParam("file") MultipartFile file) {

                String supabaseObjectPath = null;
                Path temporaryFile = null;

                try {

                        // =====================================================
                        // 1. VALIDATE FILE
                        // =====================================================

                        if (file == null || file.isEmpty()) {

                                return ResponseEntity
                                                .badRequest()
                                                .body("Please select a Job Description PDF.");
                        }

                        if (file.getSize() > MAX_FILE_SIZE) {

                                return ResponseEntity
                                                .badRequest()
                                                .body("JD file is too large. Maximum allowed size is 50 MB.");
                        }

                        String originalFileName = file.getOriginalFilename();

                        if (originalFileName == null
                                        || originalFileName.isBlank()) {

                                return ResponseEntity
                                                .badRequest()
                                                .body("Invalid Job Description file.");
                        }

                        String safeFileName = new File(originalFileName).getName();

                        if (!safeFileName.toLowerCase().endsWith(".pdf")) {

                                return ResponseEntity
                                                .badRequest()
                                                .body("Only PDF Job Description files are allowed.");
                        }

                        // =====================================================
                        // 2. CREATE TEMPORARY FILE
                        // =====================================================

                        Path tempDirectory = Files.createTempDirectory("resume-ats-jd-");

                        temporaryFile = tempDirectory.resolve(safeFileName);

                        Files.write(
                                        temporaryFile,
                                        file.getBytes());

                        File destination = temporaryFile.toFile();

                        System.out.println(
                                        "Temporary JD file : "
                                                        + destination.getAbsolutePath());

                        // =====================================================
                        // 3. EXTRACT JD TEXT
                        // =====================================================

                        String jdText = resumeService.extractText(destination);

                        if (jdText == null
                                        || jdText.trim().isEmpty()) {

                                return ResponseEntity
                                                .badRequest()
                                                .body("Unable to extract text from this JD PDF.");
                        }

                        // Limit very large extracted text
                        if (jdText.length() > 50000) {

                                jdText = jdText.substring(0, 50000);
                        }

                        // =====================================================
                        // 4. GENERATE NORMALIZED JD HASH
                        // =====================================================

                        String normalizedText = normalizeJdText(jdText);

                        String jdHash = generateSHA256(normalizedText);

                        System.out.println(
                                        "JD SHA-256 : " + jdHash);

                        // =====================================================
                        // 5. DUPLICATE CHECK
                        // =====================================================

                        boolean duplicate = jobDescriptionRepository.existsByJdHash(jdHash);

                        if (duplicate) {

                                System.out.println(
                                                "Duplicate JD rejected : "
                                                                + safeFileName);

                                return ResponseEntity
                                                .status(HttpStatus.CONFLICT)
                                                .body(
                                                                "This Job Description already exists in the JD Library.");
                        }

                        // =====================================================
                        // 6. PARSE JD
                        // =====================================================

                        JobDescription jobDescription = JobDescriptionParser.parse(jdText);

                        if (jobDescription == null) {

                                return ResponseEntity
                                                .badRequest()
                                                .body("Unable to parse Job Description.");
                        }

                        // =====================================================
                        // 7. CREATE SUPABASE OBJECT PATH
                        // =====================================================

                        supabaseObjectPath = "job-descriptions/"
                                        + UUID.randomUUID()
                                        + ".pdf";

                        System.out.println(
                                        "Supabase object : "
                                                        + supabaseObjectPath);

                        // =====================================================
                        // 8. UPLOAD JD TO SUPABASE
                        // =====================================================

                        supabaseStorageService.uploadBytes(
                                        file.getBytes(),
                                        supabaseObjectPath,
                                        "application/pdf");

                        System.out.println(
                                        "JD uploaded to Supabase successfully.");

                        // =====================================================
                        // 9. SET JD INFORMATION
                        // =====================================================

                        jobDescription.setFileName(
                                        safeFileName);

                        // IMPORTANT:
                        // Store Supabase object path, NOT local path.
                        jobDescription.setFilePath(
                                        supabaseObjectPath);

                        jobDescription.setJdText(
                                        jdText);

                        jobDescription.setJdHash(
                                        jdHash);

                        // =====================================================
                        // 10. MAKE NEW JD ACTIVE
                        // =====================================================

                        jobDescriptionRepository.deactivateAll();

                        jobDescription.setActive(true);

                        // =====================================================
                        // 11. SAVE TO POSTGRESQL
                        // =====================================================

                        JobDescription saved = jobDescriptionRepository.save(
                                        jobDescription);

                        System.out.println(
                                        "JD saved to PostgreSQL.");

                        System.out.println(
                                        "JD ID : " + saved.getId());

                        // =====================================================
                        // 12. RUN ATS
                        // =====================================================

                        try {

                                atsMatchingService.runATS();

                        } catch (Exception atsException) {

                                atsException.printStackTrace();

                                return ResponseEntity
                                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .body(
                                                                "JD uploaded successfully, but ATS execution failed: "
                                                                                + atsException.getMessage());
                        }

                        // =====================================================
                        // 13. SUCCESS
                        // =====================================================

                        System.out.println(
                                        "====================================");

                        System.out.println(
                                        "JD UPLOADED SUCCESSFULLY");

                        System.out.println(
                                        "JD ID       : "
                                                        + saved.getId());

                        System.out.println(
                                        "JD File     : "
                                                        + saved.getFileName());

                        System.out.println(
                                        "JD Title    : "
                                                        + saved.getTitle());

                        System.out.println(
                                        "JD Hash     : "
                                                        + saved.getJdHash());

                        System.out.println(
                                        "Supabase    : "
                                                        + saved.getFilePath());

                        System.out.println(
                                        "ATS         : EXECUTED");

                        System.out.println(
                                        "====================================");

                        return ResponseEntity.ok(
                                        "Job Description uploaded successfully. "
                                                        + "ATS executed and existing resumes were re-evaluated.");

                } catch (Exception e) {

                        e.printStackTrace();

                        // =====================================================
                        // CLEANUP SUPABASE FILE IF DATABASE SAVE FAILED
                        // =====================================================

                        if (supabaseObjectPath != null) {

                                try {

                                        if (supabaseStorageService.fileExists(
                                                        supabaseObjectPath)) {

                                                supabaseStorageService.deleteFile(
                                                                supabaseObjectPath);

                                                System.out.println(
                                                                "Supabase JD deleted after failure : "
                                                                                + supabaseObjectPath);
                                        }

                                } catch (Exception cleanupException) {

                                        cleanupException.printStackTrace();
                                }
                        }

                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(
                                                        "Job Description upload failed: "
                                                                        + e.getMessage());

                } finally {

                        // =====================================================
                        // DELETE TEMPORARY FILE
                        // =====================================================

                        if (temporaryFile != null) {

                                try {

                                        Files.deleteIfExists(
                                                        temporaryFile);

                                        Path parent = temporaryFile.getParent();

                                        if (parent != null) {

                                                Files.deleteIfExists(parent);
                                        }

                                } catch (Exception cleanupException) {

                                        cleanupException.printStackTrace();
                                }
                        }
                }
        }

        // =========================================================
        // ACTIVATE EXISTING JD
        // =========================================================

        @PutMapping("/{id}/activate")
        public ResponseEntity<?> activateJobDescription(
                        @PathVariable Long id) {

                try {

                        JobDescription jobDescription = jobDescriptionRepository
                                        .findById(id)
                                        .orElse(null);

                        if (jobDescription == null) {

                                return ResponseEntity
                                                .status(HttpStatus.NOT_FOUND)
                                                .body(Map.of(
                                                                "success", false,
                                                                "message",
                                                                "Job Description not found."));
                        }

                        jobDescriptionRepository.deactivateAll();

                        jobDescription.setActive(true);

                        jobDescriptionRepository.save(
                                        jobDescription);

                        try {

                                atsMatchingService.runATS();

                        } catch (Exception atsException) {

                                atsException.printStackTrace();

                                return ResponseEntity
                                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .body(Map.of(
                                                                "success", false,
                                                                "message",
                                                                "JD activated, but ATS execution failed: "
                                                                                + String.valueOf(
                                                                                                atsException.getMessage())));
                        }

                        return ResponseEntity.ok(
                                        Map.of(
                                                        "success", true,
                                                        "message",
                                                        "Job Description activated and ATS results updated.",
                                                        "activeJdId",
                                                        jobDescription.getId(),
                                                        "title",
                                                        jobDescription.getTitle() == null
                                                                        ? "Job Description"
                                                                        : jobDescription.getTitle()));

                } catch (Exception e) {

                        e.printStackTrace();

                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Map.of(
                                                        "success", false,
                                                        "message",
                                                        "Unable to activate Job Description.",
                                                        "error",
                                                        String.valueOf(e.getMessage())));
                }
        }

        // =========================================================
        // GET ALL JDs
        // =========================================================

        @GetMapping
        public ResponseEntity<List<JobDescription>> getAllJobDescriptions() {

                return ResponseEntity.ok(
                                jobDescriptionRepository.findAll());
        }

        // =========================================================
        // DELETE JD
        // =========================================================

        @DeleteMapping("/{id}")
        public ResponseEntity<?> deleteJobDescription(
                        @PathVariable Long id) {

                try {

                        JobDescription jobDescription = jobDescriptionRepository
                                        .findById(id)
                                        .orElse(null);

                        if (jobDescription == null) {

                                return ResponseEntity
                                                .status(HttpStatus.NOT_FOUND)
                                                .body(
                                                                "Job Description not found.");
                        }

                        boolean wasActive = Boolean.TRUE.equals(
                                        jobDescription.getActive());

                        String filePath = jobDescription.getFilePath();

                        // =====================================================
                        // DELETE ATS RESULTS
                        // =====================================================

                        List<ATSResult> jdResults = atsResultRepository.findAll()
                                        .stream()
                                        .filter(result -> result.getJdId() != null
                                                        && result.getJdId().equals(id))
                                        .toList();

                        if (!jdResults.isEmpty()) {

                                atsResultRepository.deleteAll(
                                                jdResults);
                        }

                        // =====================================================
                        // DELETE JD FROM SUPABASE
                        // =====================================================

                        if (filePath != null
                                        && !filePath.isBlank()) {

                                try {

                                        // New Supabase path
                                        if (filePath.startsWith(
                                                        "job-descriptions/")) {

                                                if (supabaseStorageService.fileExists(
                                                                filePath)) {

                                                        supabaseStorageService.deleteFile(
                                                                        filePath);

                                                        System.out.println(
                                                                        "JD deleted from Supabase : "
                                                                                        + filePath);
                                                }

                                        } else {

                                                // Legacy local file support
                                                File jdFile = new File(filePath);

                                                if (jdFile.exists()
                                                                && jdFile.isFile()) {

                                                        jdFile.delete();
                                                }
                                        }

                                } catch (Exception storageException) {

                                        storageException.printStackTrace();
                                }
                        }

                        // =====================================================
                        // DELETE DATABASE RECORD
                        // =====================================================

                        jobDescriptionRepository.delete(
                                        jobDescription);

                        // =====================================================
                        // ACTIVATE REPLACEMENT JD
                        // =====================================================

                        if (wasActive) {

                                JobDescription replacement = jobDescriptionRepository
                                                .findTopByOrderByIdDesc();

                                if (replacement != null) {

                                        jobDescriptionRepository.deactivateAll();

                                        replacement.setActive(true);

                                        jobDescriptionRepository.save(
                                                        replacement);

                                        try {

                                                atsMatchingService.runATS();

                                        } catch (Exception atsException) {

                                                atsException.printStackTrace();

                                                return ResponseEntity
                                                                .status(
                                                                                HttpStatus.INTERNAL_SERVER_ERROR)
                                                                .body(Map.of(
                                                                                "success",
                                                                                false,
                                                                                "message",
                                                                                "JD deleted and replacement activated, but ATS execution failed.",
                                                                                "error",
                                                                                String.valueOf(
                                                                                                atsException.getMessage())));
                                        }

                                } else {

                                        atsResultRepository.deleteAll();
                                }
                        }

                        return ResponseEntity.ok(
                                        Map.of(
                                                        "success", true,
                                                        "message",
                                                        "Job Description deleted successfully.",
                                                        "deletedId",
                                                        id,
                                                        "wasActive",
                                                        wasActive));

                } catch (Exception e) {

                        e.printStackTrace();

                        return ResponseEntity
                                        .status(
                                                        HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Map.of(
                                                        "success",
                                                        false,
                                                        "message",
                                                        "Unable to delete Job Description.",
                                                        "error",
                                                        String.valueOf(
                                                                        e.getMessage())));
                }
        }

        // =========================================================
        // GET JD FILE
        // =========================================================

        @GetMapping("/{id}/file")
        public ResponseEntity<?> viewJobDescription(
                        @PathVariable Long id) {

                try {

                        JobDescription jobDescription = jobDescriptionRepository
                                        .findById(id)
                                        .orElse(null);

                        if (jobDescription == null) {

                                return ResponseEntity
                                                .notFound()
                                                .build();
                        }

                        String filePath = jobDescription.getFilePath();

                        if (filePath == null
                                        || filePath.isBlank()) {

                                return ResponseEntity
                                                .notFound()
                                                .build();
                        }

                        byte[] fileBytes;

                        // =====================================================
                        // NEW SUPABASE JD
                        // =====================================================

                        if (filePath.startsWith(
                                        "job-descriptions/")) {

                                fileBytes = supabaseStorageService.downloadFile(
                                                filePath);

                        }

                        // =====================================================
                        // LEGACY LOCAL JD
                        // =====================================================

                        else {

                                File file = new File(filePath);

                                if (!file.exists()
                                                || !file.isFile()) {

                                        return ResponseEntity
                                                        .notFound()
                                                        .build();
                                }

                                fileBytes = Files.readAllBytes(
                                                file.toPath());
                        }

                        // =====================================================
                        // RETURN PDF
                        // =====================================================

                        return ResponseEntity.ok()
                                        .contentType(
                                                        MediaType.APPLICATION_PDF)
                                        .header(
                                                        HttpHeaders.CONTENT_DISPOSITION,
                                                        "inline; filename=\""
                                                                        + jobDescription.getFileName()
                                                                        + "\"")
                                        .body(fileBytes);

                } catch (Exception e) {

                        e.printStackTrace();

                        return ResponseEntity
                                        .status(
                                                        HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(
                                                        Map.of(
                                                                        "message",
                                                                        "Unable to open Job Description.",
                                                                        "error",
                                                                        String.valueOf(
                                                                                        e.getMessage())));
                }
        }

        // =========================================================
        // NORMALIZE JD TEXT
        // =========================================================

        private String normalizeJdText(
                        String text) {

                if (text == null) {

                        return "";
                }

                return text
                                .toLowerCase()
                                .replaceAll("\\s+", " ")
                                .trim();
        }

        // =========================================================
        // SHA-256 HASH
        // =========================================================

        private String generateSHA256(
                        String text) {

                try {

                        MessageDigest digest = MessageDigest.getInstance(
                                        "SHA-256");

                        byte[] hash = digest.digest(
                                        text.getBytes(
                                                        StandardCharsets.UTF_8));

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

                } catch (Exception e) {

                        throw new IllegalStateException(
                                        "Unable to generate JD hash.",
                                        e);
                }
        }
}