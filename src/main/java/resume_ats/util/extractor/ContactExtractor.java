package resume_ats.util.extractor;

import resume_ats.util.cleaner.TextCleaner;
import resume_ats.util.constants.RegexPatterns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ContactExtractor {

    private ContactExtractor() {
    }

    /**
     * Extract Email
     */
    public static String extractEmail(String text) {

        if (text == null)
            return "";

        text = TextCleaner.clean(text);

        Matcher matcher = Pattern.compile(RegexPatterns.EMAIL,
                Pattern.CASE_INSENSITIVE)
                .matcher(text);

        if (matcher.find()) {

            return matcher.group().trim();

        }

        return "";
    }

    /**
     * Extract Phone Number
     */
    public static String extractPhone(String text) {

        if (text == null)
            return "";

        text = TextCleaner.clean(text);

        Matcher matcher = Pattern.compile(RegexPatterns.PHONE)
                .matcher(text);

        if (matcher.find()) {

            return matcher.group().trim();

        }

        return "";
    }

    /**
     * Extract LinkedIn Profile
     */
    public static String extractLinkedIn(String text) {

        if (text == null)
            return "";

        text = TextCleaner.clean(text);

        Matcher matcher = Pattern.compile(RegexPatterns.LINKEDIN,
                Pattern.CASE_INSENSITIVE)
                .matcher(text);

        if (matcher.find()) {

            return matcher.group();

        }

        return "";
    }

    /**
     * Extract GitHub Profile
     */
    public static String extractGithub(String text) {

        if (text == null)
            return "";

        text = TextCleaner.clean(text);

        Matcher matcher = Pattern.compile(RegexPatterns.GITHUB,
                Pattern.CASE_INSENSITIVE)
                .matcher(text);

        if (matcher.find()) {

            return matcher.group();

        }

        return "";
    }

    /**
     * Extract Portfolio Website
     */
    public static String extractPortfolio(String text) {

        if (text == null)
            return "";

        text = TextCleaner.clean(text);

        Matcher matcher = Pattern.compile(RegexPatterns.WEBSITE,
                Pattern.CASE_INSENSITIVE)
                .matcher(text);

        while (matcher.find()) {

            String url = matcher.group();

            if (!url.contains("linkedin")
                    && !url.contains("github")) {

                return url;

            }
        }

        return "";
    }

    /**
     * Check if Resume contains Email
     */
    public static boolean hasEmail(String text) {

        return !extractEmail(text).isBlank();

    }

    /**
     * Check if Resume contains Phone
     */
    public static boolean hasPhone(String text) {

        return !extractPhone(text).isBlank();

    }

    /**
     * Check if Resume contains LinkedIn
     */
    public static boolean hasLinkedIn(String text) {

        return !extractLinkedIn(text).isBlank();

    }

    /**
     * Check if Resume contains Github
     */
    public static boolean hasGithub(String text) {

        return !extractGithub(text).isBlank();

    }

}