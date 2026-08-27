package resume_ats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import resume_ats.dto.ProfileRequest;

import resume_ats.entity.Profile;
import resume_ats.entity.User;

import resume_ats.repository.UserRepository;

import resume_ats.service.ProfileService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin
public class ProfileController {

    private final ProfileService profileService;
    private final UserRepository userRepository;

    public ProfileController(
            ProfileService profileService,
            UserRepository userRepository) {

        this.profileService = profileService;

        this.userRepository = userRepository;
    }

    // =====================================================
    // GET CURRENT USER PROFILE
    // =====================================================

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(401)
                    .body("Not authenticated");
        }

        String username = authentication.getName();

        User user = userRepository
                .findByUsernameIgnoreCase(
                        username)
                .orElseThrow(() -> new RuntimeException(
                        "Logged-in user not found"));

        Profile profile = profileService.getProfile(
                user.getId());

        Map<String, Object> response = new HashMap<>();

        response.put(
                "profile",
                profile);

        if (profile.getId() != null) {

            response.put(
                    "education",
                    profileService.getEducation(
                            profile.getId()));

            response.put(
                    "experience",
                    profileService.getExperience(
                            profile.getId()));

            response.put(
                    "projects",
                    profileService.getProjects(
                            profile.getId()));

            response.put(
                    "certifications",
                    profileService.getCertifications(
                            profile.getId()));

        }

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // SAVE COMPLETE PROFILE
    // =====================================================

    @PostMapping("/me")
    public ResponseEntity<?> saveMyProfile(
            Authentication authentication,
            @RequestBody ProfileRequest request) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(401)
                    .body("Not authenticated");
        }

        String username = authentication.getName();

        User user = userRepository
                .findByUsernameIgnoreCase(
                        username)
                .orElseThrow(() -> new RuntimeException(
                        "Logged-in user not found"));

        try {

            Profile savedProfile = profileService
                    .saveCompleteProfile(
                            user.getId(),
                            request);

            Map<String, Object> response = new HashMap<>();

            response.put(
                    "message",
                    "Profile saved successfully");

            response.put(
                    "profile",
                    savedProfile);

            response.put(
                    "education",
                    profileService.getEducation(
                            savedProfile.getId()));

            response.put(
                    "experience",
                    profileService.getExperience(
                            savedProfile.getId()));

            response.put(
                    "projects",
                    profileService.getProjects(
                            savedProfile.getId()));

            response.put(
                    "certifications",
                    profileService.getCertifications(
                            savedProfile.getId()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(500)
                    .body(
                            "Failed to save profile: "
                                    + e.getMessage());
        }
    }

    // =====================================================
    // DELETE CURRENT USER PROFILE
    // =====================================================

    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMyProfile(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(401)
                    .body("Not authenticated");
        }

        String username = authentication.getName();

        User user = userRepository
                .findByUsernameIgnoreCase(
                        username)
                .orElseThrow(() -> new RuntimeException(
                        "Logged-in user not found"));

        profileService.deleteProfile(
                user.getId());

        return ResponseEntity.ok(
                "Profile deleted successfully");
    }
}