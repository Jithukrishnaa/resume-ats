package resume_ats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import resume_ats.entity.ProfileEducation;

import java.util.List;

@Repository
public interface ProfileEducationRepository
        extends JpaRepository<ProfileEducation, Long> {

    List<ProfileEducation> findByProfileId(
            Long profileId);

    void deleteByProfileId(
            Long profileId);
}