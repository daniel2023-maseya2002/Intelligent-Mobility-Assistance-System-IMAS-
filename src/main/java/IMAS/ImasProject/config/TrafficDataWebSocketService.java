package IMAS.ImasProject.config;



import IMAS.ImasProject.dto.TrafficDataDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class TrafficDataWebSocketService extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("WebSocket connection established: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("WebSocket connection closed: " + session.getId());
    }

    // Diffuser une mise à jour de trafic à tous les clients connectés
    public void broadcastTrafficUpdate(TrafficDataDTO trafficData) {
        String message = createTrafficUpdateMessage(trafficData);
        synchronized (sessions) {
            sessions.removeIf(session -> {
                try {
                    session.sendMessage(new TextMessage(message));
                    return false;
                } catch (IOException e) {
                    System.err.println("Failed to send message to session " + session.getId());
                    return true; // Supprimer la session défaillante
                }
            });
        }
    }

    // Diffuser une alerte de trafic
    public void broadcastTrafficAlert(String alertType, String message, Double latitude, Double longitude) {
        String alertMessage = createTrafficAlertMessage(alertType, message, latitude, longitude);
        synchronized (sessions) {
            sessions.removeIf(session -> {
                try {
                    session.sendMessage(new TextMessage(alertMessage));
                    return false;
                } catch (IOException e) {
                    System.err.println("Failed to send alert to session " + session.getId());
                    return true;
                }
            });
        }
    }

    private String createTrafficUpdateMessage(TrafficDataDTO trafficData) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "type", "traffic_update",
                    "data", trafficData,
                    "timestamp", java.time.LocalDateTime.now()
            ));
        } catch (Exception e) {
            return "{\"type\":\"error\",\"message\":\"Failed to serialize traffic data\"}";
        }
    }

    private String createTrafficAlertMessage(String alertType, String message, Double latitude, Double longitude) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "type", "traffic_alert",
                    "alertType", alertType,
                    "message", message,
                    "location", Map.of("latitude", latitude, "longitude", longitude),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        } catch (Exception e) {
            return "{\"type\":\"error\",\"message\":\"Failed to create alert message\"}";
        }
    }
}