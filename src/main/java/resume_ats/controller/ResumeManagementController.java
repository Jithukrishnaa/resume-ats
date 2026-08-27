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

        // =========================================================
        // CONSTRUCTOR
        // =========================================================

        public ResumeManagementController(
                        ResumeRepository resumeRepository) {

                this.resumeRepository = resumeRepository;
        }

        // =========================================================
        // COUNT RESUMES
        // =========================================================

        @GetMapping("/count")
        public ResponseEntity<Long> getResumeCount() {

                long count = resumeRepository.count();

                System.out.println(
                                "Total resumes in database : " + count);

                return ResponseEntity.ok(count);
        }

        // =========================================================
        // SEARCH RESUMES
        // =========================================================

        @GetMapping("/search")
        public ResponseEntity<List<Resume>> searchResumes(
                        @RequestParam("query") String query) {

                // -----------------------------------------------------
                // Validate search query
                // -----------------------------------------------------

                if (query == null
                                || query.trim().isEmpty()) {

                        return ResponseEntity.ok(List.of());
                }

                String searchQuery = query.trim();

                // -----------------------------------------------------
                // Search by:
                //
                // 1. Candidate name
                // 2. Email
                // 3. Resume filename
                // -----------------------------------------------------

                List<Resume> results = resumeRepository
                                .findByCandidateNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFileNameContainingIgnoreCase(
                                                searchQuery,
                                                searchQuery,
                                                searchQuery);

                // -----------------------------------------------------
                // Console information
                // -----------------------------------------------------

                System.out.println("-------------------------------------");
                System.out.println("Resume Search");
                System.out.println(
                                "Search Query : " + searchQuery);
                System.out.println(
                                "Results      : " + results.size());
                System.out.println("-------------------------------------");

                return ResponseEntity.ok(results);
        }

        // =========================================================
        // DELETE RESUME
        // =========================================================

        @DeleteMapping("/{id}")
        public ResponseEntity<String> deleteResume(
                        @PathVariable Long id) {

                // -----------------------------------------------------
                // Find resume
                // -----------------------------------------------------

                Resume resume = resumeRepository
                                .findById(id)
                                .orElse(null);

                if (resume == null) {

                        System.out.println(
                                        "Resume not found : " + id);

                        return ResponseEntity
                                        .notFound()
                                        .build();
                }

                // -----------------------------------------------------
                // Resume information
                // -----------------------------------------------------

                String fileName = resume.getFileName();

                System.out.println("-------------------------------------");
                System.out.println("Deleting Resume");
                System.out.println(
                                "Resume ID : " + id);
                System.out.println(
                                "Candidate : "
                                                + resume.getCandidateName());
                System.out.println(
                                "File Name : " + fileName);
                System.out.println("-------------------------------------");

                // -----------------------------------------------------
                // Delete physical resume file
                // -----------------------------------------------------

                if (fileName != null
                                && !fileName.isBlank()) {

                        String resumeDirectory = System.getProperty("user.dir")
                                        + File.separator
                                        + "Uploads"
                                        + File.separator
                                        + "extracted";

                        // Prevent path traversal
                        fileName = new File(fileName)
                                        .getName();

                        File resumeFile = new File(
                                        resumeDirectory,
                                        fileName);

                        System.out.println(
                                        "Physical file : "
                                                        + resumeFile.getAbsolutePath());

                        if (resumeFile.exists()
                                        && resumeFile.isFile()) {

                                boolean deleted = resumeFile.delete();

                                if (deleted) {

                                        System.out.println(
                                                        "Resume file deleted successfully.");

                                } else {

                                        System.out.println(
                                                        "Could not delete resume file.");
                                }

                        } else {

                                System.out.println(
                                                "Physical resume file not found.");
                        }
                }

                // -----------------------------------------------------
                // Delete database record
                // -----------------------------------------------------

                resumeRepository.deleteById(id);

                System.out.println(
                                "Resume database record deleted : "
                                                + id);

                // -----------------------------------------------------
                // Return response
                // -----------------------------------------------------

                return ResponseEntity.ok(
                                "Resume deleted successfully.");
        }
}