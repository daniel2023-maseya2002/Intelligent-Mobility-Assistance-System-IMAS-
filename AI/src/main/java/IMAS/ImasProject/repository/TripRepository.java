package IMAS.ImasProject.repository;

import IMAS.ImasProject.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByDriverId(Long driverId);

    List<Trip> findByDriverIdAndCreatedAtBetween(Long driverId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(t) FROM Trip t WHERE t.driver.id = :driverId")
    long countByDriverId(Long driverId);

    @Query("SELECT COUNT(t) FROM Trip t WHERE t.driver.id = :driverId AND t.onTime = true")
    long countOnTimeTripsByDriverId(Long driverId);

    @Query("SELECT t FROM Trip t WHERE t.bus.id = :busId")
    List<Trip> findByBusId(Long busId);
}