package resume_ats.controller;

import java.nio.charset.StandardCharsets;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import resume_ats.service.SupabaseStorageService;

@RestController
@RequestMapping("/api/storage-test")
public class SupabaseStorageTestController {

    private final SupabaseStorageService storageService;

    public SupabaseStorageTestController(
            SupabaseStorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping
    public ResponseEntity<String> testStorage() {

        try {

            String testPath = "test/supabase-test.txt";

            // Upload test file
            storageService.uploadBytes(
                    "Supabase Storage is working!".getBytes(StandardCharsets.UTF_8),
                    testPath,
                    "text/plain");

            // Download test file
            byte[] downloaded = storageService.downloadFile(testPath);

            // Delete test file
            storageService.deleteFile(testPath);

            return ResponseEntity.ok(
                    "SUCCESS - Supabase Storage upload, download and delete are working.");

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError().body(
                    "Supabase Storage test FAILED: "
                            + e.getMessage());
        }
    }
}