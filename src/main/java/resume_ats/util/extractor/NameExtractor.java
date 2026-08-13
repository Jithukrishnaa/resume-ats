package resume_ats.util.extractor;

import resume_ats.util.cleaner.TextCleaner;

import java.util.regex.Pattern;

public final class NameExtractor {

    private NameExtractor() {
    }

    /*
     * Common words that are NOT candidate names
     */
    private static final String[] INVALID_HEADERS = {

            "resume",
            "curriculum vitae",
            "curriculum",
            "vitae",
            "profile",
            "summary",
            "career objective",
            "objective",
            "professional summary",
            "contact",
            "personal information",
            "education",
            "skills",
            "experience",
            "projects",
            "certifications",
            "languages",
            "declaration",
            "references"

    };

    /*
     * Name pattern
     */
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Z][a-zA-Z]+(?:\\s+[A-Z][a-zA-Z]+){1,3}$");

    /**
     * Extract candidate name.
     */
    public static String extractName(String text) {

        if (text == null || text.isBlank()) {
            return "";
        }

        text = TextCleaner.clean(text);

        String[] lines = text.split("\\r?\\n");

        for (int i = 0; i < Math.min(lines.length, 15); i++) {

            String line = lines[i].trim();

            if (line.isBlank())
                continue;

            String lower = line.toLowerCase();

            boolean invalid = false;

            for (String header : INVALID_HEADERS) {

                if (lower.contains(header)) {

                    invalid = true;
                    break;
                }
            }

            if (invalid)
                continue;

            if (line.length() < 3)
                continue;

            if (line.length() > 40)
                continue;

            if (line.matches(".*\\d.*"))
                continue;

            if (line.contains("@"))
                continue;

            if (line.toLowerCase().contains("linkedin"))
                continue;

            if (line.toLowerCase().contains("github"))
                continue;

            if (NAME_PATTERN.matcher(line).matches()) {

                return line;
            }
        }

        return "";
    }

}