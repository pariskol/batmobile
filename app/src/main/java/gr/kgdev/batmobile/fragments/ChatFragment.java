package gr.kgdev.batmobile.fragments;

import android.os.Build;
import android.os.Bundle;
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

import co.intentservice.chatui.ChatView;
import co.intentservice.chatui.models.ChatMessage;
import gr.kgdev.batmobile.R;
import gr.kgdev.batmobile.activities.MainActivity;
import gr.kgdev.batmobile.activities.MainViewModel;
import gr.kgdev.batmobile.models.Message;
import gr.kgdev.batmobile.models.User;
import gr.kgdev.batmobile.utils.ConvertUtils;
import gr.kgdev.batmobile.utils.HTTPClient;

@RequiresApi(api = Build.VERSION_CODES.N)
public class ChatFragment extends Fragment {

    private MainViewModel mViewModel;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<ChatMessage> chatMessages;
    private ChatView chatView;
    private User appUser;
    private User contactUser;
    private static Thread postmanDaemon;
    private ArrayList<Integer> allMessageIds;

    public ChatFragment(User appUser, User contactUser) {
        this.appUser = appUser;
        this.contactUser = contactUser;
        allMessageIds = new ArrayList<>();
        chatMessages = new ArrayList<ChatMessage>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chat_fragment, container, false);
    }

    private ArrayList<Message> convertJsonToMessages(JSONArray message) throws Exception {
        ArrayList<Message> messages = new ArrayList<>();
        for (int i = 0; i < message.length(); i++) {
            messages.add(new Message(message.getJSONObject(i)));
        }
        return messages;
    }

    private ArrayList<ChatMessage> convertMessagesToChatMessages(JSONArray messages) throws Exception { //edw 8elw n parw to arraylist, pros to paron den 3erw pws n parw ta methods apo to arraylist
        ArrayList<ChatMessage> chatMessages = new ArrayList<>();
        for (int i = 0; i < messages.length(); i++) {
            Message message = new Message(messages.getJSONObject(i));
            if (!allMessageIds.contains(message.getId())) {
                allMessageIds.add(message.getId());
                long millis = ConvertUtils.convertToDate(message.getTimestamp()).getTime();
                ChatMessage.Type type = message.getFromUserId() == appUser.getId() ? ChatMessage.Type.SENT : ChatMessage.Type.RECEIVED;
                chatMessages.add(new ChatMessage(message.getMesasage(), millis, type));
            }
        }
        return chatMessages;
    }

    //TODO K.Vasalakis get messages based on last known message ID, sort new messages and add then to chatView
    private void getChatMessages(Boolean getAll) {
        AtomicBoolean playSound = new AtomicBoolean(false);
        HTTPClient.executeAsync(() -> {
            try {
                //ArrayList<ChatMessage> newChatMessages = new ArrayList<>();
                ArrayList<Message> newMessages = new ArrayList<>();
                String url = HTTPClient.BASE_URL + "/get/gayMessages?FROM_USER=" + contactUser.getId() + "&TO_USER=" + appUser.getId();
                //newChatMessages.addAll(convertJsonToChatMessages(new JSONArray(HTTPClient.GET(url))));
                newMessages.addAll(convertJsonToMessages(new JSONArray(HTTPClient.GET(url))));
                // if new messages from other users fetched, prepare to play a notification sound
                //playSound.set(newChatMessages.size() > 0);
                playSound.set(newMessages.size() > 0);

                //TODO use getAll value when server time is ok
                if (true) {
                    url = HTTPClient.BASE_URL + "/get/gayMessages?FROM_USER=" + appUser.getId() + "&TO_USER=" + contactUser.getId();
                    //newChatMessages.addAll(convertJsonToChatMessages(new JSONArray(HTTPClient.GET(url))));
                    newMessages.addAll(convertJsonToMessages(new JSONArray(HTTPClient.GET(url))));
                }

                //if (newChatMessages.size() > 0 ) {
                if (newMessages.size() > 0 ) {
                    //skeftomai n kanw to sort edw kai n kalesw thn convertMessagesToChatMessages
                    //setMessages(newChatMessages);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            //TODO do not clear list just add the new ones based on id
                            chatView.clearMessages();
                            chatView.addMessages(chatMessages);
                        });
                    }
                    if (playSound.get())
                        ((MainActivity)getActivity()).playNotificationSound();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private synchronized void setMessages(ArrayList<ChatMessage> newChatMessages) {
        chatMessages.addAll(newChatMessages);
        chatMessages.sort(Comparator.comparing(ChatMessage::getTimestamp));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        chatView = (ChatView) getView().findViewById(R.id.chat_view);
        getChatMessages(true);
        chatView.setOnSentMessageListener(chatMessage -> sendMessage(chatMessage));

        startPostmanDaemon();
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
                System.out.println(postmanDaemon.getName() + " is now exiting...");
            }
        }, ChatFragment.class.getSimpleName() + ": Postman Daemon");
        postmanDaemon.setDaemon(true);
        postmanDaemon.start();
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
                    chatView.getInputEditText().getText().clear();;
                });
            } catch (Throwable e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
        // we return false because message will be added to view if POST request executed successfully
        return false;
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

    private synchronized void stopPostmanDaemon() {
        if (postmanDaemon != null)
        postmanDaemon.interrupt();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopPostmanDaemon();
    }
}
