package resume_ats.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    // =====================================================
    // ID
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =====================================================
    // FULL NAME
    // =====================================================

    @Column(name = "full_name", nullable = false)
    private String fullName;

    // =====================================================
    // USERNAME
    // =====================================================

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    // =====================================================
    // EMAIL
    // =====================================================

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    // =====================================================
    // PASSWORD
    // =====================================================

    /*
     * NEVER return password in API JSON responses.
     */
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    // =====================================================
    // ROLE
    // =====================================================

    @Column(nullable = false)
    private String role;

    // =====================================================
    // ENABLED
    // =====================================================

    @Column(nullable = false)
    private boolean enabled = true;

    // =====================================================
    // EXCEL PERMISSION
    // =====================================================

    /*
     * ADMIN can enable this permission for selected
     * HR / RECRUITER users.
     *
     * Default = false.
     */
    @Column(name = "can_use_excel", nullable = false)
    private boolean canUseExcel = false;

    // =====================================================
    // CREATED AT
    // =====================================================

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public User() {
    }

    // =====================================================
    // GETTERS
    // =====================================================

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isCanUseExcel() {
        return canUseExcel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // =====================================================
    // SETTERS
    // =====================================================

    public void setId(Long id) {
        this.id = id;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setCanUseExcel(boolean canUseExcel) {
        this.canUseExcel = canUseExcel;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // =====================================================
    // AUTOMATIC CREATED DATE
    // =====================================================

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}