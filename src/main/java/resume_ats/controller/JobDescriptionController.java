package resume_ats.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
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
import resume_ats.util.parser.JobDescriptionParser;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobdescriptions")
public class JobDescriptionController {

        private static final String UPLOAD_DIR = System.getProperty("user.dir")
                        + File.separator
                        + "Uploads"
                        + File.separator
                        + "jobdescriptions";

        private final JobDescriptionRepository jobDescriptionRepository;

        private final ATSResultRepository atsResultRepository;

        private final ResumeService resumeService;

        private final ATSMatchingService atsMatchingService;

        // =========================================================
        // CONSTRUCTOR
        // =========================================================

        public JobDescriptionController(
                        JobDescriptionRepository jobDescriptionRepository,
                        ATSResultRepository atsResultRepository,
                        ResumeService resumeService,
                        ATSMatchingService atsMatchingService) {

                this.jobDescriptionRepository = jobDescriptionRepository;

                this.atsResultRepository = atsResultRepository;

                this.resumeService = resumeService;

                this.atsMatchingService = atsMatchingService;
        }

        // =========================================================
        // UPLOAD JD
        // =========================================================

        @PostMapping("/upload")
        public ResponseEntity<?> uploadJobDescription(
                        @RequestParam("file") MultipartFile file) {

                try {

                        // =====================================================
                        // 1. VALIDATE FILE
                        // =====================================================

                        if (file == null || file.isEmpty()) {

                                return ResponseEntity
                                                .badRequest()
                                                .body("Please select a Job Description PDF.");
                        }

                        String originalFileName = file.getOriginalFilename();

                        if (originalFileName == null
                                        || originalFileName.isBlank()) {

                                return ResponseEntity
                                                .badRequest()
                                                .body("Invalid Job Description file.");
                        }

                        if (!originalFileName
                                        .toLowerCase()
                                        .endsWith(".pdf")) {

                                return ResponseEntity
                                                .badRequest()
                                                .body(
                                                                "Only PDF Job Description files are allowed.");
                        }

                        // =====================================================
                        // 2. CREATE UPLOAD DIRECTORY
                        // =====================================================

                        File uploadDirectory = new File(UPLOAD_DIR);

                        if (!uploadDirectory.exists()) {

                                boolean created = uploadDirectory.mkdirs();

                                if (!created
                                                && !uploadDirectory.exists()) {

                                        return ResponseEntity
                                                        .internalServerError()
                                                        .body(
                                                                        "Unable to create JD upload directory.");
                                }
                        }

                        // =====================================================
                        // 3. CREATE SAFE FILE NAME
                        // =====================================================

                        String safeFileName = new File(originalFileName)
                                        .getName();

                        File destination = new File(
                                        uploadDirectory,
                                        safeFileName);

                        // =====================================================
                        // 4. SAVE PDF
                        // =====================================================

                        file.transferTo(destination);

                        // =====================================================
                        // 5. EXTRACT JD TEXT
                        // =====================================================

                        String jdText = resumeService.extractText(destination);

                        if (jdText == null
                                        || jdText.trim().isEmpty()) {

                                // Remove invalid uploaded file.
                                if (destination.exists()) {
                                        destination.delete();
                                }

                                return ResponseEntity
                                                .badRequest()
                                                .body(
                                                                "Unable to extract text from this JD PDF.");
                        }

                        // =====================================================
                        // 6. GENERATE NORMALIZED JD HASH
                        // =====================================================

                        String normalizedText = normalizeJdText(jdText);

                        String jdHash = generateSHA256(normalizedText);

                        // =====================================================
                        // 7. DUPLICATE JD CHECK
                        // =====================================================

                        boolean duplicate = jobDescriptionRepository
                                        .existsByJdHash(jdHash);

                        if (duplicate) {

                                // Don't keep another physical copy.
                                if (destination.exists()) {
                                        destination.delete();
                                }

                                System.out.println(
                                                "Duplicate JD rejected : "
                                                                + originalFileName);

                                return ResponseEntity
                                                .status(409)
                                                .body(
                                                                "This Job Description already exists in the JD Library.");
                        }

                        // =====================================================
                        // 8. PARSE JD
                        // =====================================================

                        JobDescription jobDescription = JobDescriptionParser.parse(jdText);

                        if (jobDescription == null) {

                                if (destination.exists()) {
                                        destination.delete();
                                }

                                return ResponseEntity
                                                .badRequest()
                                                .body(
                                                                "Unable to parse Job Description.");
                        }

                        // =====================================================
                        // 9. SET JD INFORMATION
                        // =====================================================

                        jobDescription.setFileName(
                                        safeFileName);

                        jobDescription.setFilePath(
                                        destination.getAbsolutePath());

                        jobDescription.setJdText(jdText);

                        jobDescription.setJdHash(jdHash);

                        // =====================================================
                        // 10. MAKE NEW JD ACTIVE AND SAVE
                        //
                        // Previous JDs remain in the library, but only this
                        // JD becomes the active/current JD.
                        // =====================================================

                        jobDescriptionRepository.deactivateAll();

                        jobDescription.setActive(true);

                        JobDescription saved = jobDescriptionRepository
                                        .save(jobDescription);

                        // =====================================================
                        // 11. RUN ATS
                        //
                        // Because this is the newest JD, the ATS service
                        // can use it as the active/latest JD.
                        // =====================================================

                        try {

                                atsMatchingService.runATS();

                        } catch (Exception atsException) {

                                atsException.printStackTrace();

                                return ResponseEntity
                                                .status(500)
                                                .body(
                                                                "JD uploaded successfully, but ATS execution failed: "
                                                                                + atsException.getMessage());
                        }

                        // =====================================================
                        // 12. SUCCESS
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
                                        "ATS         : EXECUTED");

                        System.out.println(
                                        "====================================");

                        return ResponseEntity.ok(
                                        "Job Description uploaded successfully. "
                                                        + "ATS executed and existing resumes were re-evaluated.");

                } catch (Exception e) {

                        e.printStackTrace();

                        return ResponseEntity
                                        .status(500)
                                        .body(
                                                        "Job Description upload failed: "
                                                                        + e.getMessage());
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
                                                                "message", "Job Description not found."));
                        }

                        // Make every JD inactive first.
                        jobDescriptionRepository.deactivateAll();

                        // Make the selected existing JD active.
                        jobDescription.setActive(true);
                        jobDescriptionRepository.save(jobDescription);

                        // Re-run ATS using this selected JD.
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
                                                                                + String.valueOf(atsException
                                                                                                .getMessage())));
                        }

                        return ResponseEntity.ok(
                                        Map.of(
                                                        "success", true,
                                                        "message",
                                                        "Job Description activated and ATS results updated.",
                                                        "activeJdId", jobDescription.getId(),
                                                        "title", jobDescription.getTitle() == null
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
                                                        "error", String.valueOf(e.getMessage())));
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
                                                .body("Job Description not found.");
                        }

                        boolean wasActive = Boolean.TRUE.equals(jobDescription.getActive());

                        String fileName = jobDescription.getFileName();
                        String filePath = jobDescription.getFilePath();

                        // Remove ATS results belonging to this JD.
                        List<ATSResult> jdResults = atsResultRepository.findAll()
                                        .stream()
                                        .filter(result -> result.getJdId() != null
                                                        && result.getJdId().equals(id))
                                        .toList();

                        if (!jdResults.isEmpty()) {
                                atsResultRepository.deleteAll(jdResults);
                        }

                        // Remove the physical PDF.
                        if (filePath != null && !filePath.isBlank()) {
                                File jdFile = new File(filePath);
                                if (jdFile.exists() && jdFile.isFile()) {
                                        jdFile.delete();
                                }
                        }

                        // Remove the JD record.
                        jobDescriptionRepository.delete(jobDescription);

                        // If the active JD was deleted, activate the newest remaining JD.
                        if (wasActive) {

                                JobDescription replacement = jobDescriptionRepository
                                                .findTopByOrderByIdDesc();

                                if (replacement != null) {

                                        jobDescriptionRepository.deactivateAll();
                                        replacement.setActive(true);
                                        jobDescriptionRepository.save(replacement);

                                        try {
                                                atsMatchingService.runATS();
                                        } catch (Exception atsException) {
                                                atsException.printStackTrace();

                                                return ResponseEntity
                                                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                                .body(Map.of(
                                                                                "success", false,
                                                                                "message",
                                                                                "JD deleted and replacement activated, but ATS execution failed.",
                                                                                "error", String.valueOf(atsException
                                                                                                .getMessage())));
                                        }

                                } else {
                                        // No JDs remain. Clear all ATS results.
                                        atsResultRepository.deleteAll();
                                }
                        }

                        return ResponseEntity.ok(
                                        Map.of(
                                                        "success", true,
                                                        "message", "Job Description deleted successfully.",
                                                        "deletedId", id,
                                                        "wasActive", wasActive));

                } catch (Exception e) {

                        e.printStackTrace();

                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Map.of(
                                                        "success", false,
                                                        "message", "Unable to delete Job Description.",
                                                        "error", String.valueOf(e.getMessage())));
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

                        File file = new File(filePath);

                        if (!file.exists()
                                        || !file.isFile()) {

                                return ResponseEntity
                                                .notFound()
                                                .build();
                        }

                        Resource resource = new FileSystemResource(file);

                        return ResponseEntity.ok()
                                        .contentType(
                                                        MediaType.APPLICATION_PDF)
                                        .header(
                                                        HttpHeaders.CONTENT_DISPOSITION,
                                                        "inline; filename=\""
                                                                        + file.getName()
                                                                        + "\"")
                                        .body(resource);

                } catch (Exception e) {

                        e.printStackTrace();

                        return ResponseEntity
                                        .status(500)
                                        .body(
                                                        Map.of(
                                                                        "message",
                                                                        "Unable to open Job Description."));
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