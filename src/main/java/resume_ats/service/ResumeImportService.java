package resume_ats.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import resume_ats.entity.Resume;
import resume_ats.repository.ResumeRepository;
import resume_ats.util.parser.ResumeParser;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;

@Service
public class ResumeImportService {

        private final ZipExtractionService zipExtractionService;
        private final ResumeService resumeService;
        private final ResumeRepository resumeRepository;
        private final SupabaseStorageService supabaseStorageService;

        public ResumeImportService(
                        ZipExtractionService zipExtractionService,
                        ResumeService resumeService,
                        ResumeRepository resumeRepository,
                        SupabaseStorageService supabaseStorageService) {

                this.zipExtractionService = zipExtractionService;
                this.resumeService = resumeService;
                this.resumeRepository = resumeRepository;
                this.supabaseStorageService = supabaseStorageService;
        }

        public int importZip(MultipartFile zipFile) throws Exception {

                // ==========================================
                // Extract ZIP into temporary/local directory
                // ==========================================

                List<File> resumeFiles = zipExtractionService.extractZip(zipFile);

                int importedCount = 0;

                for (File file : resumeFiles) {

                        String supabaseObjectPath = null;

                        try {

                                System.out.println("-------------------------------------");
                                System.out.println("Processing : " + file.getName());

                                // ==========================================
                                // Check extracted file
                                // ==========================================

                                if (!file.exists() || !file.isFile()) {

                                        System.out.println(
                                                        "Skipped - File does not exist : "
                                                                        + file.getAbsolutePath());

                                        continue;
                                }

                                // ==========================================
                                // Read file bytes
                                // ==========================================

                                byte[] fileBytes = Files.readAllBytes(
                                                file.toPath());

                                if (fileBytes.length == 0) {

                                        System.out.println(
                                                        "Skipped - Empty file : "
                                                                        + file.getName());

                                        continue;
                                }

                                // ==========================================
                                // SHA-256 Duplicate Check
                                // ==========================================

                                String resumeHash = calculateSHA256(fileBytes);

                                System.out.println(
                                                "Resume SHA-256 : " + resumeHash);

                                if (resumeRepository.existsByResumeHash(
                                                resumeHash)) {

                                        System.out.println(
                                                        "Duplicate Resume Skipped by SHA-256 : "
                                                                        + file.getName());

                                        continue;
                                }

                                // ==========================================
                                // Extract Resume Text
                                // ==========================================

                                String extractedText = resumeService.extractText(file);

                                if (extractedText == null
                                                || extractedText.isBlank()) {

                                        System.out.println(
                                                        "Skipped - Empty Resume Text : "
                                                                        + file.getName());

                                        continue;
                                }

                                // ==========================================
                                // Limit extremely large resumes
                                // ==========================================

                                if (extractedText.length() > 50000) {

                                        extractedText = extractedText.substring(0, 50000);
                                }

                                // ==========================================
                                // Parse Resume
                                // ==========================================

                                Resume resume = ResumeParser.parse(
                                                extractedText,
                                                file.getName());

                                // ==========================================
                                // Email Duplicate Check
                                // ==========================================

                                if (resume.getEmail() != null
                                                && !resume.getEmail().isBlank()
                                                && resumeRepository.existsByEmailIgnoreCase(
                                                                resume.getEmail())) {

                                        System.out.println(
                                                        "Duplicate Resume Skipped by Email : "
                                                                        + resume.getEmail());

                                        continue;
                                }

                                // ==========================================
                                // Resume Information
                                // ==========================================

                                resume.setFileName(
                                                file.getName());

                                resume.setResumeHash(
                                                resumeHash);

                                resume.setRawText(
                                                extractedText);

                                // ==========================================
                                // Create Supabase Object Path
                                // ==========================================

                                String originalFileName = file.getName();

                                String extension = "";

                                int dotIndex = originalFileName.lastIndexOf(".");

                                if (dotIndex > 0) {

                                        extension = originalFileName.substring(dotIndex)
                                                        .toLowerCase();
                                }

                                supabaseObjectPath = "resumes/"
                                                + UUID.randomUUID()
                                                + extension;

                                System.out.println(
                                                "Supabase object : "
                                                                + supabaseObjectPath);

                                // ==========================================
                                // Determine Content Type
                                // ==========================================

                                String contentType = determineContentType(
                                                file,
                                                extension);

                                // ==========================================
                                // Upload Resume to Supabase
                                // ==========================================

                                supabaseStorageService.uploadBytes(
                                                fileBytes,
                                                supabaseObjectPath,
                                                contentType);

                                System.out.println(
                                                "Resume uploaded to Supabase successfully.");

                                // ==========================================
                                // Store Supabase path in PostgreSQL
                                // ==========================================

                                resume.setFilePath(
                                                supabaseObjectPath);

                                // ==========================================
                                // Save Resume to PostgreSQL
                                // ==========================================

                                Resume savedResume = resumeRepository.save(resume);

                                importedCount++;

                                System.out.println(
                                                "Resume saved to PostgreSQL.");

                                System.out.println(
                                                "Resume ID : "
                                                                + savedResume.getId());

                                System.out.println(
                                                "Candidate Name : "
                                                                + savedResume.getCandidateName());

                                System.out.println(
                                                "Stored File : "
                                                                + supabaseObjectPath);

                        } catch (Exception e) {

                                System.out.println(
                                                "Failed : " + file.getName());

                                e.printStackTrace();

                                // ==========================================
                                // Cleanup Supabase file if DB save failed
                                // ==========================================

                                if (supabaseObjectPath != null) {

                                        try {

                                                if (supabaseStorageService.fileExists(
                                                                supabaseObjectPath)) {

                                                        supabaseStorageService.deleteFile(
                                                                        supabaseObjectPath);

                                                        System.out.println(
                                                                        "Supabase file deleted after failure : "
                                                                                        + supabaseObjectPath);
                                                }

                                        } catch (Exception cleanupException) {

                                                System.out.println(
                                                                "Could not cleanup Supabase file : "
                                                                                + supabaseObjectPath);

                                                cleanupException.printStackTrace();
                                        }
                                }

                        } finally {

                                // ==========================================
                                // Delete temporary extracted file
                                // ==========================================

                                try {

                                        if (file.exists()) {

                                                Files.deleteIfExists(
                                                                file.toPath());

                                                System.out.println(
                                                                "Temporary extracted file deleted : "
                                                                                + file.getAbsolutePath());
                                        }

                                } catch (Exception cleanupException) {

                                        System.out.println(
                                                        "Could not delete temporary file : "
                                                                        + file.getAbsolutePath());

                                        cleanupException.printStackTrace();
                                }
                        }
                }

                System.out.println("-------------------------------------");
                System.out.println(
                                "Total Imported : " + importedCount);
                System.out.println("-------------------------------------");

                return importedCount;
        }

        // =====================================================
        // SHA-256
        // =====================================================

        private String calculateSHA256(byte[] data)
                        throws Exception {

                MessageDigest digest = MessageDigest.getInstance("SHA-256");

                byte[] hash = digest.digest(data);

                StringBuilder hexString = new StringBuilder();

                for (byte b : hash) {

                        String hex = Integer.toHexString(0xff & b);

                        if (hex.length() == 1) {

                                hexString.append('0');
                        }

                        hexString.append(hex);
                }

                return hexString.toString();
        }

        // =====================================================
        // Content Type
        // =====================================================

        private String determineContentType(
                        File file,
                        String extension) {

                try {

                        String detectedType = Files.probeContentType(
                                        file.toPath());

                        if (detectedType != null
                                        && !detectedType.isBlank()) {

                                return detectedType;
                        }

                } catch (Exception ignored) {
                }

                switch (extension) {

                        case ".pdf":
                                return "application/pdf";

                        case ".doc":
                                return "application/msword";

                        case ".docx":
                                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

                        default:
                                return "application/octet-stream";
                }
        }
}