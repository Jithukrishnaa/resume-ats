package resume_ats.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "job_descriptions")
public class JobDescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "skills_required", columnDefinition = "TEXT")
    private String skillsRequired;

    @Column(name = "jd_text", columnDefinition = "TEXT")
    private String jdText;

    private Double experienceRequired = 0.0;

    private String education;

    @Column(columnDefinition = "TEXT")
    private String certifications;

    private String location;

    private String employmentType;

    /*
     * SHA-256 hash of normalized JD text.
     *
     * Used to prevent duplicate Job Descriptions.
     */
    @Column(name = "jd_hash", length = 64, unique = true)
    private String jdHash;

    /*
     * Indicates whether this JD is currently being used
     * for ATS matching.
     *
     * Only one JD should have active = true.
     */
    @Column(nullable = false)
    private Boolean active = false;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public JobDescription() {
    }

    // =========================================================
    // GETTERS
    // =========================================================

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getSkillsRequired() {
        return skillsRequired;
    }

    public String getJdText() {
        return jdText;
    }

    public Double getExperienceRequired() {
        return experienceRequired;
    }

    public String getEducation() {
        return education;
    }

    public String getCertifications() {
        return certifications;
    }

    public String getLocation() {
        return location;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public String getJdHash() {
        return jdHash;
    }

    public Boolean getActive() {
        return active;
    }

    // =========================================================
    // SETTERS
    // =========================================================

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setSkillsRequired(String skillsRequired) {
        this.skillsRequired = skillsRequired;
    }

    public void setJdText(String jdText) {
        this.jdText = jdText;
    }

    public void setExperienceRequired(Double experienceRequired) {
        this.experienceRequired = experienceRequired;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public void setCertifications(String certifications) {
        this.certifications = certifications;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public void setJdHash(String jdHash) {
        this.jdHash = jdHash;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}