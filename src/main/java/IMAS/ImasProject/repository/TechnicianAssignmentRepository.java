package IMAS.ImasProject.repository;



import IMAS.ImasProject.model.TechnicianAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TechnicianAssignmentRepository extends JpaRepository<TechnicianAssignment, Long> {

    // Find assignments by technician ID
    List<TechnicianAssignment> findByTechnicianId(Long technicianId);

    // Find assignments by incident ID
    List<TechnicianAssignment> findByIncidentIncidentId(String incidentId);

    // Find assignments by team ID
    List<TechnicianAssignment> findByTeamId(Long teamId);

    // Find assignments by priority
    List<TechnicianAssignment> findByPriority(String priority);

    // Find assignments with deadline before a certain date
    List<TechnicianAssignment> findByDeadlineBefore(LocalDateTime deadline);

    // Find incomplete assignments (completion percentage < 100)
    @Query("SELECT ta FROM TechnicianAssignment ta WHERE ta.completionPercentage < 100")
    List<TechnicianAssignment> findIncompleteAssignments();

    // Find assignments for a technician that are due soon (within next 24 hours)
    @Query("SELECT ta FROM TechnicianAssignment ta WHERE ta.technician.Id = :technicianId AND ta.deadline BETWEEN :now AND :dayLater")
    List<TechnicianAssignment> findUpcomingAssignmentsForTechnician(
            @Param("technicianId") Long technicianId,
            @Param("now") LocalDateTime now,
            @Param("dayLater") LocalDateTime dayLater
    );

    // Find assignments by assignment type
    List<TechnicianAssignment> findByAssignmentType(String assignmentType);

    // Count assignments by technician ID
    long countByTechnicianId(Long technicianId);

    // Delete assignments by incident ID
    void deleteByIncidentIncidentId(String incidentId);
}
