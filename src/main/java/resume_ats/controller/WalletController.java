package resume_ats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import resume_ats.entity.User;
import resume_ats.entity.Wallet;
import resume_ats.service.AuthService;
import resume_ats.service.WalletService;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;
    private final AuthService authService;

    public WalletController(
            WalletService walletService,
            AuthService authService) {

        this.walletService = walletService;
        this.authService = authService;
    }

    // =========================================================
    // GET CURRENT USER WALLET
    // =========================================================

    @GetMapping
    public ResponseEntity<?> getMyWallet(
            Authentication authentication) {

        // Make sure the user is logged in
        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(401)
                    .body("You must be logged in.");
        }

        // Spring Security gives us the logged-in username
        String username = authentication.getName();

        User user = authService.findByUsername(username);

        if (user == null) {
            return ResponseEntity
                    .status(404)
                    .body("User not found.");
        }

        Wallet wallet = walletService.getWallet(user);

        return ResponseEntity.ok(
                new WalletResponse(
                        wallet.getId(),
                        user.getId(),
                        wallet.getCredits(),
                        wallet.getUpdatedAt()));
    }

    // =========================================================
    // WALLET RESPONSE
    // =========================================================

    public record WalletResponse(
            Long walletId,
            Long userId,
            Long credits,
            java.time.LocalDateTime updatedAt) {
    }
}