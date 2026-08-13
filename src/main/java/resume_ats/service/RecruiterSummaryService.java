package resume_ats.service;

import org.springframework.stereotype.Service;

@Service
public class RecruiterSummaryService {

    public String generateSummary(

            int experience,

            String education,

            String matchedSkills,

            String missingSkills,

            double score) {

        StringBuilder summary = new StringBuilder();

        summary.append(experience)
                .append(" years of experience. ");

        if (education != null && !education.isBlank()) {

            summary.append("Education: ")
                    .append(education)
                    .append(". ");
        }

        if (matchedSkills != null && !matchedSkills.isBlank()) {

            summary.append("Strong in ")
                    .append(matchedSkills)
                    .append(". ");
        }

        if (missingSkills != null && !missingSkills.isBlank()) {

            summary.append("Missing ")
                    .append(missingSkills)
                    .append(". ");
        }

        if (score >= 85) {

            summary.append("Candidate is highly suitable for this Job Description.");

        } else if (score >= 70) {

            summary.append("Candidate is a good match for this Job Description.");

        } else if (score >= 55) {

            summary.append("Candidate partially matches the Job Description.");

        } else {

            summary.append("Candidate is not a strong match for this Job Description.");

        }

        return summary.toString();
    }
}