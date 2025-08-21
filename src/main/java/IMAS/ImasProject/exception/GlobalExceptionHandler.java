package IMAS.ImasProject.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFoundException(NoResourceFoundException ex) {
        String resourcePath = ex.getMessage();

        // Check if it's a Base64 encoded content trying to be accessed as a resource
        if (resourcePath != null && isBase64Content(resourcePath)) {
            logger.warn("Base64 content detected in resource request: {}",
                    resourcePath.substring(0, Math.min(50, resourcePath.length())) + "...");
        } else {
            logger.warn("Resource not found: {}", resourcePath);
        }

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Not Found");
        errorResponse.put("message", "The requested resource was not found");
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("timestamp", java.time.Instant.now().toString());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    private boolean isBase64Content(String content) {
        // Check if the content contains Base64 encoded data patterns
        if (content != null && content.length() > 100) {
            return content.matches(".*[A-Za-z0-9+/=]{50,}.*");
        }
        return false;
    }
}