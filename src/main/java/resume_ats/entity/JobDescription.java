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

    // Extracted keywords from JD
    @Column(name = "skills_required", columnDefinition = "TEXT")
    private String skillsRequired;

    // Complete Job Description text
    @Column(name = "jd_text", columnDefinition = "TEXT")
    private String jdText;

    private Double experienceRequired = 0.0;

    // ---------- Additional ATS Fields ----------

    private String education;

    @Column(columnDefinition = "TEXT")
    private String certifications;

    private String location;

    private String employmentType;

    // ---------- Constructor ----------

    public JobDescription() {
    }

    // ---------- ID ----------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // ---------- Title ----------

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // ---------- File ----------

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    // ---------- Keywords ----------

    public String getSkillsRequired() {
        return skillsRequired;
    }

    public void setSkillsRequired(String skillsRequired) {
        this.skillsRequired = skillsRequired;
    }

    // ---------- Full JD Text ----------

    public String getJdText() {
        return jdText;
    }

    public void setJdText(String jdText) {
        this.jdText = jdText;
    }

    // ---------- Experience ----------

    public Double getExperienceRequired() {
        return experienceRequired;
    }

    public void setExperienceRequired(Double experienceRequired) {
        this.experienceRequired = experienceRequired;
    }

    // ---------- Education ----------

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    // ---------- Certifications ----------

    public String getCertifications() {
        return certifications;
    }

    public void setCertifications(String certifications) {
        this.certifications = certifications;
    }

    // ---------- Location ----------

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // ---------- Employment Type ----------

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

}