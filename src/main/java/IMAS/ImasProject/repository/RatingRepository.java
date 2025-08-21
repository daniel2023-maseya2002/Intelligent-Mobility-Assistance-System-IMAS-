package IMAS.ImasProject.repository;

import IMAS.ImasProject.model.Rating;
import IMAS.ImasProject.model.RatingType;
import IMAS.ImasProject.model.Staff;
import IMAS.ImasProject.dto.RatingDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    // Méthode pour vérifier les ratings existants
    @Query("SELECT r FROM Rating r WHERE r.passenger.id = :passengerId " +
            "AND r.ratingType = :ratingType " +
            "AND (:busId IS NULL OR r.bus.id = :busId) " +
            "AND (:driverId IS NULL OR r.driver.id = :driverId) " +
            "AND (:routeId IS NULL OR r.route.id = :routeId)")
    Optional<Rating> findExistingRating(@Param("passengerId") Long passengerId,
                                        @Param("ratingType") RatingType ratingType,
                                        @Param("busId") Long busId,
                                        @Param("driverId") Long driverId,
                                        @Param("routeId") Long routeId);

    // Alternative plus simple
    Optional<Rating> findByPassengerIdAndRatingType(Long passengerId, RatingType ratingType);

    // Méthodes de base
    Page<Rating> findByPassengerId(Long passengerId, Pageable pageable);

    Page<Rating> findByPassengerOrderByCreatedAtDesc(Staff passenger, Pageable pageable);

    Page<Rating> findByBusIdOrderByCreatedAtDesc(Long busId, Pageable pageable);

    Page<Rating> findByDriverIdOrderByCreatedAtDesc(Long driverId, Pageable pageable);

    Page<Rating> findByRouteIdOrderByCreatedAtDesc(Long routeId, Pageable pageable);

    Page<Rating> findByRatingTypeOrderByCreatedAtDesc(RatingType ratingType, Pageable pageable);

    // Méthodes de comptage - FIXED: Use correct property names
    Long countByRating(Integer rating);

    // FIXED: Correct method name to match entity properties
    Long countByRatingAndRatingType(Integer rating, RatingType ratingType);

    // Méthodes de statistiques
    @Query("SELECT new IMAS.ImasProject.dto.RatingDTO$RatingStatistics(AVG(r.rating), COUNT(r)) FROM Rating r")
    RatingDTO.RatingStatistics getRatingStatistics();

    @Query("SELECT new IMAS.ImasProject.dto.RatingDTO$RatingStatistics(AVG(r.rating), COUNT(r)) FROM Rating r WHERE r.ratingType = :ratingType")
    RatingDTO.RatingStatistics getRatingStatisticsByType(@Param("ratingType") RatingType ratingType);

    @Query("SELECT new IMAS.ImasProject.dto.RatingDTO$RatingStatistics(AVG(r.rating), COUNT(r)) FROM Rating r WHERE r.bus.id = :busId")
    RatingDTO.RatingStatistics getRatingStatisticsByBus(@Param("busId") Long busId);

    @Query("SELECT new IMAS.ImasProject.dto.RatingDTO$RatingStatistics(AVG(r.rating), COUNT(r)) FROM Rating r WHERE r.driver.id = :driverId")
    RatingDTO.RatingStatistics getRatingStatisticsByDriver(@Param("driverId") Long driverId);

    @Query("SELECT new IMAS.ImasProject.dto.RatingDTO$RatingStatistics(AVG(r.rating), COUNT(r)) FROM Rating r WHERE r.route.id = :routeId")
    RatingDTO.RatingStatistics getRatingStatisticsByRoute(@Param("routeId") Long routeId);

    // Méthodes de recherche avancée
    @Query("SELECT r FROM Rating r WHERE " +
            "(:passengerId IS NULL OR r.passenger.id = :passengerId) AND " +
            "(:busId IS NULL OR r.bus.id = :busId) AND " +
            "(:driverId IS NULL OR r.driver.id = :driverId) AND " +
            "(:routeId IS NULL OR r.route.id = :routeId) AND " +
            "(:ratingType IS NULL OR r.ratingType = :ratingType) AND " +
            "(:minRating IS NULL OR r.rating >= :minRating) AND " +
            "(:maxRating IS NULL OR r.rating <= :maxRating)")
    Page<Rating> findRatingsByCriteria(@Param("passengerId") Long passengerId,
                                       @Param("busId") Long busId,
                                       @Param("driverId") Long driverId,
                                       @Param("routeId") Long routeId,
                                       @Param("ratingType") RatingType ratingType,
                                       @Param("minRating") Integer minRating,
                                       @Param("maxRating") Integer maxRating,
                                       Pageable pageable);

    // Ratings récents
    @Query("SELECT r FROM Rating r WHERE r.createdAt >= :since ORDER BY r.createdAt DESC")
    Page<Rating> findRecentRatings(@Param("since") LocalDateTime since, Pageable pageable);

    // Moyennes par entité
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.bus.id = :busId")
    Optional<Double> findAverageRatingByBus(@Param("busId") Long busId);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.driver.id = :driverId")
    Optional<Double> findAverageRatingByDriver(@Param("driverId") Long driverId);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.route.id = :routeId")
    Optional<Double> findAverageRatingByRoute(@Param("routeId") Long routeId);

    // Top rated entities
    @Query("SELECT r.bus.id, r.bus.name, AVG(r.rating) as avgRating, COUNT(r) as totalRatings " +
            "FROM Rating r WHERE r.bus IS NOT NULL " +
            "GROUP BY r.bus.id, r.bus.name " +
            "ORDER BY avgRating DESC")
    List<Object[]> getTopRatedBuses(Pageable pageable);

    @Query("SELECT r.driver.id, CONCAT(r.driver.firstName, ' ', r.driver.lastName), AVG(r.rating) as avgRating, COUNT(r) as totalRatings " +
            "FROM Rating r WHERE r.driver IS NOT NULL " +
            "GROUP BY r.driver.id, r.driver.firstName, r.driver.lastName " +
            "ORDER BY avgRating DESC")
    List<Object[]> getTopRatedDrivers(Pageable pageable);

    @Query("SELECT r.route.id, r.route.routeName, AVG(r.rating) as avgRating, COUNT(r) as totalRatings " +
            "FROM Rating r WHERE r.route IS NOT NULL " +
            "GROUP BY r.route.id, r.route.routeName " +
            "ORDER BY avgRating DESC")
    List<Object[]> getTopRatedRoutes(Pageable pageable);

    // Summary by type
    @Query("SELECT new IMAS.ImasProject.dto.RatingDTO$RatingTypeSummary(r.ratingType, AVG(r.rating), COUNT(r)) " +
            "FROM Rating r GROUP BY r.ratingType")
    List<RatingDTO.RatingTypeSummary> getRatingsSummaryByType();

    // Nettoyage des anciens ratings
    void deleteByCreatedAtBefore(LocalDateTime cutoffDate);
}