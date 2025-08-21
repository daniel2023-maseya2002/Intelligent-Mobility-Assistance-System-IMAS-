package IMAS.ImasProject.config;

import IMAS.ImasProject.services.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);
    private final JwtService jwtService;

    @Autowired
    public WebSocketAuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            logger.debug("Processing WebSocket message with command: {}", accessor.getCommand());

            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                handleConnect(accessor);
            } else if (StompCommand.SEND.equals(accessor.getCommand()) ||
                    StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                // For SEND and SUBSCRIBE commands, check if user is already authenticated
                if (accessor.getUser() == null) {
                    // Try to authenticate again if no user is set
                    handleConnect(accessor);
                }
            }
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();

            try {
                String username = jwtService.extractUsername(token);

                if (username != null && jwtService.isTokenValid(token)) {
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            username, null, null);

                    // Set in both places for consistency
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    accessor.setUser(auth);

                    logger.debug("WebSocket authentication successful for user: {}", username);
                } else {
                    logger.warn("Invalid JWT token for WebSocket connection");
                }
            } catch (Exception e) {
                logger.error("Error processing WebSocket JWT token: {}", e.getMessage());
                // Don't throw exception, let connection proceed and handle errors in application layer
            }
        } else {
            logger.warn("No Authorization header found in WebSocket connection");
        }
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            Authentication auth = (Authentication) accessor.getUser();
            if (auth != null) {
                logger.debug("WebSocket disconnection for user: {}", auth.getName());
            }
        }
    }
}