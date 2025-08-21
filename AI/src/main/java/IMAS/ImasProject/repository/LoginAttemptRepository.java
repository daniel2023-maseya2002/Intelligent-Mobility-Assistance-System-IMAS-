package IMAS.ImasProject.repository;

import IMAS.ImasProject.model.LoginAttempt;
import IMAS.ImasProject.model.StaffRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    List<LoginAttempt> findByEmailOrderByAttemptTimeDesc(String email);

    List<LoginAttempt> findBySuccessfulFalseAndAttemptTimeAfter(LocalDateTime since);

    List<LoginAttempt> findByAttemptTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT la FROM LoginAttempt la ORDER BY la.attemptTime DESC")
    List<LoginAttempt> findTop10ByOrderByAttemptTimeDesc();

    @Query(value = "SELECT * FROM login_attempts ORDER BY attempt_time DESC LIMIT :limit", nativeQuery = true)
    List<LoginAttempt> findTopByOrderByAttemptTimeDesc(@Param("limit") int limit);

    long countBySuccessfulTrueAndAttemptTimeAfter(LocalDateTime since);

    long countBySuccessfulFalseAndAttemptTimeAfter(LocalDateTime since);

    long countBySuccessfulTrueAndAttemptTimeBetween(LocalDateTime start, LocalDateTime end);

    long countByUserRoleAndSuccessfulTrue(StaffRole role);

    long countByUserRoleAndAttemptTimeAfter(StaffRole role, LocalDateTime since);

    long countByUserRoleAndSuccessfulTrueAndAttemptTimeAfter(StaffRole role, LocalDateTime since);

    List<LoginAttempt> findByUserRoleAndAttemptTimeAfterOrderByAttemptTimeDesc(StaffRole role, LocalDateTime since);

    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.successful = true AND la.attemptTime >= :start AND la.attemptTime <= :end")
    long countSuccessfulLoginsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT la.ipAddress, COUNT(la) as count, " +
            "SUM(CASE WHEN la.successful = true THEN 1 ELSE 0 END) as successful " +
            "FROM LoginAttempt la WHERE la.attemptTime >= :since " +
            "GROUP BY la.ipAddress ORDER BY count DESC")
    List<Object[]> findLoginStatsByIPAddress(@Param("since") LocalDateTime since);

    @Query(value = "SELECT la.email, COUNT(*) as count, " +
            "SUM(CASE WHEN la.successful = true THEN 1 ELSE 0 END) as successful " +
            "FROM login_attempts la WHERE la.attempt_time >= :since " +
            "GROUP BY la.email ORDER BY count DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findMostActiveUsers(@Param("since") LocalDateTime since, @Param("limit") int limit);
}