package resume_ats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import resume_ats.entity.JobDescription;

@Repository
public interface JobDescriptionRepository
        extends JpaRepository<JobDescription, Long> {

    /*
     * Existing fallback method.
     *
     * Used when selecting the newest remaining JD after
     * deleting the currently active JD.
     */
    JobDescription findTopByOrderByIdDesc();

    /*
     * Find the currently active Job Description.
     */
    JobDescription findTopByActiveTrueOrderByIdDesc();

    /*
     * Duplicate JD detection.
     */
    boolean existsByJdHash(String jdHash);

    JobDescription findByJdHash(String jdHash);

    /*
     * Make every JD inactive.
     *
     * We call this before activating a selected JD.
     */
    @Modifying
    @Transactional
    @Query("UPDATE JobDescription j SET j.active = false")
    int deactivateAll();
}