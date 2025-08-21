// ========== INVENTORY AUDIT SERVICE ==========
package IMAS.ImasProject.services;


import IMAS.ImasProject.model.Inventory;
import IMAS.ImasProject.model.InventoryAlert;
import IMAS.ImasProject.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryAuditService {

    @Autowired
    private InventoryRepository inventoryRepository;

    public List<String> performInventoryAudit() {
        List<String> auditResults = new ArrayList<>();
        List<Inventory> allInventory = inventoryRepository.findAll();

        for (Inventory inventory : allInventory) {
            // Check data consistency
            auditResults.addAll(auditInventoryItem(inventory));

            // Update last audit date
            inventory.setLastAuditDate(LocalDateTime.now());
            inventoryRepository.save(inventory);
        }

        return auditResults;
    }

    private List<String> auditInventoryItem(Inventory inventory) {
        List<String> issues = new ArrayList<>();

        // Check quantity consistency
        if (inventory.getTotalQuantity() !=
                (inventory.getAvailableQuantity() + inventory.getReservedQuantity())) {
            issues.add("Quantity mismatch for " + inventory.getName() +
                    " (ID: " + inventory.getInventoryId() + ")");
        }

        // Check negative quantities
        if (inventory.getTotalQuantity() < 0 || inventory.getAvailableQuantity() < 0 ||
                inventory.getReservedQuantity() < 0) {
            issues.add("Negative quantity detected for " + inventory.getName() +
                    " (ID: " + inventory.getInventoryId() + ")");
        }

        // Check threshold values
        if (inventory.getMinimumThreshold() != null && inventory.getMaximumThreshold() != null) {
            if (inventory.getMinimumThreshold() >= inventory.getMaximumThreshold()) {
                issues.add("Invalid threshold values for " + inventory.getName() +
                        " (ID: " + inventory.getInventoryId() + ")");
            }
        }

        // Check value calculations
        if (inventory.getUnitCost() != null && inventory.getTotalValue() != null) {
            double expectedValue = inventory.getTotalQuantity() * inventory.getUnitCost();
            if (Math.abs(expectedValue - inventory.getTotalValue()) > 0.01) {
                issues.add("Value calculation mismatch for " + inventory.getName() +
                        " (ID: " + inventory.getInventoryId() + ")");
            }
        }

        return issues;
    }

    public void reconcileInventoryWithSpareParts() {
        // This method would synchronize inventory quantities with actual spare part quantities
        List<Inventory> sparePartInventory = inventoryRepository.findByItemType(Inventory.ItemType.SPARE_PART);

        for (Inventory inventory : sparePartInventory) {
            if (inventory.getSparePart() != null) {
                int actualQuantity = inventory.getSparePart().getQuantity();
                // Fixed: Use != operator instead of .equals() for primitive int comparison
                if (actualQuantity != inventory.getTotalQuantity()) {
                    // Create audit alert
                    InventoryAlert alert = new InventoryAlert(inventory,
                            InventoryAlert.AlertType.AUDIT_REQUIRED,
                            "Quantity mismatch with spare part: Inventory=" +
                                    inventory.getTotalQuantity() + ", SparePart=" + actualQuantity);
                    inventory.getAlerts().add(alert);
                    inventoryRepository.save(inventory);
                }
            }
        }
    }
}