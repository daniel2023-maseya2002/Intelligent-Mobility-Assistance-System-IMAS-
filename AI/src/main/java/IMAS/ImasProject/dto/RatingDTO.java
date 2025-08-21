package IMAS.ImasProject.dto;

import IMAS.ImasProject.model.RatingType;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public class RatingDTO {

    public static class CreateRatingRequest {
        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        private Integer rating;

        @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
        private String comment;

        @NotNull(message = "Rating type is required")
        private RatingType ratingType;

        private Long busId;
        private Long driverId;
        private Long routeId;

        // Constructors
        public CreateRatingRequest() {}

        public CreateRatingRequest(Integer rating, String comment, RatingType ratingType) {
            this.rating = rating;
            this.comment = comment;
            this.ratingType = ratingType;
        }

        // Getters and Setters
        public Integer getRating() {
            return rating;
        }

        public void setRating(Integer rating) {
            this.rating = rating;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public RatingType getRatingType() {
            return ratingType;
        }

        public void setRatingType(RatingType ratingType) {
            this.ratingType = ratingType;
        }

        public Long getBusId() {
            return busId;
        }

        public void setBusId(Long busId) {
            this.busId = busId;
        }

        public Long getDriverId() {
            return driverId;
        }

        public void setDriverId(Long driverId) {
            this.driverId = driverId;
        }

        public Long getRouteId() {
            return routeId;
        }

        public void setRouteId(Long routeId) {
            this.routeId = routeId;
        }

        @Override
        public String toString() {
            return "CreateRatingRequest{" +
                    "rating=" + rating +
                    ", comment='" + comment + '\'' +
                    ", ratingType=" + ratingType +
                    ", busId=" + busId +
                    ", driverId=" + driverId +
                    ", routeId=" + routeId +
                    '}';
        }
    }

    public static class RatingResponse {
        private Long id;
        private Integer rating;
        private String comment;
        private RatingType ratingType;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Passenger info
        private Long passengerId;
        private String passengerName;

        // Bus info
        private Long busId;
        private String busName;

        // Driver info
        private Long driverId;
        private String driverName;

        // Route info
        private Long routeId;
        private String routeName;

        // Constructors
        public RatingResponse() {}

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }

        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }

        public RatingType getRatingType() { return ratingType; }
        public void setRatingType(RatingType ratingType) { this.ratingType = ratingType; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

        public Long getPassengerId() { return passengerId; }
        public void setPassengerId(Long passengerId) { this.passengerId = passengerId; }

        public String getPassengerName() { return passengerName; }
        public void setPassengerName(String passengerName) { this.passengerName = passengerName; }

        public Long getBusId() { return busId; }
        public void setBusId(Long busId) { this.busId = busId; }

        public String getBusName() { return busName; }
        public void setBusName(String busName) { this.busName = busName; }

        public Long getDriverId() { return driverId; }
        public void setDriverId(Long driverId) { this.driverId = driverId; }

        public String getDriverName() { return driverName; }
        public void setDriverName(String driverName) { this.driverName = driverName; }

        public Long getRouteId() { return routeId; }
        public void setRouteId(Long routeId) { this.routeId = routeId; }

        public String getRouteName() { return routeName; }
        public void setRouteName(String routeName) { this.routeName = routeName; }
    }

    public static class RatingStatistics {
        private Double averageRating;
        private Long totalRatings;
        private Long fiveStars;
        private Long fourStars;
        private Long threeStars;
        private Long twoStars;
        private Long oneStar;

        // Constructors
        public RatingStatistics() {}

        public RatingStatistics(Double averageRating, Long totalRatings) {
            this.averageRating = averageRating != null ? averageRating : 0.0;
            this.totalRatings = totalRatings != null ? totalRatings : 0L;
        }

        // Getters and Setters
        public Double getAverageRating() { return averageRating; }
        public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

        public Long getTotalRatings() { return totalRatings; }
        public void setTotalRatings(Long totalRatings) { this.totalRatings = totalRatings; }

        public Long getFiveStars() { return fiveStars; }
        public void setFiveStars(Long fiveStars) { this.fiveStars = fiveStars; }

        public Long getFourStars() { return fourStars; }
        public void setFourStars(Long fourStars) { this.fourStars = fourStars; }

        public Long getThreeStars() { return threeStars; }
        public void setThreeStars(Long threeStars) { this.threeStars = threeStars; }

        public Long getTwoStars() { return twoStars; }
        public void setTwoStars(Long twoStars) { this.twoStars = twoStars; }

        public Long getOneStar() { return oneStar; }
        public void setOneStar(Long oneStar) { this.oneStar = oneStar; }
    }

    public static class RatingTypeSummary {
        private RatingType ratingType;
        private Double averageRating;
        private Long totalRatings;

        // Constructors
        public RatingTypeSummary() {}

        public RatingTypeSummary(RatingType ratingType, Double averageRating, Long totalRatings) {
            this.ratingType = ratingType;
            this.averageRating = averageRating != null ? averageRating : 0.0;
            this.totalRatings = totalRatings != null ? totalRatings : 0L;
        }

        // Getters and Setters
        public RatingType getRatingType() { return ratingType; }
        public void setRatingType(RatingType ratingType) { this.ratingType = ratingType; }

        public Double getAverageRating() { return averageRating; }
        public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

        public Long getTotalRatings() { return totalRatings; }
        public void setTotalRatings(Long totalRatings) { this.totalRatings = totalRatings; }
    }
}