package resume_ats.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class KeywordExtractor {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "the", "and", "for", "with", "this", "that", "have", "from",
            "will", "your", "our", "their", "are", "you", "job", "role",
            "candidate", "company", "required", "requirements", "responsibilities",
            "qualification", "qualifications", "experience", "years", "year",
            "good", "strong", "ability", "must", "should", "work", "working",
            "knowledge", "skills", "skill", "looking", "need", "needed",
            "full", "time", "part", "office", "team", "using", "able",
            "etc", "who", "has", "had", "all", "any", "one", "two"));

    public static Set<String> extractKeywords(String text) {

        Set<String> keywords = new LinkedHashSet<>();

        if (text == null || text.isBlank()) {
            return keywords;
        }

        text = text.toLowerCase();

        text = text.replaceAll("[^a-z0-9+#. ]", " ");

        String[] words = text.split("\\s+");

        for (String word : words) {

            word = word.trim();

            if (word.length() < 3)
                continue;

            if (STOP_WORDS.contains(word))
                continue;

            keywords.add(word);
        }

        return keywords;
    }
}