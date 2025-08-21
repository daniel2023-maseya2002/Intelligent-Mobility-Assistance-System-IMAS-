// ========== INVENTORY CONTROLLER ==========
package IMAS.ImasProject.controller;


import IMAS.ImasProject.model.Inventory;
import IMAS.ImasProject.services.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {
        List<Inventory> inventory = inventoryService.getAllInventory();
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventoryById(@PathVariable Long id) {
        Optional<Inventory> inventory = inventoryService.getInventoryById(id);
        return inventory.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Inventory> getInventoryByCode(@PathVariable String code) {
        Optional<Inventory> inventory = inventoryService.getInventoryByCode(code);
        return inventory.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Inventory>> getInventoryByType(@PathVariable Inventory.ItemType type) {
        List<Inventory> inventory = inventoryService.getInventoryByType(type);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Inventory>> getLowStockItems() {
        List<Inventory> inventory = inventoryService.getLowStockItems();
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/overstock")
    public ResponseEntity<List<Inventory>> getOverStockItems() {
        List<Inventory> inventory = inventoryService.getOverStockItems();
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Inventory>> searchInventory(@RequestParam String keyword) {
        List<Inventory> inventory = inventoryService.searchInventory(keyword);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/total-value")
    public ResponseEntity<Double> getTotalInventoryValue() {
        Double totalValue = inventoryService.getTotalInventoryValue();
        return ResponseEntity.ok(totalValue != null ? totalValue : 0.0);
    }

    @PostMapping
    public ResponseEntity<Inventory> createInventory(@RequestBody Inventory inventory) {
        Inventory savedInventory = inventoryService.saveInventory(inventory);
        return ResponseEntity.ok(savedInventory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Inventory> updateInventory(@PathVariable Long id, @RequestBody Inventory inventory) {
        Optional<Inventory> existingInventory = inventoryService.getInventoryById(id);
        if (existingInventory.isPresent()) {
            inventory.setInventoryId(id);
            Inventory updatedInventory = inventoryService.saveInventory(inventory);
            return ResponseEntity.ok(updatedInventory);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        Optional<Inventory> inventory = inventoryService.getInventoryById(id);
        if (inventory.isPresent()) {
            inventoryService.deleteInventory(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Stock Management Endpoints
    @PostMapping("/{id}/add-stock")
    public ResponseEntity<String> addStock(@PathVariable Long id,
                                           @RequestParam int quantity,
                                           @RequestParam String reason,
                                           @RequestParam String performedBy) {
        boolean success = inventoryService.addStock(id, quantity, reason, performedBy);
        if (success) {
            return ResponseEntity.ok("Stock added successfully");
        }
        return ResponseEntity.badRequest().body("Failed to add stock");
    }

    @PostMapping("/{id}/remove-stock")
    public ResponseEntity<String> removeStock(@PathVariable Long id,
                                              @RequestParam int quantity,
                                              @RequestParam String reason,
                                              @RequestParam String performedBy) {
        boolean success = inventoryService.removeStock(id, quantity, reason, performedBy);
        if (success) {
            return ResponseEntity.ok("Stock removed successfully");
        }
        return ResponseEntity.badRequest().body("Failed to remove stock");
    }

    @PostMapping("/{id}/reserve-stock")
    public ResponseEntity<String> reserveStock(@PathVariable Long id,
                                               @RequestParam int quantity,
                                               @RequestParam String reason,
                                               @RequestParam String performedBy) {
        boolean success = inventoryService.reserveStock(id, quantity, reason, performedBy);
        if (success) {
            return ResponseEntity.ok("Stock reserved successfully");
        }
        return ResponseEntity.badRequest().body("Failed to reserve stock");
    }
}
