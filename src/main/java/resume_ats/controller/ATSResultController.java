package resume_ats.controller;

import org.springframework.web.bind.annotation.*;

import resume_ats.dto.ATSResultDTO;
import resume_ats.entity.ATSResult;
import resume_ats.entity.Resume;
import resume_ats.repository.ATSResultRepository;
import resume_ats.repository.ResumeRepository;
import resume_ats.service.RecruiterSummaryService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/ats")
public class ATSResultController {

    private final ATSResultRepository atsResultRepository;
    private final ResumeRepository resumeRepository;
    private final RecruiterSummaryService recruiterSummaryService;

    public ATSResultController(
            ATSResultRepository atsResultRepository,
            ResumeRepository resumeRepository,
            RecruiterSummaryService recruiterSummaryService) {

        this.atsResultRepository = atsResultRepository;
        this.resumeRepository = resumeRepository;
        this.recruiterSummaryService = recruiterSummaryService;
    }

    @GetMapping("/results")
    public List<ATSResultDTO> getResults() {

        List<ATSResult> atsResults = atsResultRepository.findAllByOrderByRankPositionAsc();

        List<ATSResultDTO> response = new ArrayList<>();

        for (ATSResult result : atsResults) {

            ATSResultDTO dto = new ATSResultDTO();

            dto.setRank(result.getRankPosition());
            dto.setCandidateName(result.getCandidateName());
            dto.setEmail(result.getEmail());

            dto.setAtsScore(result.getAtsScore());

            dto.setMatchedSkills(result.getMatchedSkills());
            dto.setMissingSkills(result.getMissingSkills());

            dto.setResumeId(result.getResumeId());

            Resume resume = resumeRepository
                    .findById(result.getResumeId())
                    .orElse(null);

            if (resume != null) {

                dto.setExperience(resume.getExperienceYears());

                dto.setLocation(resume.getLocation());

                dto.setEducation(resume.getEducation());

                dto.setResumeFile(resume.getFileName());

                dto.setRecruiterSummary(

                        recruiterSummaryService.generateSummary(

                                resume.getExperienceYears(),

                                resume.getEducation(),

                                result.getMatchedSkills(),

                                result.getMissingSkills(),

                                result.getAtsScore()

                        )

                );

            }

            response.add(dto);

        }

        return response;

    }

}