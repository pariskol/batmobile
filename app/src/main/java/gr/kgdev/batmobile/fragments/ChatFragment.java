package gr.kgdev.batmobile.fragments;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import co.intentservice.chatui.ChatView;
import co.intentservice.chatui.models.ChatMessage;
import gr.kgdev.batmobile.R;
import gr.kgdev.batmobile.activities.MainActivity;
import gr.kgdev.batmobile.activities.MainViewModel;
import gr.kgdev.batmobile.models.ChatMessageWithId;
import gr.kgdev.batmobile.models.Message;
import gr.kgdev.batmobile.models.User;
import gr.kgdev.batmobile.utils.ConvertUtils;
import gr.kgdev.batmobile.utils.HTTPClient;

@RequiresApi(api = Build.VERSION_CODES.N)
public class ChatFragment extends Fragment {

    private static final String TAG = MainActivity.class.getName();
    private MainViewModel mViewModel;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<ChatMessage> chatMessages;
    private ChatView chatView;
    private User appUser;
    private User contactUser;
    private static Thread postmanDaemon;
    private int lastMessageId = 0;

    public ChatFragment(User appUser, User contactUser) {
        this.appUser = appUser;
        this.contactUser = contactUser;
        chatMessages = new ArrayList<ChatMessage>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chat_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        chatView = (ChatView) getView().findViewById(R.id.chat_view);
        chatView.setOnSentMessageListener(chatMessage -> sendMessage(chatMessage));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onResume() {
        super.onResume();
        startPostmanDaemon();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopPostmanDaemon();
    }

    private ArrayList<ChatMessageWithId> convertJsonToChatMessages(JSONArray messages) throws Exception {
        ArrayList<ChatMessageWithId> chatMessages = new ArrayList<>();
        for (int i = 0; i < messages.length(); i++) {
            Message message = new Message(messages.getJSONObject(i));
            long millis = ConvertUtils.convertToDate(message.getTimestamp()).getTime();
            ChatMessage.Type type = message.getFromUserId() == appUser.getId() ? ChatMessage.Type.SENT : ChatMessage.Type.RECEIVED;
            chatMessages.add(new ChatMessageWithId(message.getId(), message.getMesasage(), millis, type));
        }
        return chatMessages;
    }

    private void getChatMessages(Boolean getAll) {
        boolean playSound = false;
        try {
            ArrayList<ChatMessageWithId> newChatMessages = new ArrayList<>();
            String url = HTTPClient.BASE_URL + "/get/messages?FROM_USER=" + contactUser.getId() + "&TO_USER=" + appUser.getId() + "&FROM_ID=" + lastMessageId;
            newChatMessages.addAll(convertJsonToChatMessages(new JSONArray(HTTPClient.GET(url))));
            // if new messages from other users fetched, prepare to play a notification sound
            playSound = newChatMessages.size() > 0;

            url = HTTPClient.BASE_URL + "/get/messages?FROM_USER=" + appUser.getId() + "&TO_USER=" + contactUser.getId() + "&FROM_ID=" + lastMessageId;
            newChatMessages.addAll(convertJsonToChatMessages(new JSONArray(HTTPClient.GET(url))));

            if (newChatMessages.size() > 0) {
                    setMessages(newChatMessages);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // cast ArrayList<ChatMessageWithId> to ArrayList<ChatMessage>
                        chatView.addMessages(new ArrayList<ChatMessage>(newChatMessages));
                    });
                }
                if (playSound)
                    ((MainActivity) getActivity()).playNotificationSound();
            }
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private synchronized void setMessages(ArrayList<ChatMessageWithId> newChatMessages) {
        newChatMessages.sort(sortByChatMessageId());
        chatMessages.addAll(newChatMessages);
        ChatMessage lastChatMessage = chatMessages.get(chatMessages.size() - 1);
        lastMessageId = ((ChatMessageWithId) lastChatMessage).getId();
    }

    private Comparator<ChatMessage> sortByChatMessageId() {
        return (o1, o2) -> {
            ChatMessageWithId m1 = (ChatMessageWithId) o1;
            ChatMessageWithId m2 = (ChatMessageWithId) o2;

            return m1.getId() - m2.getId();
        };
    }

    private synchronized void startPostmanDaemon() {
        stopPostmanDaemon();
        postmanDaemon = new Thread(() -> {
            try {
                while (!postmanDaemon.isInterrupted()) {
                    getChatMessages(false);
                    postmanDaemon.sleep(1000);
                }
            } catch (InterruptedException e) {
                Log.i(TAG, postmanDaemon.getName() + " is now exiting...");
            }
        }, ChatFragment.class.getSimpleName() + ": Postman Daemon");
        postmanDaemon.setDaemon(true);
        postmanDaemon.start();
    }

    private synchronized void stopPostmanDaemon() {
        if (postmanDaemon != null)
            postmanDaemon.interrupt();
    }

    private boolean sendMessage(ChatMessage chatMessage) {
        HTTPClient.executeAsync(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("MESSAGE", chatMessage.getMessage());
                json.put("FROM_USER", appUser.getId());
                json.put("TO_USER", contactUser.getId());
                HTTPClient.POST(HTTPClient.BASE_URL + "/post/message", json);
                // here we add the message to view
                getActivity().runOnUiThread(() -> {
//                    chatMessages.add(chatMessage);
//                    chatView.addMessage(chatMessage);
                    chatView.getInputEditText().getText().clear();
                    ;
                });
            } catch (Throwable e) {
                Log.e(TAG, e.getMessage(), e);
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
        // we return false because message will be added to view if POST request executed successfully
        return false;
    }
}
