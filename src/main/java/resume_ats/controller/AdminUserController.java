package resume_ats.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import resume_ats.entity.User;
import resume_ats.entity.Wallet;
import resume_ats.repository.UserRepository;
import resume_ats.service.WalletService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;
    private final WalletService walletService;

    public AdminUserController(
            UserRepository userRepository,
            WalletService walletService) {
        this.userRepository = userRepository;
        this.walletService = walletService;
    }

    // =========================
    // ADMIN CHECK
    // =========================
    private User getAdmin(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Authentication required.");
        }

        User admin = userRepository
                .findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            throw new SecurityException("Admin access required.");
        }

        return admin;
    }

    // =========================
    // GET ALL USERS
    // =========================
    @GetMapping
    public ResponseEntity<?> getAllUsers(Authentication authentication) {

        try {

            getAdmin(authentication);

            List<User> users = userRepository.findAll();

            List<Map<String, Object>> response = new ArrayList<>();

            for (User user : users) {

                Map<String, Object> item = new LinkedHashMap<>();

                item.put("id", user.getId());
                item.put("fullName", user.getFullName());
                item.put("username", user.getUsername());
                item.put("email", user.getEmail());
                item.put("role", user.getRole());
                item.put("enabled", user.isEnabled());
                item.put("canUseExcel", user.isCanUseExcel());

                long credits = 0L;

                try {
                    Wallet wallet = walletService.getWalletByUserId(user.getId());

                    if (wallet != null && wallet.getCredits() != null) {
                        credits = wallet.getCredits();
                    }

                } catch (Exception ignored) {
                    // Keep credits as 0 if wallet is unavailable
                }

                item.put("credits", credits);

                response.add(item);
            }

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            e.getMessage()));

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            e.getMessage()));

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message",
                            "Failed to load users."));
        }
    }

    // =========================
    // CHANGE EXCEL PERMISSION
    // =========================
    @PutMapping("/{userId}/permissions")
    public ResponseEntity<?> updatePermissions(
            Authentication authentication,
            @PathVariable Long userId,
            @RequestBody PermissionRequest request) {

        try {

            getAdmin(authentication);

            User targetUser = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Target user not found."));

            // Prevent changing ADMIN permission
            if ("ADMIN".equalsIgnoreCase(targetUser.getRole())) {

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "message",
                                "ADMIN permissions are always enabled."));
            }

            targetUser.setCanUseExcel(request.isCanUseExcel());

            User savedUser = userRepository.save(targetUser);

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Permissions updated successfully.",
                            "userId",
                            savedUser.getId(),
                            "username",
                            savedUser.getUsername(),
                            "canUseExcel",
                            savedUser.isCanUseExcel()));

        } catch (SecurityException e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            e.getMessage()));

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            e.getMessage()));

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message",
                            "Failed to update permissions."));
        }
    }

    // =========================
    // REQUEST BODY
    // =========================
    public static class PermissionRequest {

        private boolean canUseExcel;

        public boolean isCanUseExcel() {
            return canUseExcel;
        }

        public void setCanUseExcel(boolean canUseExcel) {
            this.canUseExcel = canUseExcel;
        }
    }
}