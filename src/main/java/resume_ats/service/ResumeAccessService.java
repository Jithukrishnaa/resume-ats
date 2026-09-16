package resume_ats.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import resume_ats.entity.Resume;
import resume_ats.entity.ResumeAccess;
import resume_ats.entity.User;
import resume_ats.repository.ResumeAccessRepository;

@Service
public class ResumeAccessService {

    private static final long RESUME_ACCESS_COST = 50L;

    private final ResumeAccessRepository resumeAccessRepository;
    private final WalletService walletService;

    public ResumeAccessService(
            ResumeAccessRepository resumeAccessRepository,
            WalletService walletService) {

        this.resumeAccessRepository = resumeAccessRepository;
        this.walletService = walletService;
    }

    // =========================================================
    // CHECK AND UNLOCK RESUME
    // =========================================================

    @Transactional
    public void ensureResumeAccess(
            User user,
            Resume resume) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "User is required.");
        }

        if (resume == null) {
            throw new IllegalArgumentException(
                    "Resume is required.");
        }

        // =====================================================
        // ADMIN = ALWAYS FREE
        // =====================================================

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return;
        }

        // =====================================================
        // CHECK WHETHER THIS USER ALREADY UNLOCKED THIS RESUME
        // =====================================================

        boolean alreadyUnlocked =
                resumeAccessRepository
                        .existsByUserIdAndResumeId(
                                user.getId(),
                                resume.getId());

        if (alreadyUnlocked) {

            // Already paid previously.
            // No credit deduction.
            return;
        }

        // =====================================================
        // FIRST ACCESS = DEDUCT 50 CREDITS
        // =====================================================

        walletService.deductCredits(
                user,
                RESUME_ACCESS_COST);

        // =====================================================
        // SAVE PERMANENT ACCESS
        // =====================================================

        ResumeAccess resumeAccess =
                new ResumeAccess(user, resume);

        resumeAccessRepository.save(resumeAccess);
    }

    // =========================================================
    // CHECK ACCESS ONLY
    // =========================================================

    @Transactional(readOnly = true)
    public boolean hasAccess(
            User user,
            Resume resume) {

        if (user == null || resume == null) {
            return false;
        }

        // Admin always has access.
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return true;
        }

        return resumeAccessRepository
                .existsByUserIdAndResumeId(
                        user.getId(),
                        resume.getId());
    }

    // =========================================================
    // GET ACCESS COST
    // =========================================================

    public long getResumeAccessCost() {
        return RESUME_ACCESS_COST;
    }
}