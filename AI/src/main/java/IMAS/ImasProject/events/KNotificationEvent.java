package IMAS.ImasProject.events;

import IMAS.ImasProject.model.KNotificationType;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

public class KNotificationEvent extends ApplicationEvent {
    private final KNotificationType type;
    private final Map<String, Object> payload;

    public KNotificationEvent(Object source, KNotificationType type, Map<String, Object> payload) {
        super(source);
        this.type = type;
        this.payload = payload;
    }

    public KNotificationType getType() {
        return type;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }
}