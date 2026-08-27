package resume_ats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import resume_ats.entity.ProfileExperience;

import java.util.List;

@Repository
public interface ProfileExperienceRepository
        extends JpaRepository<ProfileExperience, Long> {

    List<ProfileExperience> findByProfileId(
            Long profileId);

    void deleteByProfileId(
            Long profileId);
}