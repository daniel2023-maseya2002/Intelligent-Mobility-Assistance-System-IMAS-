package IMAS.ImasProject.repository;

import IMAS.ImasProject.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {

    // Basic finder methods - Fixed to use 'id' instead of 'staffId'
    List<Task> findByTechnicianId(Long technicianId);
    List<Task> findByIncidentIncidentId(String incidentId);
    List<Task> findByStatus(Task.Status status);

    // Date range methods - Fixed to use 'id' instead of 'staffId'
    List<Task> findByTechnicianIdAndCreatedAtBetween(Long technicianId, LocalDateTime startDate, LocalDateTime endDate);

    List<Task> findByTechnicianIdInAndCreatedAtBetween(List<Long> technicianIds, LocalDateTime startDate, LocalDateTime endDate);

    List<Task> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Statistics methods by status - Fixed queries to use 'id' instead of 'staffId'
    @Query("SELECT COUNT(t) FROM Task t WHERE t.technician.id = :technicianId AND t.status = :status")
    long countByTechnicianIdAndStatus(@Param("technicianId") Long technicianId, @Param("status") Task.Status status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.technician.id = :technicianId AND t.status = :status AND t.createdAt BETWEEN :startDate AND :endDate")
    long countByTechnicianIdAndStatusAndDateBetween(@Param("technicianId") Long technicianId,
                                                    @Param("status") Task.Status status,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    // Statistics methods by priority - Fixed queries to use 'id' instead of 'staffId'
    @Query("SELECT COUNT(t) FROM Task t WHERE t.technician.id = :technicianId AND t.priority = :priority")
    long countByTechnicianIdAndPriority(@Param("technicianId") Long technicianId, @Param("priority") Task.Priority priority);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.technician.id = :technicianId AND t.priority = :priority AND t.createdAt BETWEEN :startDate AND :endDate")
    long countByTechnicianIdAndPriorityAndDateBetween(@Param("technicianId") Long technicianId,
                                                      @Param("priority") Task.Priority priority,
                                                      @Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    // Team statistics - Fixed query to use 'id' instead of 'staffId'
    @Query("SELECT t FROM Task t WHERE t.technician.id IN :memberIds AND t.createdAt BETWEEN :startDate AND :endDate")
    List<Task> findByTeamMembersAndDateBetween(@Param("memberIds") List<Long> memberIds,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    // Average completion time - Fixed query to use 'id' instead of 'staffId'
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, t.createdAt, t.updatedAt)) FROM Task t WHERE t.technician.id = :technicianId AND t.status = 'COMPLETED' AND t.createdAt BETWEEN :startDate AND :endDate")
    Double findAverageCompletionTimeByTechnicianAndDateBetween(@Param("technicianId") Long technicianId,
                                                               @Param("startDate") LocalDateTime startDate,
                                                               @Param("endDate") LocalDateTime endDate);

    // Additional useful methods
    @Query("SELECT t FROM Task t WHERE t.technician.id = :technicianId AND t.status IN :statuses")
    List<Task> findByTechnicianIdAndStatusIn(@Param("technicianId") Long technicianId, @Param("statuses") List<Task.Status> statuses);

    @Query("SELECT t FROM Task t WHERE t.priority = :priority AND t.status = :status")
    List<Task> findByPriorityAndStatus(@Param("priority") Task.Priority priority, @Param("status") Task.Status status);

    @Query("SELECT t FROM Task t WHERE t.deadline < :currentTime AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Task> findOverdueTasks(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT t FROM Task t WHERE t.technician.id = :technicianId AND t.deadline BETWEEN :startDate AND :endDate")
    List<Task> findByTechnicianIdAndDeadlineBetween(@Param("technicianId") Long technicianId,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    // Count methods for dashboard statistics
    @Query("SELECT COUNT(t) FROM Task t WHERE t.technician.id = :technicianId")
    long countByTechnicianId(@Param("technicianId") Long technicianId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = :status")
    long countByStatus(@Param("status") Task.Status status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.priority = :priority")
    long countByPriority(@Param("priority") Task.Priority priority);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Performance analytics
    @Query("SELECT AVG(t.progress) FROM Task t WHERE t.technician.id = :technicianId AND t.status = 'IN_PROGRESS'")
    Double findAverageProgressByTechnicianId(@Param("technicianId") Long technicianId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.technician.id = :technicianId AND t.deadline < CURRENT_TIMESTAMP AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    long countOverdueTasksByTechnicianId(@Param("technicianId") Long technicianId);

    // Workload distribution
    @Query("SELECT t.technician.id, COUNT(t) FROM Task t WHERE t.status IN ('PENDING', 'IN_PROGRESS') GROUP BY t.technician.id")
    List<Object[]> findWorkloadDistribution();

    @Query("SELECT t.priority, COUNT(t) FROM Task t WHERE t.status = :status GROUP BY t.priority")
    List<Object[]> findTaskCountByPriorityAndStatus(@Param("status") Task.Status status);

    // Recent tasks
    @Query("SELECT t FROM Task t WHERE t.technician.id = :technicianId ORDER BY t.createdAt DESC")
    List<Task> findRecentTasksByTechnicianId(@Param("technicianId") Long technicianId);

    // Tasks with specific progress range
    @Query("SELECT t FROM Task t WHERE t.technician.id = :technicianId AND t.progress BETWEEN :minProgress AND :maxProgress")
    List<Task> findByTechnicianIdAndProgressBetween(@Param("technicianId") Long technicianId,
                                                    @Param("minProgress") int minProgress,
                                                    @Param("maxProgress") int maxProgress);
}