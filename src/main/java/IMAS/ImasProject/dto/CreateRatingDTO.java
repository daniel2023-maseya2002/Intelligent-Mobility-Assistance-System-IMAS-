
package IMAS.ImasProject.dto;

import IMAS.ImasProject.model.RatingType;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

// DTO for creating a new rating
public class CreateRatingDTO {
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String comment;

    @NotNull(message = "Rating type is required")
    private RatingType ratingType;

    @NotNull(message = "Passenger ID is required")
    private Long passengerId;

    private Long busId;
    private Long driverId;
    private Long routeId;

    // Constructors
    public CreateRatingDTO() {}

    public CreateRatingDTO(Integer rating, String comment, RatingType ratingType, Long passengerId) {
        this.rating = rating;
        this.comment = comment;
        this.ratingType = ratingType;
        this.passengerId = passengerId;
    }

    // Getters and Setters
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public RatingType getRatingType() { return ratingType; }
    public void setRatingType(RatingType ratingType) { this.ratingType = ratingType; }

    public Long getPassengerId() { return passengerId; }
    public void setPassengerId(Long passengerId) { this.passengerId = passengerId; }

    public Long getBusId() { return busId; }
    public void setBusId(Long busId) { this.busId = busId; }

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
}