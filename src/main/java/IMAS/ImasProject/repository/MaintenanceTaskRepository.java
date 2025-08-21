package IMAS.ImasProject.repository;

import IMAS.ImasProject.model.MaintenanceTask;
import IMAS.ImasProject.model.Staff;
import IMAS.ImasProject.model.StaffRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceTaskRepository extends JpaRepository<MaintenanceTask, String> {

    // Primary key queries
    Optional<MaintenanceTask> findByTaskId(String taskId);

    // Status-based queries
    List<MaintenanceTask> findByStatus(MaintenanceTask.Status status);
    List<MaintenanceTask> findByStatusIn(List<MaintenanceTask.Status> statuses);
    long countByStatus(MaintenanceTask.Status status);

    // Priority-based queries
    List<MaintenanceTask> findByPriority(MaintenanceTask.Priority priority);
    List<MaintenanceTask> findByPriorityIn(List<MaintenanceTask.Priority> priorities);

    // Technician-based queries (using Long for assignedTechnician field)
    List<MaintenanceTask> findByAssignedTechnician(Long technicianId);
    long countByAssignedTechnician(Long technicianId);

    // Staff-based queries (using Staff entity relationship)
    List<MaintenanceTask> findByAssignedTechnicianStaff(Staff staff);
    List<MaintenanceTask> findByAssignedTechnicianStaff_Id(Long staffId);

    @Query("SELECT t FROM MaintenanceTask t WHERE t.assignedTechnicianStaff.id = :staffId")
    List<MaintenanceTask> findByAssignedTechnicianStaffId(@Param("staffId") Long staffId);

    // Skills and parts queries
    @Query("SELECT t FROM MaintenanceTask t JOIN t.requiredSkills s WHERE s = :skill")
    List<MaintenanceTask> findByRequiredSkillsContaining(@Param("skill") String skill);

    @Query("SELECT t FROM MaintenanceTask t JOIN t.requiredParts p WHERE p = :part")
    List<MaintenanceTask> findByRequiredPartsContaining(@Param("part") String part);

    // Equipment-related queries
    List<MaintenanceTask> findByRelatedEquipment_EquipmentId(Long equipmentId);

    @Query("SELECT mt FROM MaintenanceTask mt WHERE mt.relatedEquipment.equipmentId = :equipmentId")
    List<MaintenanceTask> findByEquipmentId(@Param("equipmentId") Long equipmentId);

    @Query("SELECT mt FROM MaintenanceTask mt WHERE mt.relatedEquipment.equipmentId = :equipmentId AND mt.status = :status")
    List<MaintenanceTask> findByEquipmentIdAndStatus(@Param("equipmentId") Long equipmentId,
                                                     @Param("status") MaintenanceTask.Status status);

    // Date-based queries
    List<MaintenanceTask> findByDueDateBefore(LocalDateTime date);
    List<MaintenanceTask> findByDueDateAfter(LocalDateTime date);
    List<MaintenanceTask> findByDueDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<MaintenanceTask> findByDueDateBeforeAndStatusNot(LocalDateTime date, MaintenanceTask.Status status);
    List<MaintenanceTask> findByDueDateBetweenAndStatusNot(LocalDateTime startDate, LocalDateTime endDate, MaintenanceTask.Status status);

    List<MaintenanceTask> findByCreationDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<MaintenanceTask> findByLastUpdatedAfter(LocalDateTime date);
    List<MaintenanceTask> findByLastUpdatedBefore(LocalDateTime date);
    List<MaintenanceTask> findByLastUpdatedBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Complex queries for unassigned tasks
    @Query("SELECT mt FROM MaintenanceTask mt WHERE mt.assignedTechnician IS NULL")
    List<MaintenanceTask> findUnassignedTasks();

    @Query("SELECT mt FROM MaintenanceTask mt WHERE mt.assignedTechnicianStaff IS NULL")
    List<MaintenanceTask> findUnassignedStaffTasks();

    @Query("SELECT mt FROM MaintenanceTask mt WHERE mt.assignedTechnicianStaff IS NULL AND :skill MEMBER OF mt.requiredSkills")
    List<MaintenanceTask> findUnassignedTasksRequiringSkill(@Param("skill") String skill);

    @Query("SELECT mt FROM MaintenanceTask mt WHERE mt.status IN :statuses AND mt.priority = :priority")
    List<MaintenanceTask> findByStatusInAndPriority(@Param("statuses") List<MaintenanceTask.Status> statuses,
                                                    @Param("priority") MaintenanceTask.Priority priority);

    // Technician-specific queries with direct field access
    @Query("SELECT t FROM MaintenanceTask t WHERE t.assignedTechnician = :technicianId")
    List<MaintenanceTask> findByAssignedTechnicianId(@Param("technicianId") Long technicianId);

    @Query("SELECT t FROM MaintenanceTask t WHERE t.assignedTechnician = :technicianId AND t.status = :status")
    List<MaintenanceTask> findByAssignedTechnicianIdAndStatus(@Param("technicianId") Long technicianId,
                                                              @Param("status") MaintenanceTask.Status status);

    @Query("SELECT t FROM MaintenanceTask t WHERE t.assignedTechnician = :technicianId AND t.status IN :statuses")
    List<MaintenanceTask> findByAssignedTechnicianIdAndStatusIn(@Param("technicianId") Long technicianId,
                                                                @Param("statuses") List<MaintenanceTask.Status> statuses);

    @Query("SELECT t FROM MaintenanceTask t WHERE t.assignedTechnician = :technicianId AND t.priority = :priority")
    List<MaintenanceTask> findByAssignedTechnicianIdAndPriority(@Param("technicianId") Long technicianId,
                                                                @Param("priority") MaintenanceTask.Priority priority);

    // Ordered queries
    @Query("SELECT t FROM MaintenanceTask t WHERE t.assignedTechnician = :technicianId " +
            "ORDER BY t.priority DESC, t.creationDate ASC")
    List<MaintenanceTask> findByAssignedTechnicianIdOrderByPriorityAndDate(@Param("technicianId") Long technicianId);

    @Query("SELECT t FROM MaintenanceTask t WHERE t.status = :status ORDER BY t.dueDate ASC")
    List<MaintenanceTask> findByStatusOrderByDueDateAsc(@Param("status") MaintenanceTask.Status status);

    @Query("SELECT t FROM MaintenanceTask t WHERE t.status = :status ORDER BY t.priority DESC, t.dueDate ASC")
    List<MaintenanceTask> findByStatusOrderByPriorityDescDueDateAsc(@Param("status") MaintenanceTask.Status status);

    // Count queries
    @Query("SELECT COUNT(t) FROM MaintenanceTask t WHERE t.assignedTechnician = :technicianId AND t.status = :status")
    Long countByAssignedTechnicianIdAndStatus(@Param("technicianId") Long technicianId,
                                              @Param("status") MaintenanceTask.Status status);

    @Query("SELECT COUNT(t) FROM MaintenanceTask t WHERE t.assignedTechnicianStaff.id = :staffId AND t.status = :status")
    Long countByAssignedTechnicianStaffIdAndStatus(@Param("staffId") Long staffId,
                                                   @Param("status") MaintenanceTask.Status status);

    // Completion percentage queries
    @Query("SELECT mt FROM MaintenanceTask mt WHERE mt.completionPercentage BETWEEN :minPercentage AND :maxPercentage")
    List<MaintenanceTask> findByCompletionPercentageBetween(@Param("minPercentage") double minPercentage,
                                                            @Param("maxPercentage") double maxPercentage);

    @Query("SELECT mt FROM MaintenanceTask mt WHERE mt.completionPercentage >= :percentage")
    List<MaintenanceTask> findByCompletionPercentageGreaterThanEqual(@Param("percentage") double percentage);

    // Staff-related queries (using Staff entity relationship with existing attributes only)
    @Query("SELECT t FROM MaintenanceTask t WHERE t.assignedTechnicianStaff.email = :email")
    List<MaintenanceTask> findByAssignedTechnicianEmail(@Param("email") String email);

    @Query("SELECT t FROM MaintenanceTask t WHERE t.assignedTechnicianStaff.firstName = :firstName AND t.assignedTechnicianStaff.lastName = :lastName")
    List<MaintenanceTask> findByAssignedTechnicianName(@Param("firstName") String firstName, @Param("lastName") String lastName);

    // FIXED: Changed from 'department' to 'role' which exists in Staff entity
    @Query("SELECT t FROM MaintenanceTask t WHERE t.assignedTechnicianStaff.role = :role")
    List<MaintenanceTask> findByAssignedTechnicianRole(@Param("role") StaffRole role);

    // Additional staff role queries
    @Query("SELECT t FROM MaintenanceTask t WHERE t.assignedTechnicianStaff.role IN :roles")
    List<MaintenanceTask> findByAssignedTechnicianRoleIn(@Param("roles") List<StaffRole> roles);

    @Query("SELECT t FROM MaintenanceTask t WHERE t.assignedTechnicianStaff.active = :active")
    List<MaintenanceTask> findByAssignedTechnicianActiveStatus(@Param("active") boolean active);

    // Time-based analysis queries
    @Query("SELECT t FROM MaintenanceTask t WHERE t.assignedTechnician = :technicianId AND t.dueDate < :currentDate AND t.status != :completedStatus")
    List<MaintenanceTask> findOverdueTasksByTechnicianId(@Param("technicianId") Long technicianId,
                                                         @Param("currentDate") LocalDateTime currentDate,
                                                         @Param("completedStatus") MaintenanceTask.Status completedStatus);

    @Query("SELECT t FROM MaintenanceTask t WHERE t.assignedTechnician = :technicianId AND t.dueDate BETWEEN :startDate AND :endDate")
    List<MaintenanceTask> findByAssignedTechnicianIdAndDueDateBetween(@Param("technicianId") Long technicianId,
                                                                      @Param("startDate") LocalDateTime startDate,
                                                                      @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM MaintenanceTask t WHERE t.dueDate < :currentDate AND t.status IN :activeStatuses")
    List<MaintenanceTask> findOverdueTasks(@Param("currentDate") LocalDateTime currentDate,
                                           @Param("activeStatuses") List<MaintenanceTask.Status> activeStatuses);

    // Advanced analytics queries
    @Query("SELECT t.priority, COUNT(t) FROM MaintenanceTask t WHERE t.assignedTechnician = :technicianId GROUP BY t.priority")
    List<Object[]> getTaskCountByPriorityForTechnician(@Param("technicianId") Long technicianId);

    @Query("SELECT t.status, COUNT(t) FROM MaintenanceTask t WHERE t.assignedTechnician = :technicianId GROUP BY t.status")
    List<Object[]> getTaskCountByStatusForTechnician(@Param("technicianId") Long technicianId);

    @Query("SELECT DATE(t.creationDate), COUNT(t) FROM MaintenanceTask t WHERE t.creationDate BETWEEN :startDate AND :endDate GROUP BY DATE(t.creationDate)")
    List<Object[]> getTaskCreationCountByDate(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    // Search queries - using description field only since title doesn't exist
    @Query("SELECT t FROM MaintenanceTask t WHERE " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<MaintenanceTask> searchTasksByDescription(@Param("searchTerm") String searchTerm);

    @Query("SELECT t FROM MaintenanceTask t WHERE " +
            "t.assignedTechnician = :technicianId AND " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<MaintenanceTask> searchTasksByTechnicianAndDescription(@Param("technicianId") Long technicianId,
                                                                @Param("searchTerm") String searchTerm);

    // Utility queries for dashboard and reporting
    @Query("SELECT COUNT(t) FROM MaintenanceTask t WHERE t.dueDate BETWEEN :startDate AND :endDate")
    Long countTasksDueBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT AVG(t.completionPercentage) FROM MaintenanceTask t WHERE t.assignedTechnician = :technicianId")
    Double getAverageCompletionPercentageForTechnician(@Param("technicianId") Long technicianId);

    @Query("SELECT t FROM MaintenanceTask t WHERE t.priority = :priority AND t.status = :status ORDER BY t.dueDate ASC")
    List<MaintenanceTask> findHighPriorityTasksByStatus(@Param("priority") MaintenanceTask.Priority priority,
                                                        @Param("status") MaintenanceTask.Status status);

    // Additional useful queries based on the entity structure
    @Query("SELECT t FROM MaintenanceTask t WHERE t.completionDate IS NOT NULL AND t.completionDate BETWEEN :startDate AND :endDate")
    List<MaintenanceTask> findCompletedTasksBetween(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM MaintenanceTask t WHERE t.status = 'IN_PROGRESS' AND t.lastUpdated < :cutoffDate")
    List<MaintenanceTask> findStaleInProgressTasks(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Queries for equipment maintenance scheduling
    @Query("SELECT mt FROM MaintenanceTask mt WHERE mt.relatedEquipment.equipmentId = :equipmentId " +
            "AND mt.status IN ('PLANNED', 'SCHEDULED', 'ASSIGNED') ORDER BY mt.dueDate ASC")
    List<MaintenanceTask> findUpcomingTasksForEquipment(@Param("equipmentId") Long equipmentId);

    // Workload balancing queries
    @Query("SELECT t.assignedTechnician, COUNT(t) FROM MaintenanceTask t " +
            "WHERE t.status IN ('ASSIGNED', 'IN_PROGRESS') " +
            "GROUP BY t.assignedTechnician ORDER BY COUNT(t) DESC")
    List<Object[]> getTechnicianWorkloadDistribution();

    @Query("SELECT t FROM MaintenanceTask t WHERE t.assignedTechnician IS NULL " +
            "AND t.status IN ('PLANNED', 'PENDING') ORDER BY t.priority DESC, t.dueDate ASC")
    List<MaintenanceTask> findUnassignedTasksOrderedByPriorityAndDueDate();

    // Additional queries by staff role (useful for filtering technicians by their role)
    @Query("SELECT t FROM MaintenanceTask t WHERE t.assignedTechnicianStaff.role = :role AND t.status = :status")
    List<MaintenanceTask> findByAssignedTechnicianRoleAndStatus(@Param("role") StaffRole role,
                                                                @Param("status") MaintenanceTask.Status status);

    @Query("SELECT COUNT(t) FROM MaintenanceTask t WHERE t.assignedTechnicianStaff.role = :role")
    Long countByAssignedTechnicianRole(@Param("role") StaffRole role);

    // Query to get tasks assigned to active staff members only
    @Query("SELECT t FROM MaintenanceTask t WHERE t.assignedTechnicianStaff.active = true AND t.status IN :statuses")
    List<MaintenanceTask> findTasksAssignedToActiveStaff(@Param("statuses") List<MaintenanceTask.Status> statuses);

    // Query to find tasks assigned to staff created within a date range
    @Query("SELECT t FROM MaintenanceTask t WHERE t.assignedTechnicianStaff.createdAt BETWEEN :startDate AND :endDate")
    List<MaintenanceTask> findTasksAssignedToStaffCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                                                 @Param("endDate") LocalDateTime endDate);
}