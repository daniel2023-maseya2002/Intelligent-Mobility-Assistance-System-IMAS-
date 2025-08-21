package IMAS.ImasProject.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

@Entity
@Table(name = "imas_notifications", indexes = {
        @Index(name = "idx_notification_type", columnList = "notification_type"),
        @Index(name = "idx_notification_status", columnList = "status"),
        @Index(name = "idx_notification_priority", columnList = "priority"),
        @Index(name = "idx_notification_created", columnList = "created_at"),
        @Index(name = "idx_notification_scheduled", columnList = "scheduled_time"),
        @Index(name = "idx_notification_route", columnList = "route_id"),
        @Index(name = "idx_notification_vehicle", columnList = "vehicle_id"),
        @Index(name = "idx_notification_stop", columnList = "stop_id"),
        @Index(name = "idx_notification_user", columnList = "target_user_id")
})
public class ImasNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @NotBlank(message = "Message cannot be blank")
    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType = NotificationType.INFO;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private NotificationPriority priority = NotificationPriority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_audience")
    private TargetAudience targetAudience = TargetAudience.ALL_USERS;

    @Column(name = "target_user_id")
    private Long targetUserId;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name = "sent_time")
    private LocalDateTime sentTime;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "is_push_notification")
    private Boolean isPushNotification = true;

    @Column(name = "is_email_notification")
    private Boolean isEmailNotification = false;

    @Column(name = "is_sms_notification")
    private Boolean isSmsNotification = false;

    @Column(name = "is_in_app_notification")
    private Boolean isInAppNotification = true;

    @Column(name = "push_sent")
    private Boolean pushSent = false;

    @Column(name = "email_sent")
    private Boolean emailSent = false;

    @Column(name = "sms_sent")
    private Boolean smsSent = false;

    @Column(name = "delivery_attempts")
    private Integer deliveryAttempts = 0;

    @Column(name = "max_delivery_attempts")
    private Integer maxDeliveryAttempts = 3;

    @Column(name = "last_delivery_attempt")
    private LocalDateTime lastDeliveryAttempt;

    @Column(name = "action_url")
    private String actionUrl;

    @Column(name = "action_label")
    private String actionLabel;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "image_url")
    private String imageUrl;

    @ElementCollection
    @CollectionTable(name = "notification_metadata",
            joinColumns = @JoinColumn(name = "notification_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata = new HashMap<>();

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    @JsonBackReference
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    @JsonBackReference
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stop_id")
    @JsonBackReference
    private Stop stop;

    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<NotificationDelivery> deliveries = new ArrayList<>();

    public ImasNotification() {
        this.metadata = new HashMap<>();
        this.deliveries = new ArrayList<>();
    }

    public ImasNotification(String title, String message) {
        this();
        this.title = title;
        this.message = message;
    }

    public ImasNotification(String title, String message, NotificationType type, NotificationPriority priority) {
        this(title, message);
        this.notificationType = type;
        this.priority = priority;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public NotificationPriority getPriority() {
        return priority;
    }

    public void setPriority(NotificationPriority priority) {
        this.priority = priority;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public TargetAudience getTargetAudience() {
        return targetAudience;
    }

    public void setTargetAudience(TargetAudience targetAudience) {
        this.targetAudience = targetAudience;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public LocalDateTime getSentTime() {
        return sentTime;
    }

    public void setSentTime(LocalDateTime sentTime) {
        this.sentTime = sentTime;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public Boolean getIsPushNotification() {
        return isPushNotification;
    }

    public void setIsPushNotification(Boolean isPushNotification) {
        this.isPushNotification = isPushNotification;
    }

    public Boolean getIsEmailNotification() {
        return isEmailNotification;
    }

    public void setIsEmailNotification(Boolean isEmailNotification) {
        this.isEmailNotification = isEmailNotification;
    }

    public Boolean getIsSmsNotification() {
        return isSmsNotification;
    }

    public void setIsSmsNotification(Boolean isSmsNotification) {
        this.isSmsNotification = isSmsNotification;
    }

    public Boolean getIsInAppNotification() {
        return isInAppNotification;
    }

    public void setIsInAppNotification(Boolean isInAppNotification) {
        this.isInAppNotification = isInAppNotification;
    }

    public Boolean getPushSent() {
        return pushSent;
    }

    public void setPushSent(Boolean pushSent) {
        this.pushSent = pushSent;
    }

    public Boolean getEmailSent() {
        return emailSent;
    }

    public void setEmailSent(Boolean emailSent) {
        this.emailSent = emailSent;
    }

    public Boolean getSmsSent() {
        return smsSent;
    }

    public void setSmsSent(Boolean smsSent) {
        this.smsSent = smsSent;
    }

    public Integer getDeliveryAttempts() {
        return deliveryAttempts;
    }

    public void setDeliveryAttempts(Integer deliveryAttempts) {
        this.deliveryAttempts = deliveryAttempts;
    }

    public Integer getMaxDeliveryAttempts() {
        return maxDeliveryAttempts;
    }

    public void setMaxDeliveryAttempts(Integer maxDeliveryAttempts) {
        this.maxDeliveryAttempts = maxDeliveryAttempts;
    }

    public LocalDateTime getLastDeliveryAttempt() {
        return lastDeliveryAttempt;
    }

    public void setLastDeliveryAttempt(LocalDateTime lastDeliveryAttempt) {
        this.lastDeliveryAttempt = lastDeliveryAttempt;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public void setActionLabel(String actionLabel) {
        this.actionLabel = actionLabel;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Stop getStop() {
        return stop;
    }

    public void setStop(Stop stop) {
        this.stop = stop;
    }

    public List<NotificationDelivery> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(List<NotificationDelivery> deliveries) {
        this.deliveries = deliveries;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (scheduledTime == null) {
            scheduledTime = LocalDateTime.now();
        }

        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusHours(24);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void scheduleNotification(LocalDateTime scheduleTime) {
        this.scheduledTime = scheduleTime;
        this.status = NotificationStatus.SCHEDULED;
        this.updatedAt = LocalDateTime.now();
    }

    public void sendNotification() {
        if (canBeSent()) {
            this.sentTime = LocalDateTime.now();
            this.status = NotificationStatus.SENT;
            this.deliveryAttempts++;
            this.lastDeliveryAttempt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void markAsRead() {
        if (!isRead) {
            this.isRead = true;
            this.readAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void markAsUnread() {
        this.isRead = false;
        this.readAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void markPushSent() {
        this.pushSent = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void markEmailSent() {
        this.emailSent = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void markSmsSent() {
        this.smsSent = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void failDelivery(String reason) {
        this.deliveryAttempts++;
        this.lastDeliveryAttempt = LocalDateTime.now();

        if (deliveryAttempts >= maxDeliveryAttempts) {
            this.status = NotificationStatus.FAILED;
        } else {
            this.status = NotificationStatus.RETRY;
        }

        metadata.put("failure_reason", reason);
        metadata.put("failed_at", LocalDateTime.now().toString());

        this.updatedAt = LocalDateTime.now();
    }

    public void cancelNotification(String reason) {
        this.status = NotificationStatus.CANCELLED;
        metadata.put("cancellation_reason", reason);
        metadata.put("cancelled_at", LocalDateTime.now().toString());
        this.updatedAt = LocalDateTime.now();
    }

    public void expireNotification() {
        this.status = NotificationStatus.EXPIRED;
        metadata.put("expired_at", LocalDateTime.now().toString());
        this.updatedAt = LocalDateTime.now();
    }

    public void addDelivery(NotificationDelivery delivery) {
        deliveries.add(delivery);
        delivery.setNotification(this);
    }

    public void removeDelivery(NotificationDelivery delivery) {
        deliveries.remove(delivery);
        delivery.setNotification(null);
    }

    public boolean canBeSent() {
        return status == NotificationStatus.PENDING ||
                status == NotificationStatus.SCHEDULED ||
                status == NotificationStatus.RETRY;
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isScheduledForFuture() {
        return scheduledTime != null && scheduledTime.isAfter(LocalDateTime.now());
    }

    public boolean shouldBeDelivered() {
        return canBeSent() &&
                !isExpired() &&
                !isScheduledForFuture() &&
                deliveryAttempts < maxDeliveryAttempts;
    }

    public boolean hasFailedDelivery() {
        return status == NotificationStatus.FAILED;
    }

    public boolean needsRetry() {
        return status == NotificationStatus.RETRY &&
                deliveryAttempts < maxDeliveryAttempts;
    }

    public boolean isDelivered() {
        return status == NotificationStatus.SENT || status == NotificationStatus.DELIVERED;
    }

    public boolean hasAllChannelsDelivered() {
        boolean allDelivered = true;

        if (isPushNotification && !pushSent) allDelivered = false;
        if (isEmailNotification && !emailSent) allDelivered = false;
        if (isSmsNotification && !smsSent) allDelivered = false;

        return allDelivered;
    }

    public long getMinutesUntilScheduled() {
        if (scheduledTime == null) return 0;
        return java.time.temporal.ChronoUnit.MINUTES.between(LocalDateTime.now(), scheduledTime);
    }

    public long getMinutesUntilExpiry() {
        if (expiresAt == null) return Long.MAX_VALUE;
        return java.time.temporal.ChronoUnit.MINUTES.between(LocalDateTime.now(), expiresAt);
    }

    public String getDeliveryChannels() {
        List<String> channels = new ArrayList<>();

        if (isPushNotification) channels.add("Push");
        if (isEmailNotification) channels.add("Email");
        if (isSmsNotification) channels.add("SMS");
        if (isInAppNotification) channels.add("In-App");

        return String.join(", ", channels);
    }

    public String getDeliveryStatus() {
        if (status == NotificationStatus.SENT) {
            if (hasAllChannelsDelivered()) {
                return "Fully Delivered";
            } else {
                return "Partially Delivered";
            }
        }
        return status.toString();
    }

    public void addMetadata(String key, String value) {
        metadata.put(key, value);
        this.updatedAt = LocalDateTime.now();
    }

    public String getMetadata(String key) {
        return metadata.get(key);
    }

    public void removeMetadata(String key) {
        metadata.remove(key);
        this.updatedAt = LocalDateTime.now();
    }

    public void setRouteContext(Route route) {
        this.route = route;
        addMetadata("context_type", "route");
        addMetadata("context_id", route.getId().toString());
        if (route.getRouteName() != null) {
            addMetadata("context_name", route.getRouteName());
        }
    }

    public void setVehicleContext(Vehicle vehicle) {
        this.vehicle = vehicle;
        addMetadata("context_type", "vehicle");
        addMetadata("context_id", vehicle.getId().toString());
        if (vehicle.getVehicleNumber() != null) {
            addMetadata("context_name", vehicle.getVehicleNumber());
        }
    }

    public void setStopContext(Stop stop) {
        this.stop = stop;
        addMetadata("context_type", "stop");
        addMetadata("context_id", stop.getId().toString());
        if (stop.getStopName() != null) {
            addMetadata("context_name", stop.getStopName());
        }
    }

    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ImasNotification notification;

        public Builder() {
            this.notification = new ImasNotification();
        }

        public Builder title(String title) {
            notification.setTitle(title);
            return this;
        }

        public Builder message(String message) {
            notification.setMessage(message);
            return this;
        }

        public Builder notificationType(NotificationType notificationType) {
            notification.setNotificationType(notificationType);
            return this;
        }

        public Builder priority(NotificationPriority priority) {
            notification.setPriority(priority);
            return this;
        }

        public Builder status(NotificationStatus status) {
            notification.setStatus(status);
            return this;
        }

        public Builder targetAudience(TargetAudience targetAudience) {
            notification.setTargetAudience(targetAudience);
            return this;
        }

        public Builder targetUserId(Long targetUserId) {
            notification.setTargetUserId(targetUserId);
            return this;
        }

        public Builder scheduledTime(LocalDateTime scheduledTime) {
            notification.setScheduledTime(scheduledTime);
            return this;
        }

        public Builder expiresAt(LocalDateTime expiresAt) {
            notification.setExpiresAt(expiresAt);
            return this;
        }

        public Builder isPushNotification(Boolean isPushNotification) {
            notification.setIsPushNotification(isPushNotification);
            return this;
        }

        public Builder isEmailNotification(Boolean isEmailNotification) {
            notification.setIsEmailNotification(isEmailNotification);
            return this;
        }

        public Builder isSmsNotification(Boolean isSmsNotification) {
            notification.setIsSmsNotification(isSmsNotification);
            return this;
        }

        public Builder isInAppNotification(Boolean isInAppNotification) {
            notification.setIsInAppNotification(isInAppNotification);
            return this;
        }

        public Builder actionUrl(String actionUrl) {
            notification.setActionUrl(actionUrl);
            return this;
        }

        public Builder actionLabel(String actionLabel) {
            notification.setActionLabel(actionLabel);
            return this;
        }

        public Builder iconUrl(String iconUrl) {
            notification.setIconUrl(iconUrl);
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            notification.setImageUrl(imageUrl);
            return this;
        }

        public Builder route(Route route) {
            notification.setRoute(route);
            return this;
        }

        public Builder vehicle(Vehicle vehicle) {
            notification.setVehicle(vehicle);
            return this;
        }

        public Builder stop(Stop stop) {
            notification.setStop(stop);
            return this;
        }

        public Builder createdBy(String createdBy) {
            notification.setCreatedBy(createdBy);
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            notification.setMetadata(metadata);
            return this;
        }

        public ImasNotification build() {
            return notification;
        }
    }

    public static ImasNotification createServiceAlert(String title, String message, NotificationPriority priority) {
        return ImasNotification.builder()
                .title(title)
                .message(message)
                .notificationType(NotificationType.SERVICE_ALERT)
                .priority(priority)
                .targetAudience(TargetAudience.ALL_USERS)
                .isPushNotification(true)
                .isInAppNotification(true)
                .build();
    }

    public static ImasNotification createDelayNotification(String title, String message, Route route, Vehicle vehicle) {
        return ImasNotification.builder()
                .title(title)
                .message(message)
                .notificationType(NotificationType.DELAY_ALERT)
                .priority(NotificationPriority.HIGH)
                .targetAudience(TargetAudience.ROUTE_USERS)
                .route(route)
                .vehicle(vehicle)
                .isPushNotification(true)
                .isInAppNotification(true)
                .build();
    }

    public static ImasNotification createPersonalNotification(String title, String message, Long userId) {
        return ImasNotification.builder()
                .title(title)
                .message(message)
                .notificationType(NotificationType.PERSONAL)
                .priority(NotificationPriority.NORMAL)
                .targetAudience(TargetAudience.SPECIFIC_USER)
                .targetUserId(userId)
                .isPushNotification(true)
                .isInAppNotification(true)
                .build();
    }

    @Override
    public String toString() {
        return String.format("ImasNotification{id=%d, title='%s', type=%s, status=%s, priority=%s}",
                id, title, notificationType, status, priority);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ImasNotification notification = (ImasNotification) obj;
        return Objects.equals(id, notification.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}