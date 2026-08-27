package resume_ats.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "profile_certifications")
public class ProfileCertification {

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
    // CERTIFICATION DETAILS
    // =====================================================

    @Column(name = "certification_name")
    private String certificationName;

    @Column(name = "issuing_organization")
    private String issuingOrganization;

    @Column(name = "issue_date")
    private String issueDate;

    @Column(name = "credential_url")
    private String credentialUrl;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public ProfileCertification() {
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

    public String getCertificationName() {
        return certificationName;
    }

    public void setCertificationName(
            String certificationName) {

        this.certificationName = certificationName;
    }

    public String getIssuingOrganization() {
        return issuingOrganization;
    }

    public void setIssuingOrganization(
            String issuingOrganization) {

        this.issuingOrganization = issuingOrganization;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(
            String issueDate) {

        this.issueDate = issueDate;
    }

    public String getCredentialUrl() {
        return credentialUrl;
    }

    public void setCredentialUrl(
            String credentialUrl) {

        this.credentialUrl = credentialUrl;
    }
}