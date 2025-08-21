package IMAS.ImasProject.repository;

import IMAS.ImasProject.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {


    @Query("SELECT s FROM Schedule s " +
            "WHERE s.technician.id = :technicianId " +
            "AND ((s.startTime < :endTime AND s.endTime > :startTime)) " +
            "AND s.id != :excludeId")
    List<Schedule> findConflictingSchedulesExcludingCurrent(@Param("technicianId") Long technicianId,
                                                            @Param("startTime") LocalDateTime startTime,
                                                            @Param("endTime") LocalDateTime endTime,
                                                            @Param("excludeId") Long excludeId);

    @Query("SELECT s FROM Schedule s " +
            "LEFT JOIN FETCH s.technician " +
            "LEFT JOIN FETCH s.task " +
            "WHERE s.startTime >= :today " +
            "ORDER BY s.startTime")
    List<Schedule> findUpcomingSchedules(@Param("today") LocalDateTime today);

    @Query("SELECT s FROM Schedule s " +
            "LEFT JOIN FETCH s.technician " +
            "LEFT JOIN FETCH s.task " +
            "WHERE s.technician.id = :technicianId " +
            "AND DATE(s.startTime) = DATE(:date)")
    List<Schedule> findByTechnicianAndDate(@Param("technicianId") Long technicianId,
                                           @Param("date") LocalDateTime date);

    @Query("SELECT COUNT(s) FROM Schedule s WHERE s.technician.id = :technicianId " +
            "AND s.startTime BETWEEN :weekStart AND :weekEnd")
    Long countSchedulesByTechnicianInWeek(@Param("technicianId") Long technicianId,
                                          @Param("weekStart") LocalDateTime weekStart,
                                          @Param("weekEnd") LocalDateTime weekEnd);






    // Add this missing method that's called in ScheduleService.createScheduleFromDTO()
    @Query("SELECT s FROM Schedule s " +
            "WHERE s.technician.id = :technicianId " +
            "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
    List<Schedule> findConflictingSchedules(@Param("technicianId") Long technicianId,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);
    @Query("SELECT s FROM Schedule s LEFT JOIN FETCH s.technician LEFT JOIN FETCH s.task LEFT JOIN FETCH s.task.relatedEquipment WHERE s.technician.id = :technicianId ORDER BY s.startTime")
    List<Schedule> findByTechnicianIdWithDetails(@Param("technicianId") Long technicianId);

// Also check all other similar queries in your ScheduleRepository and make the same change:

    // findAllWithDetails - change from s.task.equipment to s.task.relatedEquipment
    @Query("SELECT s FROM Schedule s LEFT JOIN FETCH s.technician LEFT JOIN FETCH s.task LEFT JOIN FETCH s.task.relatedEquipment ORDER BY s.startTime")
    List<Schedule> findAllWithDetails();

    // findByIdWithDetails - change from s.task.equipment to s.task.relatedEquipment
    @Query("SELECT s FROM Schedule s LEFT JOIN FETCH s.technician LEFT JOIN FETCH s.task LEFT JOIN FETCH s.task.relatedEquipment WHERE s.id = :id")
    Optional<Schedule> findByIdWithDetails(@Param("id") Long id);

    // findByTaskIdWithDetails - change from s.task.equipment to s.task.relatedEquipment
    @Query("SELECT s FROM Schedule s LEFT JOIN FETCH s.technician LEFT JOIN FETCH s.task LEFT JOIN FETCH s.task.relatedEquipment WHERE s.taskId = :taskId ORDER BY s.startTime")
    List<Schedule> findByTaskIdWithDetails(@Param("taskId") String taskId);

    // findByStartTimeBetweenWithDetails - change from s.task.equipment to s.task.relatedEquipment
    @Query("SELECT s FROM Schedule s LEFT JOIN FETCH s.technician LEFT JOIN FETCH s.task LEFT JOIN FETCH s.task.relatedEquipment WHERE s.startTime BETWEEN :startTime AND :endTime ORDER BY s.startTime")
    List<Schedule> findByStartTimeBetweenWithDetails(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    // findByStatusWithDetails - change from s.task.equipment to s.task.relatedEquipment
    @Query("SELECT s FROM Schedule s LEFT JOIN FETCH s.technician LEFT JOIN FETCH s.task LEFT JOIN FETCH s.task.relatedEquipment WHERE s.status = :status ORDER BY s.startTime")
    List<Schedule> findByStatusWithDetails(@Param("status") Schedule.Status status);
}