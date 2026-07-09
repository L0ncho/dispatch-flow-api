package com.dispatchflow.guides.infrastructure.adapters;

import com.dispatchflow.guides.application.ports.GuidePdfGeneratorPort;
import com.dispatchflow.guides.domain.entities.DispatchGuide;
import com.dispatchflow.shared.domain.DomainError;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Normalizer;

@Component
public class PdfBoxGuidePdfGenerator implements GuidePdfGeneratorPort {

    private static final float MARGIN = 50;
    private static final float LINE_HEIGHT = 16;

    @Override
    public byte[] generate(DispatchGuide guide) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float y = page.getMediaBox().getHeight() - MARGIN;
                y = writeLine(contentStream, font, 18, y, "Guia de Despacho");
                y -= LINE_HEIGHT;
                y = writeLine(contentStream, font, 12, y, sanitizeForPdf("Numero: " + guide.getGuideNumber().value()));
                y = writeLine(contentStream, font, 12, y, sanitizeForPdf("Transportista: " + guide.getCarrierName()));
                y = writeLine(contentStream, font, 12, y, sanitizeForPdf("Destinatario: " + guide.getRecipientName()));
                y = writeLine(contentStream, font, 12, y, sanitizeForPdf("Origen: " + guide.getOriginAddress()));
                y = writeLine(contentStream, font, 12, y, sanitizeForPdf("Destino: " + guide.getDestinationAddress()));
                y = writeLine(contentStream, font, 12, y, sanitizeForPdf("Descripcion: " + nullSafe(guide.getDescription())));
                y = writeLine(contentStream, font, 12, y, "Fecha despacho: " + guide.getDispatchDate());
                y = writeLine(contentStream, font, 12, y, "Responsable: " + guide.getOwnerEmail().value());
                writeLine(contentStream, font, 12, y, "Estado: " + guide.getStatus().name());
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw DomainError.other("Could not generate guide PDF");
        }
    }

    private float writeLine(
            PDPageContentStream contentStream,
            PDType1Font font,
            float fontSize,
            float y,
            String text) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(MARGIN, y);
        contentStream.showText(text);
        contentStream.endText();
        return y - LINE_HEIGHT;
    }

    private String nullSafe(String value) {
        return value == null ? "-" : value;
    }

    private String sanitizeForPdf(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}
