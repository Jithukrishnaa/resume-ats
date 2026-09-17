package resume_ats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import resume_ats.entity.Resume;

import java.util.List;

@Repository
public interface ResumeRepository
                extends JpaRepository<Resume, Long> {

        // =========================================================
        // EXISTING EMAIL DUPLICATE CHECK
        // =========================================================

        boolean existsByEmailIgnoreCase(String email);

        // =========================================================
        // SHA-256 RESUME DUPLICATE CHECK
        // =========================================================
        //
        // Prevents the exact same resume file from being uploaded
        // multiple times, even if the filename is changed.
        //
        // =========================================================

        boolean existsByResumeHash(String resumeHash);

        // =========================================================
        // EXISTING SEARCH
        // =========================================================

        List<Resume> findByCandidateNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFileNameContainingIgnoreCase(
                        String candidateName,
                        String email,
                        String fileName);
}