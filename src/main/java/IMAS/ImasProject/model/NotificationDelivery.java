package IMAS.ImasProject.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_deliveries", indexes = {
        @Index(name = "idx_delivery_notification", columnList = "notification_id"),
        @Index(name = "idx_delivery_channel", columnList = "delivery_channel"),
        @Index(name = "idx_delivery_status", columnList = "delivery_status"),
        @Index(name = "idx_delivery_timestamp", columnList = "delivery_timestamp"),
        @Index(name = "idx_delivery_user", columnList = "user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    @JsonBackReference
    private ImasNotification notification;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_channel", nullable = false)
    private DeliveryChannel deliveryChannel;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false)
    @Builder.Default
    private DeliveryStatus deliveryStatus = DeliveryStatus.PENDING;

    @Column(name = "delivery_timestamp")
    private LocalDateTime deliveryTimestamp;

    @Column(name = "delivery_attempt")
    @Builder.Default
    private Integer deliveryAttempt = 1;

    @Column(name = "response_code")
    private String responseCode;

    @Column(name = "response_message")
    private String responseMessage;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "provider_response")
    private String providerResponse;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "retry_after")
    private LocalDateTime retryAfter;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (deliveryTimestamp == null) {
            deliveryTimestamp = LocalDateTime.now();
        }
    }

    // Business methods
    public void markAsDelivered() {
        this.deliveryStatus = DeliveryStatus.DELIVERED;
        this.deliveryTimestamp = LocalDateTime.now();
    }

    public void markAsFailed(String reason) {
        this.deliveryStatus = DeliveryStatus.FAILED;
        this.failureReason = reason;
        this.deliveryTimestamp = LocalDateTime.now();
    }

    public void markAsSent() {
        this.deliveryStatus = DeliveryStatus.SENT;
        this.deliveryTimestamp = LocalDateTime.now();
    }

    public void markAsBounced(String reason) {
        this.deliveryStatus = DeliveryStatus.BOUNCED;
        this.failureReason = reason;
        this.deliveryTimestamp = LocalDateTime.now();
    }

    public void markAsClicked() {
        this.deliveryStatus = DeliveryStatus.CLICKED;
    }

    public void markAsOpened() {
        this.deliveryStatus = DeliveryStatus.OPENED;
    }

    public void incrementAttempt() {
        this.deliveryAttempt++;
    }

    public void scheduleRetry(LocalDateTime retryTime) {
        this.retryAfter = retryTime;
        this.deliveryStatus = DeliveryStatus.PENDING;
    }

    public boolean canRetry() {
        return deliveryStatus.isFailed() &&
                (retryAfter == null || LocalDateTime.now().isAfter(retryAfter));
    }

    public boolean isSuccessful() {
        return deliveryStatus.isSuccessful();
    }

    public boolean hasFailed() {
        return deliveryStatus.isFailed();
    }

    // Setter method to fix the compilation error
    public void setNotification(ImasNotification notification) {
        this.notification = notification;
    }

    @Override
    public String toString() {
        return String.format("NotificationDelivery{id=%d, channel=%s, status=%s, attempt=%d}",
                id, deliveryChannel, deliveryStatus, deliveryAttempt);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NotificationDelivery delivery = (NotificationDelivery) obj;
        return id != null && id.equals(delivery.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public enum DeliveryChannel {
        PUSH("Push Notification"),
        EMAIL("Email"),
        SMS("SMS"),
        IN_APP("In-App");

        private final String displayName;

        DeliveryChannel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum DeliveryStatus {
        PENDING("Pending"),
        SENT("Sent"),
        DELIVERED("Delivered"),
        FAILED("Failed"),
        BOUNCED("Bounced"),
        CLICKED("Clicked"),
        OPENED("Opened");

        private final String displayName;

        DeliveryStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean isSuccessful() {
            return this == SENT || this == DELIVERED || this == CLICKED || this == OPENED;
        }

        public boolean isFailed() {
            return this == FAILED || this == BOUNCED;
        }
    }
}