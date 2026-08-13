package resume_ats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import resume_ats.entity.JobDescription;

public interface JobDescriptionRepository extends JpaRepository<JobDescription, Long> {

    JobDescription findTopByOrderByIdDesc();

}