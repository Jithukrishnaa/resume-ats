package resume_ats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import resume_ats.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository
                extends JpaRepository<User, Long> {

        // =====================================================
        // LOGIN
        // =====================================================

        Optional<User> findByUsername(
                        String username);

        // =====================================================
        // CASE-INSENSITIVE USERNAME SEARCH
        // =====================================================

        Optional<User> findByUsernameIgnoreCase(
                        String username);

        // =====================================================
        // EMAIL
        // =====================================================

        Optional<User> findByEmail(
                        String email);

        Optional<User> findByEmailIgnoreCase(
                        String email);

        // =====================================================
        // EXISTENCE CHECKS
        // =====================================================

        boolean existsByUsername(
                        String username);

        boolean existsByUsernameIgnoreCase(
                        String username);

        boolean existsByEmail(
                        String email);

        boolean existsByEmailIgnoreCase(
                        String email);
}