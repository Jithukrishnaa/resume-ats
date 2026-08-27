package resume_ats.service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import resume_ats.entity.Resume;
import resume_ats.repository.ResumeRepository;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelCandidateImportService {

    private final ResumeRepository resumeRepository;

    public ExcelCandidateImportService(
            ResumeRepository resumeRepository) {

        this.resumeRepository = resumeRepository;
    }

    // =========================================================
    // IMPORT CANDIDATES FROM EXCEL
    // =========================================================

    public ImportResult importCandidates(
            MultipartFile file) throws Exception {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select an Excel file.");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null
                || !fileName.toLowerCase().endsWith(".xlsx")) {

            throw new IllegalArgumentException(
                    "Only .xlsx Excel files are supported.");
        }

        int imported = 0;
        int skipped = 0;

        List<String> errors = new ArrayList<>();

        try (
                InputStream inputStream = file.getInputStream();

                Workbook workbook = WorkbookFactory.create(inputStream)) {

            if (workbook.getNumberOfSheets() == 0) {

                throw new IllegalArgumentException(
                        "Excel file contains no sheets.");
            }

            Sheet sheet = workbook.getSheetAt(0);

            if (sheet.getPhysicalNumberOfRows() <= 1) {

                throw new IllegalArgumentException(
                        "Excel file does not contain candidate data.");
            }

            // =====================================================
            // HEADER
            // =====================================================

            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {

                throw new IllegalArgumentException(
                        "Excel header row is missing.");
            }

            int nameColumn = -1;
            int emailColumn = -1;
            int phoneColumn = -1;
            int skillsColumn = -1;
            int experienceColumn = -1;
            int educationColumn = -1;
            int locationColumn = -1;

            // =====================================================
            // FIND COLUMNS
            // =====================================================

            for (Cell cell : headerRow) {

                String header = getCellValue(cell)
                        .trim()
                        .toLowerCase();

                switch (header) {

                    case "name":
                    case "candidate name":
                    case "candidate_name":

                        nameColumn = cell.getColumnIndex();

                        break;

                    case "email":
                    case "email address":

                        emailColumn = cell.getColumnIndex();

                        break;

                    case "phone":
                    case "mobile":
                    case "phone number":

                        phoneColumn = cell.getColumnIndex();

                        break;

                    case "skills":
                    case "skill":

                        skillsColumn = cell.getColumnIndex();

                        break;

                    case "experience":
                    case "experience years":
                    case "experience_years":

                        experienceColumn = cell.getColumnIndex();

                        break;

                    case "education":
                    case "qualification":

                        educationColumn = cell.getColumnIndex();

                        break;

                    case "location":
                    case "city":

                        locationColumn = cell.getColumnIndex();

                        break;

                    default:
                        break;
                }
            }

            // =====================================================
            // VALIDATE HEADERS
            // =====================================================

            if (nameColumn == -1
                    && emailColumn == -1) {

                throw new IllegalArgumentException(
                        "Excel must contain at least a Name or Email column.");
            }

            // =====================================================
            // PROCESS ROWS
            // =====================================================

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {

                Row row = sheet.getRow(rowIndex);

                if (row == null) {
                    continue;
                }

                try {

                    String candidateName = getCellValue(
                            row,
                            nameColumn);

                    String email = getCellValue(
                            row,
                            emailColumn);

                    String phone = getCellValue(
                            row,
                            phoneColumn);

                    String skills = getCellValue(
                            row,
                            skillsColumn);

                    String experience = getCellValue(
                            row,
                            experienceColumn);

                    String education = getCellValue(
                            row,
                            educationColumn);

                    String location = getCellValue(
                            row,
                            locationColumn);

                    // -------------------------------------------------
                    // Empty row
                    // -------------------------------------------------

                    if (candidateName.isBlank()
                            && email.isBlank()) {

                        continue;
                    }

                    // -------------------------------------------------
                    // Duplicate email
                    // -------------------------------------------------

                    if (!email.isBlank()
                            && resumeRepository
                                    .existsByEmailIgnoreCase(
                                            email)) {

                        skipped++;
                        continue;
                    }

                    // -------------------------------------------------
                    // Create Resume
                    // -------------------------------------------------

                    Resume resume = new Resume();

                    resume.setCandidateName(
                            candidateName);

                    resume.setEmail(
                            email);

                    resume.setPhone(
                            phone);

                    resume.setSkills(
                            skills);

                    resume.setEducation(
                            education);

                    resume.setLocation(
                            location);

                    /*
                     * Excel-only candidate.
                     *
                     * There is no physical PDF/DOC/DOCX
                     * resume attached to this record.
                     */

                    resume.setFileName(null);

                    resume.setFilePath(null);

                    resume.setRawText(
                            buildRawText(
                                    candidateName,
                                    email,
                                    phone,
                                    skills,
                                    experience,
                                    education,
                                    location));

                    resumeRepository.save(resume);

                    imported++;

                } catch (Exception rowException) {

                    skipped++;

                    errors.add(
                            "Row "
                                    + (rowIndex + 1)
                                    + ": "
                                    + rowException.getMessage());
                }
            }
        }

        return new ImportResult(
                imported,
                skipped,
                errors);
    }

    // =========================================================
    // CELL VALUE
    // =========================================================

    private String getCellValue(
            Row row,
            int columnIndex) {

        if (row == null
                || columnIndex < 0) {

            return "";
        }

        Cell cell = row.getCell(
                columnIndex,
                Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

        return getCellValue(cell);
    }

    private String getCellValue(
            Cell cell) {

        if (cell == null) {
            return "";
        }

        DataFormatter formatter = new DataFormatter();

        return formatter
                .formatCellValue(cell)
                .trim();
    }

    // =========================================================
    // EXPERIENCE PARSER
    // =========================================================

    private double parseExperience(
            String value) {

        if (value == null
                || value.isBlank()) {

            return 0.0;
        }

        try {

            return Double.parseDouble(
                    value.trim());

        } catch (NumberFormatException ignored) {

            java.util.regex.Matcher matcher = java.util.regex.Pattern
                    .compile(
                            "(\\d+(?:\\.\\d+)?)")
                    .matcher(value);

            if (matcher.find()) {

                try {

                    return Double.parseDouble(
                            matcher.group(1));

                } catch (NumberFormatException ignoredAgain) {

                    return 0.0;
                }
            }

            return 0.0;
        }
    }

    // =========================================================
    // RAW TEXT
    // =========================================================

    private String buildRawText(
            String name,
            String email,
            String phone,
            String skills,
            String experience,
            String education,
            String location) {

        return "Candidate Name: " + name
                + "\nEmail: " + email
                + "\nPhone: " + phone
                + "\nSkills: " + skills
                + "\nExperience: " + experience
                + "\nEducation: " + education
                + "\nLocation: " + location;
    }

    // =========================================================
    // IMPORT RESULT
    // =========================================================

    public static class ImportResult {

        private final int imported;
        private final int skipped;
        private final List<String> errors;

        public ImportResult(
                int imported,
                int skipped,
                List<String> errors) {

            this.imported = imported;
            this.skipped = skipped;
            this.errors = errors;
        }

        public int getImported() {
            return imported;
        }

        public int getSkipped() {
            return skipped;
        }

        public List<String> getErrors() {
            return errors;
        }
    }
}