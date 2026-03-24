package resto_dev.modules.layout.tables.application.service;

import com.lowagie.text.Rectangle;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import resto_dev.modules.layout.tables.domain.model.Table;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TablePdfService {

    public byte[] generateQrCatalog(List<Table> tables, String baseUrl, String orgName, java.util.Map<java.util.UUID, String> zoneNames) {
        Document document = new Document(PageSize.A4.rotate()); // Landscape
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Font configurations - Using standard Helvetica with better sizes
            Font fontOrg = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(52, 73, 94)); // Dark Blue/Gray
            Font fontZone = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(127, 140, 141)); // Light Gray
            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 36, Color.BLACK);
            Font fontInstruction = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);

            PdfPTable pdfTable = new PdfPTable(2); // 2 columns
            pdfTable.setWidthPercentage(100);
            pdfTable.setSpacingBefore(0f);

            for (Table table : tables) {
                PdfPCell cell = new PdfPCell();
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setPadding(35); // Generous padding between cards
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);

                // Inner Table for styling each QR unit (The "Card")
                PdfPTable cardTable = new PdfPTable(1);
                cardTable.setWidthPercentage(100);
                
                PdfPCell cardContent = new PdfPCell();
                cardContent.setBorderWidth(1.5f);
                cardContent.setBorderColor(new Color(189, 195, 199)); // Elegant silver border
                cardContent.setPadding(20);
                cardContent.setHorizontalAlignment(Element.ALIGN_CENTER);
                cardContent.setBackgroundColor(Color.WHITE);

                // 1. Organization Name (Branding)
                Paragraph pOrg = new Paragraph(orgName.toUpperCase(), fontOrg);
                pOrg.setAlignment(Element.ALIGN_CENTER);
                pOrg.setSpacingAfter(0f);
                cardContent.addElement(pOrg);
                
                // 1.1 Zone Name (Context)
                String zoneName = zoneNames != null ? zoneNames.getOrDefault(table.getZoneId(), "GENERAL") : "GENERAL";
                Paragraph pZone = new Paragraph(zoneName.toUpperCase(), fontZone);
                pZone.setAlignment(Element.ALIGN_CENTER);
                pZone.setSpacingAfter(10f);
                cardContent.addElement(pZone);

                // 2. Table Number
                Paragraph pNumber = new Paragraph("MESA " + table.getTableNumber(), fontTitle);
                pNumber.setAlignment(Element.ALIGN_CENTER);
                pNumber.setSpacingAfter(15f);
                cardContent.addElement(pNumber);

                // 3. QR Code Image
                String qrData = baseUrl + "/qr/" + table.getId();
                Image qrImage = generateQrImage(qrData);
                qrImage.setAlignment(Element.ALIGN_CENTER);
                qrImage.scaleAbsolute(190f, 190f);
                cardContent.addElement(qrImage);

                // 4. Call to Action
                Paragraph pInstr = new Paragraph("Escanea • Elige • Disfruta", fontInstruction);
                pInstr.setAlignment(Element.ALIGN_CENTER);
                pInstr.setSpacingBefore(15f);
                cardContent.addElement(pInstr);

                cardTable.addCell(cardContent);
                cell.addElement(cardTable);
                pdfTable.addCell(cell);
            }

            // Fill empty cells if the last row is incomplete
            if (tables.size() % 2 != 0) {
                PdfPCell emptyCell = new PdfPCell();
                emptyCell.setBorder(Rectangle.NO_BORDER);
                pdfTable.addCell(emptyCell);
            }

            document.add(pdfTable);
            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Error generating multi-zone PDF catalog", e);
        }

        return out.toByteArray();
    }

    private Image generateQrImage(String data) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        // Generate high resolution QR
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 600, 600);
        
        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        
        return Image.getInstance(baos.toByteArray());
    }
}
