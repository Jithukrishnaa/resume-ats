package resume_ats.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================================================
    // USER ID
    // =========================================================

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // =========================================================
    // RESET TOKEN
    // =========================================================

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    // =========================================================
    // EXPIRATION
    // =========================================================

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // =========================================================
    // USED
    // =========================================================

    @Column(nullable = false)
    private boolean used = false;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public PasswordResetToken() {
    }

    // =========================================================
    // GETTERS / SETTERS
    // =========================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(
            LocalDateTime expiresAt) {

        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}