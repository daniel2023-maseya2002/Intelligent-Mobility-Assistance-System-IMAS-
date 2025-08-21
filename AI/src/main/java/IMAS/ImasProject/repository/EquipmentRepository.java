package IMAS.ImasProject.repository;


import IMAS.ImasProject.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    // Basic query methods
    List<Equipment> findByName(String name);
    List<Equipment> findByModel(String model);
    List<Equipment> findBySerialNumber(String serialNumber);
    List<Equipment> findByLocation(String location);
    List<Equipment> findByStatus(Equipment.EquipmentStatus status);

    // Custom search method with dynamic criteria
    @Query("SELECT e FROM Equipment e WHERE " +
            "(:name IS NULL OR e.name LIKE %:name%) AND " +
            "(:model IS NULL OR e.model LIKE %:model%) AND " +
            "(:location IS NULL OR e.location LIKE %:location%) AND " +
            "(:status IS NULL OR e.status = :status)")
    List<Equipment> searchEquipments(
            @Param("name") String name,
            @Param("model") String model,
            @Param("location") String location,
            @Param("status") Equipment.EquipmentStatus status);

    // Find equipment needing maintenance based on last maintenance date
    @Query("SELECT e FROM Equipment e WHERE e.lastMaintenanceDate < :thresholdDate OR e.lastMaintenanceDate IS NULL")
    List<Equipment> findEquipmentsNeedingMaintenance(@Param("thresholdDate") LocalDate thresholdDate);

    // Find old equipment that's still operational
    @Query("SELECT e FROM Equipment e WHERE e.installationDate < :thresholdDate AND e.status != 'RETIRED'")
    List<Equipment> findOldEquipmentsStillInService(@Param("thresholdDate") LocalDate thresholdDate);
}