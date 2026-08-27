package resume_ats.dto;

import resume_ats.entity.ProfileCertification;
import resume_ats.entity.ProfileEducation;
import resume_ats.entity.ProfileExperience;
import resume_ats.entity.ProfileProject;

import java.util.ArrayList;
import java.util.List;

public class ProfileRequest {

    private String fullName;
    private String professionalTitle;
    private String email;
    private String phone;
    private String location;
    private String linkedin;
    private String github;
    private String portfolio;

    private String summary;
    private String objective;
    private String skills;
    private String languages;
    private String achievements;

    private List<ProfileEducation> education = new ArrayList<>();

    private List<ProfileExperience> experience = new ArrayList<>();

    private List<ProfileProject> projects = new ArrayList<>();

    private List<ProfileCertification> certifications = new ArrayList<>();

    // =====================================================
    // GETTERS
    // =====================================================

    public String getFullName() {
        return fullName;
    }

    public String getProfessionalTitle() {
        return professionalTitle;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getLocation() {
        return location;
    }

    public String getLinkedin() {
        return linkedin;
    }

    public String getGithub() {
        return github;
    }

    public String getPortfolio() {
        return portfolio;
    }

    public String getSummary() {
        return summary;
    }

    public String getObjective() {
        return objective;
    }

    public String getSkills() {
        return skills;
    }

    public String getLanguages() {
        return languages;
    }

    public String getAchievements() {
        return achievements;
    }

    public List<ProfileEducation> getEducation() {
        return education;
    }

    public List<ProfileExperience> getExperience() {
        return experience;
    }

    public List<ProfileProject> getProjects() {
        return projects;
    }

    public List<ProfileCertification> getCertifications() {
        return certifications;
    }

    // =====================================================
    // SETTERS
    // =====================================================

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setProfessionalTitle(
            String professionalTitle) {
        this.professionalTitle = professionalTitle;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setLinkedin(String linkedin) {
        this.linkedin = linkedin;
    }

    public void setGithub(String github) {
        this.github = github;
    }

    public void setPortfolio(String portfolio) {
        this.portfolio = portfolio;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public void setAchievements(String achievements) {
        this.achievements = achievements;
    }

    public void setEducation(
            List<ProfileEducation> education) {
        this.education = education;
    }

    public void setExperience(
            List<ProfileExperience> experience) {
        this.experience = experience;
    }

    public void setProjects(
            List<ProfileProject> projects) {
        this.projects = projects;
    }

    public void setCertifications(
            List<ProfileCertification> certifications) {
        this.certifications = certifications;
    }
}