package gr.kgdev.batmobile.models;
import android.content.Context;
import android.util.AttributeSet;

import java.util.ArrayList;

import co.intentservice.chatui.ChatView;
import co.intentservice.chatui.models.ChatMessage;
import co.intentservice.chatui.views.ViewBuilderInterface;

public class BatmobileChatView extends ChatView {

    ArrayList<ChatMessageWithId> messages;

    public BatmobileChatView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BatmobileChatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BatmobileChatView(Context context, AttributeSet attrs, int defStyleAttr, ViewBuilderInterface viewBuilder) {
        super(context, attrs, defStyleAttr, viewBuilder);
    }

    @Override
    public void addMessages(ArrayList<ChatMessage> messages) {
        super.addMessages(messages);
    }
}
