package IMAS.ImasProject.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "k_notifications")
public class KNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KNotificationType type;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private Staff recipient;

    @Column(name = "is_read", nullable = false)  // Renamed to avoid reserved keyword
    private boolean read = false;

    public KNotification() {
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public KNotificationType getType() { return type; }
    public void setType(KNotificationType type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Staff getRecipient() { return recipient; }
    public void setRecipient(Staff recipient) { this.recipient = recipient; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}