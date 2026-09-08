package resume_ats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import resume_ats.entity.User;
import resume_ats.service.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // =========================================================
    // REGISTER NEW USER
    // =========================================================

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request) {

        try {

            User user = authService.register(
                    request.getFullName(),
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getRole());

            return ResponseEntity.ok(
                    new AuthResponse(
                            true,
                            "Account created successfully."));

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            new AuthResponse(
                                    false,
                                    e.getMessage()));

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(
                            new AuthResponse(
                                    false,
                                    "Unable to create account."));
        }
    }

    // =========================================================
    // CHECK USERNAME
    // =========================================================

    @GetMapping("/username/{username}")
    public ResponseEntity<Boolean> checkUsername(
            @PathVariable String username) {

        User user = authService.findByUsername(username);

        return ResponseEntity.ok(user == null);
    }

    // =========================================================
    // CHECK EMAIL
    // =========================================================

    @GetMapping("/email/{email}")
    public ResponseEntity<Boolean> checkEmail(
            @PathVariable String email) {

        User user = authService.findByEmail(email);

        return ResponseEntity.ok(user == null);
    }

    // =========================================================
    // GET CURRENT LOGGED-IN USER
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<?> currentUser(
            Authentication authentication) {

        // -----------------------------------------------------
        // NOT AUTHENTICATED
        // -----------------------------------------------------

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(401)
                    .body(
                            Map.of(
                                    "authenticated",
                                    false));
        }

        // -----------------------------------------------------
        // FIND USER
        // -----------------------------------------------------

        String username = authentication.getName();

        User user = authService.findByUsername(username);

        if (user == null) {

            return ResponseEntity
                    .status(404)
                    .body(
                            Map.of(
                                    "authenticated",
                                    false,
                                    "message",
                                    "User not found."));
        }

        // -----------------------------------------------------
        // CURRENT USER RESPONSE
        // -----------------------------------------------------

        return ResponseEntity.ok(
                Map.of(
                        "authenticated",
                        true,

                        "id",
                        user.getId(),

                        "username",
                        user.getUsername(),

                        "email",
                        user.getEmail(),

                        "fullName",
                        user.getFullName(),

                        "role",
                        user.getRole(),

                        "enabled",
                        user.isEnabled()));
    }

    // =========================================================
    // REGISTER REQUEST
    // =========================================================

    public static class RegisterRequest {

        private String fullName;

        private String username;

        private String email;

        private String password;

        private String role;

        public RegisterRequest() {
        }

        // -----------------------------------------------------
        // GET FULL NAME
        // -----------------------------------------------------

        public String getFullName() {
            return fullName;
        }

        // -----------------------------------------------------
        // SET FULL NAME
        // -----------------------------------------------------

        public void setFullName(
                String fullName) {

            this.fullName = fullName;
        }

        // -----------------------------------------------------
        // GET USERNAME
        // -----------------------------------------------------

        public String getUsername() {
            return username;
        }

        // -----------------------------------------------------
        // SET USERNAME
        // -----------------------------------------------------

        public void setUsername(
                String username) {

            this.username = username;
        }

        // -----------------------------------------------------
        // GET EMAIL
        // -----------------------------------------------------

        public String getEmail() {
            return email;
        }

        // -----------------------------------------------------
        // SET EMAIL
        // -----------------------------------------------------

        public void setEmail(
                String email) {

            this.email = email;
        }

        // -----------------------------------------------------
        // GET PASSWORD
        // -----------------------------------------------------

        public String getPassword() {
            return password;
        }

        // -----------------------------------------------------
        // SET PASSWORD
        // -----------------------------------------------------

        public void setPassword(
                String password) {

            this.password = password;
        }

        // -----------------------------------------------------
        // GET ROLE
        // -----------------------------------------------------

        public String getRole() {
            return role;
        }

        // -----------------------------------------------------
        // SET ROLE
        // -----------------------------------------------------

        public void setRole(
                String role) {

            this.role = role;
        }
    }

    // =========================================================
    // RESPONSE
    // =========================================================

    public static class AuthResponse {

        private boolean success;

        private String message;

        public AuthResponse(
                boolean success,
                String message) {

            this.success = success;

            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}