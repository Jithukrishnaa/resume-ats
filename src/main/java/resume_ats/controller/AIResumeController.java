package resume_ats.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import resume_ats.entity.Profile;
import resume_ats.entity.ProfileCertification;
import resume_ats.entity.ProfileEducation;
import resume_ats.entity.ProfileExperience;
import resume_ats.entity.ProfileProject;
import resume_ats.entity.User;
import resume_ats.repository.UserRepository;
import resume_ats.service.GeminiAIService;
import resume_ats.service.ProfileService;

@RestController
@RequestMapping("/api/ai/resume")
@CrossOrigin
public class AIResumeController {

    private final GeminiAIService geminiAIService;
    private final ProfileService profileService;
    private final UserRepository userRepository;

    public AIResumeController(
            GeminiAIService geminiAIService,
            ProfileService profileService,
            UserRepository userRepository) {

        this.geminiAIService = geminiAIService;

        this.profileService = profileService;

        this.userRepository = userRepository;
    }

    // =========================================================
    // GENERATE AI RESUME CONTENT
    // =========================================================

    @GetMapping("/generate")
    public ResponseEntity<?> generateResume(
            Authentication authentication) {

        System.out.println();
        System.out.println(
                "==========================================");

        System.out.println(
                "AI RESUME GENERATION REQUEST");

        System.out.println(
                "==========================================");

        try {

            // =================================================
            // CHECK LOGIN
            // =================================================

            if (authentication == null
                    || !authentication.isAuthenticated()) {

                return ResponseEntity
                        .status(401)
                        .body("Not authenticated");
            }

            // =================================================
            // FIND USER
            // =================================================

            String username = authentication.getName();

            User user = userRepository
                    .findByUsernameIgnoreCase(
                            username)
                    .orElseThrow(() -> new RuntimeException(
                            "User not found"));

            System.out.println(
                    "Username: "
                            + username);

            System.out.println(
                    "User ID: "
                            + user.getId());

            // =================================================
            // GET PROFILE
            // =================================================

            Profile profile = profileService.getProfile(
                    user.getId());

            if (profile == null
                    || profile.getId() == null) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                "Please complete your profile first.");
            }

            // =================================================
            // GET PROFILE SECTIONS
            // =================================================

            List<ProfileEducation> education = profileService.getEducation(
                    profile.getId());

            List<ProfileExperience> experience = profileService.getExperience(
                    profile.getId());

            List<ProfileProject> projects = profileService.getProjects(
                    profile.getId());

            List<ProfileCertification> certifications = profileService.getCertifications(
                    profile.getId());

            // =================================================
            // CALL GEMINI
            // =================================================

            String generatedResume = geminiAIService.generateResume(
                    profile,
                    education,
                    experience,
                    projects,
                    certifications);

            // =================================================
            // RESPONSE
            // =================================================

            Map<String, Object> result = new HashMap<>();

            result.put(
                    "success",
                    true);

            result.put(
                    "message",
                    "AI resume generated successfully");

            result.put(
                    "resume",
                    generatedResume);

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            System.out.println(
                    "==========================================");

            System.out.println(
                    "AI RESUME GENERATION FAILED");

            System.out.println(
                    "==========================================");

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "AI resume generation failed: "
                                    + e.getMessage());
        }
    }
}