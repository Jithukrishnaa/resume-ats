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

    // Complete Resume Text
    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;

    // Extracted Skills
    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills;

    // =======================
    // Parsed Information
    // =======================

    private Integer experienceYears = 0;

    private String education;

    @Column(columnDefinition = "TEXT")
    private String certifications;

    private String location;

    // =======================
    // New ATS Fields
    // =======================

    private String linkedIn;

    private String github;

    private String portfolio;

    private String employmentType;

    private Integer projectCount = 0;

    @Column(columnDefinition = "TEXT")
    private String projectSkills;

    // =======================
    // Constructor
    // =======================

    public Resume() {
    }

    // =======================
    // ID
    // =======================

    public Long getId() {
        return id;
    }

    // =======================
    // Candidate Name
    // =======================

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    // =======================
    // Email
    // =======================

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // =======================
    // Phone
    // =======================

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // =======================
    // File Name
    // =======================

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    // =======================
    // File Path
    // =======================

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    // =======================
    // Raw Resume Text
    // =======================

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    // =======================
    // Skills
    // =======================

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    // =======================
    // Experience
    // =======================

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    // =======================
    // Education
    // =======================

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    // =======================
    // Certifications
    // =======================

    public String getCertifications() {
        return certifications;
    }

    public void setCertifications(String certifications) {
        this.certifications = certifications;
    }

    // =======================
    // Location
    // =======================

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // =======================
    // LinkedIn
    // =======================

    public String getLinkedIn() {
        return linkedIn;
    }

    public void setLinkedIn(String linkedIn) {
        this.linkedIn = linkedIn;
    }

    // =======================
    // GitHub
    // =======================

    public String getGithub() {
        return github;
    }

    public void setGithub(String github) {
        this.github = github;
    }

    // =======================
    // Portfolio
    // =======================

    public String getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(String portfolio) {
        this.portfolio = portfolio;
    }

    // =======================
    // Employment Type
    // =======================

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    // =======================
    // Project Count
    // =======================

    public Integer getProjectCount() {
        return projectCount;
    }

    public void setProjectCount(Integer projectCount) {
        this.projectCount = projectCount;
    }

    // =======================
    // Project Skills
    // =======================

    public String getProjectSkills() {
        return projectSkills;
    }

    public void setProjectSkills(String projectSkills) {
        this.projectSkills = projectSkills;
    }

}