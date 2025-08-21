package IMAS.ImasProject.services;

import IMAS.ImasProject.dto.*;
import IMAS.ImasProject.model.Arrival;
import IMAS.ImasProject.model.ArrivalStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ArrivalService {

    // CRUD operations
    ArrivalDTO createArrival(CreateArrivalDTO createDto);
    ArrivalDTO updateArrival(Long id, UpdateArrivalDTO updateDto);
    ArrivalDTO getArrivalById(Long id);
    Page<ArrivalDTO> getAllArrivals(int page, int size, String sortBy, String sortDirection);
    void deleteArrival(Long id);
    void deactivateArrival(Long id);
    void activateArrival(Long id);

    // Search and filter operations
    Page<ArrivalDTO> searchArrivals(ArrivalSearchCriteria criteria);
    List<ArrivalDTO> getArrivalsByStop(Long stopId);
    List<ArrivalDTO> getArrivalsByVehicle(Long vehicleId);
    List<ArrivalDTO> getArrivalsByStatus(ArrivalStatus status);
    List<ArrivalDTO> getArrivalsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    // Business operations
    ArrivalDTO recordActualArrival(Long id, LocalDateTime arrivalTime);
    ArrivalDTO updateEstimatedArrival(Long id, LocalDateTime estimatedTime);
    ArrivalDTO cancelArrival(Long id, String reason);
    ArrivalDTO uncancelArrival(Long id);
    ArrivalDTO recordPassengerActivity(Long id, Integer boarding, Integer alighting);
    ArrivalDTO updateDistanceFromStop(Long id, Double distance);

    // Query operations
    List<ArrivalDTO> getUpcomingArrivalsByStop(Long stopId);
    List<ArrivalDTO> getDelayedArrivals(Integer delayThreshold);
    List<ArrivalDTO> getArrivalsByStopAndDate(Long stopId, LocalDateTime date);
    List<ArrivalDTO> getArrivalsByVehicleAndDateRange(Long vehicleId, LocalDateTime startDate, LocalDateTime endDate);
    List<ArrivalDTO> getCancelledArrivals();
    List<ArrivalDTO> getArrivalsWithinDistance(Double distance);
    Optional<ArrivalDTO> getNextArrivalByStop(Long stopId);

    // Analytics operations
    ArrivalAnalyticsDTO getArrivalAnalytics(LocalDateTime startDate, LocalDateTime endDate);
    ArrivalAnalyticsDTO getArrivalAnalyticsByStop(Long stopId, LocalDateTime startDate, LocalDateTime endDate);
    ArrivalAnalyticsDTO getArrivalAnalyticsByVehicle(Long vehicleId, LocalDateTime startDate, LocalDateTime endDate);
    Double getAverageDelayByStop(Long stopId, LocalDateTime startDate, LocalDateTime endDate);
    Double getAverageDelayByVehicle(Long vehicleId, LocalDateTime startDate, LocalDateTime endDate);
}
