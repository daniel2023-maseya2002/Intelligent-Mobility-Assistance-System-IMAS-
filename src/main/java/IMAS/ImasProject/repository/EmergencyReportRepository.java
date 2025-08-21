package IMAS.ImasProject.repository;

import IMAS.ImasProject.model.EmergencyReport;
import IMAS.ImasProject.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmergencyReportRepository extends JpaRepository<EmergencyReport, Long> {

    List<EmergencyReport> findByDriverOrderByTimestampDesc(Staff driver);

    List<EmergencyReport> findByDriverIdOrderByTimestampDesc(Long driverId);

    @Query("SELECT er FROM EmergencyReport er WHERE er.driver.id = :driverId AND er.timestamp >= :since ORDER BY er.timestamp DESC")
    List<EmergencyReport> findRecentByDriverId(@Param("driverId") Long driverId, @Param("since") LocalDateTime since);

    @Query("SELECT er FROM EmergencyReport er WHERE er.severity = 'CRITICAL' OR er.severity = 'HIGH' ORDER BY er.timestamp DESC")
    List<EmergencyReport> findHighPriorityReports();

    long countByDriverIdAndTimestampBetween(Long driverId, LocalDateTime start, LocalDateTime end);
}