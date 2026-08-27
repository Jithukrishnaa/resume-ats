package resume_ats.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import resume_ats.entity.Resume;
import resume_ats.repository.ResumeRepository;

import java.io.File;

@RestController
@RequestMapping("/api/resume")
public class ResumeFileController {

    private static final String RESUME_DIR = System.getProperty("user.dir")
            + File.separator
            + "Uploads"
            + File.separator
            + "extracted";

    private final ResumeRepository resumeRepository;

    public ResumeFileController(
            ResumeRepository resumeRepository) {

        this.resumeRepository = resumeRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> viewResume(
            @PathVariable Long id) {

        try {

            // ==========================================
            // Find resume
            // ==========================================

            Resume resume = resumeRepository
                    .findById(id)
                    .orElse(null);

            if (resume == null) {

                System.out.println(
                        "Resume not found in database : " + id);

                return ResponseEntity.notFound().build();
            }

            // ==========================================
            // Get filename
            // ==========================================

            String fileName = resume.getFileName();

            if (fileName == null || fileName.isBlank()) {

                System.out.println(
                        "Resume filename missing : " + id);

                return ResponseEntity.notFound().build();
            }

            // ==========================================
            // Prevent path traversal
            // ==========================================

            fileName = new File(fileName)
                    .getName();

            // ==========================================
            // Construct actual resume path
            // ==========================================

            File resumeFile = new File(
                    RESUME_DIR,
                    fileName);

            System.out.println("----------------------------------");
            System.out.println(
                    "Resume ID     : " + id);
            System.out.println(
                    "Candidate     : "
                            + resume.getCandidateName());
            System.out.println(
                    "File Name     : " + fileName);
            System.out.println(
                    "Resume Folder : " + RESUME_DIR);
            System.out.println(
                    "Absolute Path : "
                            + resumeFile.getAbsolutePath());
            System.out.println(
                    "File Exists   : "
                            + resumeFile.exists());
            System.out.println("----------------------------------");

            // ==========================================
            // Check file
            // ==========================================

            if (!resumeFile.exists()
                    || !resumeFile.isFile()) {

                return ResponseEntity.notFound().build();
            }

            // ==========================================
            // Create resource
            // ==========================================

            Resource resource = new FileSystemResource(resumeFile);

            // ==========================================
            // Determine content type
            // ==========================================

            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

            if (fileName
                    .toLowerCase()
                    .endsWith(".pdf")) {

                mediaType = MediaType.APPLICATION_PDF;
            }

            // ==========================================
            // Return PDF in browser
            // ==========================================

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\""
                                    + fileName
                                    + "\"")
                    .body(resource);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .build();
        }
    }
}