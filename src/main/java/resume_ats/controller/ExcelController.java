package resume_ats.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import resume_ats.entity.User;
import resume_ats.repository.UserRepository;
import resume_ats.service.ExcelExportService;

@RestController
@RequestMapping("/api/report")
public class ExcelController {

    private final ExcelExportService excelExportService;
    private final UserRepository userRepository;

    public ExcelController(
            ExcelExportService excelExportService,
            UserRepository userRepository) {

        this.excelExportService = excelExportService;
        this.userRepository = userRepository;
    }

    // =========================================================
    // DOWNLOAD ATS EXCEL REPORT
    // ADMIN ONLY
    // =========================================================

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadExcel(
            Authentication authentication) {

        // -----------------------------------------------------
        // CHECK LOGIN
        // -----------------------------------------------------

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        // -----------------------------------------------------
        // FIND CURRENT USER
        // -----------------------------------------------------

        String username = authentication.getName();

        User currentUser = userRepository
                .findByUsernameIgnoreCase(username)
                .orElse(null);

        if (currentUser == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        // -----------------------------------------------------
        // ADMIN CHECK
        // -----------------------------------------------------

        if (!"ADMIN".equalsIgnoreCase(
                currentUser.getRole())) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        // -----------------------------------------------------
        // GENERATE EXCEL
        // -----------------------------------------------------

        try {

            byte[] excelFile = excelExportService.exportATSResults();

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"ATS_Report.xlsx\"")
                    .contentType(
                            MediaType.parseMediaType(
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(
                            excelFile.length)
                    .body(excelFile);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .build();
        }
    }
}