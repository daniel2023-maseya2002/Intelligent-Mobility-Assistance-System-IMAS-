package IMAS.ImasProject.repository;

import IMAS.ImasProject.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Ticket findByTicketNumber(String ticketNumber);

    List<Ticket> findByPassengerId(Long passengerId);

    // CORRECT: Navigate through the bus relationship using proper JPA syntax
    List<Ticket> findByBus_Id(Long busId);

    // OR use explicit JPQL query (recommended for clarity)
    @Query("SELECT t FROM Ticket t WHERE t.bus.id = :busId")
    List<Ticket> findByBusId(@Param("busId") Long busId);

    // Additional useful queries you might need
    List<Ticket> findByStatus(String status);

    @Query("SELECT t FROM Ticket t WHERE t.bus.id = :busId AND t.status = :status")
    List<Ticket> findByBusIdAndStatus(@Param("busId") Long busId, @Param("status") String status);

    @Query("SELECT t FROM Ticket t WHERE t.driver.id = :driverId")
    List<Ticket> findByDriverId(@Param("driverId") Long driverId);








    List<Ticket> findByStatusAndIssuedAtBetween(String status, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = 'PAID' AND t.issuedAt BETWEEN :start AND :end")
    long countPaidTicketsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = 'BOARDED' AND t.issuedAt BETWEEN :start AND :end")
    long countBoardedTicketsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(CASE WHEN t.hasLuggage = true THEN 2500 ELSE 2000 END) FROM Ticket t WHERE (t.status = 'PAID' OR t.status = 'BOARDED') AND t.issuedAt BETWEEN :start AND :end")
    Double calculateRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT t.passengerId, COUNT(t) as ticketCount FROM Ticket t WHERE (t.status = 'PAID' OR t.status = 'BOARDED') GROUP BY t.passengerId ORDER BY COUNT(t) DESC")
    List<Object[]> findTopCustomersByTicketCount();



    // Revenue analytics queries
    @Query("SELECT DATE(t.issuedAt), SUM(CASE WHEN t.hasLuggage = true THEN 2500 ELSE 2000 END) " +
            "FROM Ticket t WHERE (t.status = 'PAID' OR t.status = 'BOARDED') AND t.issuedAt >= :since " +
            "GROUP BY DATE(t.issuedAt) ORDER BY DATE(t.issuedAt)")
    List<Object[]> getDailyRevenue(@Param("since") LocalDateTime since);

    @Query("SELECT t.bus.name, SUM(CASE WHEN t.hasLuggage = true THEN 2500 ELSE 2000 END) " +
            "FROM Ticket t WHERE (t.status = 'PAID' OR t.status = 'BOARDED') AND t.issuedAt >= :since AND t.bus IS NOT NULL " +
            "GROUP BY t.bus.name ORDER BY SUM(CASE WHEN t.hasLuggage = true THEN 2500 ELSE 2000 END) DESC")
    List<Object[]> getRevenueByBus(@Param("since") LocalDateTime since);

    @Query("SELECT CONCAT(t.driver.firstName, ' ', t.driver.lastName), SUM(CASE WHEN t.hasLuggage = true THEN 2500 ELSE 2000 END) " +
            "FROM Ticket t WHERE (t.status = 'PAID' OR t.status = 'BOARDED') AND t.issuedAt >= :since AND t.driver IS NOT NULL " +
            "GROUP BY t.driver.id ORDER BY SUM(CASE WHEN t.hasLuggage = true THEN 2500 ELSE 2000 END) DESC")
    List<Object[]> getRevenueByDriver(@Param("since") LocalDateTime since);

    @Query("SELECT t.passengerId, t.firstName, t.lastName, COUNT(t), SUM(CASE WHEN t.hasLuggage = true THEN 2500 ELSE 2000 END) " +
            "FROM Ticket t WHERE (t.status = 'PAID' OR t.status = 'BOARDED') " +
            "GROUP BY t.passengerId, t.firstName, t.lastName " +
            "ORDER BY SUM(CASE WHEN t.hasLuggage = true THEN 2500 ELSE 2000 END) DESC")
    List<Object[]> getTopCustomersBySpending();

    @Query("SELECT t.passengerId, t.firstName, t.lastName, COUNT(t), SUM(CASE WHEN t.hasLuggage = true THEN 2500 ELSE 2000 END) " +
            "FROM Ticket t WHERE (t.status = 'PAID' OR t.status = 'BOARDED') " +
            "GROUP BY t.passengerId, t.firstName, t.lastName " +
            "ORDER BY COUNT(t) DESC")
    List<Object[]> getTopCustomersByTickets();
}