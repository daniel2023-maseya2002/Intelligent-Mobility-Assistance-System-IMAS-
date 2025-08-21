package IMAS.ImasProject.model;

/**
 * NotificationStatus Enum - Defines the status lifecycle of notifications
 */
public enum NotificationStatus {
    /**
     * Notification created but not yet processed
     */
    PENDING("Pending", "Notification created and waiting to be sent"),

    /**
     * Notification scheduled for future delivery
     */
    SCHEDULED("Scheduled", "Notification scheduled for future delivery"),

    /**
     * Notification is being processed/sent
     */
    PROCESSING("Processing", "Notification is being processed"),

    /**
     * Notification has been sent successfully
     */
    SENT("Sent", "Notification sent successfully"),

    /**
     * Notification delivered to recipient
     */
    DELIVERED("Delivered", "Notification delivered to recipient"),

    /**
     * Notification delivery failed
     */
    FAILED("Failed", "Notification delivery failed"),

    /**
     * Notification marked for retry after failure
     */
    RETRY("Retry", "Notification marked for retry delivery"),

    /**
     * Notification cancelled before delivery
     */
    CANCELLED("Cancelled", "Notification cancelled"),

    /**
     * Notification expired without delivery
     */
    EXPIRED("Expired", "Notification expired"),

    /**
     * Notification read by recipient
     */
    READ("Read", "Notification read by recipient"),

    /**
     * Notification archived
     */
    ARCHIVED("Archived", "Notification archived");

    private final String displayName;
    private final String description;

    NotificationStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if the status indicates a successful delivery
     */
    public boolean isDelivered() {
        return this == SENT || this == DELIVERED || this == READ;
    }

    /**
     * Check if the status indicates a failure
     */
    public boolean isFailed() {
        return this == FAILED || this == EXPIRED || this == CANCELLED;
    }

    /**
     * Check if the status allows for retry
     */
    public boolean canRetry() {
        return this == FAILED || this == RETRY;
    }

    /**
     * Check if the status is a final state
     */
    public boolean isFinalState() {
        return this == DELIVERED || this == READ || this == FAILED ||
                this == CANCELLED || this == EXPIRED || this == ARCHIVED;
    }

    /**
     * Check if the status indicates the notification is active
     */
    public boolean isActive() {
        return !isFinalState();
    }

    @Override
    public String toString() {
        return displayName;
    }
}