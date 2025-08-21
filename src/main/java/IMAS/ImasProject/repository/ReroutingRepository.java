package IMAS.ImasProject.repository;

import IMAS.ImasProject.model.Rerouting;
import IMAS.ImasProject.model.ReroutingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReroutingRepository extends JpaRepository<Rerouting, Long> {
    List<Rerouting> findByStatus(ReroutingStatus status);
    List<Rerouting> findAllByOrderByCreatedAtDesc();
}