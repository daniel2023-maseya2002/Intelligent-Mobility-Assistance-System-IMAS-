package IMAS.ImasProject.repository;


import IMAS.ImasProject.model.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {

    // Correction: utiliser technician.id au lieu de technicianId
    @Query("SELECT ta FROM TaskAssignment ta WHERE ta.technician.id = :technicianId")
    List<TaskAssignment> findByTechnicianId(@Param("technicianId") Long technicianId);

    @Query("SELECT ta FROM TaskAssignment ta WHERE ta.technician.id = :technicianId AND ta.status = :status")
    List<TaskAssignment> findByTechnicianIdAndStatus(@Param("technicianId") Long technicianId,
                                                     @Param("status") TaskAssignment.AssignmentStatus status);

    @Query("SELECT ta FROM TaskAssignment ta WHERE ta.technician.id = :technicianId AND ta.status IN :statuses")
    List<TaskAssignment> findByTechnicianIdAndStatusIn(@Param("technicianId") Long technicianId,
                                                       @Param("statuses") List<TaskAssignment.AssignmentStatus> statuses);

    // Correction: utiliser task.taskId au lieu de taskId
    @Query("SELECT ta FROM TaskAssignment ta WHERE ta.task.taskId = :taskId")
    List<TaskAssignment> findByTaskId(@Param("taskId") String taskId);

    // Ces méthodes sont correctes car elles référencent directement les champs de TaskAssignment
    List<TaskAssignment> findByAssignedAtBetween(LocalDateTime start, LocalDateTime end);
    List<TaskAssignment> findByCompletedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(ta) FROM TaskAssignment ta WHERE ta.technician.id = :technicianId AND ta.status IN ('ACCEPTED', 'IN_PROGRESS')")
    int countActiveAssignmentsByTechnician(@Param("technicianId") Long technicianId);

    @Query("SELECT ta FROM TaskAssignment ta WHERE ta.status = 'PENDING_ACCEPTANCE' ORDER BY ta.assignedAt ASC")
    List<TaskAssignment> findPendingAssignments();

    @Query("SELECT ta FROM TaskAssignment ta WHERE ta.technician.id = :technicianId AND ta.status IN ('ACCEPTED', 'IN_PROGRESS') ORDER BY ta.assignedAt DESC")
    List<TaskAssignment> findActiveAssignmentsByTechnician(@Param("technicianId") Long technicianId);

    // Méthodes supplémentaires utiles avec les bonnes références
    @Query("SELECT ta FROM TaskAssignment ta WHERE ta.task.taskId = :taskId AND ta.status = :status")
    List<TaskAssignment> findByTaskIdAndStatus(@Param("taskId") String taskId,
                                               @Param("status") TaskAssignment.AssignmentStatus status);

    @Query("SELECT ta FROM TaskAssignment ta WHERE ta.technician.id = :technicianId AND ta.task.taskId = :taskId")
    List<TaskAssignment> findByTechnicianIdAndTaskId(@Param("technicianId") Long technicianId,
                                                     @Param("taskId") String taskId);
}