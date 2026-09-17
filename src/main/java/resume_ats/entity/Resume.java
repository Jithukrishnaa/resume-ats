package resume_ats.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "candidate_name", length = 255)
    private String candidateName;

    @Column(unique = true, length = 255)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "file_name", length = 500)
    private String fileName;

    @Column(name = "file_path", length = 1000)
    private String filePath;

    // =========================================================
    // RESUME HASH
    // =========================================================
    // SHA-256 hash of the actual uploaded file.
    // Used to prevent the exact same resume from being uploaded
    // more than once, even if the filename is changed.
    // =========================================================

    @Column(name = "resume_hash", unique = true, length = 64)
    private String resumeHash;

    // =========================================================
    // COMPLETE RESUME TEXT
    // =========================================================

    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;

    // =========================================================
    // EXTRACTED SKILLS
    // =========================================================

    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills;

    // =========================================================
    // PARSED INFORMATION
    // =========================================================

    private Integer experienceYears = 0;

    private String education;

    @Column(columnDefinition = "TEXT")
    private String certifications;

    private String location;

    // =========================================================
    // ATS FIELDS
    // =========================================================

    private String linkedIn;

    private String github;

    private String portfolio;

    private String employmentType;

    private Integer projectCount = 0;

    @Column(columnDefinition = "TEXT")
    private String projectSkills;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public Resume() {
    }

    // =========================================================
    // ID
    // =========================================================

    public Long getId() {
        return id;
    }

    // =========================================================
    // CANDIDATE NAME
    // =========================================================

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    // =========================================================
    // EMAIL
    // =========================================================

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // =========================================================
    // PHONE
    // =========================================================

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // =========================================================
    // FILE NAME
    // =========================================================

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    // =========================================================
    // FILE PATH
    // =========================================================

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    // =========================================================
    // RESUME HASH
    // =========================================================

    public String getResumeHash() {
        return resumeHash;
    }

    public void setResumeHash(String resumeHash) {
        this.resumeHash = resumeHash;
    }

    // =========================================================
    // RAW RESUME TEXT
    // =========================================================

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    // =========================================================
    // SKILLS
    // =========================================================

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    // =========================================================
    // EXPERIENCE
    // =========================================================

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    // =========================================================
    // EDUCATION
    // =========================================================

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    // =========================================================
    // CERTIFICATIONS
    // =========================================================

    public String getCertifications() {
        return certifications;
    }

    public void setCertifications(String certifications) {
        this.certifications = certifications;
    }

    // =========================================================
    // LOCATION
    // =========================================================

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // =========================================================
    // LINKEDIN
    // =========================================================

    public String getLinkedIn() {
        return linkedIn;
    }

    public void setLinkedIn(String linkedIn) {
        this.linkedIn = linkedIn;
    }

    // =========================================================
    // GITHUB
    // =========================================================

    public String getGithub() {
        return github;
    }

    public void setGithub(String github) {
        this.github = github;
    }

    // =========================================================
    // PORTFOLIO
    // =========================================================

    public String getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(String portfolio) {
        this.portfolio = portfolio;
    }

    // =========================================================
    // EMPLOYMENT TYPE
    // =========================================================

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    // =========================================================
    // PROJECT COUNT
    // =========================================================

    public Integer getProjectCount() {
        return projectCount;
    }

    public void setProjectCount(Integer projectCount) {
        this.projectCount = projectCount;
    }

    // =========================================================
    // PROJECT SKILLS
    // =========================================================

    public String getProjectSkills() {
        return projectSkills;
    }

    public void setProjectSkills(String projectSkills) {
        this.projectSkills = projectSkills;
    }
}