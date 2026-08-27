package resume_ats.service;

import org.springframework.stereotype.Service;

import resume_ats.entity.ATSResult;
import resume_ats.entity.JobDescription;
import resume_ats.entity.Resume;
import resume_ats.repository.ATSResultRepository;
import resume_ats.repository.JobDescriptionRepository;
import resume_ats.repository.ResumeRepository;
import resume_ats.util.dictionary.SkillNormalizer;

import java.util.*;

@Service
public class ATSMatchingService {

    private final ResumeRepository resumeRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ATSResultRepository atsResultRepository;
    private final ATSScoringService atsScoringService;

    private static final double ATS_CUTOFF = 50.0;

    public ATSMatchingService(
            ResumeRepository resumeRepository,
            JobDescriptionRepository jobDescriptionRepository,
            ATSResultRepository atsResultRepository,
            ATSScoringService atsScoringService) {

        this.resumeRepository = resumeRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.atsResultRepository = atsResultRepository;
        this.atsScoringService = atsScoringService;
    }

    public void runATS() {

        JobDescription latestJD = jobDescriptionRepository.findTopByOrderByIdDesc();

        if (latestJD == null) {
            throw new RuntimeException("No Job Description uploaded.");
        }

        atsResultRepository.deleteAll();

        List<Resume> resumes = resumeRepository.findAll();

        Set<String> jdSkills = convertToNormalizedSkillSet(
                latestJD.getSkillsRequired());

        List<ATSResult> shortlisted = new ArrayList<>();

        for (Resume resume : resumes) {

            Set<String> resumeSkills = convertToNormalizedSkillSet(
                    resume.getSkills());

            double score = atsScoringService.calculateScore(

                    jdSkills,
                    resumeSkills,

                    latestJD.getExperienceRequired().intValue(),
                    resume.getExperienceYears(),

                    latestJD.getEducation(),
                    resume.getEducation(),

                    latestJD.getCertifications(),
                    resume.getCertifications(),

                    latestJD.getLocation(),
                    resume.getLocation());

            if (score < ATS_CUTOFF) {
                continue;
            }

            Set<String> matchedSkills = new LinkedHashSet<>();

            Set<String> missingSkills = new LinkedHashSet<>(jdSkills);

            for (String jdSkill : jdSkills) {

                if (resumeSkills.contains(jdSkill)) {

                    matchedSkills.add(jdSkill);

                    missingSkills.remove(jdSkill);
                }
            }

            ATSResult result = new ATSResult();

            result.setResumeId(resume.getId());

            result.setJdId(latestJD.getId());

            result.setCandidateName(
                    resume.getCandidateName());

            result.setEmail(
                    resume.getEmail());

            result.setMatchedSkills(
                    String.join(", ", matchedSkills));

            result.setMissingSkills(
                    String.join(", ", missingSkills));

            result.setAtsScore(
                    Math.round(score * 100.0) / 100.0);

            shortlisted.add(result);
        }

        shortlisted.sort(
                Comparator.comparing(
                        ATSResult::getAtsScore)
                        .reversed());

        int rank = 1;

        for (ATSResult result : shortlisted) {

            result.setRankPosition(rank++);

            atsResultRepository.save(result);
        }
    }

    /**
     * Converts comma-separated skills into a normalized set.
     */
    private Set<String> convertToNormalizedSkillSet(
            String skills) {

        Set<String> normalized = new LinkedHashSet<>();

        if (skills == null || skills.isBlank()) {
            return normalized;
        }

        for (String skill : skills.split(",")) {

            skill = skill.trim();

            if (!skill.isBlank()) {

                normalized.add(
                        SkillNormalizer.normalize(skill));

            }
        }

        return normalized;
    }

}