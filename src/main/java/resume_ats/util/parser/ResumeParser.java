package resume_ats.util.parser;

import resume_ats.entity.Resume;
import resume_ats.util.cleaner.TextCleaner;
import resume_ats.util.extractor.*;

import java.util.regex.Pattern;

public final class ResumeParser {

        private ResumeParser() {
        }

        /**
         * =========================================================
         * ORIGINAL PARSE METHOD
         * =========================================================
         *
         * This keeps compatibility with any existing code that calls:
         *
         * ResumeParser.parse(text)
         */
        public static Resume parse(String text) {

                return parse(text, null);
        }

        /**
         * =========================================================
         * PARSE RESUME WITH FILENAME FALLBACK
         * =========================================================
         *
         * This method should be used when importing a resume file.
         *
         * It first tries to extract the candidate name from the
         * resume text.
         *
         * If that fails, it tries to extract the name from the
         * filename.
         */
        public static Resume parse(
                        String text,
                        String fileName) {

                Resume resume = new Resume();

                if (text == null) {
                        text = "";
                }

                text = TextCleaner.clean(text);

                // ============================
                // Basic Details
                // ============================

                String candidateName = extractCandidateName(text);

                /*
                 * If the name extracted from the resume is not reliable,
                 * try extracting the candidate name from the filename.
                 */
                if (!isValidCandidateName(candidateName)) {

                        String nameFromFile = extractNameFromFileName(fileName);

                        if (isValidCandidateName(nameFromFile)) {

                                candidateName = cleanName(nameFromFile);
                        }
                }

                /*
                 * Final fallback.
                 */
                if (!isValidCandidateName(candidateName)) {

                        candidateName = "-";
                }

                resume.setCandidateName(candidateName);

                resume.setEmail(
                                ContactExtractor.extractEmail(text));

                resume.setPhone(
                                ContactExtractor.extractPhone(text));

                // ============================
                // Professional Links
                // ============================

                resume.setLinkedIn(
                                ContactExtractor.extractLinkedIn(text));

                resume.setGithub(
                                ContactExtractor.extractGithub(text));

                resume.setPortfolio(
                                ContactExtractor.extractPortfolio(text));

                // ============================
                // Skills
                // ============================

                resume.setSkills(
                                SkillExtractor.extractSkillsAsString(text));

                // ============================
                // Experience
                // ============================

                resume.setExperienceYears(
                                (int) Math.round(
                                                ExperienceExtractor.extractExperience(text)));

                // ============================
                // Education
                // ============================

                resume.setEducation(
                                EducationExtractor.extractEducation(text));

                // ============================
                // Certifications
                // ============================

                resume.setCertifications(
                                CertificationExtractor.extractCertificationString(text));

                // ============================
                // Location
                // ============================

                resume.setLocation(
                                LocationExtractor.extractLocation(text));

                // ============================
                // Employment Type
                // ============================

                resume.setEmploymentType(
                                EmploymentTypeExtractor.extractEmploymentType(text));

                // ============================
                // Projects
                // ============================

                resume.setProjectCount(
                                ProjectExtractor.countProjects(text));

                resume.setProjectSkills(
                                String.join(
                                                ", ",
                                                ProjectExtractor.extractProjectSkills(text)));

                // ============================
                // Raw Resume
                // ============================

                resume.setRawText(text);

                return resume;
        }

        // =========================================================
        // CANDIDATE NAME EXTRACTION FROM RESUME TEXT
        // =========================================================

        private static String extractCandidateName(String text) {

                /*
                 * First try your existing NameExtractor.
                 */
                String extractedName = NameExtractor.extractName(text);

                if (isValidCandidateName(extractedName)) {

                        return cleanName(extractedName);
                }

                /*
                 * If NameExtractor fails, examine the first
                 * part of the resume.
                 */

                String[] lines = text.split("\\r?\\n");

                int checkedLines = 0;

                for (String line : lines) {

                        if (line == null) {
                                continue;
                        }

                        line = line.trim();

                        if (line.isBlank()) {
                                continue;
                        }

                        checkedLines++;

                        /*
                         * Candidate names normally appear near the top.
                         */
                        if (checkedLines > 20) {
                                break;
                        }

                        /*
                         * Ignore email lines.
                         */
                        if (line.contains("@")) {
                                continue;
                        }

                        String lower = line.toLowerCase();

                        /*
                         * Ignore URLs and social links.
                         */
                        if (lower.contains("http://")
                                        || lower.contains("https://")
                                        || lower.contains("www.")
                                        || lower.contains("linkedin")
                                        || lower.contains("github")) {

                                continue;
                        }

                        /*
                         * Ignore phone numbers.
                         */
                        if (line.matches(".*\\d{5,}.*")) {
                                continue;
                        }

                        /*
                         * Ignore resume headings.
                         */
                        if (isResumeHeading(lower)) {
                                continue;
                        }

                        /*
                         * Ignore companies.
                         */
                        if (isCompanyName(lower)) {
                                continue;
                        }

                        /*
                         * Ignore job titles.
                         */
                        if (isJobTitle(lower)) {
                                continue;
                        }

                        /*
                         * Check whether the line looks like
                         * an actual person's name.
                         */
                        if (looksLikePersonName(line)) {

                                return cleanName(line);
                        }
                }

                return "-";
        }

        // =========================================================
        // FILENAME NAME EXTRACTION
        // =========================================================

        private static String extractNameFromFileName(
                        String fileName) {

                if (fileName == null
                                || fileName.isBlank()) {

                        return "-";
                }

                /*
                 * Remove file extension.
                 */
                String name = fileName.replaceFirst(
                                "(?i)\\.(pdf|doc|docx)$",
                                "");

                /*
                 * Replace common separators.
                 */
                name = name.replace("_", " ");
                name = name.replace("-", " ");

                /*
                 * Remove brackets.
                 */
                name = name.replace("(", " ");
                name = name.replace(")", " ");
                name = name.replace("[", " ");
                name = name.replace("]", " ");

                /*
                 * Remove common resume words.
                 */
                name = name.replaceAll(
                                "(?i)\\b(resume|cv|curriculum|vitae|profile|"
                                                + "biodata|bio\\s*data)\\b",
                                " ");

                /*
                 * Remove experience information.
                 *
                 * Examples:
                 *
                 * 4 Yrs
                 * 4 Years
                 * 4 Yrs 6 Months
                 * 10 yrs
                 */
                name = name.replaceAll(
                                "(?i)\\b\\d+\\s*"
                                                + "(yrs?|years?|mos?|months?)\\b",
                                " ");

                /*
                 * Remove job-title words.
                 */
                name = name.replaceAll(
                                "(?i)\\b("
                                                + "senior|"
                                                + "junior|"
                                                + "software|"
                                                + "developer|"
                                                + "engineer|"
                                                + "python|"
                                                + "java|"
                                                + "fullstack|"
                                                + "full|"
                                                + "stack|"
                                                + "backend|"
                                                + "frontend|"
                                                + "front|"
                                                + "end|"
                                                + "data|"
                                                + "scientist|"
                                                + "analyst|"
                                                + "devops|"
                                                + "cloud|"
                                                + "support|"
                                                + "technical|"
                                                + "consultant|"
                                                + "manager|"
                                                + "administrator|"
                                                + "admin|"
                                                + "architect|"
                                                + "trainee|"
                                                + "intern"
                                                + ")\\b",
                                " ");

                /*
                 * Remove standalone numbers.
                 *
                 * Example:
                 *
                 * 140 Amaldev prasannan
                 *
                 * becomes:
                 *
                 * Amaldev prasannan
                 */
                name = name.replaceAll(
                                "\\b\\d+\\b",
                                " ");

                /*
                 * Normalize spaces.
                 */
                name = name.replaceAll(
                                "\\s+",
                                " ").trim();

                if (name.isBlank()) {
                        return "-";
                }

                /*
                 * A filename can still contain something such as:
                 *
                 * "Justkare Technologies PVT LTD"
                 *
                 * Do not accept that as a candidate.
                 */
                if (isCompanyName(name.toLowerCase())) {
                        return "-";
                }

                if (isJobTitle(name.toLowerCase())) {
                        return "-";
                }

                /*
                 * Convert:
                 *
                 * amaldev prasannan
                 *
                 * into:
                 *
                 * Amaldev Prasannan
                 */
                return toTitleCase(name);
        }

        // =========================================================
        // VALIDATE CANDIDATE NAME
        // =========================================================

        private static boolean isValidCandidateName(
                        String name) {

                if (name == null) {
                        return false;
                }

                name = name.trim();

                if (name.isBlank()) {
                        return false;
                }

                if (name.equals("-")
                                || name.equalsIgnoreCase("null")
                                || name.equalsIgnoreCase("unknown")) {

                        return false;
                }

                if (name.contains("@")) {
                        return false;
                }

                String lower = name.toLowerCase();

                if (isCompanyName(lower)) {
                        return false;
                }

                if (isJobTitle(lower)) {
                        return false;
                }

                if (isResumeHeading(lower)) {
                        return false;
                }

                return looksLikePersonName(name);
        }

        // =========================================================
        // PERSON NAME CHECK
        // =========================================================

        private static boolean looksLikePersonName(
                        String value) {

                if (value == null) {
                        return false;
                }

                value = value.trim();

                /*
                 * Candidate names normally contain
                 * 2-5 words.
                 */
                String[] words = value.split("\\s+");

                if (words.length < 2
                                || words.length > 5) {

                        return false;
                }

                /*
                 * Prevent entire sentences from being treated
                 * as names.
                 */
                if (value.length() > 60) {
                        return false;
                }

                /*
                 * Only allow normal name characters.
                 */
                if (!Pattern.matches(
                                "[A-Za-z][A-Za-z .'-]*",
                                value)) {

                        return false;
                }

                /*
                 * Validate every word.
                 */
                for (String word : words) {

                        word = word
                                        .replace(".", "")
                                        .replace("-", "")
                                        .replace("'", "");

                        if (word.length() < 2) {
                                return false;
                        }

                        if (!word.matches("[A-Za-z]+")) {
                                return false;
                        }
                }

                return true;
        }

        // =========================================================
        // COMPANY DETECTION
        // =========================================================

        private static boolean isCompanyName(
                        String text) {

                if (text == null) {
                        return false;
                }

                String value = text.toLowerCase();

                String[] companyKeywords = {

                                "pvt ltd",
                                "pvt. ltd",
                                "private limited",
                                "private ltd",
                                "limited",
                                "ltd",
                                "llp",
                                "inc",
                                "inc.",
                                "corporation",
                                "corp",
                                "technologies",
                                "technology",
                                "solutions",
                                "solution",
                                "systems",
                                "system",
                                "services",
                                "service",
                                "consulting",
                                "consultants",
                                "company",
                                "industries",
                                "industry",
                                "enterprises",
                                "enterprise",
                                "software solutions",
                                "infotech"
                };

                for (String keyword : companyKeywords) {

                        if (value.contains(keyword)) {
                                return true;
                        }
                }

                return false;
        }

        // =========================================================
        // JOB TITLE DETECTION
        // =========================================================

        private static boolean isJobTitle(
                        String text) {

                if (text == null) {
                        return false;
                }

                String value = text.toLowerCase();

                String[] jobKeywords = {

                                "software developer",
                                "software engineer",
                                "senior software developer",
                                "senior software engineer",
                                "junior software developer",
                                "junior software engineer",
                                "full stack developer",
                                "fullstack developer",
                                "backend developer",
                                "backend engineer",
                                "frontend developer",
                                "front end developer",
                                "front-end developer",
                                "python developer",
                                "java developer",
                                "java engineer",
                                "python engineer",
                                "web developer",
                                "web designer",
                                "data scientist",
                                "data analyst",
                                "data engineer",
                                "machine learning engineer",
                                "ml engineer",
                                "ai engineer",
                                "devops engineer",
                                "cloud engineer",
                                "cloud administrator",
                                "system administrator",
                                "systems administrator",
                                "network engineer",
                                "network administrator",
                                "networking engineer",
                                "it engineer",
                                "it support engineer",
                                "technical support engineer",
                                "technical support",
                                "support engineer",
                                "associate technical support engineer",
                                "project manager",
                                "product manager",
                                "business analyst",
                                "qa engineer",
                                "test engineer",
                                "software tester",
                                "intern",
                                "trainee",
                                "developer",
                                "engineer",
                                "administrator",
                                "consultant",
                                "manager",
                                "architect",
                                "analyst"
                };

                for (String keyword : jobKeywords) {

                        if (value.contains(keyword)) {
                                return true;
                        }
                }

                return false;
        }

        // =========================================================
        // RESUME HEADING DETECTION
        // =========================================================

        private static boolean isResumeHeading(
                        String text) {

                if (text == null) {
                        return false;
                }

                String value = text.trim().toLowerCase();

                String[] headings = {

                                "resume",
                                "curriculum vitae",
                                "cv",
                                "profile",
                                "professional summary",
                                "summary",
                                "objective",
                                "career objective",
                                "experience",
                                "work experience",
                                "professional experience",
                                "employment",
                                "education",
                                "skills",
                                "technical skills",
                                "core skills",
                                "certifications",
                                "projects",
                                "project experience",
                                "contact",
                                "contact details",
                                "personal details",
                                "references",
                                "achievements",
                                "declaration"
                };

                for (String heading : headings) {

                        if (value.equals(heading)) {
                                return true;
                        }
                }

                return false;
        }

        // =========================================================
        // CLEAN NAME
        // =========================================================

        private static String cleanName(
                        String name) {

                if (name == null) {
                        return "-";
                }

                name = name.trim();

                /*
                 * Remove unnecessary characters at beginning
                 * and end.
                 */
                name = name.replaceAll(
                                "^[\\s\\-:|]+|[\\s\\-:|]+$",
                                "");

                /*
                 * Normalize spaces.
                 */
                name = name.replaceAll(
                                "\\s+",
                                " ");

                return name;
        }

        // =========================================================
        // TITLE CASE
        // =========================================================

        private static String toTitleCase(
                        String text) {

                if (text == null
                                || text.isBlank()) {

                        return "-";
                }

                String[] words = text.toLowerCase()
                                .split("\\s+");

                StringBuilder result = new StringBuilder();

                for (String word : words) {

                        if (word.isBlank()) {
                                continue;
                        }

                        if (result.length() > 0) {
                                result.append(" ");
                        }

                        result.append(
                                        Character.toUpperCase(
                                                        word.charAt(0)));

                        if (word.length() > 1) {

                                result.append(
                                                word.substring(1));
                        }
                }

                return result.toString();
        }
}