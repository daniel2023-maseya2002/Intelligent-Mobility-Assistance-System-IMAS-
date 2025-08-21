package IMAS.ImasProject.events;

import IMAS.ImasProject.model.KNotification;
import IMAS.ImasProject.model.Staff;
import IMAS.ImasProject.services.KNotificationService;
import IMAS.ImasProject.services.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class KNotificationEventListener {
    @Autowired
    private KNotificationService notificationService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleNotificationEvent(KNotificationEvent event) {
        KNotification notification = new KNotification();
        notification.setType(event.getType());
        notification.setMessage((String) event.getPayload().get("message"));
        notification.setTimestamp(LocalDateTime.now());

        if (event.getType().toString().startsWith("BUS_")) {
            notification.setRecipient(null); // Global notification
            messagingTemplate.convertAndSend("/topic/notifications", notification);
        } else if (event.getType().toString().startsWith("TICKET_")) {
            Long passengerId = (Long) event.getPayload().get("passengerId");
            Staff passenger = staffService.findById(passengerId).orElse(null);
            if (passenger != null) {
                notification.setRecipient(passenger);
                messagingTemplate.convertAndSendToUser(passenger.getEmail(), "/notifications", notification);
            }
        }

        notificationService.save(notification);
    }
}