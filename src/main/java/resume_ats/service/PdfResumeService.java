package resume_ats.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import resume_ats.entity.Profile;
import resume_ats.entity.ProfileCertification;
import resume_ats.entity.ProfileEducation;
import resume_ats.entity.ProfileExperience;
import resume_ats.entity.ProfileProject;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfResumeService {

    private final ProfileService profileService;

    public PdfResumeService(
            ProfileService profileService) {

        this.profileService = profileService;
    }

    // =====================================================
    // GENERATE PDF
    // =====================================================

    public byte[] generatePdf(
            Long userId) throws Exception {

        System.out.println(
                "====================================");

        System.out.println(
                "STARTING PDF GENERATION");

        System.out.println(
                "User ID : " + userId);

        System.out.println(
                "====================================");

        // =================================================
        // GET PROFILE
        // =================================================

        Profile profile = profileService.getProfile(userId);

        if (profile == null) {

            throw new RuntimeException(
                    "No profile found for user ID: "
                            + userId);
        }

        System.out.println(
                "Profile found : "
                        + profile.getFullName());

        // =================================================
        // GET PROFILE DATA
        // =================================================

        List<ProfileEducation> education = profileService.getEducation(
                profile.getId());

        List<ProfileExperience> experience = profileService.getExperience(
                profile.getId());

        List<ProfileProject> projects = profileService.getProjects(
                profile.getId());

        List<ProfileCertification> certifications = profileService.getCertifications(
                profile.getId());

        // =================================================
        // CREATE PDF
        // =================================================

        try (
                PDDocument document = new PDDocument();

                ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(document);

            writer.addPage();

            // =================================================
            // HEADER
            // =================================================

            writer.addTitle(
                    safe(profile.getFullName(),
                            "Candidate"));

            writer.addSubtitle(
                    safe(
                            profile.getProfessionalTitle(),
                            ""));

            // =================================================
            // CONTACT INFORMATION
            // =================================================

            StringBuilder contact = new StringBuilder();

            appendContact(
                    contact,
                    profile.getEmail());

            appendContact(
                    contact,
                    profile.getPhone());

            appendContact(
                    contact,
                    profile.getLocation());

            if (contact.length() > 0) {

                writer.addContact(
                        contact.toString());
            }

            // =================================================
            // LINKS
            // =================================================

            StringBuilder links = new StringBuilder();

            appendContact(
                    links,
                    profile.getLinkedin());

            appendContact(
                    links,
                    profile.getGithub());

            appendContact(
                    links,
                    profile.getPortfolio());

            if (links.length() > 0) {

                writer.addSmallText(
                        links.toString());
            }

            // =================================================
            // SUMMARY
            // =================================================

            if (hasText(
                    profile.getSummary())) {

                writer.addSection(
                        "PROFESSIONAL SUMMARY");

                writer.addParagraph(
                        profile.getSummary());
            }

            // =================================================
            // OBJECTIVE
            // =================================================

            if (hasText(
                    profile.getObjective())) {

                writer.addSection(
                        "CAREER OBJECTIVE");

                writer.addParagraph(
                        profile.getObjective());
            }

            // =================================================
            // SKILLS
            // =================================================

            if (hasText(
                    profile.getSkills())) {

                writer.addSection(
                        "SKILLS");

                writer.addParagraph(
                        profile.getSkills());
            }

            // =================================================
            // EXPERIENCE
            // =================================================

            if (experience != null
                    && !experience.isEmpty()) {

                writer.addSection(
                        "PROFESSIONAL EXPERIENCE");

                for (ProfileExperience exp : experience) {

                    String title = safe(
                            exp.getJobTitle(),
                            "");

                    String company = safe(
                            exp.getCompany(),
                            "");

                    if (!title.isBlank()) {

                        writer.addBoldLine(
                                title);
                    }

                    if (!company.isBlank()) {

                        StringBuilder line = new StringBuilder(
                                company);

                        if (hasText(
                                exp.getLocation())) {

                            line.append(
                                    " | ");

                            line.append(
                                    exp.getLocation());
                        }

                        if (hasText(
                                exp.getStartDate())) {

                            line.append(
                                    " | ");

                            line.append(
                                    exp.getStartDate());
                        }

                        if (hasText(
                                exp.getEndDate())) {

                            line.append(
                                    " - ");

                            line.append(
                                    exp.getEndDate());
                        }

                        writer.addSmallText(
                                line.toString());
                    }

                    if (hasText(
                            exp.getDescription())) {

                        writer.addParagraph(
                                exp.getDescription());
                    }

                    writer.addSpacing();
                }
            }

            // =================================================
            // PROJECTS
            // =================================================

            if (projects != null
                    && !projects.isEmpty()) {

                writer.addSection(
                        "PROJECTS");

                for (ProfileProject project : projects) {

                    if (hasText(
                            project.getProjectName())) {

                        writer.addBoldLine(
                                project.getProjectName());
                    }

                    if (hasText(
                            project.getTechnologies())) {

                        writer.addSmallText(
                                "Technologies: "
                                        + project.getTechnologies());
                    }

                    if (hasText(
                            project.getDescription())) {

                        writer.addParagraph(
                                project.getDescription());
                    }

                    if (hasText(
                            project.getProjectLink())) {

                        writer.addSmallText(
                                project.getProjectLink());
                    }

                    writer.addSpacing();
                }
            }

            // =================================================
            // EDUCATION
            // =================================================

            if (education != null
                    && !education.isEmpty()) {

                writer.addSection(
                        "EDUCATION");

                for (ProfileEducation edu : education) {

                    if (hasText(
                            edu.getDegree())) {

                        writer.addBoldLine(
                                edu.getDegree());
                    }

                    if (hasText(
                            edu.getInstitution())) {

                        writer.addSmallText(
                                edu.getInstitution());
                    }

                    StringBuilder line = new StringBuilder();

                    if (hasText(
                            edu.getLocation())) {

                        line.append(
                                edu.getLocation());
                    }

                    if (hasText(
                            edu.getStartYear())) {

                        if (line.length() > 0) {

                            line.append(
                                    " | ");
                        }

                        line.append(
                                edu.getStartYear());
                    }

                    if (hasText(
                            edu.getEndYear())) {

                        line.append(
                                " - ");

                        line.append(
                                edu.getEndYear());
                    }

                    if (hasText(
                            edu.getGrade())) {

                        if (line.length() > 0) {

                            line.append(
                                    " | ");
                        }

                        line.append(
                                edu.getGrade());
                    }

                    if (line.length() > 0) {

                        writer.addSmallText(
                                line.toString());
                    }

                    writer.addSpacing();
                }
            }

            // =================================================
            // CERTIFICATIONS
            // =================================================

            if (certifications != null
                    && !certifications.isEmpty()) {

                writer.addSection(
                        "CERTIFICATIONS");

                for (ProfileCertification cert : certifications) {

                    if (hasText(
                            cert.getCertificationName())) {

                        writer.addBoldLine(
                                cert.getCertificationName());
                    }

                    if (hasText(
                            cert.getIssuingOrganization())) {

                        writer.addSmallText(
                                cert.getIssuingOrganization());
                    }

                    if (hasText(
                            cert.getIssueDate())) {

                        writer.addSmallText(
                                "Issued: "
                                        + cert.getIssueDate());
                    }

                    if (hasText(
                            cert.getCredentialUrl())) {

                        writer.addSmallText(
                                cert.getCredentialUrl());
                    }

                    writer.addSpacing();
                }
            }

            // =================================================
            // LANGUAGES
            // =================================================

            if (hasText(
                    profile.getLanguages())) {

                writer.addSection(
                        "LANGUAGES");

                writer.addParagraph(
                        profile.getLanguages());
            }

            // =================================================
            // ACHIEVEMENTS
            // =================================================

            if (hasText(
                    profile.getAchievements())) {

                writer.addSection(
                        "ACHIEVEMENTS");

                writer.addParagraph(
                        profile.getAchievements());
            }

            // =================================================
            // CLOSE PAGE CONTENT
            // =================================================

            writer.close();

            // =================================================
            // SAVE PDF
            // =================================================

            document.save(output);

            byte[] pdf = output.toByteArray();

            System.out.println(
                    "PDF SIZE : "
                            + pdf.length
                            + " bytes");

            if (pdf.length == 0) {

                throw new RuntimeException(
                        "PDF generation produced zero bytes");
            }

            System.out.println(
                    "PDF GENERATION SUCCESSFUL");

            return pdf;
        }
    }

    // =====================================================
    // CONTACT HELPER
    // =====================================================

    private void appendContact(
            StringBuilder builder,
            String value) {

        if (!hasText(value)) {

            return;
        }

        if (builder.length() > 0) {

            builder.append(
                    "  |  ");
        }

        builder.append(
                value.trim());
    }

    // =====================================================
    // SAFE STRING
    // =====================================================

    private String safe(
            String value,
            String fallback) {

        if (value == null
                || value.isBlank()) {

            return fallback;
        }

        return value.trim();
    }

    // =====================================================
    // HAS TEXT
    // =====================================================

    private boolean hasText(
            String value) {

        return value != null
                && !value.isBlank();
    }

    // =====================================================
    // INTERNAL PDF WRITER
    // =====================================================

    private static class PdfWriter {

        private final PDDocument document;

        private PDPage page;

        private PDPageContentStream content;

        private float y;

        private final float margin = 50f;

        private final float pageWidth = PDRectangle.A4.getWidth();

        private final float pageHeight = PDRectangle.A4.getHeight();

        private final PDType1Font regular = new PDType1Font(
                Standard14Fonts.FontName.HELVETICA);

        private final PDType1Font bold = new PDType1Font(
                Standard14Fonts.FontName.HELVETICA_BOLD);

        private final PDType1Font italic = new PDType1Font(
                Standard14Fonts.FontName.HELVETICA_OBLIQUE);

        PdfWriter(
                PDDocument document) {

            this.document = document;
        }

        // =================================================
        // ADD PAGE
        // =================================================

        void addPage()
                throws Exception {

            page = new PDPage(
                    PDRectangle.A4);

            document.addPage(
                    page);

            content = new PDPageContentStream(
                    document,
                    page);

            y = pageHeight
                    - margin;
        }

        // =================================================
        // CHECK SPACE
        // =================================================

        private void checkSpace(
                float required)
                throws Exception {

            if (y - required < margin) {

                content.close();

                addPage();
            }
        }

        // =================================================
        // TITLE
        // =================================================

        void addTitle(
                String text)
                throws Exception {

            checkSpace(35);

            content.beginText();

            content.setFont(
                    bold,
                    22);

            content.newLineAtOffset(
                    margin,
                    y);

            content.showText(
                    cleanPdfText(text));

            content.endText();

            y -= 28;
        }

        // =================================================
        // SUBTITLE
        // =================================================

        void addSubtitle(
                String text)
                throws Exception {

            if (text == null
                    || text.isBlank()) {

                return;
            }

            checkSpace(25);

            content.beginText();

            content.setFont(
                    regular,
                    12);

            content.newLineAtOffset(
                    margin,
                    y);

            content.showText(
                    cleanPdfText(text));

            content.endText();

            y -= 20;
        }

        // =================================================
        // CONTACT
        // =================================================

        void addContact(
                String text)
                throws Exception {

            checkSpace(20);

            content.beginText();

            content.setFont(
                    regular,
                    9);

            content.newLineAtOffset(
                    margin,
                    y);

            content.showText(
                    cleanPdfText(text));

            content.endText();

            y -= 16;
        }

        // =================================================
        // SMALL TEXT
        // =================================================

        void addSmallText(
                String text)
                throws Exception {

            if (text == null
                    || text.isBlank()) {

                return;
            }

            writeWrapped(
                    text,
                    regular,
                    9,
                    13);
        }

        // =================================================
        // BOLD LINE
        // =================================================

        void addBoldLine(
                String text)
                throws Exception {

            if (text == null
                    || text.isBlank()) {

                return;
            }

            writeWrapped(
                    text,
                    bold,
                    11,
                    15);
        }

        // =================================================
        // PARAGRAPH
        // =================================================

        void addParagraph(
                String text)
                throws Exception {

            if (text == null
                    || text.isBlank()) {

                return;
            }

            writeWrapped(
                    text,
                    regular,
                    10,
                    15);

            y -= 4;
        }

        // =================================================
        // SECTION
        // =================================================

        void addSection(
                String title)
                throws Exception {

            checkSpace(35);

            y -= 7;

            content.beginText();

            content.setFont(
                    bold,
                    12);

            content.newLineAtOffset(
                    margin,
                    y);

            content.showText(
                    cleanPdfText(title));

            content.endText();

            y -= 5;

            content.moveTo(
                    margin,
                    y);

            content.lineTo(
                    pageWidth - margin,
                    y);

            content.stroke();

            y -= 15;
        }

        // =================================================
        // SPACING
        // =================================================

        void addSpacing()
                throws Exception {

            y -= 8;

            checkSpace(10);
        }

        // =================================================
        // WRAPPED TEXT
        // =================================================

        private void writeWrapped(
                String text,
                PDType1Font font,
                float fontSize,
                float lineHeight)
                throws Exception {

            String clean = cleanPdfText(text);

            if (clean.isBlank()) {

                return;
            }

            float maxWidth = pageWidth
                    - (margin * 2);

            String[] paragraphs = clean.split(
                    "\\r?\\n");

            for (String paragraph : paragraphs) {

                String[] words = paragraph.trim()
                        .split("\\s+");

                StringBuilder line = new StringBuilder();

                for (String word : words) {

                    String test;

                    if (line.length() == 0) {

                        test = word;

                    } else {

                        test = line
                                + " "
                                + word;
                    }

                    float width = font.getStringWidth(
                            test)
                            / 1000f
                            * fontSize;

                    if (width > maxWidth
                            && line.length() > 0) {

                        writeLine(
                                line.toString(),
                                font,
                                fontSize,
                                lineHeight);

                        line = new StringBuilder(
                                word);

                    } else {

                        line = new StringBuilder(
                                test);
                    }
                }

                if (line.length() > 0) {

                    writeLine(
                            line.toString(),
                            font,
                            fontSize,
                            lineHeight);
                }
            }
        }

        // =================================================
        // WRITE LINE
        // =================================================

        private void writeLine(
                String text,
                PDType1Font font,
                float fontSize,
                float lineHeight)
                throws Exception {

            checkSpace(
                    lineHeight + 5);

            content.beginText();

            content.setFont(
                    font,
                    fontSize);

            content.newLineAtOffset(
                    margin,
                    y);

            content.showText(
                    cleanPdfText(text));

            content.endText();

            y -= lineHeight;
        }

        // =================================================
        // CLOSE
        // =================================================

        void close()
                throws Exception {

            if (content != null) {

                content.close();

                content = null;
            }
        }

        // =================================================
        // CLEAN PDF TEXT
        // =================================================

        private static String cleanPdfText(
                String text) {

            if (text == null) {

                return "";
            }

            /*
             * Helvetica is a WinAnsi font.
             *
             * Remove unsupported Unicode characters
             * so PDFBox does not create a corrupt PDF.
             */

            return text
                    .replace(
                            "\u2018",
                            "'")
                    .replace(
                            "\u2019",
                            "'")
                    .replace(
                            "\u201C",
                            "\"")
                    .replace(
                            "\u201D",
                            "\"")
                    .replace(
                            "\u2013",
                            "-")
                    .replace(
                            "\u2014",
                            "-")
                    .replace(
                            "\u2022",
                            "-")
                    .replaceAll(
                            "[^\\x00-\\xFF]",
                            "");
        }
    }
}