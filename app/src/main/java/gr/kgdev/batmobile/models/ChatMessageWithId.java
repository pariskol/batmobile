package gr.kgdev.batmobile.models;
import co.intentservice.chatui.models.ChatMessage;

public class ChatMessageWithId extends ChatMessage {

    private Integer id;

    public ChatMessageWithId(Integer id, String message, long timestamp, Type type) {
        super(message, timestamp, type);
        this.id = id;
    }

    public ChatMessageWithId(Integer id, String message, long timestamp, Type type, String sender) {
        super(message, timestamp, type, sender);
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
