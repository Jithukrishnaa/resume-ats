package resume_ats.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import resume_ats.entity.ATSResult;
import resume_ats.repository.ATSResultRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportService {

    private final ATSResultRepository atsResultRepository;

    public ExcelExportService(ATSResultRepository atsResultRepository) {
        this.atsResultRepository = atsResultRepository;
    }

    public byte[] exportATSResults() throws IOException {

        List<ATSResult> results = atsResultRepository.findAll();

        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("ATS Results");

        // ---------------- HEADER ----------------

        Row header = sheet.createRow(0);

        header.createCell(0).setCellValue("Rank");
        header.createCell(1).setCellValue("Candidate Name");
        header.createCell(2).setCellValue("Email");
        header.createCell(3).setCellValue("ATS Score");
        header.createCell(4).setCellValue("Matched Skills");
        header.createCell(5).setCellValue("Missing Skills");

        // ---------------- DATA ----------------

        int rowNum = 1;

        for (ATSResult result : results) {

            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(result.getRankPosition());

            row.createCell(1).setCellValue(result.getCandidateName());

            row.createCell(2).setCellValue(result.getEmail());

            row.createCell(3).setCellValue(result.getAtsScore());

            row.createCell(4).setCellValue(result.getMatchedSkills());

            row.createCell(5).setCellValue(result.getMissingSkills());
        }

        // Auto-size columns

        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        workbook.write(outputStream);

        workbook.close();

        return outputStream.toByteArray();
    }

}