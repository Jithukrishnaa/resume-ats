package resume_ats.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ZipExtractionService {

    private static final String EXTRACT_DIR = System.getProperty("user.dir")
            + File.separator
            + "Uploads"
            + File.separator
            + "extracted";

    public List<File> extractZip(
            MultipartFile zipFile) throws IOException {

        System.out.println("======================================");
        System.out.println(
                "USER DIR    : "
                        + System.getProperty("user.dir"));
        System.out.println(
                "EXTRACT DIR : "
                        + EXTRACT_DIR);
        System.out.println("======================================");

        List<File> extractedFiles = new ArrayList<>();

        // ==========================================
        // Create extraction directory
        // ==========================================

        File folder = new File(EXTRACT_DIR);

        if (!folder.exists()) {

            boolean created = folder.mkdirs();

            if (!created && !folder.exists()) {

                throw new IOException(
                        "Unable to create extraction folder: "
                                + folder.getAbsolutePath());
            }
        }

        System.out.println(
                "Extraction Folder : "
                        + folder.getAbsolutePath());

        // ==========================================
        // Extract ZIP
        //
        // IMPORTANT:
        // Do NOT delete existing resumes.
        // ==========================================

        try (ZipInputStream zis = new ZipInputStream(
                zipFile.getInputStream())) {

            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {

                // ----------------------------------
                // Ignore folders
                // ----------------------------------

                if (entry.isDirectory()) {

                    zis.closeEntry();

                    continue;
                }

                String lowerName = entry.getName()
                        .toLowerCase();

                // ----------------------------------
                // Only resume documents
                // ----------------------------------

                if (!(lowerName.endsWith(".pdf")
                        || lowerName.endsWith(".doc")
                        || lowerName.endsWith(".docx"))) {

                    zis.closeEntry();

                    continue;
                }

                // ----------------------------------
                // Get safe filename
                // ----------------------------------

                String fileName = new File(entry.getName())
                        .getName();

                fileName = fileName.replaceAll(
                        "[\\\\/:*?\"<>|]",
                        "_");

                File outputFile = new File(
                        folder,
                        fileName);

                // ----------------------------------
                // Handle duplicate filenames
                // ----------------------------------

                if (outputFile.exists()) {

                    String baseName = fileName;

                    String extension = "";

                    int dotIndex = fileName.lastIndexOf('.');

                    if (dotIndex > 0) {

                        baseName = fileName.substring(
                                0,
                                dotIndex);

                        extension = fileName.substring(
                                dotIndex);
                    }

                    int counter = 1;

                    do {

                        fileName = baseName
                                + "_"
                                + counter
                                + extension;

                        outputFile = new File(
                                folder,
                                fileName);

                        counter++;

                    } while (outputFile.exists());
                }

                // ----------------------------------
                // Extract file
                // ----------------------------------

                System.out.println("--------------------------------");
                System.out.println(
                        "Extracting : "
                                + fileName);
                System.out.println(
                        "Writing To : "
                                + outputFile
                                        .getAbsolutePath());

                try (FileOutputStream fos = new FileOutputStream(
                        outputFile)) {

                    byte[] buffer = new byte[8192];

                    int length;

                    while ((length = zis.read(buffer)) != -1) {

                        fos.write(
                                buffer,
                                0,
                                length);
                    }

                    fos.flush();
                }

                // ----------------------------------
                // Verify extraction
                // ----------------------------------

                System.out.println(
                        "Exists After Write : "
                                + outputFile.exists());

                System.out.println(
                        "File Size          : "
                                + outputFile.length());

                if (outputFile.exists()
                        && outputFile.isFile()
                        && outputFile.length() > 0) {

                    extractedFiles.add(
                            outputFile);
                }

                zis.closeEntry();
            }
        }

        // ==========================================
        // Final verification
        // ==========================================

        System.out.println();
        System.out.println(
                "=========== FINAL CHECK ===========");

        for (File file : extractedFiles) {

            System.out.println(
                    file.getAbsolutePath());

            System.out.println(
                    "Exists : "
                            + file.exists());

            System.out.println(
                    "Size   : "
                            + file.length());

            System.out.println();
        }

        System.out.println(
                "New Resume Files Extracted : "
                        + extractedFiles.size());

        System.out.println(
                "===================================");

        return extractedFiles;
    }
}