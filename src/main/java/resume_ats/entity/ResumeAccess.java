package resume_ats.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "resume_access",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_resume_access_user_resume",
                        columnNames = {"user_id", "resume_id"}
                )
        }
)
public class ResumeAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "unlocked_at", nullable = false)
    private LocalDateTime unlockedAt;

    public ResumeAccess() {
    }

    public ResumeAccess(User user, Resume resume) {
        this.user = user;
        this.resume = resume;
        this.unlockedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Resume getResume() {
        return resume;
    }

    public LocalDateTime getUnlockedAt() {
        return unlockedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setResume(Resume resume) {
        this.resume = resume;
    }

    public void setUnlockedAt(LocalDateTime unlockedAt) {
        this.unlockedAt = unlockedAt;
    }
}