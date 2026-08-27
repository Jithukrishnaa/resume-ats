package resume_ats.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import resume_ats.entity.PasswordResetToken;
import resume_ats.entity.User;
import resume_ats.repository.PasswordResetTokenRepository;
import resume_ats.repository.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository) {

        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    // =========================================================
    // CREATE RESET TOKEN
    // =========================================================

    @Transactional
    public String createResetToken(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email address is required.");
        }

        email = email.trim().toLowerCase();

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElse(null);

        if (user == null) {
            throw new IllegalArgumentException(
                    "No account found with this email address.");
        }

        if (!user.isEnabled()) {
            throw new IllegalArgumentException(
                    "This account is disabled.");
        }

        // Remove old tokens for this user
        tokenRepository.deleteByUserId(
                user.getId());

        // Generate secure token
        byte[] randomBytes = new byte[32];

        secureRandom.nextBytes(randomBytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        // Token valid for 15 minutes
        LocalDateTime expiry = LocalDateTime.now()
                .plusMinutes(15);

        PasswordResetToken resetToken = new PasswordResetToken();

        resetToken.setUserId(
                user.getId());

        resetToken.setToken(token);

        resetToken.setExpiresAt(
                expiry);

        resetToken.setUsed(false);

        tokenRepository.save(resetToken);

        return token;
    }

    // =========================================================
    // VALIDATE TOKEN
    // =========================================================

    public boolean isTokenValid(String token) {

        if (token == null || token.isBlank()) {
            return false;
        }

        PasswordResetToken resetToken = tokenRepository
                .findByToken(token)
                .orElse(null);

        if (resetToken == null) {
            return false;
        }

        // Already used
        if (resetToken.isUsed()) {
            return false;
        }

        // Expired
        if (resetToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            return false;
        }

        return true;
    }

    // =========================================================
    // RESET PASSWORD
    // =========================================================

    @Transactional
    public void resetPassword(
            String token,
            String newPassword) {

        if (token == null || token.isBlank()) {

            throw new IllegalArgumentException(
                    "Invalid reset token.");
        }

        if (newPassword == null
                || newPassword.length() < 8) {

            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters.");
        }

        PasswordResetToken resetToken = tokenRepository
                .findByToken(token)
                .orElse(null);

        if (resetToken == null) {

            throw new IllegalArgumentException(
                    "Invalid reset token.");
        }

        // Check if already used
        if (resetToken.isUsed()) {

            throw new IllegalArgumentException(
                    "This reset link has already been used.");
        }

        // Check expiry
        if (resetToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "This reset link has expired.");
        }

        // Find user
        User user = userRepository
                .findById(
                        resetToken.getUserId())
                .orElse(null);

        if (user == null) {

            throw new IllegalArgumentException(
                    "User account no longer exists.");
        }

        // =====================================================
        // UPDATE PASSWORD
        // =====================================================

        user.setPassword(
                passwordEncoder.encode(
                        newPassword));

        userRepository.save(user);

        // =====================================================
        // MARK TOKEN AS USED
        // =====================================================

        resetToken.setUsed(true);

        tokenRepository.save(resetToken);
    }
}