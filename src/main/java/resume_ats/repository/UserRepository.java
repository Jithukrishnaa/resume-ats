package resume_ats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import resume_ats.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}