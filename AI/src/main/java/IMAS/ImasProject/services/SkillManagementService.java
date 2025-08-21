package IMAS.ImasProject.services;


import IMAS.ImasProject.exception.ResourceNotFoundException;
import IMAS.ImasProject.model.Staff;
import IMAS.ImasProject.model.TechnicianSkill;
import IMAS.ImasProject.repository.StaffRepository;
import IMAS.ImasProject.repository.TechnicianSkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkillManagementService {

    @Autowired
    private TechnicianSkillRepository skillRepository;

    @Autowired
    private StaffRepository staffRepository;

    public TechnicianSkill addSkillToTechnician(Long technicianId, String skill,
                                                TechnicianSkill.ProficiencyLevel proficiency) {
        Staff technician = staffRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found"));

        TechnicianSkill technicianSkill = new TechnicianSkill(technician, skill, proficiency);
        return skillRepository.save(technicianSkill);
    }

    public void removeSkillFromTechnician(Long skillId) {
        skillRepository.deleteById(skillId);
    }

    public List<TechnicianSkill> getTechnicianSkills(Long technicianId) {
        return skillRepository.findByTechnicianId(technicianId);
    }

    public List<TechnicianSkill> getTechniciansWithSkill(String skill) {
        return skillRepository.findBySkill(skill);
    }

    public boolean verifyTechnicianSkill(Long technicianId, String skill) {
        return skillRepository.existsByTechnicianIdAndSkill(technicianId, skill);
    }
}