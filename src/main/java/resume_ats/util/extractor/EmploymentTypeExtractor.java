package resume_ats.util.extractor;

import resume_ats.util.cleaner.TextCleaner;

public final class EmploymentTypeExtractor {

    private EmploymentTypeExtractor() {
    }

    private static final String FULL_TIME = "Full Time";

    private static final String PART_TIME = "Part Time";

    private static final String INTERNSHIP = "Internship";

    private static final String CONTRACT = "Contract";

    private static final String FREELANCE = "Freelance";

    private static final String TEMPORARY = "Temporary";

    private static final String REMOTE = "Remote";

    private static final String HYBRID = "Hybrid";

    private static final String ON_SITE = "On Site";

    /**
     * Extract employment type.
     */
    public static String extractEmploymentType(String text) {

        if (text == null || text.isBlank()) {
            return "";
        }

        text = TextCleaner.normalizeForMatching(text);

        if (text.contains("full time")
                || text.contains("full-time")) {

            return FULL_TIME;
        }

        if (text.contains("part time")
                || text.contains("part-time")) {

            return PART_TIME;
        }

        if (text.contains("internship")
                || text.contains("intern")) {

            return INTERNSHIP;
        }

        if (text.contains("contract")) {

            return CONTRACT;
        }

        if (text.contains("freelance")
                || text.contains("freelancer")) {

            return FREELANCE;
        }

        if (text.contains("temporary")) {

            return TEMPORARY;
        }

        if (text.contains("remote")
                || text.contains("work from home")
                || text.contains("wfh")) {

            return REMOTE;
        }

        if (text.contains("hybrid")) {

            return HYBRID;
        }

        if (text.contains("onsite")
                || text.contains("on site")) {

            return ON_SITE;
        }

        return "";
    }

    /**
     * Calculate matching score.
     */
    public static double calculateMatch(
            String resumeEmploymentType,
            String jdEmploymentType) {

        if (jdEmploymentType == null
                || jdEmploymentType.isBlank()) {

            return 1.0;
        }

        if (resumeEmploymentType == null
                || resumeEmploymentType.isBlank()) {

            return 0.0;
        }

        if (resumeEmploymentType.equalsIgnoreCase(jdEmploymentType)) {

            return 1.0;
        }

        return 0.0;
    }

    /**
     * Convenience methods.
     */

    public static boolean isFullTime(String text) {

        return FULL_TIME.equalsIgnoreCase(
                extractEmploymentType(text));
    }

    public static boolean isInternship(String text) {

        return INTERNSHIP.equalsIgnoreCase(
                extractEmploymentType(text));
    }

    public static boolean isRemote(String text) {

        return REMOTE.equalsIgnoreCase(
                extractEmploymentType(text));
    }

    public static boolean isHybrid(String text) {

        return HYBRID.equalsIgnoreCase(
                extractEmploymentType(text));
    }

    public static boolean isContract(String text) {

        return CONTRACT.equalsIgnoreCase(
                extractEmploymentType(text));
    }

}