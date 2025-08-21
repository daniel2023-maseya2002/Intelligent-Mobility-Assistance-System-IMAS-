package IMAS.ImasProject.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @Column(name = "recipient_id")
    private Long recipientId;

    @Column(name = "recipient_type")
    private String recipientType;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "content_hash")
    private String contentHash;

    @Column(name = "is_duplicate", nullable = false)
    private boolean duplicate = false;

    @Column(name = "sender_id")
    private String senderId;

    @Column(name = "priority")
    private String priority = "NORMAL";

    @Column(name = "category")
    private String category;

    @Column(name = "action_url")
    private String actionUrl;

    @Column(name = "expires_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Constructeur pour les notifications basiques
    public Notification(String message, String type) {
        this.message = message;
        this.type = type;
        this.timestamp = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    // Constructeur pour les notifications avec destinataire
    public Notification(String message, String type, Long recipientId, String recipientType) {
        this(message, type);
        this.recipientId = recipientId;
        this.recipientType = recipientType;
    }

    // Constructeur complet
    public Notification(String message, String type, Long recipientId, String recipientType, String senderId) {
        this(message, type, recipientId, recipientType);
        this.senderId = senderId;
    }

    // Méthodes de cycle de vie
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Méthodes utilitaires
    public boolean isHighPriority() {
        return "HIGH".equalsIgnoreCase(this.priority) ||
                "URGENT".equalsIgnoreCase(this.priority) ||
                "ERROR".equalsIgnoreCase(this.type) ||
                "ACCIDENT".equalsIgnoreCase(this.type) ||
                "EMERGENCY".equalsIgnoreCase(this.type);
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public void markAsRead() {
        this.read = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsUnread() {
        this.read = false;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isForDriver() {
        return "DRIVER".equalsIgnoreCase(this.recipientType);
    }

    public boolean isForPassenger() {
        return "PASSENGER".equalsIgnoreCase(this.recipientType);
    }

    public boolean isSystemNotification() {
        return "SYSTEM".equalsIgnoreCase(this.senderId);
    }

    // Méthodes pour définir des priorités
    public void setHighPriority() {
        this.priority = "HIGH";
    }

    public void setNormalPriority() {
        this.priority = "NORMAL";
    }

    public void setLowPriority() {
        this.priority = "LOW";
    }

    // Méthodes pour définir des catégories communes
    public void setTripCategory() {
        this.category = "TRIP";
    }

    public void setSystemCategory() {
        this.category = "SYSTEM";
    }

    public void setAlertCategory() {
        this.category = "ALERT";
    }

    public void setInfoCategory() {
        this.category = "INFO";
    }

    // Méthode pour créer une notification d'erreur
    public static Notification createErrorNotification(String message, Long recipientId, String recipientType) {
        Notification notification = new Notification(message, "ERROR", recipientId, recipientType);
        notification.setHighPriority();
        notification.setAlertCategory();
        return notification;
    }

    // Méthode pour créer une notification d'information
    public static Notification createInfoNotification(String message, Long recipientId, String recipientType) {
        Notification notification = new Notification(message, "INFO", recipientId, recipientType);
        notification.setNormalPriority();
        notification.setInfoCategory();
        return notification;
    }

    // Méthode pour créer une notification de voyage
    public static Notification createTripNotification(String message, Long recipientId, String recipientType) {
        Notification notification = new Notification(message, "TRIP", recipientId, recipientType);
        notification.setNormalPriority();
        notification.setTripCategory();
        return notification;
    }

    // Méthode pour créer une notification système
    public static Notification createSystemNotification(String message, Long recipientId, String recipientType) {
        Notification notification = new Notification(message, "SYSTEM", recipientId, recipientType, "SYSTEM");
        notification.setNormalPriority();
        notification.setSystemCategory();
        return notification;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", type='" + type + '\'' +
                ", timestamp=" + timestamp +
                ", recipientId=" + recipientId +
                ", recipientType='" + recipientType + '\'' +
                ", read=" + read +
                ", priority='" + priority + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}