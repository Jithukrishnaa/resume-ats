package resume_ats.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import resume_ats.entity.User;
import resume_ats.entity.Wallet;
import resume_ats.repository.WalletRepository;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    // =========================================================
    // GET USER WALLET
    // =========================================================

    @Transactional(readOnly = true)
    public Wallet getWallet(User user) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "User is required.");
        }

        return walletRepository
                .findByUser(user)
                .orElseThrow(() -> new IllegalStateException(
                        "Wallet not found for user."));
    }

    // =========================================================
    // GET WALLET BY USER ID
    // =========================================================

    @Transactional(readOnly = true)
    public Wallet getWalletByUserId(Long userId) {

        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID is required.");
        }

        return walletRepository
                .findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "Wallet not found for user."));
    }

    // =========================================================
    // GET CREDIT BALANCE
    // =========================================================

    @Transactional(readOnly = true)
    public long getCredits(User user) {

        return getWallet(user).getCredits();
    }

    // =========================================================
    // ADD CREDITS
    // =========================================================

    @Transactional
    public Wallet addCredits(User user, long credits) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "User is required.");
        }

        if (credits <= 0) {
            throw new IllegalArgumentException(
                    "Credits must be greater than zero.");
        }

        Wallet wallet = getWallet(user);

        long currentCredits = wallet.getCredits();

        final long newCredits;

        try {
            newCredits = Math.addExact(currentCredits, credits);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException(
                    "Credit balance is too large.");
        }

        wallet.setCredits(newCredits);

        return walletRepository.save(wallet);
    }

    // =========================================================
    // CHECK WHETHER USER HAS ENOUGH CREDITS
    // =========================================================

    @Transactional(readOnly = true)
    public boolean hasEnoughCredits(
            User user,
            long requiredCredits) {

        if (requiredCredits <= 0) {
            return true;
        }

        return getCredits(user) >= requiredCredits;
    }

    @Transactional
    public void deductCredits(User user, long requiredCredits) {

        if (user == null) {
            throw new IllegalArgumentException("User is required.");
        }

        if (requiredCredits <= 0) {
            return;
        }

        Wallet wallet = walletRepository
                .findByUserIdForUpdate(user.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Wallet not found for user."));

        long currentCredits = wallet.getCredits();

        if (currentCredits < requiredCredits) {
            throw new IllegalStateException(
                    "Insufficient credits. You need "
                            + requiredCredits
                            + " credits to view or download this resume.");
        }

        wallet.setCredits(
                currentCredits - requiredCredits);

        walletRepository.save(wallet);
    }
}