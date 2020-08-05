package gr.kgdev.batmobile.fragments;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import co.intentservice.chatui.ChatView;
import co.intentservice.chatui.models.ChatMessage;
import gr.kgdev.batmobile.R;
import gr.kgdev.batmobile.models.User;
import gr.kgdev.batmobile.utils.HTTPClient;
import gr.kgdev.batmobile.activities.MainViewModel;

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
    private Thread postmanDaemon;
    private ArrayList<Integer> allMessageIds;

    public ChatFragment() {

    }

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

    private void getChatMessages(Boolean getAll) {
        AtomicBoolean playSound = new AtomicBoolean(false);
        HTTPClient.executeAsync(() -> {
            try {
                ArrayList<ChatMessage> newChatMessages = new ArrayList<>();
                newChatMessages.addAll(
                        convertJsonToChatMessages(new JSONArray(HTTPClient.GET(HTTPClient.BASE_URL + "/get/messages?FROM_USER=" + contactUser.getId() + "&TO_USER=" + appUser.getId())))
                );
                playSound.set(newChatMessages.size() > 0);

                //TODO use getAll value when server time is ok
                if (true) {
                    newChatMessages.addAll(
                            convertJsonToChatMessages(new JSONArray(HTTPClient.GET(HTTPClient.BASE_URL + "/get/messages?FROM_USER=" + appUser.getId() + "&TO_USER=" + contactUser.getId())))
                    );
                }

                if (newChatMessages.size() > 0 ) {
                    chatMessages.addAll(newChatMessages);
                    chatMessages.sort(Comparator.comparing(ChatMessage::getTimestamp));
                    getActivity().runOnUiThread(() -> {
                        chatView.clearMessages();
                        chatView.addMessages(chatMessages);
                    });
                    if (playSound.get())
                        playNotificationSound();
                }
            } catch (Throwable t) {
                t.printStackTrace();
//                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void playNotificationSound() {
        Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(getContext(), defaultRingtoneUri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(mp -> mp.release());
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<ChatMessage> convertJsonToChatMessages(JSONArray messages) throws Exception {
        ArrayList<ChatMessage> chatMessages = new ArrayList<>();
        for (int i = 0; i < messages.length(); i++) {
            Integer messageId = messages.getJSONObject(i).getInt("ID");
            if (!allMessageIds.contains(messageId)) {
                allMessageIds.add(messageId);
                String message = messages.getJSONObject(i).getString("MESSAGE");
                int userId = messages.getJSONObject(i).getInt("FROM_USER");
                long millis = convertToDate(messages.getJSONObject(i).getString("TIMESTAMP")).getTime();
                ChatMessage.Type type = userId == appUser.getId() ? ChatMessage.Type.SENT : ChatMessage.Type.RECEIVED;
                chatMessages.add(new ChatMessage(message, millis, type));
            }
        }
        return chatMessages;
    }

    private Date convertToDate(String str) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = format.parse(str);
        System.out.println(date);
        return date;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        chatView = (ChatView) getView().findViewById(R.id.chat_view);
        getChatMessages(true);
        chatView.setOnSentMessageListener(chatMessage -> sendMessage(chatMessage));

        startPostmanDaemon();
    }

    private void startPostmanDaemon() {
        postmanDaemon = new Thread(() -> {
            try {
                while (!postmanDaemon.isInterrupted()) {
                    getChatMessages(false);
                    postmanDaemon.sleep(1000);
                }
            } catch (InterruptedException e) {
                System.out.println("Postman Daemon is now exiting...");
            }
        }, "Postman Daemon");
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
        if (!postmanDaemon.isAlive())
            startPostmanDaemon();
    }

    public void stopPostmanDaemon() {
        postmanDaemon.interrupt();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopPostmanDaemon();
    }
}
