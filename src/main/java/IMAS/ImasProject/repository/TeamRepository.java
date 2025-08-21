package IMAS.ImasProject.repository;

import IMAS.ImasProject.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    // Fixed: Using m.id instead of m.staffId
    @Query("SELECT t FROM Team t JOIN t.members m WHERE m.id = :staffId")
    Optional<Team> findFirstByMemberId(@Param("staffId") Long staffId);

    // Alternative using Spring Data derived query method (recommended)
    Optional<Team> findDistinctByMembers_Id(Long memberId);

    // Find team by name
    Optional<Team> findByName(String name);

    // Find teams by name containing string
    List<Team> findByNameContaining(String name);

    // Find all teams that contain a specific member
    @Query("SELECT t FROM Team t JOIN t.members m WHERE m.id = :memberId")
    List<Team> findAllByMemberId(@Param("memberId") Long memberId);

    // Find teams by member email
    @Query("SELECT t FROM Team t JOIN t.members m WHERE m.email = :email")
    List<Team> findByMemberEmail(@Param("email") String email);

    // Find teams by member role
    @Query("SELECT t FROM Team t JOIN t.members m WHERE m.role = :role")
    List<Team> findByMemberRole(@Param("role") String role);

    // Count teams by member count
    @Query("SELECT t FROM Team t WHERE SIZE(t.members) = :memberCount")
    List<Team> findByMemberCount(@Param("memberCount") int memberCount);

    // Find teams with member count greater than specified value
    @Query("SELECT t FROM Team t WHERE SIZE(t.members) > :minCount")
    List<Team> findTeamsWithMemberCountGreaterThan(@Param("minCount") int minCount);

    // Find teams with member count less than specified value
    @Query("SELECT t FROM Team t WHERE SIZE(t.members) < :maxCount")
    List<Team> findTeamsWithMemberCountLessThan(@Param("maxCount") int maxCount);

    // Find active teams (teams with at least one active member)
    @Query("SELECT DISTINCT t FROM Team t JOIN t.members m WHERE m.active = true")
    List<Team> findActiveTeams();

    // Check if a team exists by name (case insensitive)
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Team t WHERE LOWER(t.name) = LOWER(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    // Find teams ordered by member count
    @Query("SELECT t FROM Team t ORDER BY SIZE(t.members) DESC")
    List<Team> findAllOrderByMemberCountDesc();

    // Find teams by name pattern (case insensitive)
    @Query("SELECT t FROM Team t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :pattern, '%'))")
    List<Team> findByNameContainingIgnoreCase(@Param("pattern") String pattern);
}