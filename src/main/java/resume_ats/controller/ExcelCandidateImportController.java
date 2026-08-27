package resume_ats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import resume_ats.service.ExcelCandidateImportService;
import resume_ats.service.ExcelCandidateImportService.ImportResult;

@RestController
@RequestMapping("/api/candidates")
public class ExcelCandidateImportController {

    private final ExcelCandidateImportService importService;

    public ExcelCandidateImportController(
            ExcelCandidateImportService importService) {

        this.importService = importService;
    }

    // =========================================================
    // EXCEL CANDIDATE IMPORT
    // =========================================================

    @PostMapping(value = "/excel-import", consumes = "multipart/form-data")
    public ResponseEntity<?> importExcel(
            @RequestParam("file") MultipartFile file) {

        try {

            ImportResult result = importService.importCandidates(file);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Excel import failed: "
                                    + e.getMessage());
        }
    }
}