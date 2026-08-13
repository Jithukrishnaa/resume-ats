package resume_ats.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import resume_ats.entity.Resume;
import resume_ats.repository.ResumeRepository;
import resume_ats.util.parser.ResumeParser;

import java.io.File;
import java.util.List;

@Service
public class ResumeImportService {

    private final ZipExtractionService zipExtractionService;
    private final ResumeService resumeService;
    private final ResumeRepository resumeRepository;

    public ResumeImportService(
            ZipExtractionService zipExtractionService,
            ResumeService resumeService,
            ResumeRepository resumeRepository) {

        this.zipExtractionService = zipExtractionService;
        this.resumeService = resumeService;
        this.resumeRepository = resumeRepository;
    }

    public int importZip(MultipartFile zipFile) throws Exception {

        // Extract ZIP into Uploads/extracted
        List<File> resumeFiles = zipExtractionService.extractZip(zipFile);

        int importedCount = 0;

        for (File file : resumeFiles) {

            try {

                System.out.println("-------------------------------------");
                System.out.println("Processing : " + file.getName());

                // ===========================
                // Check extracted file
                // ===========================

                if (!file.exists()) {

                    System.out.println(
                            "Skipped - File does not exist : "
                                    + file.getAbsolutePath());

                    continue;
                }

                // ===========================
                // Extract Resume Text
                // ===========================

                String extractedText = resumeService.extractText(file);

                if (extractedText == null
                        || extractedText.isBlank()) {

                    System.out.println(
                            "Skipped (Empty Resume)");

                    continue;
                }

                // ===========================
                // Limit extremely large resumes
                // ===========================

                if (extractedText.length() > 50000) {

                    extractedText = extractedText.substring(0, 50000);
                }

                // ===========================
                // Parse Resume
                // ===========================

                /*
                 * IMPORTANT:
                 *
                 * Pass BOTH:
                 *
                 * 1. extracted resume text
                 * 2. original filename
                 *
                 * The parser will first try to find the
                 * candidate name inside the resume.
                 *
                 * If that fails, it will use the filename
                 * as a fallback.
                 */
                Resume resume = ResumeParser.parse(
                        extractedText,
                        file.getName());

                // ===========================
                // Duplicate Check
                // ===========================

                if (resume.getEmail() != null
                        && !resume.getEmail().isBlank()
                        && resumeRepository.existsByEmailIgnoreCase(
                                resume.getEmail())) {

                    System.out.println(
                            "Duplicate Resume Skipped : "
                                    + resume.getEmail());

                    continue;
                }

                // ===========================
                // Resume File Information
                // ===========================

                resume.setFileName(
                        file.getName());

                /*
                 * Store ONLY the filename in the database.
                 *
                 * Example:
                 *
                 * Amaldev.pdf
                 *
                 * NOT:
                 *
                 * C:\Users\JITHU\Downloads\resume-ats\
                 * resume-ats\Uploads\extracted\Amaldev.pdf
                 */
                resume.setFilePath(
                        file.getName());

                // ===========================
                // Save Resume
                // ===========================

                resumeRepository.save(resume);

                importedCount++;

                System.out.println(
                        "Imported Successfully : "
                                + resume.getCandidateName());

                System.out.println(
                        "Stored Resume File : "
                                + file.getName());

                System.out.println(
                        "Candidate Name : "
                                + resume.getCandidateName());

            } catch (Exception e) {

                System.out.println(
                        "Failed : " + file.getName());

                e.printStackTrace();
            }
        }

        System.out.println("-------------------------------------");
        System.out.println(
                "Total Imported : " + importedCount);
        System.out.println("-------------------------------------");

        return importedCount;
    }
}