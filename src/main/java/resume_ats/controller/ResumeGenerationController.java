package resume_ats.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import resume_ats.entity.User;
import resume_ats.repository.UserRepository;
import resume_ats.service.PdfResumeService;

@RestController
@RequestMapping("/api/resume-generator")
@CrossOrigin
public class ResumeGenerationController {

        private final PdfResumeService pdfResumeService;
        private final UserRepository userRepository;

        public ResumeGenerationController(
                        PdfResumeService pdfResumeService,
                        UserRepository userRepository) {

                this.pdfResumeService = pdfResumeService;
                this.userRepository = userRepository;
        }

        // =========================================================
        // GENERATE AND DOWNLOAD RESUME PDF
        // =========================================================

        @GetMapping("/generate-pdf")
        public ResponseEntity<?> generatePdf(
                        Authentication authentication) {

                System.out.println();
                System.out.println(
                                "==========================================");

                System.out.println(
                                "AI RESUME PDF GENERATION REQUEST");

                System.out.println(
                                "==========================================");

                try {

                        // =================================================
                        // 1. CHECK AUTHENTICATION
                        // =================================================

                        if (authentication == null
                                        || !authentication.isAuthenticated()) {

                                System.out.println(
                                                "ERROR: User is not authenticated");

                                return ResponseEntity
                                                .status(401)
                                                .body("Not authenticated");
                        }

                        // =================================================
                        // 2. GET LOGGED-IN USERNAME
                        // =================================================

                        String username = authentication.getName();

                        System.out.println(
                                        "Username: "
                                                        + username);

                        // =================================================
                        // 3. FIND USER
                        // =================================================

                        User user = userRepository
                                        .findByUsernameIgnoreCase(
                                                        username)
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Logged-in user not found: "
                                                                        + username));

                        System.out.println(
                                        "User ID: "
                                                        + user.getId());

                        System.out.println(
                                        "User Name: "
                                                        + user.getFullName());

                        // =================================================
                        // 4. GENERATE PDF
                        // =================================================

                        System.out.println(
                                        "Calling PdfResumeService...");

                        byte[] pdf = pdfResumeService.generatePdf(
                                        user.getId());

                        // =================================================
                        // 5. VALIDATE PDF
                        // =================================================

                        if (pdf == null
                                        || pdf.length == 0) {

                                throw new RuntimeException(
                                                "PDF generation returned empty data");
                        }

                        System.out.println(
                                        "PDF generated successfully");

                        System.out.println(
                                        "PDF size: "
                                                        + pdf.length
                                                        + " bytes");

                        // =================================================
                        // 6. CREATE FILE NAME
                        // =================================================

                        String name = user.getFullName();

                        if (name == null
                                        || name.isBlank()) {

                                name = user.getUsername();
                        }

                        if (name == null
                                        || name.isBlank()) {

                                name = "Candidate";
                        }

                        // Remove invalid filename characters

                        name = name.trim()
                                        .replaceAll(
                                                        "[^a-zA-Z0-9]+",
                                                        "_");

                        String fileName = name + "_Resume.pdf";

                        // =================================================
                        // 7. RESPONSE HEADERS
                        // =================================================

                        HttpHeaders headers = new HttpHeaders();

                        headers.setContentType(
                                        MediaType.APPLICATION_PDF);

                        headers.setContentLength(
                                        pdf.length);

                        headers.setContentDisposition(
                                        ContentDisposition
                                                        .attachment()
                                                        .filename(fileName)
                                                        .build());

                        // =================================================
                        // 8. RETURN PDF
                        // =================================================

                        System.out.println(
                                        "Downloading file: "
                                                        + fileName);

                        System.out.println(
                                        "==========================================");

                        System.out.println(
                                        "RESUME PDF GENERATION SUCCESS");

                        System.out.println(
                                        "==========================================");

                        return ResponseEntity
                                        .ok()
                                        .headers(headers)
                                        .body(pdf);

                } catch (Exception e) {

                        // =================================================
                        // ERROR
                        // =================================================

                        System.out.println();
                        System.out.println(
                                        "==========================================");

                        System.out.println(
                                        "RESUME PDF GENERATION FAILED");

                        System.out.println(
                                        "==========================================");

                        System.out.println(
                                        "Error type: "
                                                        + e.getClass()
                                                                        .getName());

                        System.out.println(
                                        "Error message: "
                                                        + e.getMessage());

                        e.printStackTrace();

                        return ResponseEntity
                                        .internalServerError()
                                        .body(
                                                        "Resume generation failed: "
                                                                        + e.getClass()
                                                                                        .getSimpleName()
                                                                        + " - "
                                                                        + e.getMessage());
                }
        }
}