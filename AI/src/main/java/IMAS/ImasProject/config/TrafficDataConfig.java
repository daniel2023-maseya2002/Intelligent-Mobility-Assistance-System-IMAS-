package IMAS.ImasProject.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@EnableScheduling
public class TrafficDataConfig implements WebSocketConfigurer {

    private final TrafficDataWebSocketService trafficDataWebSocketService;

    public TrafficDataConfig(TrafficDataWebSocketService trafficDataWebSocketService) {
        this.trafficDataWebSocketService = trafficDataWebSocketService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(trafficDataWebSocketService, "/ws/traffic-data")
                .setAllowedOrigins("*");
    }
}