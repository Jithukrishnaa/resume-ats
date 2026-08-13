package resume_ats.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Service
public class ResumeService {

    public String extractText(File file) throws IOException {

        String fileName = file.getName().toLowerCase();

        String text;

        if (fileName.endsWith(".pdf")) {
            text = readPDF(file);

        } else if (fileName.endsWith(".doc")) {
            text = readDOC(file);

        } else if (fileName.endsWith(".docx")) {
            text = readDOCX(file);

        } else {
            throw new IOException("Unsupported file type : " + file.getName());
        }

        // Clean text before returning
        return cleanText(text);
    }

    // ================= PDF =================

    private String readPDF(File file) throws IOException {

        try (PDDocument document = Loader.loadPDF(file)) {

            PDFTextStripper stripper = new PDFTextStripper();

            return stripper.getText(document);
        }
    }

    // ================= DOC =================

    private String readDOC(File file) throws IOException {

        try (FileInputStream fis = new FileInputStream(file);
                HWPFDocument document = new HWPFDocument(fis);
                WordExtractor extractor = new WordExtractor(document)) {

            return extractor.getText();
        }
    }

    // ================= DOCX =================

    private String readDOCX(File file) throws IOException {

        try (FileInputStream fis = new FileInputStream(file);
                XWPFDocument document = new XWPFDocument(fis);
                XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {

            return extractor.getText();
        }
    }

    // ================= CLEAN TEXT =================

    private String cleanText(String text) {

        if (text == null) {
            return "";
        }

        // Remove NULL bytes
        text = text.replace("\u0000", "");

        // Remove invalid control characters except newline, carriage return and tab
        text = text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

        // Replace multiple spaces with one space
        text = text.replaceAll("[ ]{2,}", " ");

        // Replace multiple blank lines
        text = text.replaceAll("\\n{3,}", "\n\n");

        return text.trim();
    }
}