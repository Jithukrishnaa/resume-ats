package resume_ats.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import resume_ats.service.ExcelExportService;

@RestController
@RequestMapping("/api/report")
public class ExcelController {

    private final ExcelExportService excelExportService;

    public ExcelController(ExcelExportService excelExportService) {
        this.excelExportService = excelExportService;
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadExcel() {

        try {

            byte[] excelFile = excelExportService.exportATSResults();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=ATS_Report.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelFile);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError().build();
        }
    }
}