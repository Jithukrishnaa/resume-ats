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
            // Find resume from database
            // ==========================================

            Resume resume = resumeRepository.findById(id)
                    .orElse(null);

            if (resume == null) {

                return ResponseEntity.notFound().build();
            }

            // ==========================================
            // Get stored file path
            // ==========================================

            String filePath = resume.getFilePath();

            System.out.println("----------------------------------");
            System.out.println("Resume ID     : " + id);
            System.out.println("Candidate     : " + resume.getCandidateName());
            System.out.println("File Name     : " + resume.getFileName());
            System.out.println("Stored Path   : " + filePath);
            System.out.println("----------------------------------");

            if (filePath == null || filePath.isBlank()) {

                return ResponseEntity.notFound().build();
            }

            // ==========================================
            // Check physical file
            // ==========================================

            File file = new File(filePath);

            System.out.println("Absolute Path : " + file.getAbsolutePath());
            System.out.println("File Exists   : " + file.exists());

            if (!file.exists() || !file.isFile()) {

                return ResponseEntity.notFound().build();
            }

            // ==========================================
            // Create resource
            // ==========================================

            Resource resource = new FileSystemResource(file);

            // ==========================================
            // Determine file type
            // ==========================================

            String fileName = file.getName().toLowerCase();

            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

            if (fileName.endsWith(".pdf")) {

                mediaType = MediaType.APPLICATION_PDF;
            }

            // ==========================================
            // Return file
            // ==========================================

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + file.getName() + "\"")
                    .body(resource);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .build();
        }
    }
}