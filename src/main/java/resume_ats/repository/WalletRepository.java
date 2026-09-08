package resume_ats.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import resume_ats.entity.User;
import resume_ats.entity.Wallet;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUser(User user);

    Optional<Wallet> findByUserId(Long userId);

    // =========================================================
    // LOCK WALLET ROW DURING CREDIT DEDUCTION
    // =========================================================
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT w
            FROM Wallet w
            WHERE w.user.id = :userId
            """)
    Optional<Wallet> findByUserIdForUpdate(
            @Param("userId") Long userId);
}