package resume_ats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import resume_ats.entity.User;
import resume_ats.service.AuthService;

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
                    request.getPassword());

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
    // REGISTER REQUEST
    // =========================================================

    public static class RegisterRequest {

        private String fullName;
        private String username;
        private String email;
        private String password;

        public RegisterRequest() {
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(
                String fullName) {

            this.fullName = fullName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(
                String username) {

            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(
                String email) {

            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(
                String password) {

            this.password = password;
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