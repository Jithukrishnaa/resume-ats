package resume_ats.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import resume_ats.dto.RegisterRequest;
import resume_ats.entity.User;
import resume_ats.entity.Wallet;
import resume_ats.repository.UserRepository;
import resume_ats.repository.WalletRepository;

@Service
public class RegistrationService {

    private final UserRepository userRepository;

    private final WalletRepository walletRepository;

    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            UserRepository userRepository,
            WalletRepository walletRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;

        this.walletRepository = walletRepository;

        this.passwordEncoder = passwordEncoder;
    }

    // =========================================================
    // REGISTER USER
    // =========================================================

    @Transactional
    public User register(RegisterRequest request) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Registration data is required.");
        }

        // -----------------------------------------------------
        // FULL NAME
        // -----------------------------------------------------

        String fullName = clean(request.getFullName());

        if (fullName.isBlank()) {

            throw new IllegalArgumentException(
                    "Full name is required.");
        }

        // -----------------------------------------------------
        // USERNAME
        // -----------------------------------------------------

        String username = clean(request.getUsername());

        if (username.isBlank()) {

            throw new IllegalArgumentException(
                    "Username is required.");
        }

        // -----------------------------------------------------
        // EMAIL
        // -----------------------------------------------------

        String email = clean(request.getEmail());

        if (email.isBlank()) {

            throw new IllegalArgumentException(
                    "Email is required.");
        }

        // -----------------------------------------------------
        // PASSWORD
        // -----------------------------------------------------

        String password = request.getPassword();

        if (password == null ||
                password.length() < 6) {

            throw new IllegalArgumentException(
                    "Password must contain at least 6 characters.");
        }

        // -----------------------------------------------------
        // CHECK USERNAME
        // -----------------------------------------------------

        if (userRepository
                .existsByUsernameIgnoreCase(username)) {

            throw new IllegalArgumentException(
                    "Username is already registered.");
        }

        // -----------------------------------------------------
        // CHECK EMAIL
        // -----------------------------------------------------

        if (userRepository
                .existsByEmailIgnoreCase(email)) {

            throw new IllegalArgumentException(
                    "Email is already registered.");
        }

        // -----------------------------------------------------
        // ROLE
        // -----------------------------------------------------

        String role = normalizeRole(request.getRole());

        /*
         * Never allow public registration to create
         * an ADMIN account.
         */

        if ("ADMIN".equals(role)) {

            throw new IllegalArgumentException(
                    "ADMIN accounts cannot be created through public registration.");
        }

        /*
         * Only supported public roles.
         */

        if (!"HR".equals(role) &&
                !"RECRUITER".equals(role)) {

            role = "HR";
        }

        // -----------------------------------------------------
        // CREATE USER
        // -----------------------------------------------------

        User user = new User();

        user.setFullName(fullName);

        user.setUsername(username);

        user.setEmail(email);

        user.setPassword(
                passwordEncoder.encode(password));

        user.setRole(role);

        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        // -----------------------------------------------------
        // CREATE WALLET
        // -----------------------------------------------------

        Wallet wallet = new Wallet(savedUser);

        walletRepository.save(wallet);

        return savedUser;
    }

    // =========================================================
    // CLEAN STRING
    // =========================================================

    private String clean(String value) {

        if (value == null) {
            return "";
        }

        return value.trim();
    }

    // =========================================================
    // NORMALIZE ROLE
    // =========================================================

    private String normalizeRole(
            String role) {

        if (role == null ||
                role.isBlank()) {

            return "HR";
        }

        return role
                .trim()
                .toUpperCase();
    }
}