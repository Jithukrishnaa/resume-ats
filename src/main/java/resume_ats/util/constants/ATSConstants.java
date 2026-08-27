package resume_ats.util.constants;

import java.util.List;
import java.util.Set;

public final class ATSConstants {

        private ATSConstants() {
        }

        // =========================================================
        // ATS SCORE WEIGHTAGE
        // =========================================================

        public static final double SKILL_WEIGHT = 45.0;

        public static final double EXPERIENCE_WEIGHT = 20.0;

        public static final double EDUCATION_WEIGHT = 10.0;

        public static final double CERTIFICATION_WEIGHT = 10.0;

        public static final double PROJECT_WEIGHT = 10.0;

        public static final double LOCATION_WEIGHT = 5.0;

        // =========================================================
        // ATS RESULT
        // =========================================================

        public static final double ATS_MAX_SCORE = 100.0;

        public static final double ATS_SHORTLIST_SCORE = 85.0;

        public static final double ATS_INTERVIEW_SCORE = 85.0;

        // =========================================================
        // EXPERIENCE
        // =========================================================

        public static final int DEFAULT_EXPERIENCE = 0;

        public static final int MAX_EXPERIENCE = 50;

        // =========================================================
        // FILE TYPES
        // =========================================================

        public static final Set<String> SUPPORTED_RESUME_TYPES = Set.of(

                        "pdf",
                        "doc",
                        "docx"

        );

        public static final Set<String> SUPPORTED_JD_TYPES = Set.of(

                        "pdf"

        );

        // =========================================================
        // MAX FILE SIZE
        // =========================================================

        public static final long MAX_FILE_SIZE_MB = 20;

        // =========================================================
        // EMPLOYMENT TYPES
        // =========================================================

        public static final List<String> EMPLOYMENT_TYPES = List.of(

                        "Full Time",
                        "Part Time",
                        "Internship",
                        "Contract",
                        "Freelance",
                        "Temporary",
                        "Remote",
                        "Hybrid"

        );

        // =========================================================
        // EDUCATION
        // =========================================================

        public static final List<String> EDUCATION_LEVELS = List.of(

                        "PhD",
                        "Doctorate",
                        "M.Tech",
                        "ME",
                        "MBA",
                        "MCA",
                        "M.Sc",
                        "MS",
                        "B.Tech",
                        "BE",
                        "BCA",
                        "B.Sc",
                        "Diploma",
                        "12th",
                        "10th"

        );

        // =========================================================
        // ATS RECOMMENDATION
        // =========================================================

        public static final String EXCELLENT = "Excellent Match";

        public static final String HIGHLY_RECOMMENDED = "Highly Recommended";

        public static final String RECOMMENDED = "Recommended";

        public static final String NEEDS_REVIEW = "Needs Review";

        public static final String NOT_RECOMMENDED = "Not Recommended";

        // =========================================================
        // COMMON RESUME HEADERS
        // =========================================================

        public static final List<String> RESUME_SECTIONS = List.of(

                        "summary",
                        "objective",
                        "profile",
                        "education",
                        "experience",
                        "projects",
                        "skills",
                        "technical skills",
                        "professional experience",
                        "internship",
                        "achievements",
                        "certifications",
                        "languages",
                        "interests",
                        "declaration"

        );

        // =========================================================
        // COMMON JD HEADERS
        // =========================================================

        public static final List<String> JD_SECTIONS = List.of(

                        "job description",
                        "roles",
                        "responsibilities",
                        "required skills",
                        "preferred skills",
                        "qualifications",
                        "experience",
                        "education",
                        "benefits",
                        "salary",
                        "location"

        );

        // =========================================================
        // DEFAULT VALUES
        // =========================================================

        public static final String UNKNOWN = "Unknown";

        public static final String NOT_AVAILABLE = "N/A";

}