package resume_ats.util.parser;

import org.springframework.stereotype.Component;

import resume_ats.entity.Resume;
import resume_ats.util.cleaner.TextCleaner;
import resume_ats.util.extractor.CertificationExtractor;
import resume_ats.util.extractor.ContactExtractor;
import resume_ats.util.extractor.EducationExtractor;
import resume_ats.util.extractor.EmploymentTypeExtractor;
import resume_ats.util.extractor.ExperienceExtractor;
import resume_ats.util.extractor.LocationExtractor;
import resume_ats.util.extractor.NameExtractor;
import resume_ats.util.extractor.ProjectExtractor;
import resume_ats.util.extractor.SkillExtractor;

import java.util.regex.Pattern;

@Component
public final class ResumeParser {

        private ResumeParser() {
        }

        // =========================================================
        // MAIN PARSER
        // =========================================================

        public static Resume parse(String text) {
                return parse(text, null);
        }

        public static Resume parse(
                        String text,
                        String fileName) {

                Resume resume = new Resume();

                if (text == null) {
                        text = "";
                }

                text = TextCleaner.clean(text);

                // =====================================================
                // CANDIDATE NAME
                // =====================================================

                resume.setCandidateName(
                                extractCandidateName(
                                                text,
                                                fileName));

                // =====================================================
                // EMAIL
                // =====================================================

                resume.setEmail(
                                ContactExtractor.extractEmail(text));

                // =====================================================
                // PHONE
                // =====================================================

                resume.setPhone(
                                ContactExtractor.extractPhone(text));

                // =====================================================
                // LINKEDIN
                // =====================================================

                resume.setLinkedIn(
                                ContactExtractor.extractLinkedIn(text));

                // =====================================================
                // GITHUB
                // =====================================================

                resume.setGithub(
                                ContactExtractor.extractGithub(text));

                // =====================================================
                // PORTFOLIO
                // =====================================================

                resume.setPortfolio(
                                ContactExtractor.extractPortfolio(text));

                // =====================================================
                // SKILLS
                // =====================================================

                resume.setSkills(
                                SkillExtractor.extractSkillsAsString(text));

                // =====================================================
                // EXPERIENCE
                // =====================================================

                resume.setExperienceYears(
                                (int) Math.round(
                                                ExperienceExtractor
                                                                .extractExperience(text)));

                // =====================================================
                // EDUCATION
                // =====================================================

                resume.setEducation(
                                EducationExtractor.extractEducation(text));

                // =====================================================
                // CERTIFICATIONS
                // =====================================================

                resume.setCertifications(
                                CertificationExtractor
                                                .extractCertificationString(text));

                // =====================================================
                // LOCATION
                // =====================================================

                resume.setLocation(
                                LocationExtractor.extractLocation(text));

                // =====================================================
                // EMPLOYMENT TYPE
                // =====================================================

                resume.setEmploymentType(
                                EmploymentTypeExtractor
                                                .extractEmploymentType(text));

                // =====================================================
                // PROJECT COUNT
                // =====================================================

                resume.setProjectCount(
                                ProjectExtractor.countProjects(text));

                // =====================================================
                // PROJECT SKILLS
                // =====================================================

                resume.setProjectSkills(
                                String.join(
                                                ", ",
                                                ProjectExtractor
                                                                .extractProjectSkills(text)));

                // =====================================================
                // RAW TEXT
                // =====================================================

                resume.setRawText(text);

                return resume;
        }

        // =========================================================
        // CANDIDATE NAME EXTRACTION
        //
        // PRIORITY:
        //
        // 1. Structured filename
        // 2. Explicit name in resume
        // 3. Top section of resume
        // 4. NameExtractor
        // 5. Email username
        // 6. No Name
        // =========================================================

        private static String extractCandidateName(
                        String text,
                        String fileName) {

                // =====================================================
                // STEP 1
                // FILENAME
                //
                // Example:
                //
                // 24 - Vignesh m - Senior analyst - 4 Yrs 0 Month.pdf
                //
                // -> Vignesh M
                // =====================================================

                String nameFromFile = extractNameFromFileName(fileName);

                if (isValidFilenameName(nameFromFile)) {

                        return cleanName(nameFromFile);
                }

                // =====================================================
                // STEP 2
                // EXPLICIT NAME FIELD
                // =====================================================

                String labeledName = extractLabeledName(text);

                if (isValidCandidateName(labeledName)) {

                        return cleanName(labeledName);
                }

                // =====================================================
                // STEP 3
                // TOP RESUME LINES
                // =====================================================

                String topName = extractNameFromTopLines(text);

                if (isValidCandidateName(topName)) {

                        return cleanName(topName);
                }

                // =====================================================
                // STEP 4
                // EXISTING NAME EXTRACTOR
                // =====================================================

                String extractedName = NameExtractor.extractName(text);

                if (isValidCandidateName(extractedName)) {

                        return cleanName(extractedName);
                }

                // =====================================================
                // STEP 5
                // EMAIL FALLBACK
                // =====================================================

                String email = ContactExtractor.extractEmail(text);

                String emailName = extractNameFromEmail(email);

                if (!emailName.isBlank()) {

                        return emailName;
                }

                // =====================================================
                // STEP 6
                // FINAL FALLBACK
                // =====================================================

                return "No Name";
        }

        // =========================================================
        // STRUCTURED FILENAME EXTRACTION
        // =========================================================

        private static String extractNameFromFileName(
                        String fileName) {

                if (fileName == null
                                || fileName.isBlank()) {

                        return "";
                }

                // -----------------------------------------------------
                // Remove extension
                // -----------------------------------------------------

                String name = fileName.replaceFirst(
                                "(?i)\\.(pdf|doc|docx)$",
                                "");

                // =====================================================
                // FORMAT:
                //
                // 24 - Vignesh m - Senior analyst - 4 Yrs 0 Month
                //
                // 10 - Unnimaya c o - Python odoo developer...
                //
                // 39 - Tinu tom - Senior system engineer...
                //
                // 3 - Safee s - Area visual merchandiser...
                // =====================================================

                String[] parts = name.split(
                                "\\s+-\\s+",
                                3);

                if (parts.length >= 2) {

                        String possibleName = parts[1].trim();

                        // Remove accidental numbers
                        possibleName = possibleName.replaceAll(
                                        "\\d+",
                                        " ");

                        possibleName = possibleName.replaceAll(
                                        "\\s+",
                                        " ").trim();

                        // Remove attached Resume / CV
                        possibleName = possibleName.replaceAll(
                                        "(?i)(resume|cv)$",
                                        "").trim();

                        if (isSimplePersonName(
                                        possibleName)) {

                                return possibleName;
                        }
                }

                // =====================================================
                // OTHER FILENAME FORMATS
                // =====================================================

                name = name.replace(
                                "_",
                                " ");

                name = name.replace(
                                "-",
                                " ");

                // -----------------------------------------------------
                // Remove random upload prefixes
                // -----------------------------------------------------

                name = name.replaceFirst(
                                "(?i)^.*?file[-_]?submit[-_]+",
                                "");

                // -----------------------------------------------------
                // Candidate Evaluation Report
                // -----------------------------------------------------

                name = name.replaceFirst(
                                "(?i)^candidate[-_\\s]+"
                                                + "evaluation[-_\\s]+"
                                                + "report[-_]*",
                                "");

                // -----------------------------------------------------
                // Resume / CV prefixes
                // -----------------------------------------------------

                name = name.replaceFirst(
                                "(?i)^resume[-_\\s]+",
                                "");

                name = name.replaceFirst(
                                "(?i)^cv[-_\\s]+",
                                "");

                // -----------------------------------------------------
                // Remove attached Resume / CV
                // -----------------------------------------------------

                name = name.replaceAll(
                                "(?i)(resume|cv)$",
                                "");

                // -----------------------------------------------------
                // Remove resume-related words
                // -----------------------------------------------------

                name = name.replaceAll(
                                "(?i)\\b("
                                                + "resume|"
                                                + "cv|"
                                                + "curriculum|"
                                                + "vitae|"
                                                + "profile|"
                                                + "biodata|"
                                                + "candidate|"
                                                + "evaluation|"
                                                + "report"
                                                + ")\\b",
                                " ");

                // -----------------------------------------------------
                // Remove experience
                //
                // 4 Yrs
                // 5 Years
                // 3 Months
                // -----------------------------------------------------

                name = name.replaceAll(
                                "(?i)\\b"
                                                + "\\d+(?:\\.\\d+)?"
                                                + "\\s*"
                                                + "(yrs?|years?|"
                                                + "mos?|months?)"
                                                + "\\b",
                                " ");

                // -----------------------------------------------------
                // Remove numbers
                // -----------------------------------------------------

                name = name.replaceAll(
                                "\\b\\d+\\b",
                                " ");

                // -----------------------------------------------------
                // Remove job titles / technologies
                // -----------------------------------------------------

                name = name.replaceAll(
                                "(?i)\\b("
                                                + "senior|"
                                                + "junior|"
                                                + "software|"
                                                + "developer|"
                                                + "engineer|"
                                                + "python|"
                                                + "java|"
                                                + "javascript|"
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
                                                + "technical|"
                                                + "consultant|"
                                                + "manager|"
                                                + "administrator|"
                                                + "admin|"
                                                + "architect|"
                                                + "trainee|"
                                                + "intern|"
                                                + "support|"
                                                + "designer|"
                                                + "design|"
                                                + "aws|"
                                                + "azure|"
                                                + "gcp|"
                                                + "linux|"
                                                + "sql|"
                                                + "odoo|"
                                                + "system|"
                                                + "visual|"
                                                + "merchandiser"
                                                + ")\\b",
                                " ");

                // -----------------------------------------------------
                // Normalize spaces
                // -----------------------------------------------------

                name = name.replaceAll(
                                "\\s+",
                                " ").trim();

                if (isSimplePersonName(name)) {
                        return name;
                }

                return "";
        }

        // =========================================================
        // SIMPLE PERSON NAME VALIDATION
        // =========================================================

        private static boolean isSimplePersonName(
                        String name) {

                if (name == null
                                || name.isBlank()) {

                        return false;
                }

                name = name.trim();

                if (name.length() < 2
                                || name.length() > 40) {

                        return false;
                }

                // -----------------------------------------------------
                // Numbers are never allowed
                // -----------------------------------------------------

                if (name.matches(".*\\d.*")) {
                        return false;
                }

                // -----------------------------------------------------
                // Email is never allowed
                // -----------------------------------------------------

                if (name.contains("@")) {
                        return false;
                }

                // -----------------------------------------------------
                // Only letters, spaces, apostrophe and dot
                // -----------------------------------------------------

                if (!name.matches(
                                "[A-Za-z][A-Za-z .']*")) {

                        return false;
                }

                String lower = name.toLowerCase();

                // =====================================================
                // KNOWN BAD VALUES
                // =====================================================

                String[] invalidNames = {

                                "resume",
                                "cv",
                                "curriculum vitae",

                                "access healthcare",
                                "power automate",
                                "problem solving",
                                "key result areas",
                                "collect the payment",

                                "about me",
                                "profile",
                                "summary",
                                "professional summary",
                                "career objective",

                                "skills",
                                "technical skills",
                                "core skills",
                                "key skills",
                                "core competencies",

                                "experience",
                                "work experience",
                                "professional experience",

                                "education",
                                "projects",
                                "certifications",

                                "software engineer",
                                "software developer",
                                "python developer",
                                "java developer",
                                "senior analyst",
                                "data analyst",
                                "system engineer",

                                "branch operations manager",
                                "sales manager",
                                "sales supervisor",
                                "sales consultant",

                                "area visual merchandiser"
                };

                for (String invalid : invalidNames) {

                        if (lower.equals(invalid)) {
                                return false;
                        }
                }

                // =====================================================
                // COMPANY CHECK
                // =====================================================

                if (isCompanyName(lower)) {
                        return false;
                }

                // =====================================================
                // JOB TITLE CHECK
                // =====================================================

                if (isJobTitle(lower)) {
                        return false;
                }

                // =====================================================
                // RESUME CONTENT CHECK
                // =====================================================

                if (containsNonNameWords(lower)) {
                        return false;
                }

                return true;
        }

        // =========================================================
        // FILENAME VALIDATION
        // =========================================================

        private static boolean isValidFilenameName(
                        String name) {

                if (name == null
                                || name.isBlank()) {

                        return false;
                }

                name = name.trim();

                if (name.equalsIgnoreCase("no name")
                                || name.equalsIgnoreCase("unknown")
                                || name.equals("-")) {

                        return false;
                }

                if (name.matches(".*\\d.*")) {
                        return false;
                }

                if (name.contains("@")) {
                        return false;
                }

                return isSimplePersonName(name);
        }

        // =========================================================
        // EXPLICIT NAME EXTRACTION
        // =========================================================

        private static String extractLabeledName(
                        String text) {

                if (text == null
                                || text.isBlank()) {

                        return "";
                }

                String[] lines = text.split("\\r?\\n");

                for (String line : lines) {

                        if (line == null) {
                                continue;
                        }

                        line = line.trim();

                        if (line.isBlank()) {
                                continue;
                        }

                        String lower = line.toLowerCase();

                        if (lower.startsWith(
                                        "full name:")) {

                                return line.substring(
                                                line.indexOf(":") + 1).trim();
                        }

                        if (lower.startsWith(
                                        "candidate name:")) {

                                return line.substring(
                                                line.indexOf(":") + 1).trim();
                        }

                        if (lower.startsWith(
                                        "candidate:")) {

                                return line.substring(
                                                line.indexOf(":") + 1).trim();
                        }

                        if (lower.startsWith(
                                        "name:")) {

                                return line.substring(
                                                line.indexOf(":") + 1).trim();
                        }
                }

                return "";
        }

        // =========================================================
        // TOP RESUME LINES
        // =========================================================

        private static String extractNameFromTopLines(
                        String text) {

                if (text == null
                                || text.isBlank()) {

                        return "";
                }

                String[] lines = text.split("\\r?\\n");

                int checkedLines = 0;

                for (String originalLine : lines) {

                        if (originalLine == null) {
                                continue;
                        }

                        String line = originalLine.trim();

                        if (line.isBlank()) {
                                continue;
                        }

                        checkedLines++;

                        if (checkedLines > 20) {
                                break;
                        }

                        // -------------------------------------------------
                        // Remove bullets
                        // -------------------------------------------------

                        line = line.replaceFirst(
                                        "^[\\-•▪►*]+\\s*",
                                        "");

                        line = line.trim();

                        // -------------------------------------------------
                        // Length
                        // -------------------------------------------------

                        if (line.length() < 3
                                        || line.length() > 45) {

                                continue;
                        }

                        String lower = line.toLowerCase();

                        // -------------------------------------------------
                        // Email
                        // -------------------------------------------------

                        if (line.contains("@")) {
                                continue;
                        }

                        // -------------------------------------------------
                        // URL
                        // -------------------------------------------------

                        if (lower.contains("http")
                                        || lower.contains("www.")
                                        || lower.contains("linkedin")
                                        || lower.contains("github")
                                        || lower.contains("portfolio")) {

                                continue;
                        }

                        // -------------------------------------------------
                        // Numbers
                        // -------------------------------------------------

                        if (line.matches(".*\\d.*")) {
                                continue;
                        }

                        // -------------------------------------------------
                        // Labels
                        // -------------------------------------------------

                        if (line.contains(":")) {
                                continue;
                        }

                        // -------------------------------------------------
                        // Reject headings
                        // -------------------------------------------------

                        if (isResumeHeading(lower)) {
                                continue;
                        }

                        // -------------------------------------------------
                        // Reject company names
                        // -------------------------------------------------

                        if (isCompanyName(lower)) {
                                continue;
                        }

                        // -------------------------------------------------
                        // Reject job titles
                        // -------------------------------------------------

                        if (isJobTitle(lower)) {
                                continue;
                        }

                        // -------------------------------------------------
                        // Reject resume content
                        // -------------------------------------------------

                        if (containsNonNameWords(lower)) {
                                continue;
                        }

                        if (isSimplePersonName(line)) {
                                return line;
                        }
                }

                return "";
        }

        // =========================================================
        // EMAIL FALLBACK
        // =========================================================

        private static String extractNameFromEmail(
                        String email) {

                if (email == null
                                || email.isBlank()) {

                        return "";
                }

                email = email.trim();

                int atIndex = email.indexOf("@");

                if (atIndex <= 0) {
                        return "";
                }

                String username = email.substring(
                                0,
                                atIndex);

                username = username.trim();

                if (username.isBlank()) {
                        return "";
                }

                // -----------------------------------------------------
                // Convert separators
                // -----------------------------------------------------

                username = username.replace(
                                ".",
                                " ");

                username = username.replace(
                                "_",
                                " ");

                username = username.replace(
                                "-",
                                " ");

                // -----------------------------------------------------
                // Remove trailing numbers
                //
                // mkvignesh304 -> mkvignesh
                // jareenajilson2014 -> jareenajilson
                // unnimayaco94 -> unnimayaco
                // -----------------------------------------------------

                username = username.replaceAll(
                                "\\d+$",
                                "");

                // -----------------------------------------------------
                // Normalize
                // -----------------------------------------------------

                username = username.replaceAll(
                                "\\s+",
                                " ").trim();

                if (username.isBlank()) {
                        return "";
                }

                String lower = username.toLowerCase();

                // =====================================================
                // GENERIC EMAILS
                // =====================================================

                String[] genericEmails = {

                                "hr",
                                "admin",
                                "administrator",
                                "recruitment",
                                "recruiter",
                                "careers",
                                "career",
                                "jobs",
                                "job",
                                "info",
                                "contact",
                                "support",
                                "hello",
                                "office"
                };

                for (String generic : genericEmails) {

                        if (lower.equals(generic)) {
                                return "";
                        }
                }

                return toTitleCase(username);
        }

        // =========================================================
        // GENERAL NAME VALIDATION
        // =========================================================

        private static boolean isValidCandidateName(
                        String name) {

                if (name == null
                                || name.isBlank()) {

                        return false;
                }

                name = name.trim();

                if (name.equals("-")
                                || name.equalsIgnoreCase(
                                                "no name")
                                || name.equalsIgnoreCase(
                                                "null")
                                || name.equalsIgnoreCase(
                                                "unknown")) {

                        return false;
                }

                if (name.contains("@")) {
                        return false;
                }

                if (name.matches(".*\\d.*")) {
                        return false;
                }

                String lower = name.toLowerCase();

                if (lower.contains("http")
                                || lower.contains("www.")
                                || lower.contains("linkedin")
                                || lower.contains("github")) {

                        return false;
                }

                if (isResumeHeading(lower)) {
                        return false;
                }

                if (isCompanyName(lower)) {
                        return false;
                }

                if (isJobTitle(lower)) {
                        return false;
                }

                if (containsNonNameWords(lower)) {
                        return false;
                }

                return isSimplePersonName(name);
        }

        // =========================================================
        // RESUME HEADING CHECK
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

                                "about me",
                                "profile",
                                "professional profile",
                                "professional summary",
                                "summary",
                                "career summary",
                                "objective",
                                "career objective",

                                "experience",
                                "work experience",
                                "professional experience",
                                "employment",
                                "employment history",
                                "work history",

                                "education",
                                "educational qualification",
                                "educational qualifications",
                                "academic qualification",
                                "academic qualifications",
                                "higher secondary education",
                                "higher secondary",
                                "secondary education",

                                "skills",
                                "technical skills",
                                "core skills",
                                "key skills",
                                "core competencies",
                                "competencies",
                                "professional strengths",
                                "professional strength",

                                "certifications",
                                "certification",

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
        // COMPANY CHECK
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

                        if (value.matches(
                                        ".*\\b"
                                                        + Pattern.quote(keyword)
                                                        + "\\b.*")) {

                                return true;
                        }
                }

                return false;
        }

        // =========================================================
        // JOB TITLE CHECK
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

                                "branch operations manager",
                                "sales manager",
                                "sales supervisor",
                                "sales consultant",
                                "area visual merchandiser",

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

                        if (value.matches(
                                        ".*\\b"
                                                        + Pattern.quote(keyword)
                                                        + "\\b.*")) {

                                return true;
                        }
                }

                return false;
        }

        // =========================================================
        // NON-NAME WORDS
        // =========================================================

        private static boolean containsNonNameWords(
                        String text) {

                if (text == null) {
                        return true;
                }

                String value = text.toLowerCase().trim();

                String[] words = {

                                "resume",
                                "curriculum",
                                "vitae",
                                "profile",
                                "summary",
                                "objective",
                                "career",
                                "contact",
                                "personal",
                                "information",
                                "details",

                                "about",
                                "education",
                                "educational",
                                "academic",
                                "qualification",
                                "qualifications",
                                "secondary",
                                "higher",
                                "school",
                                "college",
                                "university",

                                "skill",
                                "skills",
                                "technical",
                                "competency",
                                "competencies",
                                "core",

                                "experience",
                                "experiences",
                                "professional",
                                "employment",
                                "history",
                                "work",

                                "project",
                                "projects",

                                "certification",
                                "certifications",
                                "certificate",

                                "achievement",
                                "achievements",
                                "award",
                                "awards",

                                "developer",
                                "engineer",
                                "scientist",
                                "analyst",
                                "manager",
                                "consultant",
                                "administrator",
                                "architect",
                                "designer",
                                "intern",
                                "trainee",
                                "associate",
                                "specialist",
                                "executive",
                                "lead",
                                "director",

                                "java",
                                "python",
                                "javascript",
                                "typescript",
                                "react",
                                "angular",
                                "node",
                                "spring",
                                "sql",
                                "mysql",
                                "postgresql",
                                "mongodb",
                                "aws",
                                "azure",
                                "docker",
                                "kubernetes",

                                "and",
                                "with",
                                "from",
                                "for",
                                "using",
                                "worked",
                                "working",
                                "created",
                                "creating",
                                "developed",
                                "responsible",
                                "extensively",
                                "parametric",
                                "experienced",
                                "seeking",
                                "looking",
                                "currently",
                                "years",
                                "year",
                                "months",
                                "month",
                                "goals",
                                "strength",
                                "strengths",

                                "application",
                                "applications",
                                "environment",
                                "environments",
                                "pools",

                                "payment",
                                "collect",
                                "problem",
                                "solving",
                                "result",
                                "results",
                                "areas",
                                "key"
                };

                for (String word : words) {

                        if (value.matches(
                                        ".*\\b"
                                                        + Pattern.quote(word)
                                                        + "\\b.*")) {

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

                if (name == null
                                || name.isBlank()) {

                        return "No Name";
                }

                name = name.trim();

                name = name.replaceAll(
                                "^[\\s\\-:|]+|[\\s\\-:|]+$",
                                "");

                name = name.replaceAll(
                                "\\s+",
                                " ");

                return toTitleCase(name);
        }

        // =========================================================
        // TITLE CASE
        // =========================================================

        private static String toTitleCase(
                        String text) {

                if (text == null
                                || text.isBlank()) {

                        return "No Name";
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