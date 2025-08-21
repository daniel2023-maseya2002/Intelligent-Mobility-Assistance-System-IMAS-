/*
package IMAS.ImasProject.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Operator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String operatorName;
    private String teamName;
    private String position;
    private String email;
    private String phoneNumber;
    private boolean isActive;

    @OneToMany(mappedBy = "operator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Bus> buses;

    // Removed the incidents mapping since Incident doesn't have an operator property

    public Operator() {
        this.isActive = true;
    }

    public Operator(String operatorName, String teamName, String position) {
        this.operatorName = operatorName;
        this.teamName = teamName;
        this.position = position;
        this.isActive = true;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<Bus> getBuses() {
        return buses;
    }

    public void setBuses(List<Bus> buses) {
        this.buses = buses;
    }

    // Method to get incidents related to this operator's buses
    // This would need to be implemented in a service layer with a custom query
    // Example: SELECT i FROM Incident i WHERE i.bus.operator = :operator
}*/
