package resume_ats.util.extractor;

import resume_ats.util.cleaner.TextCleaner;

import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;

public final class NameExtractor {

    private NameExtractor() {
    }

    // =========================================================
    // STRONGLY INVALID PHRASES
    // =========================================================

    private static final Set<String> INVALID_PHRASES = new HashSet<>();

    static {

        String[] phrases = {

                "resume",
                "curriculum vitae",
                "curriculum",
                "vitae",
                "cv",

                "about me",
                "profile",
                "profile summary",
                "professional profile",
                "professional summary",
                "summary",
                "career summary",
                "objective",
                "career objective",
                "professional objective",

                "contact",
                "contact details",
                "personal details",
                "personal information",

                "education",
                "educational qualification",
                "educational qualifications",
                "academic qualification",
                "academic qualifications",
                "higher secondary education",
                "higher secondary",
                "secondary education",

                "skills",
                "skill",
                "technical skills",
                "key skills",
                "core skills",
                "core competencies",
                "competencies",
                "technical competencies",
                "professional skills",
                "professional strengths",
                "professional strength",

                "experience",
                "work experience",
                "professional experience",
                "employment history",
                "work history",
                "career history",

                "projects",
                "project experience",
                "academic projects",
                "personal projects",

                "certifications",
                "certification",
                "licenses",

                "languages",
                "achievements",
                "achievement",
                "awards",
                "award",

                "references",
                "reference",
                "declaration",

                "responsibilities",
                "responsibility",
                "key responsibilities"
        };

        for (String phrase : phrases) {
            INVALID_PHRASES.add(phrase);
        }
    }

    // =========================================================
    // INVALID WORDS
    // =========================================================

    private static final Set<String> INVALID_WORDS = new HashSet<>();

    static {

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
                "specialist",
                "executive",
                "trainee",
                "intern",
                "associate",
                "director",
                "lead",

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

                "about",
                "me",
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
                "strengths"
        };

        for (String word : words) {
            INVALID_WORDS.add(word);
        }
    }

    // =========================================================
    // NAME PATTERN
    // =========================================================

    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[A-Za-z]+(?:[.'-][A-Za-z]+)?"
                    + "(?:\\s+[A-Za-z]+(?:[.'-][A-Za-z]+)?){1,3}$");

    // =========================================================
    // EXTRACT NAME
    // =========================================================

    public static String extractName(String text) {

        if (text == null || text.isBlank()) {
            return "";
        }

        text = TextCleaner.clean(text);

        String[] lines = text.split("\\r?\\n");

        /*
         * Only inspect the very beginning of the resume.
         */
        int limit = Math.min(lines.length, 20);

        for (int i = 0; i < limit; i++) {

            String line = lines[i].trim();

            if (line.isBlank()) {
                continue;
            }

            // Remove bullets
            line = line.replaceFirst(
                    "^[\\-•▪►*]+\\s*",
                    "").trim();

            // Basic length
            if (line.length() < 3
                    || line.length() > 45) {
                continue;
            }

            // Email
            if (line.contains("@")) {
                continue;
            }

            // Numbers
            if (line.matches(".*\\d.*")) {
                continue;
            }

            // URL
            String lower = line.toLowerCase();

            if (lower.contains("http")
                    || lower.contains("www.")
                    || lower.contains("linkedin")
                    || lower.contains("github")
                    || lower.contains("facebook")
                    || lower.contains("instagram")) {

                continue;
            }

            // Label
            if (line.contains(":")) {
                continue;
            }

            boolean hasTitle = hasNameTitle(line);

            line = removeNameTitle(line);

            lower = line.toLowerCase().trim();

            // Exact invalid phrase
            if (INVALID_PHRASES.contains(lower)) {
                continue;
            }

            // Invalid phrase
            if (containsInvalidPhrase(lower)) {
                continue;
            }

            // Invalid word
            if (containsInvalidWord(lower)) {
                continue;
            }

            // Sentence
            if (looksLikeSentence(lower)) {
                continue;
            }

            // Name format
            if (!NAME_PATTERN.matcher(line).matches()) {
                continue;
            }

            String[] words = line.split("\\s+");

            if (words.length < 2
                    || words.length > 4) {
                continue;
            }

            /*
             * Reject ALL CAPS headings.
             *
             * MR. JOHN DOE is allowed because it has
             * an explicit name title.
             */
            if (line.equals(line.toUpperCase())
                    && !hasTitle) {
                continue;
            }

            return formatName(line);
        }

        return "";
    }

    // =========================================================
    // TITLE
    // =========================================================

    private static boolean hasNameTitle(
            String text) {

        if (text == null) {
            return false;
        }

        return text.matches(
                "(?i)^\\s*"
                        + "(mr\\.?|mrs\\.?|ms\\.?|miss|"
                        + "dr\\.?|prof\\.?)"
                        + "\\s+.*");
    }

    private static String removeNameTitle(
            String text) {

        return text.replaceFirst(
                "(?i)^\\s*"
                        + "(mr\\.?|mrs\\.?|ms\\.?|miss|"
                        + "dr\\.?|prof\\.?)"
                        + "\\s+",
                "").trim();
    }

    // =========================================================
    // INVALID PHRASE
    // =========================================================

    private static boolean containsInvalidPhrase(
            String text) {

        for (String phrase : INVALID_PHRASES) {

            if (text.equals(phrase)
                    || text.startsWith(phrase + " ")
                    || text.endsWith(" " + phrase)
                    || text.contains(" " + phrase + " ")) {

                return true;
            }
        }

        return false;
    }

    // =========================================================
    // INVALID WORD
    // =========================================================

    private static boolean containsInvalidWord(
            String text) {

        String[] words = text.split("\\s+");

        for (String word : words) {

            String cleaned = word.replaceAll(
                    "[^a-z]",
                    "");

            if (INVALID_WORDS.contains(cleaned)) {
                return true;
            }
        }

        return false;
    }

    // =========================================================
    // SENTENCE CHECK
    // =========================================================

    private static boolean looksLikeSentence(
            String text) {

        String[] sentenceWords = {

                "is",
                "are",
                "was",
                "were",
                "has",
                "have",
                "had",
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
                "goals"
        };

        for (String word : sentenceWords) {

            if (text.matches(
                    ".*\\b"
                            + Pattern.quote(word)
                            + "\\b.*")) {

                return true;
            }
        }

        return false;
    }

    // =========================================================
    // FORMAT NAME
    // =========================================================

    private static String formatName(
            String name) {

        String[] words = name.toLowerCase()
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