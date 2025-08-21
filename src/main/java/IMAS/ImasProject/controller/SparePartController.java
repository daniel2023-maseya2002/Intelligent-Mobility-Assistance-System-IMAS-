package IMAS.ImasProject.controller;


import IMAS.ImasProject.model.SparePart;
import IMAS.ImasProject.services.SparePartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/spare-parts")
public class SparePartController {

    private final SparePartService sparePartService;

    @Autowired
    public SparePartController(SparePartService sparePartService) {
        this.sparePartService = sparePartService;
    }

    @GetMapping
    public ResponseEntity<List<SparePart>> getAllSpareParts() {
        List<SparePart> parts = sparePartService.getAllSpareParts();
        return ResponseEntity.ok(parts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SparePart> getSparePartById(@PathVariable Long id) {
        Optional<SparePart> part = sparePartService.getSparePartById(id);
        return part.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/part/{partNumber}")
    public ResponseEntity<SparePart> getSparePartByPartNumber(@PathVariable String partNumber) {
        SparePart part = sparePartService.getSparePartByPartNumber(partNumber);
        return part != null ? ResponseEntity.ok(part) : ResponseEntity.notFound().build();
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<SparePart>> getPartsByCategory(@PathVariable String category) {
        List<SparePart> parts = sparePartService.getPartsByCategory(category);
        return ResponseEntity.ok(parts);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getSparePartCategories() {
        Set<String> categories = sparePartService.getAllCategories();
        return ResponseEntity.ok(categories.stream().sorted().toList());
    }

    @PostMapping
    public ResponseEntity<SparePart> createSparePart(@Valid @RequestBody SparePart sparePart) {
        SparePart savedPart = sparePartService.saveSparePart(sparePart);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPart);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SparePart> updateSparePart(@PathVariable Long id, @Valid @RequestBody SparePart sparePart) {
        Optional<SparePart> existingPart = sparePartService.getSparePartById(id);
        if (existingPart.isPresent()) {
            sparePart.setPartId(id);
            SparePart updatedPart = sparePartService.saveSparePart(sparePart);
            return ResponseEntity.ok(updatedPart);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSparePart(@PathVariable Long id) {
        Optional<SparePart> existingPart = sparePartService.getSparePartById(id);
        if (existingPart.isPresent()) {
            sparePartService.deleteSparePart(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<SparePart>> searchByName(@RequestParam String name) {
        List<SparePart> parts = sparePartService.searchByName(name);
        return ResponseEntity.ok(parts);
    }

    @GetMapping("/supplier/{supplier}")
    public ResponseEntity<List<SparePart>> getPartsBySupplier(@PathVariable String supplier) {
        List<SparePart> parts = sparePartService.getPartsBySupplier(supplier);
        return ResponseEntity.ok(parts);
    }

    @GetMapping("/location/{location}")
    public ResponseEntity<List<SparePart>> getPartsByLocation(@PathVariable String location) {
        List<SparePart> parts = sparePartService.getPartsByLocation(location);
        return ResponseEntity.ok(parts);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<SparePart>> getLowStockParts() {
        List<SparePart> parts = sparePartService.getLowStockParts();
        return ResponseEntity.ok(parts);
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<List<SparePart>> getOutOfStockParts() {
        List<SparePart> parts = sparePartService.getOutOfStockParts();
        return ResponseEntity.ok(parts);
    }

    @PatchMapping("/{id}/decrease-stock")
    public ResponseEntity<String> decreaseStock(@PathVariable Long id, @RequestBody Map<String, Integer> requestBody) {
        Integer amount = requestBody.get("amount");
        if (amount == null || amount <= 0) {
            return ResponseEntity.badRequest().body("Amount must be positive");
        }
        boolean success = sparePartService.decreaseStock(id, amount);
        if (success) {
            return ResponseEntity.ok("Stock decreased successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to decrease stock");
        }
    }

    @PatchMapping("/{id}/increase-stock")
    public ResponseEntity<String> increaseStock(@PathVariable Long id, @RequestBody Map<String, Integer> requestBody) {
        Integer amount = requestBody.get("amount");
        if (amount == null || amount <= 0) {
            return ResponseEntity.badRequest().body("Amount must be positive");
        }
        boolean success = sparePartService.increaseStock(id, amount);
        if (success) {
            return ResponseEntity.ok("Stock increased successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to increase stock");
        }
    }




    @PostMapping("/{id}/order")
    public ResponseEntity<Map<String, String>> orderPart(@PathVariable Long id, @RequestBody Map<String, Integer> requestBody) {
        Integer amount = requestBody.get("amount");
        if (amount == null || amount <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Amount must be positive"));
        }
        String result = sparePartService.orderPart(id, amount);
        return ResponseEntity.ok(Map.of("message", result));
    }
    @GetMapping("/inventory-usage")
    public ResponseEntity<List<SparePart>> getSparePartsForInventoryUsage() {
        List<SparePart> parts = sparePartService.getAllSpareParts();
        return ResponseEntity.ok(parts);
    }
}