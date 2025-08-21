package IMAS.ImasProject.controller;


import IMAS.ImasProject.model.Inventory;
import IMAS.ImasProject.services.InventoryPdfExportService;
import IMAS.ImasProject.services.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryExportController {

    private final InventoryService inventoryService;
    private final InventoryPdfExportService pdfExportService;

    @Autowired
    public InventoryExportController(InventoryService inventoryService,
                                     InventoryPdfExportService pdfExportService) {
        this.inventoryService = inventoryService;
        this.pdfExportService = pdfExportService;
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<InputStreamResource> exportToPdf() {
        try {
            List<Inventory> inventoryItems = inventoryService.getAllInventory(); // Using correct method name
            ByteArrayInputStream bis = pdfExportService.exportToPdf(inventoryItems);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=inventory_report.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(bis));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}