package resume_ats.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import resume_ats.entity.User;
import resume_ats.repository.UserRepository;
import resume_ats.service.ExcelCandidateImportService;
import resume_ats.service.ExcelCandidateImportService.ImportResult;

import java.util.Map;

@RestController
@RequestMapping("/api/candidates")
public class ExcelCandidateImportController {

    private final ExcelCandidateImportService importService;
    private final UserRepository userRepository;

    public ExcelCandidateImportController(
            ExcelCandidateImportService importService,
            UserRepository userRepository) {

        this.importService = importService;
        this.userRepository = userRepository;
    }

    // =========================================================
    // EXCEL CANDIDATE IMPORT
    // ADMIN OR GRANTED EXCEL PERMISSION ONLY
    // =========================================================

    @PostMapping(value = "/excel-import", consumes = "multipart/form-data")
    public ResponseEntity<?> importExcel(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        // -----------------------------------------------------
        // CHECK LOGIN
        // -----------------------------------------------------

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "message",
                                    "Authentication required."));
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
                    .body(
                            Map.of(
                                    "message",
                                    "User account not found."));
        }

        // -----------------------------------------------------
        // CHECK EXCEL PERMISSION
        // -----------------------------------------------------

        boolean isAdmin = "ADMIN".equalsIgnoreCase(
                currentUser.getRole());

        boolean hasExcelPermission = currentUser.isCanUseExcel();

        if (!isAdmin &&
                !hasExcelPermission) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            Map.of(
                                    "message",
                                    "You do not have permission to import Excel files."));
        }

        // -----------------------------------------------------
        // CHECK FILE
        // -----------------------------------------------------

        if (file == null ||
                file.isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Please select an Excel file."));
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null ||
                !fileName
                        .toLowerCase()
                        .endsWith(".xlsx")) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Only .xlsx Excel files are supported."));
        }

        // -----------------------------------------------------
        // IMPORT EXCEL
        // -----------------------------------------------------

        try {

            ImportResult result = importService.importCandidates(file);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()));

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "message",
                                    "Excel import failed: "
                                            + e.getMessage()));
        }
    }
}