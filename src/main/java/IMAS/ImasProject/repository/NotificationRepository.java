package IMAS.ImasProject.repository;

import IMAS.ImasProject.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Méthodes pour récupérer les notifications par destinataire et type
    List<Notification> findByRecipientIdAndRecipientTypeOrderByTimestampDesc(Long recipientId, String recipientType);

    List<Notification> findByRecipientIdAndRecipientTypeAndReadFalseOrderByTimestampDesc(Long recipientId, String recipientType);

    // Méthodes pour récupérer les notifications par destinataire (tous types)
    List<Notification> findByRecipientIdOrderByTimestampDesc(Long recipientId);

    List<Notification> findByRecipientIdAndReadFalseOrderByTimestampDesc(Long recipientId);

    // Méthodes pour compter les notifications
    long countByRecipientId(Long recipientId);

    long countByRecipientIdAndReadFalse(Long recipientId);

    // Méthode pour trouver les notifications similaires après un certain temps
    @Query("SELECT n FROM Notification n WHERE n.contentHash = :contentHash AND n.timestamp > :cutoffTime")
    List<Notification> findSimilarNotificationsAfterTime(@Param("contentHash") String contentHash, @Param("cutoffTime") LocalDateTime cutoffTime);

    // Méthodes pour récupérer les notifications par type
    List<Notification> findByTypeOrderByTimestampDesc(String type);

    List<Notification> findByTypeAndReadFalseOrderByTimestampDesc(String type);

    // Méthodes pour récupérer les notifications par destinataire et type spécifique
    List<Notification> findByRecipientIdAndTypeOrderByTimestampDesc(Long recipientId, String type);

    List<Notification> findByRecipientIdAndTypeAndReadFalseOrderByTimestampDesc(Long recipientId, String type);

    // Méthodes pour récupérer les notifications dans une plage de temps
    @Query("SELECT n FROM Notification n WHERE n.timestamp BETWEEN :startTime AND :endTime ORDER BY n.timestamp DESC")
    List<Notification> findByTimestampBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.timestamp BETWEEN :startTime AND :endTime ORDER BY n.timestamp DESC")
    List<Notification> findByRecipientIdAndTimestampBetween(@Param("recipientId") Long recipientId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    // Méthodes pour récupérer les notifications non dupliquées
    List<Notification> findByDuplicateFalseOrderByTimestampDesc();

    List<Notification> findByRecipientIdAndDuplicateFalseOrderByTimestampDesc(Long recipientId);

    // Méthodes pour récupérer les notifications récentes
    @Query("SELECT n FROM Notification n WHERE n.timestamp > :cutoffTime ORDER BY n.timestamp DESC")
    List<Notification> findRecentNotifications(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.timestamp > :cutoffTime ORDER BY n.timestamp DESC")
    List<Notification> findRecentNotificationsByRecipient(@Param("recipientId") Long recipientId, @Param("cutoffTime") LocalDateTime cutoffTime);

    // Méthodes pour récupérer les notifications par priorité
    @Query("SELECT n FROM Notification n WHERE n.type IN ('error', 'accident', 'emergency') ORDER BY n.timestamp DESC")
    List<Notification> findHighPriorityNotifications();

    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.type IN ('error', 'accident', 'emergency') ORDER BY n.timestamp DESC")
    List<Notification> findHighPriorityNotificationsByRecipient(@Param("recipientId") Long recipientId);

    // Méthodes pour nettoyer les anciennes notifications
    @Query("DELETE FROM Notification n WHERE n.timestamp < :cutoffTime")
    void deleteOldNotifications(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("DELETE FROM Notification n WHERE n.duplicate = true AND n.timestamp < :cutoffTime")
    void deleteOldDuplicateNotifications(@Param("cutoffTime") LocalDateTime cutoffTime);
}