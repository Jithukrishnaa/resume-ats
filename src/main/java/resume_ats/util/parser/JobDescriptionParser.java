package resume_ats.util.parser;

import resume_ats.entity.JobDescription;
import resume_ats.util.cleaner.TextCleaner;
import resume_ats.util.extractor.CertificationExtractor;
import resume_ats.util.extractor.EducationExtractor;
import resume_ats.util.extractor.EmploymentTypeExtractor;
import resume_ats.util.extractor.ExperienceExtractor;
import resume_ats.util.extractor.LocationExtractor;
import resume_ats.util.extractor.SkillExtractor;

public final class JobDescriptionParser {

    private JobDescriptionParser() {
    }

    /**
     * Parse complete Job Description
     */
    public static JobDescription parse(String text) {

        JobDescription jobDescription = new JobDescription();

        if (text == null) {
            text = "";
        }

        text = TextCleaner.clean(text);

        // =========================
        // Job Title
        // =========================

        jobDescription.setTitle(
                extractJobTitle(text));

        // =========================
        // Skills
        // =========================

        jobDescription.setSkillsRequired(
                SkillExtractor.extractSkillsAsString(text));

        // =========================
        // Experience
        // =========================

        jobDescription.setExperienceRequired(
                ExperienceExtractor.extractExperience(text));

        // =========================
        // Education
        // =========================

        jobDescription.setEducation(
                EducationExtractor.extractEducation(text));

        // =========================
        // Certifications
        // =========================

        jobDescription.setCertifications(
                CertificationExtractor.extractCertificationString(text));

        // =========================
        // Location
        // =========================

        jobDescription.setLocation(
                LocationExtractor.extractLocation(text));

        // =========================
        // Employment Type
        // =========================

        jobDescription.setEmploymentType(
                EmploymentTypeExtractor.extractEmploymentType(text));

        // =========================
        // Complete JD
        // =========================

        jobDescription.setJdText(text);

        return jobDescription;
    }

    /**
     * Extract Job Title
     */
    private static String extractJobTitle(String text) {

        String[] lines = text.split("\\r?\\n");

        for (String line : lines) {

            line = line.trim();

            if (line.isBlank())
                continue;

            if (line.length() < 4)
                continue;

            if (line.length() > 100)
                continue;

            String lower = line.toLowerCase();

            if (lower.contains("job description"))
                continue;

            if (lower.contains("responsibilities"))
                continue;

            if (lower.contains("requirements"))
                continue;

            if (lower.contains("qualification"))
                continue;

            if (lower.contains("company"))
                continue;

            return line;
        }

        return "Job Description";
    }

}