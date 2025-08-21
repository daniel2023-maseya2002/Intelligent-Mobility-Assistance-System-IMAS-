package IMAS.ImasProject.listeners;

import IMAS.ImasProject.events.KNotificationEvent;
import IMAS.ImasProject.model.Notification;
import IMAS.ImasProject.model.Staff;
import IMAS.ImasProject.services.NotificationService;
import IMAS.ImasProject.services.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class KNotificationListener {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleNotificationEvent(KNotificationEvent event) {
        // Extract message and type from the event
        String message = (String) event.getPayload().get("message");
        String type = event.getType().name();

        // Create the notification
        Notification notification = notificationService.createNotification(message, type);

        // If notification is created (not a duplicate), send it via WebSocket
        if (notification != null) {
            if (type.startsWith("BUS_")) {
                // Global notification for bus-related events
                messagingTemplate.convertAndSend("/topic/notifications", notification);
            } else if (type.startsWith("TICKET_")) {
                // User-specific notification for ticket-related events
                Long passengerId = (Long) event.getPayload().get("passengerId");
                if (passengerId != null) {
                    Staff passenger = staffService.findById(passengerId).orElse(null);
                    if (passenger != null) {
                        messagingTemplate.convertAndSendToUser(
                                passenger.getEmail(),
                                "/notifications",
                                notification
                        );
                    }
                }
            }
        }
    }
}