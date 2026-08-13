package resume_ats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import resume_ats.entity.Resume;
import resume_ats.repository.ResumeRepository;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/resumes")
public class ResumeManagementController {

    private final ResumeRepository resumeRepository;

    public ResumeManagementController(
            ResumeRepository resumeRepository) {

        this.resumeRepository = resumeRepository;
    }

    // =========================================================
    // SEARCH RESUMES
    // =========================================================

    @GetMapping("/search")
    public ResponseEntity<List<Resume>> searchResumes(
            @RequestParam("query") String query) {

        if (query == null || query.trim().isEmpty()) {

            return ResponseEntity.badRequest().build();
        }

        String searchQuery = query.trim();

        List<Resume> results = resumeRepository
                .findByCandidateNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFileNameContainingIgnoreCase(
                        searchQuery,
                        searchQuery,
                        searchQuery);

        return ResponseEntity.ok(results);
    }

    // =========================================================
    // DELETE RESUME
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteResume(
            @PathVariable Long id) {

        Resume resume = resumeRepository.findById(id)
                .orElse(null);

        if (resume == null) {

            return ResponseEntity.notFound().build();
        }

        // -----------------------------------------------------
        // Delete physical resume file
        // -----------------------------------------------------

        String fileName = resume.getFileName();

        if (fileName != null && !fileName.isBlank()) {

            String resumeDirectory = System.getProperty("user.dir")
                    + File.separator
                    + "Uploads"
                    + File.separator
                    + "extracted";

            File resumeFile = new File(
                    resumeDirectory
                            + File.separator
                            + fileName);

            if (resumeFile.exists()) {

                boolean deleted = resumeFile.delete();

                if (deleted) {

                    System.out.println(
                            "Resume file deleted : "
                                    + resumeFile.getAbsolutePath());

                } else {

                    System.out.println(
                            "Could not delete resume file : "
                                    + resumeFile.getAbsolutePath());
                }
            }
        }

        // -----------------------------------------------------
        // Delete database record
        // -----------------------------------------------------

        resumeRepository.deleteById(id);

        System.out.println(
                "Resume database record deleted : "
                        + id);

        return ResponseEntity.ok(
                "Resume deleted successfully.");
    }
}