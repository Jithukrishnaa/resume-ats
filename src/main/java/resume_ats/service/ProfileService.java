package resume_ats.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import resume_ats.dto.ProfileRequest;

import resume_ats.entity.Profile;
import resume_ats.entity.ProfileCertification;
import resume_ats.entity.ProfileEducation;
import resume_ats.entity.ProfileExperience;
import resume_ats.entity.ProfileProject;
import resume_ats.entity.User;

import resume_ats.repository.ProfileCertificationRepository;
import resume_ats.repository.ProfileEducationRepository;
import resume_ats.repository.ProfileExperienceRepository;
import resume_ats.repository.ProfileProjectRepository;
import resume_ats.repository.ProfileRepository;
import resume_ats.repository.UserRepository;

import java.util.List;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    private final ProfileEducationRepository educationRepository;
    private final ProfileExperienceRepository experienceRepository;
    private final ProfileProjectRepository projectRepository;
    private final ProfileCertificationRepository certificationRepository;

    public ProfileService(
            ProfileRepository profileRepository,
            UserRepository userRepository,
            ProfileEducationRepository educationRepository,
            ProfileExperienceRepository experienceRepository,
            ProfileProjectRepository projectRepository,
            ProfileCertificationRepository certificationRepository) {

        this.profileRepository = profileRepository;
        this.userRepository = userRepository;

        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
        this.projectRepository = projectRepository;
        this.certificationRepository = certificationRepository;
    }

    // =====================================================
    // GET PROFILE
    // =====================================================

    @Transactional(readOnly = true)
    public Profile getProfile(Long userId) {

        return profileRepository
                .findByUserId(userId)
                .orElseGet(() -> {

                    User user = userRepository
                            .findById(userId)
                            .orElseThrow(() -> new RuntimeException(
                                    "User not found"));

                    Profile profile = new Profile();

                    profile.setUser(user);

                    profile.setFullName(
                            user.getFullName());

                    profile.setEmail(
                            user.getEmail());

                    return profile;
                });
    }

    // =====================================================
    // SAVE COMPLETE PROFILE
    // =====================================================

    @Transactional
    public Profile saveCompleteProfile(
            Long userId,
            ProfileRequest request) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found"));

        // =================================================
        // PROFILE
        // =================================================

        Profile profile = profileRepository
                .findByUserId(userId)
                .orElseGet(Profile::new);

        profile.setUser(user);

        profile.setFullName(
                request.getFullName());

        profile.setProfessionalTitle(
                request.getProfessionalTitle());

        profile.setEmail(
                request.getEmail());

        profile.setPhone(
                request.getPhone());

        profile.setLocation(
                request.getLocation());

        profile.setLinkedin(
                request.getLinkedin());

        profile.setGithub(
                request.getGithub());

        profile.setPortfolio(
                request.getPortfolio());

        profile.setSummary(
                request.getSummary());

        profile.setObjective(
                request.getObjective());

        profile.setSkills(
                request.getSkills());

        profile.setLanguages(
                request.getLanguages());

        profile.setAchievements(
                request.getAchievements());

        // Save profile first
        profile = profileRepository.save(profile);

        Long profileId = profile.getId();

        // =================================================
        // DELETE OLD CHILD DATA
        // =================================================

        educationRepository.deleteByProfileId(
                profileId);

        experienceRepository.deleteByProfileId(
                profileId);

        projectRepository.deleteByProfileId(
                profileId);

        certificationRepository.deleteByProfileId(
                profileId);

        // =================================================
        // SAVE EDUCATION
        // =================================================

        if (request.getEducation() != null) {

            for (ProfileEducation education : request.getEducation()) {

                if (education == null) {
                    continue;
                }

                if (isEducationEmpty(education)) {
                    continue;
                }

                // Never accept an ID from the browser
                education.setId(null);

                education.setProfile(profile);

                educationRepository.save(
                        education);
            }
        }

        // =================================================
        // SAVE EXPERIENCE
        // =================================================

        if (request.getExperience() != null) {

            for (ProfileExperience experience : request.getExperience()) {

                if (experience == null) {
                    continue;
                }

                if (isExperienceEmpty(experience)) {
                    continue;
                }

                experience.setId(null);

                experience.setProfile(profile);

                experienceRepository.save(
                        experience);
            }
        }

        // =================================================
        // SAVE PROJECTS
        // =================================================

        if (request.getProjects() != null) {

            for (ProfileProject project : request.getProjects()) {

                if (project == null) {
                    continue;
                }

                if (isProjectEmpty(project)) {
                    continue;
                }

                project.setId(null);

                project.setProfile(profile);

                projectRepository.save(project);
            }
        }

        // =================================================
        // SAVE CERTIFICATIONS
        // =================================================

        if (request.getCertifications() != null) {

            for (ProfileCertification certification : request.getCertifications()) {

                if (certification == null) {
                    continue;
                }

                if (isCertificationEmpty(
                        certification)) {

                    continue;
                }

                certification.setId(null);

                certification.setProfile(profile);

                certificationRepository.save(
                        certification);
            }
        }

        return profile;
    }

    // =====================================================
    // GET EDUCATION
    // =====================================================

    @Transactional(readOnly = true)
    public List<ProfileEducation> getEducation(
            Long profileId) {

        return educationRepository
                .findByProfileId(profileId);
    }

    // =====================================================
    // GET EXPERIENCE
    // =====================================================

    @Transactional(readOnly = true)
    public List<ProfileExperience> getExperience(
            Long profileId) {

        return experienceRepository
                .findByProfileId(profileId);
    }

    // =====================================================
    // GET PROJECTS
    // =====================================================

    @Transactional(readOnly = true)
    public List<ProfileProject> getProjects(
            Long profileId) {

        return projectRepository
                .findByProfileId(profileId);
    }

    // =====================================================
    // GET CERTIFICATIONS
    // =====================================================

    @Transactional(readOnly = true)
    public List<ProfileCertification> getCertifications(
            Long profileId) {

        return certificationRepository
                .findByProfileId(profileId);
    }

    // =====================================================
    // CHECK EDUCATION
    // =====================================================

    private boolean isEducationEmpty(
            ProfileEducation education) {

        return isBlank(
                education.getDegree())

                && isBlank(
                        education.getInstitution())

                && isBlank(
                        education.getLocation())

                && isBlank(
                        education.getGrade())

                && isBlank(
                        education.getStartYear())

                && isBlank(
                        education.getEndYear());
    }

    // =====================================================
    // CHECK EXPERIENCE
    // =====================================================

    private boolean isExperienceEmpty(
            ProfileExperience experience) {

        return isBlank(
                experience.getJobTitle())

                && isBlank(
                        experience.getCompany())

                && isBlank(
                        experience.getLocation())

                && isBlank(
                        experience.getStartDate())

                && isBlank(
                        experience.getEndDate())

                && isBlank(
                        experience.getDescription());
    }

    // =====================================================
    // CHECK PROJECT
    // =====================================================

    private boolean isProjectEmpty(
            ProfileProject project) {

        return isBlank(
                project.getProjectName())

                && isBlank(
                        project.getTechnologies())

                && isBlank(
                        project.getProjectLink())

                && isBlank(
                        project.getDescription());
    }

    // =====================================================
    // CHECK CERTIFICATION
    // =====================================================

    private boolean isCertificationEmpty(
            ProfileCertification certification) {

        return isBlank(
                certification.getCertificationName())

                && isBlank(
                        certification.getIssuingOrganization())

                && isBlank(
                        certification.getIssueDate())

                && isBlank(
                        certification.getCredentialUrl());
    }

    // =====================================================
    // CHECK BLANK
    // =====================================================

    private boolean isBlank(String value) {

        return value == null
                || value.trim().isEmpty();
    }

    // =====================================================
    // DELETE PROFILE
    // =====================================================

    @Transactional
    public void deleteProfile(Long userId) {

        Profile profile = profileRepository
                .findByUserId(userId)
                .orElse(null);

        if (profile == null) {
            return;
        }

        Long profileId = profile.getId();

        // Delete child records first
        educationRepository
                .deleteByProfileId(profileId);

        experienceRepository
                .deleteByProfileId(profileId);

        projectRepository
                .deleteByProfileId(profileId);

        certificationRepository
                .deleteByProfileId(profileId);

        // Delete profile
        profileRepository.delete(profile);
    }
}