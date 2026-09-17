package resume_ats.controller;

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
import resume_ats.service.ResumeAccessService;
import resume_ats.service.SupabaseStorageService;

import java.util.Map;

@RestController
@RequestMapping("/api/resume")
public class ResumeFileController {

        // =========================================================
        // DEPENDENCIES
        // =========================================================

        private final ResumeRepository resumeRepository;
        private final UserRepository userRepository;
        private final ResumeAccessService resumeAccessService;
        private final SupabaseStorageService supabaseStorageService;

        // =========================================================
        // CONSTRUCTOR
        // =========================================================

        public ResumeFileController(
                        ResumeRepository resumeRepository,
                        UserRepository userRepository,
                        ResumeAccessService resumeAccessService,
                        SupabaseStorageService supabaseStorageService) {

                this.resumeRepository = resumeRepository;
                this.userRepository = userRepository;
                this.resumeAccessService = resumeAccessService;
                this.supabaseStorageService = supabaseStorageService;
        }

        // =========================================================
        // VIEW RESUME
        // =========================================================
        //
        // Existing endpoint:
        //
        // GET /api/resume/{id}
        //
        // The frontend does NOT need to change.
        //
        // =========================================================

        @GetMapping("/{id}")
        public ResponseEntity<?> viewResume(
                        @PathVariable Long id,
                        Authentication authentication) {

                try {

                        // =====================================================
                        // 1. AUTHENTICATION CHECK
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
                        // 2. FIND CURRENT USER
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
                        // 3. FIND RESUME
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
                        // 4. GET ORIGINAL FILE NAME
                        // =====================================================

                        String fileName = resume.getFileName();

                        if (fileName == null
                                        || fileName.isBlank()) {

                                System.out.println(
                                                "Resume filename missing : " + id);

                                return ResponseEntity
                                                .notFound()
                                                .build();
                        }

                        // =====================================================
                        // 5. SANITIZE FILE NAME
                        // =====================================================
                        //
                        // Prevent a malicious filename from being used in
                        // the response header.
                        //
                        // =====================================================

                        fileName = sanitizeFileName(fileName);

                        // =====================================================
                        // 6. GET SUPABASE OBJECT PATH
                        // =====================================================
                        //
                        // IMPORTANT:
                        //
                        // resume.filePath now contains something like:
                        //
                        // resumes/abc123-uuid.pdf
                        //
                        // It is NOT a local Windows/Linux file path.
                        //
                        // =====================================================

                        String objectPath = resume.getFilePath();

                        if (objectPath == null
                                        || objectPath.isBlank()) {

                                System.out.println(
                                                "Supabase object path missing for resume : "
                                                                + id);

                                return ResponseEntity
                                                .notFound()
                                                .build();
                        }

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
                                        "Supabase Path : " + objectPath);
                        System.out.println("----------------------------------");

                        // =====================================================
                        // 7. CHECK SUPABASE FILE BEFORE CHARGING
                        // =====================================================
                        //
                        // Very important:
                        //
                        // We don't charge the user if the actual file doesn't
                        // exist in Supabase.
                        //
                        // =====================================================

                        if (!supabaseStorageService.fileExists(objectPath)) {

                                System.out.println(
                                                "Resume file not found in Supabase : "
                                                                + objectPath);

                                return ResponseEntity
                                                .notFound()
                                                .build();
                        }

                        // =====================================================
                        // 8. CHECK / UNLOCK ACCESS
                        // =====================================================
                        //
                        // ADMIN:
                        // FREE
                        //
                        // EXTERNAL USER:
                        // First access = 50 credits
                        // Existing access = FREE
                        //
                        // =====================================================

                        try {

                                resumeAccessService.ensureResumeAccess(
                                                currentUser,
                                                resume);

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
                                                                resumeAccessService
                                                                                .getResumeAccessCost()));
                        }

                        // =====================================================
                        // 9. DOWNLOAD FROM SUPABASE
                        // =====================================================

                        byte[] fileBytes = supabaseStorageService.downloadFile(objectPath);

                        if (fileBytes == null
                                        || fileBytes.length == 0) {

                                System.out.println(
                                                "Supabase returned empty file : "
                                                                + objectPath);

                                return ResponseEntity
                                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .body(Map.of(
                                                                "message",
                                                                "Resume file is empty."));
                        }

                        // =====================================================
                        // 10. RETURN PDF TO BROWSER
                        // =====================================================

                        return ResponseEntity
                                        .ok()
                                        .contentType(MediaType.APPLICATION_PDF)
                                        .contentLength(fileBytes.length)
                                        .header(
                                                        HttpHeaders.CONTENT_DISPOSITION,
                                                        "inline; filename=\""
                                                                        + fileName
                                                                        + "\"")
                                        .body(fileBytes);

                } catch (Exception e) {

                        e.printStackTrace();

                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Map.of(
                                                        "message",
                                                        "Unable to open resume."));
                }
        }

        // =========================================================
        // DOWNLOAD RESUME
        // =========================================================
        //
        // Existing endpoint:
        //
        // GET /api/resume/{id}/download
        //
        // =========================================================

        @GetMapping("/{id}/download")
        public ResponseEntity<?> downloadResume(
                        @PathVariable Long id,
                        Authentication authentication) {

                try {

                        // =====================================================
                        // 1. AUTHENTICATION CHECK
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
                        // 2. FIND CURRENT USER
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
                        // 3. FIND RESUME
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
                        // 4. GET FILE NAME
                        // =====================================================

                        String fileName = resume.getFileName();

                        if (fileName == null
                                        || fileName.isBlank()) {

                                return ResponseEntity
                                                .notFound()
                                                .build();
                        }

                        // =====================================================
                        // 5. SANITIZE FILE NAME
                        // =====================================================

                        fileName = sanitizeFileName(fileName);

                        // =====================================================
                        // 6. GET SUPABASE OBJECT PATH
                        // =====================================================

                        String objectPath = resume.getFilePath();

                        if (objectPath == null
                                        || objectPath.isBlank()) {

                                System.out.println(
                                                "Supabase object path missing for resume : "
                                                                + id);

                                return ResponseEntity
                                                .notFound()
                                                .build();
                        }

                        // =====================================================
                        // 7. CHECK FILE BEFORE CHARGING
                        // =====================================================

                        if (!supabaseStorageService.fileExists(objectPath)) {

                                System.out.println(
                                                "Resume file not found in Supabase : "
                                                                + objectPath);

                                return ResponseEntity
                                                .notFound()
                                                .build();
                        }

                        // =====================================================
                        // 8. CHECK / UNLOCK ACCESS
                        // =====================================================
                        //
                        // ADMIN:
                        // FREE
                        //
                        // EXTERNAL USER:
                        // First access = 50 credits
                        // Existing access = FREE
                        //
                        // =====================================================

                        try {

                                resumeAccessService.ensureResumeAccess(
                                                currentUser,
                                                resume);

                        } catch (IllegalStateException e) {

                                System.out.println(
                                                "Resume download denied for user "
                                                                + currentUser.getUsername()
                                                                + " : "
                                                                + e.getMessage());

                                return ResponseEntity
                                                .status(HttpStatus.FORBIDDEN)
                                                .body(Map.of(
                                                                "message",
                                                                e.getMessage(),
                                                                "requiredCredits",
                                                                resumeAccessService
                                                                                .getResumeAccessCost()));
                        }

                        // =====================================================
                        // 9. DOWNLOAD FROM SUPABASE
                        // =====================================================

                        byte[] fileBytes = supabaseStorageService.downloadFile(objectPath);

                        if (fileBytes == null
                                        || fileBytes.length == 0) {

                                return ResponseEntity
                                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .body(Map.of(
                                                                "message",
                                                                "Resume file is empty."));
                        }

                        // =====================================================
                        // 10. FORCE DOWNLOAD
                        // =====================================================

                        return ResponseEntity
                                        .ok()
                                        .contentType(MediaType.APPLICATION_PDF)
                                        .contentLength(fileBytes.length)
                                        .header(
                                                        HttpHeaders.CONTENT_DISPOSITION,
                                                        "attachment; filename=\""
                                                                        + fileName
                                                                        + "\"")
                                        .body(fileBytes);

                } catch (Exception e) {

                        e.printStackTrace();

                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Map.of(
                                                        "message",
                                                        "Unable to download resume."));
                }
        }

        // =========================================================
        // FILE NAME SANITIZATION
        // =========================================================

        private String sanitizeFileName(String fileName) {

                if (fileName == null || fileName.isBlank()) {
                        return "resume.pdf";
                }

                // Remove directory/path information
                fileName = fileName.replace("\\", "/");

                int lastSlash = fileName.lastIndexOf("/");

                if (lastSlash >= 0) {
                        fileName = fileName.substring(lastSlash + 1);
                }

                // Remove characters that are unsafe for a Content-Disposition
                // filename.
                fileName = fileName
                                .replace("\"", "")
                                .replace("\r", "")
                                .replace("\n", "");

                if (fileName.isBlank()) {
                        return "resume.pdf";
                }

                return fileName;
        }
}