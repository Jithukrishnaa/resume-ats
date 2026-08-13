package resume_ats.util.dictionary;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class StopWords {

    private StopWords() {
    }

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(

            // =========================================================
            // ENGLISH STOP WORDS
            // =========================================================

            "a", "an", "the", "and", "or", "but", "if", "else",
            "for", "from", "to", "into", "onto", "of", "on", "in", "at",
            "by", "with", "without", "within", "about", "above", "below",
            "between", "after", "before", "during", "over", "under",
            "is", "am", "are", "was", "were", "be", "been", "being",
            "do", "does", "did", "doing",
            "have", "has", "had", "having",
            "will", "would", "shall", "should", "can", "could", "may", "might",
            "this", "that", "these", "those",
            "it", "its", "they", "them", "their", "he", "she", "his", "her",
            "you", "your", "we", "our", "i", "me", "my", "mine",

            // =========================================================
            // RESUME WORDS
            // =========================================================

            "resume",
            "curriculum",
            "vitae",
            "cv",
            "summary",
            "profile",
            "objective",
            "career",
            "professional",
            "declaration",
            "reference",
            "references",
            "personal",
            "information",
            "contact",
            "details",

            // =========================================================
            // JOB DESCRIPTION WORDS
            // =========================================================

            "job",
            "role",
            "position",
            "opening",
            "vacancy",
            "candidate",
            "company",
            "organization",
            "department",
            "employee",
            "employer",
            "manager",
            "reporting",
            "responsibility",
            "responsibilities",
            "requirement",
            "requirements",
            "qualification",
            "qualifications",
            "preferred",
            "mandatory",
            "desired",

            // =========================================================
            // COMMON NOISE WORDS
            // =========================================================

            "excellent",
            "good",
            "strong",
            "ability",
            "abilities",
            "knowledge",
            "understanding",
            "experience",
            "experienced",
            "skill",
            "skills",
            "work",
            "working",
            "worked",
            "team",
            "teams",
            "member",
            "members",
            "using",
            "used",
            "use",
            "based",
            "across",
            "through",
            "towards",
            "around",
            "various",
            "multiple",
            "different",

            // =========================================================
            // DOCUMENT WORDS
            // =========================================================

            "page",
            "date",
            "name",
            "email",
            "phone",
            "mobile",
            "address",
            "location",

            // =========================================================
            // FILLER WORDS
            // =========================================================

            "etc",
            "including",
            "include",
            "includes",
            "such",
            "well",
            "also",
            "very",
            "highly",
            "more",
            "most",
            "less",
            "least",
            "many",
            "much",
            "few",
            "another",
            "other",
            "others"

    ));

    /**
     * Returns true if the word should be ignored.
     */
    public static boolean isStopWord(String word) {

        if (word == null || word.isBlank()) {
            return true;
        }

        return STOP_WORDS.contains(word.trim().toLowerCase());
    }

    /**
     * Returns all stop words.
     */
    public static Set<String> getAll() {
        return STOP_WORDS;
    }

}