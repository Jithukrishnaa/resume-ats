package resume_ats.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import resume_ats.entity.User;
import resume_ats.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // =========================================================
    // REGISTER USER
    // =========================================================

    public User register(
            String fullName,
            String username,
            String email,
            String password) {

        // Validate full name
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException(
                    "Full name is required.");
        }

        // Validate username
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Username is required.");
        }

        username = username.trim();

        // Validate email
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email is required.");
        }

        email = email.trim().toLowerCase();

        // Validate password
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters.");
        }

        // Check username
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException(
                    "Username already exists.");
        }

        // Check email
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "Email is already registered.");
        }

        // Create user
        User user = new User();

        user.setFullName(fullName.trim());
        user.setUsername(username);
        user.setEmail(email);

        // IMPORTANT:
        // Store only the BCrypt hash.
        user.setPassword(
                passwordEncoder.encode(password));

        user.setRole("HR");
        user.setEnabled(true);

        return userRepository.save(user);
    }

    // =========================================================
    // FIND USER BY USERNAME
    // =========================================================

    public User findByUsername(String username) {

        if (username == null || username.isBlank()) {
            return null;
        }

        return userRepository
                .findByUsernameIgnoreCase(
                        username.trim())
                .orElse(null);
    }

    // =========================================================
    // FIND USER BY EMAIL
    // =========================================================

    public User findByEmail(String email) {

        if (email == null || email.isBlank()) {
            return null;
        }

        return userRepository
                .findByEmailIgnoreCase(
                        email.trim())
                .orElse(null);
    }

    // =========================================================
    // VERIFY PASSWORD
    // =========================================================

    public boolean verifyPassword(
            String username,
            String password) {

        User user = findByUsername(username);

        if (user == null) {
            return false;
        }

        if (!user.isEnabled()) {
            return false;
        }

        return passwordEncoder.matches(
                password,
                user.getPassword());
    }
}