package resume_ats.util.extractor;

import resume_ats.util.cleaner.TextCleaner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ExperienceExtractor {

    private ExperienceExtractor() {
    }

    // Matches:
    // 3 years
    // 3+ years
    // 5.5 years
    // 2 yrs
    // 4 year
    private static final Pattern YEAR_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*\\+?\\s*(year|years|yr|yrs)",
            Pattern.CASE_INSENSITIVE);

    // Matches:
    // 6 months
    // 10 month
    private static final Pattern MONTH_PATTERN = Pattern.compile("(\\d+)\\s*(month|months)",
            Pattern.CASE_INSENSITIVE);

    /**
     * Returns experience in years.
     */
    public static double extractExperience(String text) {

        if (text == null || text.isBlank()) {
            return 0;
        }

        text = TextCleaner.normalizeForMatching(text);

        // Fresher
        if (text.contains("fresher")
                || text.contains("no experience")
                || text.contains("entry level")) {

            return 0;
        }

        double years = 0;

        Matcher yearMatcher = YEAR_PATTERN.matcher(text);

        while (yearMatcher.find()) {

            double value = Double.parseDouble(yearMatcher.group(1));

            if (value > years) {
                years = value;
            }
        }

        Matcher monthMatcher = MONTH_PATTERN.matcher(text);

        while (monthMatcher.find()) {

            double months = Double.parseDouble(monthMatcher.group(1));

            double value = months / 12.0;

            if (value > years) {
                years = value;
            }
        }

        return years;
    }

    /**
     * Whether candidate satisfies JD experience.
     */
    public static boolean satisfiesRequirement(
            double resumeExp,
            double jdExp) {

        return resumeExp >= jdExp;

    }

    /**
     * Score between 0 and 1
     */
    public static double calculateMatch(
            double resumeExp,
            double jdExp) {

        if (jdExp <= 0)
            return 1.0;

        return Math.min(resumeExp / jdExp, 1.0);

    }

}