package IMAS.ImasProject.repository;


import IMAS.ImasProject.model.TechnicianSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnicianSkillRepository extends JpaRepository<TechnicianSkill, Long> {
    List<TechnicianSkill> findByTechnicianId(Long technicianId);
    List<TechnicianSkill> findBySkill(String skill);
    boolean existsByTechnicianIdAndSkill(Long technicianId, String skill);
}