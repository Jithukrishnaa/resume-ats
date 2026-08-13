package resume_ats.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class ATSScoringService {

    private static final double SKILL_WEIGHT = 45.0;
    private static final double EXPERIENCE_WEIGHT = 20.0;
    private static final double EDUCATION_WEIGHT = 10.0;
    private static final double CERTIFICATION_WEIGHT = 10.0;
    private static final double LOCATION_WEIGHT = 5.0;
    private static final double PROJECT_WEIGHT = 10.0;

    public double calculateScore(

            Set<String> jdSkills,
            Set<String> resumeSkills,

            int jdExperience,
            int resumeExperience,

            String jdEducation,
            String resumeEducation,

            String jdCertifications,
            String resumeCertifications,

            String jdLocation,
            String resumeLocation) {

        double score = 0;

        score += calculateSkillScore(
                jdSkills,
                resumeSkills);

        score += calculateExperienceScore(
                jdExperience,
                resumeExperience);

        score += calculateEducationScore(
                jdEducation,
                resumeEducation);

        score += calculateCertificationScore(
                jdCertifications,
                resumeCertifications);

        score += calculateLocationScore(
                jdLocation,
                resumeLocation);

        /*
         * Temporary project score.
         * Later this will compare JD skills against
         * project technologies.
         */
        score += PROJECT_WEIGHT;

        return Math.min(100.0, Math.round(score * 100.0) / 100.0);
    }

    // ==========================================================
    // Skill Score
    // ==========================================================

    private double calculateSkillScore(
            Set<String> jdSkills,
            Set<String> resumeSkills) {

        if (jdSkills == null || jdSkills.isEmpty())
            return 0;

        if (resumeSkills == null)
            return 0;

        int matched = 0;

        for (String skill : jdSkills) {

            if (resumeSkills.contains(skill)) {

                matched++;

            }

        }

        return ((double) matched / jdSkills.size())
                * SKILL_WEIGHT;
    }

    // ==========================================================
    // Experience Score
    // ==========================================================

    private double calculateExperienceScore(
            int jdExperience,
            int resumeExperience) {

        if (jdExperience <= 0)
            return EXPERIENCE_WEIGHT;

        if (resumeExperience >= jdExperience)
            return EXPERIENCE_WEIGHT;

        return ((double) resumeExperience
                / jdExperience)
                * EXPERIENCE_WEIGHT;
    }

    // ==========================================================
    // Education Score
    // ==========================================================

    private double calculateEducationScore(
            String jdEducation,
            String resumeEducation) {

        if (jdEducation == null || jdEducation.isBlank())
            return EDUCATION_WEIGHT;

        if (resumeEducation == null || resumeEducation.isBlank())
            return 0;

        jdEducation = jdEducation.toLowerCase();
        resumeEducation = resumeEducation.toLowerCase();

        if (jdEducation.equals(resumeEducation))
            return EDUCATION_WEIGHT;

        // Similar degree family

        if (jdEducation.contains("b.tech")
                && resumeEducation.contains("b.e")) {

            return EDUCATION_WEIGHT * 0.9;

        }

        if (jdEducation.contains("b.e")
                && resumeEducation.contains("b.tech")) {

            return EDUCATION_WEIGHT * 0.9;

        }

        if (jdEducation.contains("m.tech")
                && resumeEducation.contains("m.e")) {

            return EDUCATION_WEIGHT * 0.9;

        }

        return 0;
    }

    // ==========================================================
    // Certification Score
    // ==========================================================

    private double calculateCertificationScore(
            String jdCertifications,
            String resumeCertifications) {

        Set<String> jd = convertToSet(jdCertifications);

        Set<String> resume = convertToSet(resumeCertifications);

        if (jd.isEmpty())
            return CERTIFICATION_WEIGHT;

        if (resume.isEmpty())
            return 0;

        int matched = 0;

        for (String cert : jd) {

            if (resume.contains(cert)) {

                matched++;

            }

        }

        return ((double) matched / jd.size())
                * CERTIFICATION_WEIGHT;
    }

    // ==========================================================
    // Location Score
    // ==========================================================

    private double calculateLocationScore(
            String jdLocation,
            String resumeLocation) {

        if (jdLocation == null || jdLocation.isBlank())
            return LOCATION_WEIGHT;

        if (resumeLocation == null || resumeLocation.isBlank())
            return 0;

        jdLocation = jdLocation.toLowerCase().trim();
        resumeLocation = resumeLocation.toLowerCase().trim();

        if (jdLocation.contains("remote"))
            return LOCATION_WEIGHT;

        if (jdLocation.equals(resumeLocation))
            return LOCATION_WEIGHT;

        return 0;
    }

    // ==========================================================
    // Utility
    // ==========================================================

    private Set<String> convertToSet(String value) {

        LinkedHashSet<String> set = new LinkedHashSet<>();

        if (value == null || value.isBlank())
            return set;

        Arrays.stream(value.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isBlank())
                .forEach(set::add);

        return set;
    }

}