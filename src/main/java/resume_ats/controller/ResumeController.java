package resume_ats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import resume_ats.entity.Resume;
import resume_ats.repository.ResumeRepository;
import resume_ats.service.ResumeService;
import resume_ats.util.parser.ResumeParser;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

        // =========================================================
        // RESUME UPLOAD DIRECTORY
        // =========================================================

        private static final String UPLOAD_DIR = System.getProperty("user.dir")
                        + File.separator
                        + "Uploads"
                        + File.separator
                        + "extracted";

        private final ResumeRepository resumeRepository;
        private final ResumeService resumeService;

        // =========================================================
        // CONSTRUCTOR
        // =========================================================

        public ResumeController(
                        ResumeRepository resumeRepository,
                        ResumeService resumeService) {

                this.resumeRepository = resumeRepository;
                this.resumeService = resumeService;
        }

        // =========================================================
        // SINGLE RESUME UPLOAD
        // =========================================================

        @PostMapping("/upload")
        public ResponseEntity<String> uploadResume(
                        @RequestParam("file") MultipartFile file) {

                try {

                        // -----------------------------------------------------
                        // Validate file
                        // -----------------------------------------------------

                        if (file == null || file.isEmpty()) {

                                return ResponseEntity.badRequest()
                                                .body("Please select a resume PDF.");
                        }

                        String originalFileName = file.getOriginalFilename();

                        if (originalFileName == null
                                        || originalFileName.isBlank()) {

                                return ResponseEntity.badRequest()
                                                .body("Invalid resume file.");
                        }

                        // -----------------------------------------------------
                        // Validate PDF
                        // -----------------------------------------------------

                        String lowerFileName = originalFileName.toLowerCase();

                        if (!lowerFileName.endsWith(".pdf")) {

                                return ResponseEntity.badRequest()
                                                .body("Only PDF resumes are allowed.");
                        }

                        // -----------------------------------------------------
                        // Create upload directory
                        // -----------------------------------------------------

                        File uploadDir = new File(UPLOAD_DIR);

                        if (!uploadDir.exists()) {

                                boolean created = uploadDir.mkdirs();

                                if (!created && !uploadDir.exists()) {

                                        return ResponseEntity
                                                        .internalServerError()
                                                        .body(
                                                                        "Unable to create resume storage folder.");
                                }
                        }

                        // -----------------------------------------------------
                        // Clean filename
                        // -----------------------------------------------------

                        String fileName = new File(originalFileName).getName();

                        fileName = fileName.replaceAll(
                                        "[\\\\/:*?\"<>|]",
                                        "_");

                        File destination = new File(
                                        uploadDir,
                                        fileName);

                        // -----------------------------------------------------
                        // Save file
                        // -----------------------------------------------------

                        file.transferTo(destination);

                        System.out.println("-------------------------------------");
                        System.out.println("Single Resume Upload");
                        System.out.println(
                                        "File Name : " + fileName);
                        System.out.println(
                                        "File Path : "
                                                        + destination.getAbsolutePath());
                        System.out.println(
                                        "File Exists : "
                                                        + destination.exists());
                        System.out.println("-------------------------------------");

                        // -----------------------------------------------------
                        // Extract text
                        // -----------------------------------------------------

                        String extractedText = resumeService.extractText(
                                        destination);

                        if (extractedText == null
                                        || extractedText.isBlank()) {

                                if (destination.exists()) {
                                        destination.delete();
                                }

                                return ResponseEntity.badRequest()
                                                .body(
                                                                "Unable to extract text from the resume.");
                        }

                        // -----------------------------------------------------
                        // Limit very large resumes
                        // -----------------------------------------------------

                        if (extractedText.length() > 50000) {

                                extractedText = extractedText.substring(
                                                0,
                                                50000);
                        }

                        // -----------------------------------------------------
                        // Parse resume
                        // -----------------------------------------------------

                        Resume resume = ResumeParser.parse(
                                        extractedText,
                                        fileName);

                        // -----------------------------------------------------
                        // Duplicate email check
                        // -----------------------------------------------------

                        if (resume.getEmail() != null
                                        && !resume.getEmail().isBlank()
                                        && resumeRepository
                                                        .existsByEmailIgnoreCase(
                                                                        resume.getEmail())) {

                                if (destination.exists()) {
                                        destination.delete();
                                }

                                return ResponseEntity.ok(
                                                "Resume already exists for : "
                                                                + resume.getEmail());
                        }

                        // -----------------------------------------------------
                        // Store file information
                        // -----------------------------------------------------

                        resume.setFileName(fileName);

                        /*
                         * Store the filename.
                         *
                         * The ResumeFileController can resolve this
                         * against Uploads/extracted.
                         */

                        resume.setFilePath(fileName);

                        // -----------------------------------------------------
                        // Save to database
                        // -----------------------------------------------------

                        resumeRepository.save(resume);

                        // -----------------------------------------------------
                        // Console information
                        // -----------------------------------------------------

                        System.out.println("-------------------------------------");
                        System.out.println(
                                        "Resume Imported Successfully");

                        System.out.println(
                                        "Resume ID      : "
                                                        + resume.getId());

                        System.out.println(
                                        "Candidate Name : "
                                                        + resume.getCandidateName());

                        System.out.println(
                                        "Email          : "
                                                        + resume.getEmail());

                        System.out.println(
                                        "File Name      : "
                                                        + resume.getFileName());

                        System.out.println(
                                        "File Path      : "
                                                        + resume.getFilePath());

                        System.out.println("-------------------------------------");

                        // -----------------------------------------------------
                        // Success response
                        // -----------------------------------------------------

                        return ResponseEntity.ok(

                                        "Resume uploaded successfully.\n\n"
                                                        + "✓ Resume Parsed\n"
                                                        + "✓ Candidate Name Extracted\n"
                                                        + "✓ Skills Extracted\n"
                                                        + "✓ Experience Extracted\n"
                                                        + "✓ Education Extracted\n"
                                                        + "✓ Certifications Extracted\n"
                                                        + "✓ Projects Extracted\n"
                                                        + "✓ Resume Stored"

                        );

                } catch (IOException e) {

                        e.printStackTrace();

                        return ResponseEntity
                                        .internalServerError()
                                        .body(
                                                        "Upload failed : "
                                                                        + e.getMessage());

                } catch (Exception e) {

                        e.printStackTrace();

                        return ResponseEntity
                                        .internalServerError()
                                        .body(
                                                        "Parsing failed : "
                                                                        + e.getMessage());
                }
        }


        // =========================================================
        // GET ALL UPLOADED RESUMES
        // =========================================================

        @GetMapping
        public ResponseEntity<?> getAllResumes() {

                try {
                        return ResponseEntity.ok(
                                        resumeRepository.findAll()
                        );

                } catch (Exception e) {

                        e.printStackTrace();

                        return ResponseEntity
                                        .internalServerError()
                                        .body("Unable to load resumes.");
                }
        }

}