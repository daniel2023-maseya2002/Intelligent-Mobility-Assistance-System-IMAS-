package IMAS.ImasProject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AssignmentException extends RuntimeException {
    public AssignmentException(String message) {
        super(message);
    }
}