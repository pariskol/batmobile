package gr.kgdev.simplemessengerapp.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import co.intentservice.chatui.ChatView;
import co.intentservice.chatui.models.ChatMessage;
import gr.kgdev.simplemessengerapp.R;
import gr.kgdev.simplemessengerapp.utils.HTTPClient;
import gr.kgdev.simplemessengerapp.MainViewModel;

public class ChatFragment extends Fragment {

    private MainViewModel mViewModel;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<ChatMessage> chatMessages;
    private ChatView chatView;

    public ChatFragment() {

    }

    public ChatFragment(String appUser, String contactUser) {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chat_fragment, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getChatMessages() {
        chatMessages = new ArrayList<>();
        HTTPClient.executeAsync(() -> {
            try {
                convertJsonToChatMessages(new JSONArray(HTTPClient.GET("http://192.168.2.6:8080/get/messages?FROM_USER=1&TO_USER=2")));
                convertJsonToChatMessages(new JSONArray(HTTPClient.GET("http://192.168.2.6:8080/get/messages?FROM_USER=2&TO_USER=1")));
                chatMessages.sort(Comparator.comparing(ChatMessage::getTimestamp));
                getActivity().runOnUiThread(() -> chatView.addMessages(chatMessages));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void convertJsonToChatMessages(JSONArray messages) throws Exception {
        for (int i = 0; i < messages.length(); i++) {
            String message = messages.getJSONObject(i).getString("MESSAGE");
            int userId = messages.getJSONObject(i).getInt("FROM_USER");
            long millis = convertToDate(messages.getJSONObject(i).getString("TIMESTAMP")).getTime();
            ChatMessage.Type type = userId == 1 ? ChatMessage.Type.RECEIVED : ChatMessage.Type.SENT;
            chatMessages.add(new ChatMessage(message, millis, type));
        }
    }

    private Date convertToDate(String str) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = format.parse(str);
        System.out.println(date);
        return date;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        chatView = (ChatView) getView().findViewById(R.id.chat_view);
        getChatMessages();
        chatView.setOnSentMessageListener(chatMessage -> sendMessage(chatMessage));
    }

    private boolean sendMessage(ChatMessage chatMessage) {
        HTTPClient.executeAsync(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("MESSAGE", chatMessage.getMessage());
                json.put("FROM_USER", 1);
                json.put("TO_USER", 2);
                HTTPClient.POST("http://192.168.2.6:8080/save?table=messages", json);
                // here we add the message to view
                getActivity().runOnUiThread(() -> chatView.addMessage(chatMessage));
            } catch (JSONException | IOException e) {
                e.printStackTrace();
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

}
