package IMAS.ImasProject.repository;

import IMAS.ImasProject.model.Staff;
import IMAS.ImasProject.model.StaffRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    // Basic CRUD operations
    boolean existsByEmail(String email);

    Optional<Staff> findByEmail(String email);

    List<Staff> findByRole(StaffRole role);

    List<Staff> findByActive(boolean active);

    List<Staff> findByRoleAndActive(StaffRole role, boolean active);

    // Active staff by role
    @Query("SELECT s FROM Staff s WHERE s.role = :role AND s.active = true")
    List<Staff> findByRoleAndActiveTrue(@Param("role") StaffRole role);

    // Search methods with proper LIKE syntax
    @Query("SELECT s FROM Staff s WHERE " +
            "LOWER(s.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Staff> searchStaff(@Param("searchTerm") String searchTerm);

    // Legacy method for backward compatibility
    @Query("SELECT s FROM Staff s WHERE " +
            "LOWER(s.firstName) LIKE LOWER(:firstName) OR " +
            "LOWER(s.lastName) LIKE LOWER(:lastName) OR " +
            "LOWER(s.email) LIKE LOWER(:email)")
    List<Staff> findByFirstNameLikeOrLastNameLikeOrEmailLike(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("email") String email);

    // Reset token methods
    Optional<Staff> findByResetToken(String resetToken);

    @Query("SELECT s FROM Staff s WHERE s.resetToken = :token AND s.resetTokenExpiration > :currentTime")
    Optional<Staff> findByValidResetToken(@Param("token") String token, @Param("currentTime") LocalDateTime currentTime);

    // Statistics methods
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByActiveTrueAndCreatedAtBefore(LocalDateTime date);

    @Query("SELECT COUNT(s) FROM Staff s WHERE s.active = true AND s.role = :role")
    long countActiveStaffByRole(@Param("role") StaffRole role);

    // Available technicians methods
    @Query("SELECT s FROM Staff s WHERE s.active = true AND s.role = :role")
    List<Staff> findAvailableStaffByRole(@Param("role") StaffRole role);

    // Find technicians with workload limit (assuming you have TaskAssignment entity)
    @Query("SELECT s FROM Staff s WHERE s.active = true AND s.role = :role AND " +
            "(SELECT COUNT(ta) FROM TaskAssignment ta WHERE ta.technician.id = s.id AND " +
            "ta.status IN ('ACCEPTED', 'IN_PROGRESS')) < :maxAssignments")
    List<Staff> findAvailableTechniciansWithWorkloadLimit(@Param("role") StaffRole role,
                                                          @Param("maxAssignments") int maxAssignments);

    // Specific method for technicians only
    @Query("SELECT s FROM Staff s WHERE s.active = true AND s.role = 'TECHNICIAN'")
    List<Staff> findAvailableTechnicians();

    // Find drivers specifically
    @Query("SELECT s FROM Staff s WHERE s.active = true AND s.role = 'DRIVER'")
    List<Staff> findAvailableDrivers();

    // Find staff by multiple roles
    @Query("SELECT s FROM Staff s WHERE s.active = true AND s.role IN :roles")
    List<Staff> findActiveStaffByRoles(@Param("roles") List<StaffRole> roles);

    // Find recently created staff
    @Query("SELECT s FROM Staff s WHERE s.createdAt >= :fromDate ORDER BY s.createdAt DESC")
    List<Staff> findRecentlyCreatedStaff(@Param("fromDate") LocalDateTime fromDate);

    // Find staff by partial name match
    @Query("SELECT s FROM Staff s WHERE " +
            "LOWER(CONCAT(s.firstName, ' ', s.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Staff> findByFullNameContaining(@Param("name") String name);















    List<Staff> findByActiveTrue();

    List<Staff> findByActiveFalse();



    long countByRole(StaffRole role);

    long countByActiveTrue();

    long countByRoleAndActiveTrue(StaffRole role);



    @Query("SELECT s FROM Staff s WHERE s.firstName LIKE %:name% OR s.lastName LIKE %:name%")
    List<Staff> findByNameContaining(@Param("name") String name);

    @Query("SELECT s.password FROM Staff s WHERE s.email = :email")
    String findPasswordByEmail(@Param("email") String email);

    // Analytics queries
    @Query("SELECT COUNT(s) FROM Staff s WHERE s.createdAt >= :since")
    long countStaffCreatedSince(@Param("since") java.time.LocalDateTime since);

    @Query("SELECT s.role, COUNT(s) FROM Staff s GROUP BY s.role")
    List<Object[]> countStaffByRole();

    @Query("SELECT DATE(s.createdAt), COUNT(s) FROM Staff s WHERE s.createdAt >= :since GROUP BY DATE(s.createdAt) ORDER BY DATE(s.createdAt)")
    List<Object[]> getStaffRegistrationTrends(@Param("since") java.time.LocalDateTime since);
}