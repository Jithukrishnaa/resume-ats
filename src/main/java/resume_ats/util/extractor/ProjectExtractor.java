package resume_ats.util.extractor;

import resume_ats.util.cleaner.TextCleaner;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ProjectExtractor {

    private ProjectExtractor() {
    }

    /*
     * Common project section headers
     */
    private static final String[] PROJECT_HEADERS = {

            "projects",
            "academic projects",
            "personal projects",
            "professional projects",
            "major project",
            "minor project",
            "project experience"

    };

    /*
     * Github URL
     */
    private static final Pattern GITHUB_PATTERN = Pattern.compile(
            "(https?://)?(www\\.)?github\\.com/[A-Za-z0-9_\\-/]+",
            Pattern.CASE_INSENSITIVE);

    /*
     * Live Website
     */
    private static final Pattern WEBSITE_PATTERN = Pattern.compile(
            "(https?://)?([A-Za-z0-9\\-]+\\.)+[A-Za-z]{2,}(/[A-Za-z0-9_\\-./?=&%]*)?",
            Pattern.CASE_INSENSITIVE);

    /**
     * Extract project section.
     */
    public static String extractProjectSection(String text) {

        if (text == null || text.isBlank())
            return "";

        text = TextCleaner.clean(text);

        String lower = text.toLowerCase();

        for (String header : PROJECT_HEADERS) {

            int start = lower.indexOf(header);

            if (start != -1) {

                int end = lower.indexOf("education", start);

                if (end == -1)
                    end = lower.indexOf("certifications", start);

                if (end == -1)
                    end = lower.indexOf("skills", start);

                if (end == -1)
                    end = text.length();

                return text.substring(start, end).trim();
            }
        }

        return "";
    }

    /**
     * Count projects.
     */
    public static int countProjects(String text) {

        String section = extractProjectSection(text);

        if (section.isBlank())
            return 0;

        int count = 0;

        String[] lines = section.split("\\r?\\n");

        for (String line : lines) {

            line = line.trim();

            if (line.length() > 10) {
                count++;
            }
        }

        return Math.max(count - 1, 0);
    }

    /**
     * Github links.
     */
    public static Set<String> extractGithubLinks(String text) {

        LinkedHashSet<String> links = new LinkedHashSet<>();

        Matcher matcher = GITHUB_PATTERN.matcher(text);

        while (matcher.find()) {

            links.add(matcher.group());

        }

        return links;
    }

    /**
     * Portfolio links.
     */
    public static Set<String> extractPortfolioLinks(String text) {

        LinkedHashSet<String> links = new LinkedHashSet<>();

        Matcher matcher = WEBSITE_PATTERN.matcher(text);

        while (matcher.find()) {

            String url = matcher.group();

            if (!url.contains("github")) {

                links.add(url);

            }

        }

        return links;
    }

    /**
     * Detect project technologies.
     */
    public static Set<String> extractProjectSkills(String text) {

        return SkillExtractor.extractSkills(
                extractProjectSection(text));

    }

    /**
     * Score projects.
     */
    public static double calculateProjectScore(String text) {

        int count = countProjects(text);

        if (count >= 5)
            return 1.0;

        if (count == 4)
            return 0.9;

        if (count == 3)
            return 0.8;

        if (count == 2)
            return 0.6;

        if (count == 1)
            return 0.4;

        return 0;
    }

}