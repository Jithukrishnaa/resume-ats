package resume_ats.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class SupabaseStorageService {

    private final S3Client s3Client;

    @Value("${supabase.storage.bucket}")
    private String bucketName;

    public SupabaseStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Upload a file to Supabase Storage.
     *
     * Example:
     * resumes/5/abc123.pdf
     */
    public String uploadFile(
            MultipartFile file,
            String folder) throws IOException {

        String originalFilename = file.getOriginalFilename();

        String extension = "";

        if (originalFilename != null
                && originalFilename.contains(".")) {

            extension = originalFilename.substring(
                    originalFilename.lastIndexOf("."));
        }

        String uniqueFilename = UUID.randomUUID() + extension;

        String objectPath = folder + "/" + uniqueFilename;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectPath)
                .contentType(
                        file.getContentType() != null
                                ? file.getContentType()
                                : "application/pdf")
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromInputStream(
                        file.getInputStream(),
                        file.getSize()));

        return objectPath;
    }

    /**
     * Upload byte data to Supabase Storage.
     * Used for testing and small generated files.
     */
    public void uploadBytes(
            byte[] data,
            String objectPath,
            String contentType) {

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectPath)
                .contentType(contentType)
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromBytes(data));
    }

    /**
     * Download a file from Supabase Storage.
     */
    public byte[] downloadFile(String objectPath) {

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectPath)
                .build();

        ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(request);

        return response.asByteArray();
    }

    /**
     * Delete a file from Supabase Storage.
     */
    public void deleteFile(String objectPath) {

        s3Client.deleteObject(
                builder -> builder
                        .bucket(bucketName)
                        .key(objectPath));
    }

    /**
     * Check whether a file exists.
     */
    public boolean fileExists(String objectPath) {

        try {

            s3Client.headObject(
                    builder -> builder
                            .bucket(bucketName)
                            .key(objectPath));

            return true;

        } catch (Exception e) {

            return false;
        }
    }
}