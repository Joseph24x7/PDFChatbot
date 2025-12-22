package com.docqa.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class PDFExtractor {

    public static String extractTextFromPDF(MultipartFile file) {

        try {

            File tempFile = Files.createTempFile("upload_", ".pdf").toFile();
            file.transferTo(tempFile);

            try (PDDocument document = Loader.loadPDF(tempFile)) {

                if (document.isEncrypted()) {
                    throw new IllegalArgumentException("Encrypted PDFs are not supported");
                }

                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document).trim();

            } finally {
                log.info("Deleting temporary file: {}", tempFile.delete());
            }

        } catch (IOException e) {
            log.error("Error extracting text from PDF", e);
            throw new RuntimeException("Failed to extract text from PDF", e);
        }

    }
}
