package resume_ats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import resume_ats.entity.Resume;

import java.util.List;

@Repository
public interface ResumeRepository
        extends JpaRepository<Resume, Long> {

    boolean existsByEmailIgnoreCase(String email);

    List<Resume> findByCandidateNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFileNameContainingIgnoreCase(
            String candidateName,
            String email,
            String fileName);
}