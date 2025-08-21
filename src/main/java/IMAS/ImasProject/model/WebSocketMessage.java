package IMAS.ImasProject.model;



/**
 * Concrete implementation of WebSocketMessage that can be instantiated
 */
public class WebSocketMessage {
    private String type;
    private String content;

    public WebSocketMessage() {}

    public WebSocketMessage(String type, String content) {
        this.type = type;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "WebSocketMessage{" +
                "type='" + type + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}