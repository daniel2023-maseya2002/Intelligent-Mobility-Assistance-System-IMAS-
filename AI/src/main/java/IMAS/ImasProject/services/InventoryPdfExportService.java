package IMAS.ImasProject.services;


import IMAS.ImasProject.model.Inventory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class InventoryPdfExportService {

    public ByteArrayInputStream exportToPdf(List<Inventory> inventoryItems) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Initialize PDF writer and document
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4.rotate());

        // Title
        Paragraph title = new Paragraph("Inventory Report")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        // Create table with 10 columns
        float[] columnWidths = {1, 2, 1, 1, 1, 1, 1, 1, 1, 1};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        // Table headers
        String[] headers = {
                "Code", "Name", "Type", "Category", "Location",
                "Available/Total", "Status", "Unit Cost", "Total Value", "Actions"
        };

        for (String header : headers) {
            table.addHeaderCell(
                    new Cell().add(new Paragraph(header))
                            .setBold()
                            .setBackgroundColor(new DeviceRgb(211, 211, 211)) // Light gray
            );
        }

        // Table data
        for (Inventory item : inventoryItems) {
            table.addCell(createCell(item.getInventoryCode()));
            table.addCell(createCell(item.getName()));
            table.addCell(createCell(item.getItemType() != null ? item.getItemType().toString() : ""));
            table.addCell(createCell(item.getCategory()));
            table.addCell(createCell(item.getLocation()));

            String availableTotal = (item.getAvailableQuantity() != null && item.getTotalQuantity() != null)
                    ? item.getAvailableQuantity() + "/" + item.getTotalQuantity()
                    : "N/A";
            table.addCell(createCell(availableTotal));

            table.addCell(createCell(item.getStatus() != null ? item.getStatus().toString() : "N/A"));

            String unitCost = item.getUnitCost() != null ? "$" + String.format("%.2f", item.getUnitCost()) : "N/A";
            table.addCell(createCell(unitCost));

            String totalValue = item.getTotalValue() != null ? "$" + String.format("%.2f", item.getTotalValue()) : "N/A";
            table.addCell(createCell(totalValue));

            table.addCell(createCell("")); // Empty cell for actions
        }

        document.add(table);
        document.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    private Cell createCell(String content) {
        return new Cell().add(new Paragraph(content != null ? content : ""));
    }
}