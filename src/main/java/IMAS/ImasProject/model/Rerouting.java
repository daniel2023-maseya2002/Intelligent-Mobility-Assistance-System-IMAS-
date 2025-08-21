package IMAS.ImasProject.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reroutings")
public class Rerouting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "affected_route_id")
    private Route affectedRoute;

    @ManyToOne
    @JoinColumn(name = "alternative_route_id")
    private Route alternativeRoute;

    @Column(nullable = false)
    private String reason;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "estimated_duration")
    private Integer estimatedDuration;

    @Column(name = "additional_notes")
    private String additionalNotes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReroutingStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    // Constructeurs
    public Rerouting() {
        this.createdAt = LocalDateTime.now();
        this.startTime = LocalDateTime.now();
        this.status = ReroutingStatus.ACTIVE;
    }

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Route getAffectedRoute() { return affectedRoute; }
    public void setAffectedRoute(Route affectedRoute) { this.affectedRoute = affectedRoute; }

    public Route getAlternativeRoute() { return alternativeRoute; }
    public void setAlternativeRoute(Route alternativeRoute) { this.alternativeRoute = alternativeRoute; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Integer getEstimatedDuration() { return estimatedDuration; }
    public void setEstimatedDuration(Integer estimatedDuration) { this.estimatedDuration = estimatedDuration; }

    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }

    public ReroutingStatus getStatus() { return status; }
    public void setStatus(ReroutingStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}