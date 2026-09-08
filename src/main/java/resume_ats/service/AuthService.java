package resume_ats.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import resume_ats.entity.User;
import resume_ats.entity.Wallet;
import resume_ats.repository.UserRepository;
import resume_ats.repository.WalletRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(
            UserRepository userRepository,
            WalletRepository walletRepository) {

        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
    }

    // =========================================================
    // REGISTER USER
    // =========================================================

    @Transactional
    public User register(
            String fullName,
            String username,
            String email,
            String password,
            String role) {

        // -----------------------------------------------------
        // VALIDATE FULL NAME
        // -----------------------------------------------------

        if (fullName == null || fullName.isBlank()) {

            throw new IllegalArgumentException(
                    "Full name is required.");
        }

        // -----------------------------------------------------
        // VALIDATE USERNAME
        // -----------------------------------------------------

        if (username == null || username.isBlank()) {

            throw new IllegalArgumentException(
                    "Username is required.");
        }

        username = username.trim();

        // -----------------------------------------------------
        // VALIDATE EMAIL
        // -----------------------------------------------------

        if (email == null || email.isBlank()) {

            throw new IllegalArgumentException(
                    "Email is required.");
        }

        email = email.trim().toLowerCase();

        // -----------------------------------------------------
        // VALIDATE PASSWORD
        // -----------------------------------------------------

        if (password == null || password.length() < 8) {

            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters.");
        }

        // -----------------------------------------------------
        // CHECK USERNAME
        // -----------------------------------------------------

        if (userRepository.existsByUsernameIgnoreCase(username)) {

            throw new IllegalArgumentException(
                    "Username already exists.");
        }

        // -----------------------------------------------------
        // CHECK EMAIL
        // -----------------------------------------------------

        if (userRepository.existsByEmailIgnoreCase(email)) {

            throw new IllegalArgumentException(
                    "Email is already registered.");
        }

        // =====================================================
        // VALIDATE ROLE
        // =====================================================

        String finalRole;

        if (role == null || role.isBlank()) {

            // Default role
            finalRole = "HR";

        } else {

            finalRole = role
                    .trim()
                    .toUpperCase();
        }

        /*
         * ADMIN must NEVER be assignable from public
         * registration.
         */

        if ("ADMIN".equals(finalRole)) {

            throw new IllegalArgumentException(
                    "ADMIN accounts cannot be created through public registration.");
        }

        /*
         * Only these roles can be selected publicly.
         */

        if (!"HR".equals(finalRole)
                && !"RECRUITER".equals(finalRole)) {

            throw new IllegalArgumentException(
                    "Invalid account type.");
        }

        // =====================================================
        // CREATE USER
        // =====================================================

        User user = new User();

        user.setFullName(
                fullName.trim());

        user.setUsername(
                username);

        user.setEmail(
                email);

        // Store BCrypt password hash
        user.setPassword(
                passwordEncoder.encode(password));

        // Set selected role
        user.setRole(
                finalRole);

        user.setEnabled(true);

        // Save user first to generate ID
        User savedUser = userRepository.save(user);

        // =====================================================
        // CREATE WALLET
        // =====================================================

        Wallet wallet = new Wallet(savedUser);

        // New users start with zero credits
        wallet.setCredits(0L);

        walletRepository.save(wallet);

        return savedUser;
    }

    // =========================================================
    // FIND USER BY USERNAME
    // =========================================================

    public User findByUsername(
            String username) {

        if (username == null ||
                username.isBlank()) {

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

    public User findByEmail(
            String email) {

        if (email == null ||
                email.isBlank()) {

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