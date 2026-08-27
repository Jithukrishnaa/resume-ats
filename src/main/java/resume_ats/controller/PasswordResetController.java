package resume_ats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import resume_ats.service.PasswordResetService;

@RestController
@RequestMapping("/api/auth/password")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(
            PasswordResetService passwordResetService) {

        this.passwordResetService = passwordResetService;
    }

    // =========================================================
    // REQUEST PASSWORD RESET
    // =========================================================

    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {

        try {

            String token = passwordResetService.createResetToken(
                    request.getEmail());

            /*
             * DEVELOPMENT MODE
             *
             * We return the token so that you can test
             * the complete reset flow locally.
             *
             * Later we will replace this with an email
             * service and the token will NOT be returned
             * to the browser.
             */

            return ResponseEntity.ok(
                    new PasswordResponse(
                            true,
                            "Password reset request created.",
                            token));

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            new PasswordResponse(
                                    false,
                                    e.getMessage(),
                                    null));

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(
                            new PasswordResponse(
                                    false,
                                    "Unable to process password reset request.",
                                    null));
        }
    }

    // =========================================================
    // CHECK TOKEN
    // =========================================================

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(
            @RequestParam String token) {

        boolean valid = passwordResetService.isTokenValid(
                token);

        if (!valid) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            new PasswordResponse(
                                    false,
                                    "Invalid or expired reset link.",
                                    null));
        }

        return ResponseEntity.ok(
                new PasswordResponse(
                        true,
                        "Reset token is valid.",
                        null));
    }

    // =========================================================
    // RESET PASSWORD
    // =========================================================

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(
            @RequestBody ResetPasswordRequest request) {

        try {

            if (request.getPassword() == null
                    || request.getConfirmPassword() == null) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                new PasswordResponse(
                                        false,
                                        "Password fields are required.",
                                        null));
            }

            if (!request.getPassword()
                    .equals(
                            request.getConfirmPassword())) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                new PasswordResponse(
                                        false,
                                        "Passwords do not match.",
                                        null));
            }

            passwordResetService.resetPassword(
                    request.getToken(),
                    request.getPassword());

            return ResponseEntity.ok(
                    new PasswordResponse(
                            true,
                            "Password changed successfully.",
                            null));

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            new PasswordResponse(
                                    false,
                                    e.getMessage(),
                                    null));

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(
                            new PasswordResponse(
                                    false,
                                    "Unable to reset password.",
                                    null));
        }
    }

    // =========================================================
    // FORGOT PASSWORD REQUEST
    // =========================================================

    public static class ForgotPasswordRequest {

        private String email;

        public ForgotPasswordRequest() {
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(
                String email) {

            this.email = email;
        }
    }

    // =========================================================
    // RESET PASSWORD REQUEST
    // =========================================================

    public static class ResetPasswordRequest {

        private String token;

        private String password;

        private String confirmPassword;

        public ResetPasswordRequest() {
        }

        public String getToken() {
            return token;
        }

        public void setToken(
                String token) {

            this.token = token;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(
                String password) {

            this.password = password;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(
                String confirmPassword) {

            this.confirmPassword = confirmPassword;
        }
    }

    // =========================================================
    // RESPONSE
    // =========================================================

    public static class PasswordResponse {

        private boolean success;

        private String message;

        private String token;

        public PasswordResponse(
                boolean success,
                String message,
                String token) {

            this.success = success;
            this.message = message;
            this.token = token;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getToken() {
            return token;
        }
    }
}