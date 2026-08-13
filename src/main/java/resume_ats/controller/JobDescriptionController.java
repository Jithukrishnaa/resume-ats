package resume_ats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import resume_ats.entity.JobDescription;
import resume_ats.repository.JobDescriptionRepository;
import resume_ats.service.ATSMatchingService;
import resume_ats.service.ResumeService;
import resume_ats.util.parser.JobDescriptionParser;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/jobdescriptions")
public class JobDescriptionController {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/Uploads/jobdescriptions/";

    private final JobDescriptionRepository jobDescriptionRepository;
    private final ResumeService resumeService;
    private final ATSMatchingService atsMatchingService;

    public JobDescriptionController(
            JobDescriptionRepository jobDescriptionRepository,
            ResumeService resumeService,
            ATSMatchingService atsMatchingService) {

        this.jobDescriptionRepository = jobDescriptionRepository;
        this.resumeService = resumeService;
        this.atsMatchingService = atsMatchingService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadJobDescription(
            @RequestParam("file") MultipartFile file) {

        try {

            // Validate file
            if (file == null || file.isEmpty()) {

                return ResponseEntity.badRequest()
                        .body("Please select a Job Description PDF.");
            }

            // Create upload directory
            File uploadDir = new File(UPLOAD_DIR);

            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Save uploaded file
            File destination = new File(
                    uploadDir,
                    file.getOriginalFilename());

            file.transferTo(destination);

            // Extract text from JD
            String jdText = resumeService.extractText(destination);

            // Parse complete Job Description
            JobDescription jobDescription = JobDescriptionParser.parse(jdText);

            // Store file information
            jobDescription.setFileName(
                    file.getOriginalFilename());

            jobDescription.setFilePath(
                    destination.getAbsolutePath());

            // Keep only the latest JD
            jobDescriptionRepository.deleteAll();

            // Save parsed JD
            jobDescriptionRepository.save(jobDescription);

            // Automatically evaluate every stored resume
            atsMatchingService.runATS();

            return ResponseEntity.ok(

                    "Job Description uploaded successfully.\n\n" +

                            "✓ Parsed Successfully\n" +

                            "✓ ATS Executed\n" +

                            "✓ Existing resumes re-evaluated\n" +

                            "✓ Rankings updated"

            );

        } catch (IOException e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()

                    .body("Failed to upload Job Description.\n"
                            + e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()

                    .body("ATS execution failed.\n"
                            + e.getMessage());

        }
    }

}