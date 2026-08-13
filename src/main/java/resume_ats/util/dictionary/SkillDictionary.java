package resume_ats.util.dictionary;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public final class SkillDictionary {

    private SkillDictionary() {
    }

    private static final Set<String> SKILLS = new LinkedHashSet<>(Arrays.asList(

            // =====================================================
            // PROGRAMMING
            // =====================================================

            "java",
            "python",
            "c",
            "c++",
            "c#",
            "javascript",
            "typescript",
            "kotlin",
            "swift",
            "php",
            "go",
            "golang",
            "ruby",
            "r",
            "scala",
            "perl",

            // =====================================================
            // WEB
            // =====================================================

            "html",
            "css",
            "bootstrap",
            "tailwind css",
            "jquery",
            "react",
            "react js",
            "angular",
            "vue",
            "node js",
            "express js",
            "next js",

            // =====================================================
            // JAVA
            // =====================================================

            "spring",
            "spring boot",
            "spring mvc",
            "hibernate",
            "jpa",
            "servlet",
            "jsp",
            "maven",
            "gradle",

            // =====================================================
            // DATABASE
            // =====================================================

            "mysql",
            "postgresql",
            "oracle",
            "sql server",
            "mongodb",
            "sqlite",
            "redis",
            "cassandra",
            "firebase",

            // =====================================================
            // CLOUD
            // =====================================================

            "aws",
            "azure",
            "gcp",
            "google cloud",

            // =====================================================
            // DEVOPS
            // =====================================================

            "docker",
            "kubernetes",
            "jenkins",
            "git",
            "github",
            "gitlab",
            "linux",
            "bash",

            // =====================================================
            // DATA SCIENCE
            // =====================================================

            "machine learning",
            "deep learning",
            "artificial intelligence",
            "data science",
            "nlp",
            "computer vision",
            "tensorflow",
            "keras",
            "pytorch",
            "opencv",
            "scikit learn",
            "pandas",
            "numpy",
            "matplotlib",
            "seaborn",

            // =====================================================
            // BIG DATA
            // =====================================================

            "hadoop",
            "spark",
            "kafka",
            "hive",

            // =====================================================
            // BI
            // =====================================================

            "tableau",
            "power bi",
            "excel",

            // =====================================================
            // TESTING
            // =====================================================

            "junit",
            "selenium",
            "postman",
            "rest api",
            "soap",

            // =====================================================
            // MOBILE
            // =====================================================

            "android",
            "ios",
            "flutter",
            "react native",

            // =====================================================
            // SALES
            // =====================================================

            "sales",
            "business development",
            "lead generation",
            "crm",
            "cold calling",
            "negotiation",
            "upselling",
            "cross selling",

            // =====================================================
            // HR
            // =====================================================

            "recruitment",
            "talent acquisition",
            "payroll",
            "employee engagement",
            "hrms",

            // =====================================================
            // FINANCE
            // =====================================================

            "accounting",
            "gst",
            "tally",
            "sap",
            "sap fico",
            "taxation",
            "bookkeeping",

            // =====================================================
            // BANKING
            // =====================================================

            "banking",
            "retail banking",
            "investment banking",
            "loan processing",
            "credit analysis",

            // =====================================================
            // INSURANCE
            // =====================================================

            "insurance",
            "claims",
            "underwriting",
            "policy servicing",

            // =====================================================
            // MARKETING
            // =====================================================

            "digital marketing",
            "seo",
            "sem",
            "social media marketing",
            "content marketing",
            "google analytics",

            // =====================================================
            // PROJECT MANAGEMENT
            // =====================================================

            "project management",
            "agile",
            "scrum",
            "kanban",

            // =====================================================
            // SOFT SKILLS
            // =====================================================

            "communication",
            "leadership",
            "teamwork",
            "problem solving",
            "analytical thinking",
            "critical thinking",
            "presentation",
            "time management"

    ));

    /**
     * Returns true if the skill exists.
     */
    public static boolean contains(String skill) {

        if (skill == null || skill.isBlank())
            return false;

        return SKILLS.contains(skill.trim().toLowerCase());
    }

    /**
     * Returns all supported skills.
     */
    public static Set<String> getAllSkills() {
        return SKILLS;
    }

}