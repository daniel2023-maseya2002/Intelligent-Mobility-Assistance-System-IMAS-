/*
package IMAS.ImasProject.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class ResourceController {

    private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);

    @GetMapping("/**")
    public ResponseEntity<Map<String, Object>> handleMalformedResourceRequests(HttpServletRequest request) {
        String requestURI = request.getRequestURI();

        // Log malformed requests
        if (isBase64Content(requestURI)) {
            logger.warn("Intercepted Base64 content request: {}",
                    requestURI.substring(0, Math.min(50, requestURI.length())) + "...");
        } else {
            logger.debug("Handling unknown resource request: {}", requestURI);
        }

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Not Found");
        errorResponse.put("message", "The requested resource was not found");
        errorResponse.put("path", requestURI);
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("timestamp", java.time.Instant.now().toString());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    private boolean isBase64Content(String content) {
        if (content != null && content.length() > 100) {
            return content.matches(".*[A-Za-z0-9+/=]{50,}.*");
        }
        return false;
    }
}*/
