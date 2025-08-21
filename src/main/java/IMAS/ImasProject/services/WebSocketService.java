package IMAS.ImasProject.services;


import IMAS.ImasProject.model.WebSocketMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyIncidentCreated(Long busId, String busName) {
        Map<String, Object> payload = Map.of(
                "type", "NEW_INCIDENT",
                "busId", busId,
                "busName", busName
        );
        messagingTemplate.convertAndSend("/topic/incidents", payload);
    }

    public void notifyIncidentResolved(Long busId, String busName) {
        Map<String, Object> payload = Map.of(
                "type", "INCIDENT_RESOLVED",
                "busId", busId,
                "busName", busName
        );
        messagingTemplate.convertAndSend("/topic/incidents", payload);
    }

    public void notifyTechnicianAssigned(Long busId, String busName, String technicianName) {
        Map<String, Object> payload = Map.of(
                "type", "TECHNICIAN_ASSIGNED",
                "busId", busId,
                "busName", busName,
                "technicianName", technicianName
        );
        messagingTemplate.convertAndSend("/topic/incidents", payload);
    }

    /**
     * Notify when a team is assigned to an incident
     */
    public void notifyTeamAssigned(Long busId, String busName, String teamName) {
        Map<String, Object> payload = Map.of(
                "type", "TEAM_ASSIGNED",
                "busId", busId,
                "busName", busName,
                "teamName", teamName
        );
        messagingTemplate.convertAndSend("/topic/incidents", payload);
    }

    /**
     * Send a WebSocketMessage to clients
     */
    public void sendIncidentUpdate(WebSocketMessage message) {
        messagingTemplate.convertAndSend("/topic/incidents", message);
    }

    /**
     * Create and send a WebSocketMessage with the given type and content
     */
    public void sendIncidentUpdateByType(String type, String content) {
        WebSocketMessage message = new WebSocketMessage(type, content);
        messagingTemplate.convertAndSend("/topic/incidents", message);
    }
}