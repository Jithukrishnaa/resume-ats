package resume_ats.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "profile_projects")
public class ProfileProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =====================================================
    // PROFILE
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    // =====================================================
    // PROJECT DETAILS
    // =====================================================

    @Column(name = "project_name")
    private String projectName;

    @Column(columnDefinition = "TEXT")
    private String technologies;

    @Column(name = "project_link")
    private String projectLink;

    @Column(columnDefinition = "TEXT")
    private String description;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public ProfileProject() {
    }

    // =====================================================
    // GETTERS / SETTERS
    // =====================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(
            String projectName) {

        this.projectName = projectName;
    }

    public String getTechnologies() {
        return technologies;
    }

    public void setTechnologies(
            String technologies) {

        this.technologies = technologies;
    }

    public String getProjectLink() {
        return projectLink;
    }

    public void setProjectLink(
            String projectLink) {

        this.projectLink = projectLink;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description) {

        this.description = description;
    }
}