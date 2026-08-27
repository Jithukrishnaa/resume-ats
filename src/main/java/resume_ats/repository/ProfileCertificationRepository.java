package resume_ats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import resume_ats.entity.ProfileCertification;

import java.util.List;

@Repository
public interface ProfileCertificationRepository
        extends JpaRepository<ProfileCertification, Long> {

    List<ProfileCertification> findByProfileId(
            Long profileId);

    void deleteByProfileId(
            Long profileId);
}