package resume_ats.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import resume_ats.entity.Profile;
import resume_ats.entity.ProfileCertification;
import resume_ats.entity.ProfileEducation;
import resume_ats.entity.ProfileExperience;
import resume_ats.entity.ProfileProject;

@Service
public class GeminiAIService {

    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    public GeminiAIService() {

        this.restTemplate = new RestTemplate();
    }

    // =========================================================
    // GENERATE ATS RESUME CONTENT
    // =========================================================

    public String generateResume(
            Profile profile,
            List<ProfileEducation> education,
            List<ProfileExperience> experience,
            List<ProfileProject> projects,
            List<ProfileCertification> certifications) {

        String prompt = buildResumePrompt(
                profile,
                education,
                experience,
                projects,
                certifications);

        // =====================================================
        // REQUEST BODY
        // =====================================================

        Map<String, Object> part = new HashMap<>();

        part.put(
                "text",
                prompt);

        Map<String, Object> parts = new HashMap<>();

        parts.put(
                "parts",
                List.of(part));

        Map<String, Object> request = new HashMap<>();

        request.put(
                "contents",
                List.of(parts));

        // =====================================================
        // HEADERS
        // =====================================================

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON);

        headers.set(
                "x-goog-api-key",
                apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
                request,
                headers);

        // =====================================================
        // CALL GEMINI
        // =====================================================

        System.out.println();
        System.out.println(
                "==========================================");

        System.out.println(
                "CALLING GEMINI AI");

        System.out.println(
                "==========================================");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                apiUrl,
                entity,
                Map.class);

        // =====================================================
        // CHECK RESPONSE
        // =====================================================

        if (!response.getStatusCode().is2xxSuccessful()) {

            throw new RuntimeException(
                    "Gemini API returned status: "
                            + response.getStatusCode());
        }

        Map body = response.getBody();

        if (body == null) {

            throw new RuntimeException(
                    "Gemini API returned empty response");
        }

        // =====================================================
        // EXTRACT GENERATED TEXT
        // =====================================================

        try {

            List<?> candidates = (List<?>) body.get(
                    "candidates");

            if (candidates == null
                    || candidates.isEmpty()) {

                throw new RuntimeException(
                        "Gemini returned no candidates");
            }

            Map<?, ?> candidate = (Map<?, ?>) candidates.get(0);

            Map<?, ?> content = (Map<?, ?>) candidate.get(
                    "content");

            List<?> responseParts = (List<?>) content.get(
                    "parts");

            if (responseParts == null
                    || responseParts.isEmpty()) {

                throw new RuntimeException(
                        "Gemini returned no text");
            }

            Map<?, ?> firstPart = (Map<?, ?>) responseParts.get(0);

            Object text = firstPart.get("text");

            if (text == null) {

                throw new RuntimeException(
                        "Gemini response contains no text");
            }

            String result = text.toString();

            System.out.println(
                    "Gemini response received");

            System.out.println(
                    "Generated characters: "
                            + result.length());

            return result;

        } catch (Exception e) {

            System.out.println(
                    "Failed to parse Gemini response");

            System.out.println(
                    "Response body: "
                            + body);

            throw new RuntimeException(
                    "Could not parse Gemini response: "
                            + e.getMessage(),
                    e);
        }
    }

    // =========================================================
    // BUILD RESUME PROMPT
    // =========================================================

    private String buildResumePrompt(
            Profile profile,
            List<ProfileEducation> education,
            List<ProfileExperience> experience,
            List<ProfileProject> projects,
            List<ProfileCertification> certifications) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("""
                You are an expert professional resume writer
                and ATS optimization specialist.

                Create an ATS-friendly professional resume using
                ONLY the information provided below.

                IMPORTANT RULES:

                1. Never invent information.
                2. Never invent companies.
                3. Never invent job titles.
                4. Never invent dates.
                5. Never invent qualifications.
                6. Never invent certifications.
                7. Never invent skills.
                8. Never invent achievements.
                9. Improve grammar and professional wording.
                10. Use strong action verbs where appropriate.
                11. Keep technical terminology accurate.
                12. Do not exaggerate the candidate's experience.
                13. Optimize the content for ATS systems.
                14. Keep the resume concise and professional.
                15. Do not use tables.
                16. Do not use emojis.
                17. Do not include explanations outside the resume.

                Use the following sections when information exists:

                PROFESSIONAL SUMMARY
                CAREER OBJECTIVE
                SKILLS
                PROFESSIONAL EXPERIENCE
                PROJECTS
                EDUCATION
                CERTIFICATIONS
                LANGUAGES
                ACHIEVEMENTS

                Candidate information:

                """);

        // =====================================================
        // PERSONAL INFORMATION
        // =====================================================

        prompt.append("\nFULL NAME: ")
                .append(value(profile.getFullName()));

        prompt.append("\nPROFESSIONAL TITLE: ")
                .append(value(
                        profile.getProfessionalTitle()));

        prompt.append("\nEMAIL: ")
                .append(value(profile.getEmail()));

        prompt.append("\nPHONE: ")
                .append(value(profile.getPhone()));

        prompt.append("\nLOCATION: ")
                .append(value(profile.getLocation()));

        prompt.append("\nLINKEDIN: ")
                .append(value(profile.getLinkedin()));

        prompt.append("\nGITHUB: ")
                .append(value(profile.getGithub()));

        prompt.append("\nPORTFOLIO: ")
                .append(value(profile.getPortfolio()));

        prompt.append("\nSUMMARY: ")
                .append(value(profile.getSummary()));

        prompt.append("\nOBJECTIVE: ")
                .append(value(profile.getObjective()));

        prompt.append("\nSKILLS: ")
                .append(value(profile.getSkills()));

        prompt.append("\nLANGUAGES: ")
                .append(value(profile.getLanguages()));

        prompt.append("\nACHIEVEMENTS: ")
                .append(value(profile.getAchievements()));

        // =====================================================
        // EDUCATION
        // =====================================================

        prompt.append("\n\nEDUCATION:\n");

        if (education != null) {

            for (ProfileEducation edu : education) {

                prompt.append("\nDegree: ")
                        .append(value(
                                edu.getDegree()));

                prompt.append("\nInstitution: ")
                        .append(value(
                                edu.getInstitution()));

                prompt.append("\nLocation: ")
                        .append(value(
                                edu.getLocation()));

                prompt.append("\nStart Year: ")
                        .append(value(
                                edu.getStartYear()));

                prompt.append("\nEnd Year: ")
                        .append(value(
                                edu.getEndYear()));

                prompt.append("\nGrade: ")
                        .append(value(
                                edu.getGrade()));

                prompt.append("\n");
            }
        }

        // =====================================================
        // EXPERIENCE
        // =====================================================

        prompt.append("\nEXPERIENCE:\n");

        if (experience != null) {

            for (ProfileExperience exp : experience) {

                prompt.append("\nJob Title: ")
                        .append(value(
                                exp.getJobTitle()));

                prompt.append("\nCompany: ")
                        .append(value(
                                exp.getCompany()));

                prompt.append("\nLocation: ")
                        .append(value(
                                exp.getLocation()));

                prompt.append("\nStart Date: ")
                        .append(value(
                                exp.getStartDate()));

                prompt.append("\nEnd Date: ")
                        .append(value(
                                exp.getEndDate()));

                prompt.append("\nCurrently Working: ")
                        .append(exp.isCurrentlyWorking());

                prompt.append("\nDescription: ")
                        .append(value(
                                exp.getDescription()));

                prompt.append("\n");
            }
        }

        // =====================================================
        // PROJECTS
        // =====================================================

        prompt.append("\nPROJECTS:\n");

        if (projects != null) {

            for (ProfileProject project : projects) {

                prompt.append("\nProject Name: ")
                        .append(value(
                                project.getProjectName()));

                prompt.append("\nTechnologies: ")
                        .append(value(
                                project.getTechnologies()));

                prompt.append("\nDescription: ")
                        .append(value(
                                project.getDescription()));

                prompt.append("\nProject Link: ")
                        .append(value(
                                project.getProjectLink()));

                prompt.append("\n");
            }
        }

        // =====================================================
        // CERTIFICATIONS
        // =====================================================

        prompt.append("\nCERTIFICATIONS:\n");

        if (certifications != null) {

            for (ProfileCertification cert : certifications) {

                prompt.append("\nCertification: ")
                        .append(value(
                                cert.getCertificationName()));

                prompt.append("\nIssuing Organization: ")
                        .append(value(
                                cert.getIssuingOrganization()));

                prompt.append("\nIssue Date: ")
                        .append(value(
                                cert.getIssueDate()));

                prompt.append("\nCredential URL: ")
                        .append(value(
                                cert.getCredentialUrl()));

                prompt.append("\n");
            }
        }

        return prompt.toString();
    }

    // =========================================================
    // SAFE VALUE
    // =========================================================

    private String value(String value) {

        if (value == null
                || value.isBlank()) {

            return "Not provided";
        }

        return value.trim();
    }
}