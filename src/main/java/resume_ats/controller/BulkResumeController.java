package resume_ats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import resume_ats.service.ResumeImportService;

@RestController
@RequestMapping("/api/bulk")
public class BulkResumeController {

    private final ResumeImportService resumeImportService;

    public BulkResumeController(ResumeImportService resumeImportService) {
        this.resumeImportService = resumeImportService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadZip(
            @RequestParam("zipFile") MultipartFile zipFile) {

        try {

            int importedCount = resumeImportService.importZip(zipFile);

            return ResponseEntity.ok(
                    importedCount + " resumes imported successfully.");

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body("Bulk upload failed : " + e.getMessage());
        }
    }
}