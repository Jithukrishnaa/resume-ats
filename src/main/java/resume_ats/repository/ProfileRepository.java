package resume_ats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import resume_ats.entity.Profile;
import resume_ats.entity.User;

import java.util.Optional;

@Repository
public interface ProfileRepository
        extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUser(
            User user);

    Optional<Profile> findByUserId(
            Long userId);

    boolean existsByUserId(
            Long userId);
}