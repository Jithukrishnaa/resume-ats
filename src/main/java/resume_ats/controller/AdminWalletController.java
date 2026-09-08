package resume_ats.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import resume_ats.entity.User;
import resume_ats.entity.Wallet;
import resume_ats.repository.UserRepository;
import resume_ats.service.WalletService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/wallet")
public class AdminWalletController {

    private final UserRepository userRepository;
    private final WalletService walletService;

    public AdminWalletController(
            UserRepository userRepository,
            WalletService walletService) {

        this.userRepository = userRepository;
        this.walletService = walletService;
    }

    // =========================================================
    // ADD CREDITS TO USER WALLET
    // =========================================================

    @PutMapping("/{userId}")
    public ResponseEntity<?> addCredits(
            @PathVariable Long userId,
            @RequestBody AddCreditsRequest request,
            Authentication authentication) {

        // -----------------------------------------------------
        // CHECK AUTHENTICATION
        // -----------------------------------------------------

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Authentication required."));
        }

        // -----------------------------------------------------
        // FIND LOGGED-IN USER
        // -----------------------------------------------------

        String username = authentication.getName();

        User admin = userRepository
                .findByUsernameIgnoreCase(username)
                .orElse(null);

        if (admin == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "User account not found."));
        }

        // -----------------------------------------------------
        // ADMIN CHECK
        // -----------------------------------------------------

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            "Admin access required."));
        }

        // -----------------------------------------------------
        // VALIDATE REQUEST
        // -----------------------------------------------------

        if (request == null || request.credits() == null) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            "Credits are required."));
        }

        long credits = request.credits();

        if (credits <= 0) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            "Credits must be greater than zero."));
        }

        // -----------------------------------------------------
        // FIND TARGET USER
        // -----------------------------------------------------

        User targetUser = userRepository
                .findById(userId)
                .orElse(null);

        if (targetUser == null) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "message",
                            "Target user not found."));
        }

        // -----------------------------------------------------
        // ADD CREDITS
        // -----------------------------------------------------

        try {

            Wallet wallet = walletService.addCredits(
                    targetUser,
                    credits);

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Credits added successfully.",
                            "userId",
                            targetUser.getId(),
                            "username",
                            targetUser.getUsername(),
                            "creditsAdded",
                            credits,
                            "totalCredits",
                            wallet.getCredits()));

        } catch (IllegalArgumentException ex) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            ex.getMessage()));
        }
    }

    // =========================================================
    // REQUEST BODY
    // =========================================================

    public record AddCreditsRequest(
            Long credits) {
    }
}