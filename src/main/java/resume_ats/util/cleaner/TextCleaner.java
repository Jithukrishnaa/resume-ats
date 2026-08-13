package resume_ats.util.cleaner;

public final class TextCleaner {

    private TextCleaner() {
    }

    /**
     * Basic cleaning
     */
    public static String clean(String text) {

        if (text == null) {
            return "";
        }

        text = text.replace("\u0000", "");

        text = text.replaceAll(
                "[\\p{Cntrl}&&[^\\r\\n\\t]]",
                "");

        return text.trim();
    }

    /**
     * Normalize text for ATS matching
     */
    public static String normalizeForMatching(String text) {

        text = clean(text);

        text = text.toLowerCase();

        // Remove punctuation
        text = text.replaceAll("[^a-z0-9+#. ]", " ");

        // Remove extra spaces
        text = text.replaceAll("\\s+", " ");

        return text.trim();
    }

}