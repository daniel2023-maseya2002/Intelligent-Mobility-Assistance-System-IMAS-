package IMAS.ImasProject.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "technician_skills")
public class TechnicianSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "technician_id", nullable = false)
    private Staff technician;

    @Column(nullable = false)
    private String skill;

    @Enumerated(EnumType.STRING)
    private ProficiencyLevel proficiency;

    @Column(name = "certification_date")
    private LocalDate certificationDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    public enum ProficiencyLevel {
        BASIC, INTERMEDIATE, ADVANCED, EXPERT
    }

    // Constructors, getters and setters
    public TechnicianSkill() {}

    public TechnicianSkill(Staff technician, String skill, ProficiencyLevel proficiency) {
        this.technician = technician;
        this.skill = skill;
        this.proficiency = proficiency;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public Staff getTechnician() {
        return technician;
    }

    public void setTechnician(Staff technician) {
        this.technician = technician;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public ProficiencyLevel getProficiency() {
        return proficiency;
    }

    public void setProficiency(ProficiencyLevel proficiency) {
        this.proficiency = proficiency;
    }

    public LocalDate getCertificationDate() {
        return certificationDate;
    }

    public void setCertificationDate(LocalDate certificationDate) {
        this.certificationDate = certificationDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }
}