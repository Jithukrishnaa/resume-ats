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

    public List<File> extractZip(MultipartFile zipFile) throws IOException {

        System.out.println("======================================");
        System.out.println("USER DIR      : " + System.getProperty("user.dir"));
        System.out.println("EXTRACT DIR   : " + EXTRACT_DIR);
        System.out.println("======================================");

        List<File> extractedFiles = new ArrayList<>();

        File folder = new File(EXTRACT_DIR);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Delete previously extracted files
        File[] oldFiles = folder.listFiles();

        if (oldFiles != null) {
            for (File file : oldFiles) {
                file.delete();
            }
        }

        System.out.println("Extraction Folder : " + folder.getAbsolutePath());

        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {

            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {

                if (entry.isDirectory()) {
                    continue;
                }

                String lowerName = entry.getName().toLowerCase();

                if (!(lowerName.endsWith(".pdf")
                        || lowerName.endsWith(".doc")
                        || lowerName.endsWith(".docx"))) {

                    continue;
                }

                String fileName = new File(entry.getName()).getName();

                fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");

                File outputFile = new File(folder, fileName);

                System.out.println("--------------------------------");
                System.out.println("Extracting : " + fileName);
                System.out.println("Writing To : " + outputFile.getAbsolutePath());

                try (FileOutputStream fos = new FileOutputStream(outputFile)) {

                    byte[] buffer = new byte[8192];
                    int length;

                    while ((length = zis.read(buffer)) != -1) {
                        fos.write(buffer, 0, length);
                    }

                    fos.flush();
                }

                System.out.println("Exists After Write : " + outputFile.exists());
                System.out.println("File Size          : " + outputFile.length());

                extractedFiles.add(outputFile);

                zis.closeEntry();
            }
        }

        System.out.println();
        System.out.println("=========== FINAL CHECK ===========");

        for (File file : extractedFiles) {

            System.out.println(file.getAbsolutePath());
            System.out.println("Exists : " + file.exists());
            System.out.println("Size   : " + file.length());
            System.out.println();
        }

        System.out.println("Total Resume Files : " + extractedFiles.size());
        System.out.println("===================================");

        return extractedFiles;
    }
}