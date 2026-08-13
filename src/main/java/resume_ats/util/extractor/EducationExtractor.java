package resume_ats.util.extractor;

import resume_ats.util.cleaner.TextCleaner;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EducationExtractor {

    private EducationExtractor() {
    }

    /*
     * Degree priority
     */
    private static final Map<String, Integer> EDUCATION_LEVELS = new LinkedHashMap<>();

    static {

        EDUCATION_LEVELS.put("phd", 6);

        EDUCATION_LEVELS.put("doctorate", 6);

        EDUCATION_LEVELS.put("mtech", 5);
        EDUCATION_LEVELS.put("m.tech", 5);

        EDUCATION_LEVELS.put("me", 5);
        EDUCATION_LEVELS.put("m.e", 5);

        EDUCATION_LEVELS.put("mca", 5);

        EDUCATION_LEVELS.put("msc", 5);
        EDUCATION_LEVELS.put("m.sc", 5);

        EDUCATION_LEVELS.put("mba", 5);

        EDUCATION_LEVELS.put("btech", 4);
        EDUCATION_LEVELS.put("b.tech", 4);

        EDUCATION_LEVELS.put("be", 4);
        EDUCATION_LEVELS.put("b.e", 4);

        EDUCATION_LEVELS.put("bca", 4);

        EDUCATION_LEVELS.put("bsc", 4);
        EDUCATION_LEVELS.put("b.sc", 4);

        EDUCATION_LEVELS.put("diploma", 3);

        EDUCATION_LEVELS.put("12th", 2);

        EDUCATION_LEVELS.put("10th", 1);
    }

    /**
     * Extract highest qualification
     */
    public static String extractEducation(String text) {

        if (text == null || text.isBlank()) {
            return "";
        }

        text = TextCleaner.normalizeForMatching(text);

        int highestLevel = -1;

        String highestDegree = "";

        for (String degree : EDUCATION_LEVELS.keySet()) {

            if (text.contains(degree)) {

                int level = EDUCATION_LEVELS.get(degree);

                if (level > highestLevel) {

                    highestLevel = level;
                    highestDegree = degree;
                }
            }
        }

        return highestDegree.toUpperCase();
    }

    /**
     * Compare resume education with JD.
     */
    public static double calculateMatch(
            String resumeEducation,
            String jdEducation) {

        if (jdEducation == null || jdEducation.isBlank()) {
            return 1.0;
        }

        if (resumeEducation == null || resumeEducation.isBlank()) {
            return 0;
        }

        int resumeLevel = getEducationLevel(resumeEducation);

        int jdLevel = getEducationLevel(jdEducation);

        if (resumeLevel >= jdLevel) {
            return 1.0;
        }

        return (double) resumeLevel / jdLevel;
    }

    /**
     * Numeric level
     */
    public static int getEducationLevel(String degree) {

        if (degree == null)
            return 0;

        degree = degree
                .replace(".", "")
                .toLowerCase();

        return EDUCATION_LEVELS
                .getOrDefault(degree, 0);

    }

}