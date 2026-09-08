package resume_ats.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import resume_ats.entity.Resume;
import resume_ats.entity.User;
import resume_ats.repository.ResumeRepository;
import resume_ats.repository.UserRepository;
import resume_ats.service.WalletService;

import java.io.File;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
public class ResumeFileController {

        private static final String RESUME_DIR = System.getProperty("user.dir")
                        + File.separator
                        + "Uploads"
                        + File.separator
                        + "extracted";

        private static final long RESUME_VIEW_COST = 50L;

        private final ResumeRepository resumeRepository;
        private final UserRepository userRepository;
        private final WalletService walletService;

        public ResumeFileController(
                        ResumeRepository resumeRepository,
                        UserRepository userRepository,
                        WalletService walletService) {

                this.resumeRepository = resumeRepository;
                this.userRepository = userRepository;
                this.walletService = walletService;
        }

        // =========================================================
        // VIEW RESUME
        // =========================================================

        @GetMapping("/{id}")
        public ResponseEntity<?> viewResume(
                        @PathVariable Long id,
                        Authentication authentication) {

                try {

                        // =====================================================
                        // 1. Authentication check
                        // =====================================================

                        if (authentication == null
                                        || !authentication.isAuthenticated()) {

                                return ResponseEntity
                                                .status(HttpStatus.UNAUTHORIZED)
                                                .body(Map.of(
                                                                "message",
                                                                "Please log in to view this resume."));
                        }

                        // =====================================================
                        // 2. Find current user
                        // =====================================================

                        User currentUser = userRepository
                                        .findByUsernameIgnoreCase(
                                                        authentication.getName())
                                        .orElse(null);

                        if (currentUser == null) {

                                return ResponseEntity
                                                .status(HttpStatus.UNAUTHORIZED)
                                                .body(Map.of(
                                                                "message",
                                                                "User account not found."));
                        }

                        // =====================================================
                        // 3. Find resume
                        // =====================================================

                        Resume resume = resumeRepository
                                        .findById(id)
                                        .orElse(null);

                        if (resume == null) {

                                System.out.println(
                                                "Resume not found in database : " + id);

                                return ResponseEntity
                                                .notFound()
                                                .build();
                        }

                        // =====================================================
                        // 4. Get filename
                        // =====================================================

                        String fileName = resume.getFileName();

                        if (fileName == null || fileName.isBlank()) {

                                System.out.println(
                                                "Resume filename missing : " + id);

                                return ResponseEntity
                                                .notFound()
                                                .build();
                        }

                        // =====================================================
                        // 5. Prevent path traversal
                        // =====================================================

                        fileName = new File(fileName)
                                        .getName();

                        // =====================================================
                        // 6. Construct actual resume path
                        // =====================================================

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
                                        "User          : "
                                                        + currentUser.getUsername());
                        System.out.println(
                                        "Role          : "
                                                        + currentUser.getRole());
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

                        // =====================================================
                        // 7. Check physical file BEFORE charging
                        // =====================================================

                        if (!resumeFile.exists()
                                        || !resumeFile.isFile()) {

                                return ResponseEntity
                                                .notFound()
                                                .build();
                        }

                        // =====================================================
                        // 8. ADMIN = FREE
                        // =====================================================

                        boolean isAdmin = "ADMIN".equalsIgnoreCase(
                                        currentUser.getRole());

                        // =====================================================
                        // 9. NORMAL USER = 50 CREDITS
                        // =====================================================

                        if (!isAdmin) {

                                try {

                                        walletService.deductCredits(
                                                        currentUser,
                                                        RESUME_VIEW_COST);

                                } catch (IllegalStateException e) {

                                        System.out.println(
                                                        "Resume access denied for user "
                                                                        + currentUser.getUsername()
                                                                        + " : "
                                                                        + e.getMessage());

                                        return ResponseEntity
                                                        .status(HttpStatus.FORBIDDEN)
                                                        .body(Map.of(
                                                                        "message",
                                                                        e.getMessage(),
                                                                        "requiredCredits",
                                                                        RESUME_VIEW_COST));
                                }
                        }

                        // =====================================================
                        // 10. Create resource
                        // =====================================================

                        Resource resource = new FileSystemResource(resumeFile);

                        // =====================================================
                        // 11. Determine content type
                        // =====================================================

                        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

                        if (fileName
                                        .toLowerCase()
                                        .endsWith(".pdf")) {

                                mediaType = MediaType.APPLICATION_PDF;
                        }

                        // =====================================================
                        // 12. Return PDF in browser
                        // =====================================================

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
                                        .status(
                                                        HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Map.of(
                                                        "message",
                                                        "Unable to open resume."));
                }
        }

        // =========================================================
        // DOWNLOAD RESUME
        // =========================================================

        @GetMapping("/{id}/download")
        public ResponseEntity<?> downloadResume(
                        @PathVariable Long id,
                        Authentication authentication) {

                try {

                        // =====================================================
                        // 1. Authentication
                        // =====================================================

                        if (authentication == null
                                        || !authentication.isAuthenticated()) {

                                return ResponseEntity
                                                .status(HttpStatus.UNAUTHORIZED)
                                                .body(Map.of(
                                                                "message",
                                                                "Please log in to download this resume."));
                        }

                        // =====================================================
                        // 2. Find current user
                        // =====================================================

                        User currentUser = userRepository
                                        .findByUsernameIgnoreCase(
                                                        authentication.getName())
                                        .orElse(null);

                        if (currentUser == null) {

                                return ResponseEntity
                                                .status(HttpStatus.UNAUTHORIZED)
                                                .body(Map.of(
                                                                "message",
                                                                "User account not found."));
                        }

                        // =====================================================
                        // 3. Find resume
                        // =====================================================

                        Resume resume = resumeRepository
                                        .findById(id)
                                        .orElse(null);

                        if (resume == null) {

                                return ResponseEntity
                                                .notFound()
                                                .build();
                        }

                        // =====================================================
                        // 4. Get filename
                        // =====================================================

                        String fileName = resume.getFileName();

                        if (fileName == null
                                        || fileName.isBlank()) {

                                return ResponseEntity
                                                .notFound()
                                                .build();
                        }

                        // =====================================================
                        // 5. Prevent path traversal
                        // =====================================================

                        fileName = new File(fileName)
                                        .getName();

                        // =====================================================
                        // 6. Build file path
                        // =====================================================

                        File resumeFile = new File(
                                        RESUME_DIR,
                                        fileName);

                        // =====================================================
                        // 7. Check file BEFORE charging
                        // =====================================================

                        if (!resumeFile.exists()
                                        || !resumeFile.isFile()) {

                                return ResponseEntity
                                                .notFound()
                                                .build();
                        }

                        // =====================================================
                        // 8. ADMIN = FREE
                        // =====================================================

                        boolean isAdmin = "ADMIN".equalsIgnoreCase(
                                        currentUser.getRole());

                        // =====================================================
                        // 9. NORMAL USER = 50 CREDITS
                        // =====================================================

                        if (!isAdmin) {

                                try {

                                        walletService.deductCredits(
                                                        currentUser,
                                                        RESUME_VIEW_COST);

                                } catch (IllegalStateException e) {

                                        return ResponseEntity
                                                        .status(HttpStatus.FORBIDDEN)
                                                        .body(Map.of(
                                                                        "message",
                                                                        e.getMessage(),
                                                                        "requiredCredits",
                                                                        RESUME_VIEW_COST));
                                }
                        }

                        // =====================================================
                        // 10. Create resource
                        // =====================================================

                        Resource resource = new FileSystemResource(resumeFile);

                        // =====================================================
                        // 11. Force download
                        // =====================================================

                        return ResponseEntity.ok()
                                        .contentType(
                                                        MediaType.APPLICATION_PDF)
                                        .header(
                                                        HttpHeaders.CONTENT_DISPOSITION,
                                                        "attachment; filename=\""
                                                                        + fileName
                                                                        + "\"")
                                        .body(resource);

                } catch (Exception e) {

                        e.printStackTrace();

                        return ResponseEntity
                                        .status(
                                                        HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Map.of(
                                                        "message",
                                                        "Unable to download resume."));
                }
        }
}