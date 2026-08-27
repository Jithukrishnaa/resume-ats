package resume_ats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import resume_ats.entity.ProfileProject;

import java.util.List;

@Repository
public interface ProfileProjectRepository
        extends JpaRepository<ProfileProject, Long> {

    List<ProfileProject> findByProfileId(
            Long profileId);

    void deleteByProfileId(
            Long profileId);
}