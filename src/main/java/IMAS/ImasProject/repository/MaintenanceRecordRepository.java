package IMAS.ImasProject.repository;

import IMAS.ImasProject.model.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {

    // Recherche par ID d'équipement
    List<MaintenanceRecord> findByEquipmentEquipmentId(Long equipmentId);

    // Recherche par priorité
    List<MaintenanceRecord> findByPriority(MaintenanceRecord.Priority priority);

    // Recherche par plage de dates
    List<MaintenanceRecord> findByStartDateBetween(LocalDate startDate, LocalDate endDate);

    // Recherche par date de fin
    List<MaintenanceRecord> findByEndDateBefore(LocalDate endDate);

    // Recherche par date de fin après une date donnée
    List<MaintenanceRecord> findByEndDateAfter(LocalDate endDate);

    // Recherche combinée par équipement et priorité
    List<MaintenanceRecord> findByEquipmentEquipmentIdAndPriority(Long equipmentId, MaintenanceRecord.Priority priority);

    // Recherche par heures estimées
    List<MaintenanceRecord> findByEstimatedHoursLessThanEqual(Integer hours);

    // Recherche par heures estimées supérieures à
    List<MaintenanceRecord> findByEstimatedHoursGreaterThanEqual(Integer hours);

    // Recherche par description contenant un mot-clé
    List<MaintenanceRecord> findByDescriptionContainingIgnoreCase(String keyword);

    // Recherche des maintenances en cours (date de fin nulle ou future)
    @Query("SELECT m FROM MaintenanceRecord m WHERE m.endDate IS NULL OR m.endDate >= CURRENT_DATE")
    List<MaintenanceRecord> findOngoingMaintenance();

    // Recherche des maintenances terminées
    @Query("SELECT m FROM MaintenanceRecord m WHERE m.endDate IS NOT NULL AND m.endDate < CURRENT_DATE")
    List<MaintenanceRecord> findCompletedMaintenance();

    // Recherche par équipement et plage de dates
    @Query("SELECT m FROM MaintenanceRecord m WHERE m.equipment.equipmentId = :equipmentId " +
            "AND m.startDate BETWEEN :startDate AND :endDate")
    List<MaintenanceRecord> findByEquipmentAndDateRange(
            @Param("equipmentId") Long equipmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Requête personnalisée pour recherche avancée
    @Query("SELECT m FROM MaintenanceRecord m WHERE " +
            "(:equipmentId IS NULL OR m.equipment.equipmentId = :equipmentId) AND " +
            "(:priority IS NULL OR m.priority = :priority) AND " +
            "(:startDate IS NULL OR m.startDate >= :startDate) AND " +
            "(:endDate IS NULL OR m.endDate <= :endDate) AND " +
            "(:maxHours IS NULL OR m.estimatedHours <= :maxHours) AND " +
            "(:keyword IS NULL OR LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<MaintenanceRecord> searchMaintenanceRecords(
            @Param("equipmentId") Long equipmentId,
            @Param("priority") MaintenanceRecord.Priority priority,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("maxHours") Integer maxHours,
            @Param("keyword") String keyword);

    // Compter les maintenances par priorité
    @Query("SELECT COUNT(m) FROM MaintenanceRecord m WHERE m.priority = :priority")
    Long countByPriority(@Param("priority") MaintenanceRecord.Priority priority);

    // Obtenir les maintenances les plus récentes
    @Query("SELECT m FROM MaintenanceRecord m ORDER BY m.startDate DESC")
    List<MaintenanceRecord> findAllOrderByStartDateDesc();

    // Recherche des maintenances prévues (date de début future)
    @Query("SELECT m FROM MaintenanceRecord m WHERE m.startDate > CURRENT_DATE")
    List<MaintenanceRecord> findScheduledMaintenance();
}