// ========== INVENTORY REPOSITORY ==========
package IMAS.ImasProject.repository;

import IMAS.ImasProject.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByInventoryCode(String inventoryCode);

    List<Inventory> findByItemType(Inventory.ItemType itemType);

    List<Inventory> findByStatus(Inventory.ItemStatus status);

    List<Inventory> findByLocation(String location);

    List<Inventory> findByCategory(String category);

    @Query("SELECT i FROM Inventory i WHERE i.totalQuantity <= i.minimumThreshold")
    List<Inventory> findLowStockItems();

    @Query("SELECT i FROM Inventory i WHERE i.totalQuantity >= i.maximumThreshold")
    List<Inventory> findOverStockItems();

    @Query("SELECT i FROM Inventory i WHERE i.equipment.equipmentId = :equipmentId")
    Optional<Inventory> findByEquipmentId(@Param("equipmentId") Long equipmentId);

    @Query("SELECT i FROM Inventory i WHERE i.sparePart.partId = :sparePartId")
    Optional<Inventory> findBySparePartId(@Param("sparePartId") Long sparePartId);

    @Query("SELECT i FROM Inventory i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Inventory> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT SUM(i.totalValue) FROM Inventory i WHERE i.status = 'ACTIVE'")
    Double getTotalInventoryValue();
}
