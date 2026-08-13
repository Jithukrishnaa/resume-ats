package resume_ats.service;

import org.springframework.stereotype.Service;

import resume_ats.entity.ATSResult;
import resume_ats.entity.JobDescription;
import resume_ats.entity.Resume;
import resume_ats.repository.ATSResultRepository;
import resume_ats.repository.JobDescriptionRepository;
import resume_ats.repository.ResumeRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ATSService {

    private final ResumeRepository resumeRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ATSResultRepository atsResultRepository;

    public ATSService(
            ResumeRepository resumeRepository,
            JobDescriptionRepository jobDescriptionRepository,
            ATSResultRepository atsResultRepository) {

        this.resumeRepository = resumeRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.atsResultRepository = atsResultRepository;
    }

    public void runATSMatching() {

        // Get latest Job Description
        JobDescription job = jobDescriptionRepository.findTopByOrderByIdDesc();

        if (job == null) {
            throw new RuntimeException("No Job Description found.");
        }

        // Get all resumes
        List<Resume> resumes = resumeRepository.findAll();

        // Remove previous ATS results
        atsResultRepository.deleteAll();

        // Convert Job Description skills to Set
        Set<String> jdSkills = new HashSet<>();

        if (job.getSkillsRequired() != null &&
                !job.getSkillsRequired().isBlank()) {

            for (String skill : job.getSkillsRequired().split(",")) {
                jdSkills.add(skill.trim().toLowerCase());
            }
        }

        int rank = 1;

        for (Resume resume : resumes) {

            Set<String> resumeSkills = new HashSet<>();

            if (resume.getSkills() != null &&
                    !resume.getSkills().isBlank()) {

                for (String skill : resume.getSkills().split(",")) {
                    resumeSkills.add(skill.trim().toLowerCase());
                }
            }

            // Matched Skills
            Set<String> matchedSkills = new HashSet<>(resumeSkills);
            matchedSkills.retainAll(jdSkills);

            // Missing Skills
            Set<String> missingSkills = new HashSet<>(jdSkills);
            missingSkills.removeAll(resumeSkills);

            // ATS Score
            double score = 0.0;

            if (!jdSkills.isEmpty()) {
                score = ((double) matchedSkills.size() / jdSkills.size()) * 100;
            }

            // Save Result
            ATSResult result = new ATSResult();

            result.setResumeId(resume.getId());

            result.setJdId(job.getId());

            result.setCandidateName(resume.getCandidateName());

            result.setMatchedSkills(String.join(", ", matchedSkills));

            result.setMissingSkills(String.join(", ", missingSkills));

            result.setAtsScore(score);

            result.setRankPosition(rank++);

            atsResultRepository.save(result);
        }
    }
}