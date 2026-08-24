package com.resume.screener;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PdfResumeExtractor {

    public String extractText(Path pdfPath) throws IOException {

        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {

            PDFTextStripper stripper = new PDFTextStripper();

            return stripper.getText(document);
        }
    }
}
