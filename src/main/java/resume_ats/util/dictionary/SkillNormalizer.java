package resume_ats.util.dictionary;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class SkillNormalizer {

    private SkillNormalizer() {
    }

    /*
     * Alias -> Canonical Skill
     */

    private static final Map<String, String> NORMALIZED_SKILLS = new HashMap<>();

    static {

        // =====================================================
        // JAVA
        // =====================================================

        NORMALIZED_SKILLS.put("spring", "spring boot");
        NORMALIZED_SKILLS.put("spring mvc", "spring boot");
        NORMALIZED_SKILLS.put("spring framework", "spring boot");

        NORMALIZED_SKILLS.put("hibernate orm", "hibernate");

        NORMALIZED_SKILLS.put("jpa repository", "jpa");

        // =====================================================
        // JAVASCRIPT
        // =====================================================

        NORMALIZED_SKILLS.put("react js", "react");
        NORMALIZED_SKILLS.put("reactjs", "react");

        NORMALIZED_SKILLS.put("node", "node js");
        NORMALIZED_SKILLS.put("nodejs", "node js");

        NORMALIZED_SKILLS.put("express", "express js");

        NORMALIZED_SKILLS.put("vue js", "vue");

        // =====================================================
        // DATABASE
        // =====================================================

        NORMALIZED_SKILLS.put("postgres", "postgresql");

        NORMALIZED_SKILLS.put("ms sql", "sql server");
        NORMALIZED_SKILLS.put("mssql", "sql server");

        // =====================================================
        // CLOUD
        // =====================================================

        NORMALIZED_SKILLS.put("amazon web services", "aws");

        NORMALIZED_SKILLS.put("google cloud platform", "gcp");

        // =====================================================
        // AI
        // =====================================================

        NORMALIZED_SKILLS.put("ai", "artificial intelligence");

        NORMALIZED_SKILLS.put("ml", "machine learning");

        NORMALIZED_SKILLS.put("dl", "deep learning");

        NORMALIZED_SKILLS.put("natural language processing", "nlp");

        // =====================================================
        // DEVOPS
        // =====================================================

        NORMALIZED_SKILLS.put("k8s", "kubernetes");

        NORMALIZED_SKILLS.put("docker container", "docker");

        // =====================================================
        // BI
        // =====================================================

        NORMALIZED_SKILLS.put("powerbi", "power bi");

        // =====================================================
        // SALES
        // =====================================================

        NORMALIZED_SKILLS.put("customer support", "customer service");

        NORMALIZED_SKILLS.put("customer relationship management", "crm");

        NORMALIZED_SKILLS.put("lead generation executive", "lead generation");

        NORMALIZED_SKILLS.put("business development executive", "business development");

        // =====================================================
        // HR
        // =====================================================

        NORMALIZED_SKILLS.put("talent hiring", "recruitment");

        NORMALIZED_SKILLS.put("human resources", "hr");

        // =====================================================
        // FINANCE
        // =====================================================

        NORMALIZED_SKILLS.put("sap fico consultant", "sap fico");

        NORMALIZED_SKILLS.put("accounts", "accounting");

        // =====================================================
        // INSURANCE
        // =====================================================

        NORMALIZED_SKILLS.put("life insurance", "insurance");

        NORMALIZED_SKILLS.put("general insurance", "insurance");

        // =====================================================
        // PROJECT MANAGEMENT
        // =====================================================

        NORMALIZED_SKILLS.put("scrum master", "scrum");

    }

    /**
     * Returns normalized skill.
     */

    public static String normalize(String skill) {

        if (skill == null)
            return "";

        skill = skill.trim().toLowerCase(Locale.ENGLISH);

        return NORMALIZED_SKILLS.getOrDefault(skill, skill);

    }

    /**
     * Checks whether two skills are same.
     */

    public static boolean isSameSkill(String a, String b) {

        return normalize(a).equals(normalize(b));

    }

}