package resume_ats.util.extractor;

import resume_ats.util.cleaner.TextCleaner;
import resume_ats.util.dictionary.SkillDictionary;
import resume_ats.util.dictionary.SkillNormalizer;
import resume_ats.util.dictionary.StopWords;

import java.util.LinkedHashSet;
import java.util.Set;

public final class SkillExtractor {

    private SkillExtractor() {
    }

    /**
     * Extract professional skills from resume/JD
     */
    public static Set<String> extractSkills(String text) {

        LinkedHashSet<String> extractedSkills = new LinkedHashSet<>();

        if (text == null || text.isBlank()) {
            return extractedSkills;
        }

        // Clean the document
        text = TextCleaner.normalizeForMatching(text);

        // =====================================================
        // STEP 1 : Detect complete skills
        // =====================================================

        for (String skill : SkillDictionary.getAllSkills()) {

            String normalizedSkill = TextCleaner.normalizeForMatching(skill);

            if (text.contains(normalizedSkill)) {

                extractedSkills.add(
                        SkillNormalizer.normalize(skill));
            }
        }

        // =====================================================
        // STEP 2 : Remove stop words (extra safety)
        // =====================================================

        extractedSkills.removeIf(
                skill -> StopWords.isStopWord(skill));

        return extractedSkills;
    }

    /**
     * Returns comma separated skills.
     */
    public static String extractSkillsAsString(String text) {

        return String.join(", ", extractSkills(text));

    }

    /**
     * Count extracted skills.
     */
    public static int countSkills(String text) {

        return extractSkills(text).size();

    }

    /**
     * Returns whether a skill exists.
     */
    public static boolean containsSkill(
            String text,
            String skill) {

        if (text == null || skill == null)
            return false;

        return extractSkills(text)
                .contains(
                        SkillNormalizer.normalize(skill));

    }

}