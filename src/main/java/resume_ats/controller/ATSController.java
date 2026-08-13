package resume_ats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import resume_ats.service.ATSMatchingService;

@RestController
@RequestMapping("/api/ats")
public class ATSController {

    private final ATSMatchingService atsMatchingService;

    public ATSController(ATSMatchingService atsMatchingService) {
        this.atsMatchingService = atsMatchingService;
    }

    @PostMapping("/run")
    public ResponseEntity<String> runATS() {

        try {

            atsMatchingService.runATS();

            return ResponseEntity.ok(
                    "ATS Matching completed successfully.");

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(e.getMessage());
        }
    }

}