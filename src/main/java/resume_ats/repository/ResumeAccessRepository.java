package resume_ats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import resume_ats.entity.ResumeAccess;

import java.util.Optional;

@Repository
public interface ResumeAccessRepository
        extends JpaRepository<ResumeAccess, Long> {

    boolean existsByUserIdAndResumeId(
            Long userId,
            Long resumeId);

    Optional<ResumeAccess> findByUserIdAndResumeId(
            Long userId,
            Long resumeId);
}