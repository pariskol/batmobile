package gr.kgdev.batmobile.fragments;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import gr.kgdev.batmobile.R;
import gr.kgdev.batmobile.activities.MainActivity;
import gr.kgdev.batmobile.activities.MainViewModel;
import gr.kgdev.batmobile.models.User;
import gr.kgdev.batmobile.services.NotificationsService;
import gr.kgdev.batmobile.utils.AppCache;
import gr.kgdev.batmobile.utils.HttpClient;

@RequiresApi(api = Build.VERSION_CODES.N)
public class UsersListFragment extends Fragment {

    private static final String TAG = MainActivity.class.getName();
    private MainViewModel mViewModel;
    private RecyclerView recyclerView;
    private UsersAdapter usersAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private SearchView searchView;
    private ArrayList<User> users;

    private Thread postmanDaemon;
    private HttpClient httpClient;
    private boolean isFiltered = false;

    public UsersListFragment(HttpClient httpClient) {
        super();
        this.httpClient = httpClient;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_list_fragment, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        recyclerView = (RecyclerView) getView().findViewById(R.id.my_recycler_view);
        searchView = (SearchView) getView().findViewById(R.id.search);
        searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty())
                    UsersListFragment.this.filterUsers("");
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                UsersListFragment.this.filterUsers(query);
                return false;
            }

        });
        searchView.setOnCloseListener(() -> {
            filterUsers("");
            return false;
        });
        searchView.setSubmitButtonEnabled(true);
        searchView.setIconifiedByDefault(false);
        searchView.setRevealOnFocusHint(true);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.addOnItemTouchListener(new UserListTouchListener(view, getActivity()));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onPause() {
        super.onPause();
        stopPostmanDaemon();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume() {
        super.onResume();
        isFiltered = false;
        usersAdapter = null;
        startPostmanDaemon();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopPostmanDaemon();
    }


    private void getActiveUsersAndSetAdapter() {
        try {
            JSONArray usersJson = (JSONArray) httpClient.get("/get/active_users_details");
            users = new ArrayList<>();
            for (int i = 0; i < usersJson.length(); i++) {
                if (!AppCache.getAppUser().getId().equals(usersJson.getJSONObject(i).getInt("ID")))
                    users.add(new User(usersJson.getJSONObject(i)));
            }

            if (isFiltered)
                return;

            if (usersAdapter == null) {
                usersAdapter = new UsersAdapter(users, getActivity());
                getActivity().runOnUiThread(() -> recyclerView.setAdapter(usersAdapter));
            }

            usersAdapter.setUsers(users);

        } catch (Throwable e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void filterUsers(String query) {
        List<User> filteredUsers = null;
        if (query.isEmpty()) {
            filteredUsers = users;
            isFiltered = false;
        } else {
            filteredUsers = users.stream().filter(user -> {
                Boolean answer = user.getUsername().toLowerCase().contains(query.toLowerCase());
                return answer;
            }).collect(Collectors.toList());
            isFiltered = true;
        }
        usersAdapter.setUsers(filteredUsers);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getUnreadMessagesCountPerUser() {
        if (usersAdapter == null)
            return;

        AtomicBoolean playSound = new AtomicBoolean(false);
        AtomicInteger totalCount = new AtomicInteger(0);
        final int oldTotalCount = usersAdapter.getTotalUnreadMessagesCount();
        try {
            String url = "/get/unread_messages?TO_USER=" + AppCache.getAppUser().getId();
            JSONArray unreadMessages = (JSONArray) httpClient.get(url);
            for (int i = 0; i < unreadMessages.length(); i++) {
                Integer userId = unreadMessages.getJSONObject(i).getInt("FROM_USER");
                Integer count = unreadMessages.getJSONObject(i).getInt("UNREAD_NUM");
                totalCount.getAndAdd(count);
                usersAdapter.setNotificationsForUser(userId, count);
                if (count > 0 && NotificationsService.shouldNotify())
                    playSound.set(true);
            }

            if (this.isVisible()) {
                getActivity().runOnUiThread(() -> {
                    usersAdapter.notifyDataSetChanged();
                    if (playSound.get() && totalCount.get() > oldTotalCount) {
                        ((MainActivity) getActivity()).playNotificationSound();
                    }
                });
            }
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private synchronized void startPostmanDaemon() {
        stopPostmanDaemon();
        postmanDaemon = new Thread(() -> {
            try {
                while (!postmanDaemon.isInterrupted()) {
                    getActiveUsersAndSetAdapter();
                    getUnreadMessagesCountPerUser();
                    postmanDaemon.sleep(5000);
                }
            } catch (InterruptedException e) {
                Log.d(TAG, postmanDaemon.getName() + " is now exiting...");
            } catch (Throwable e) {
                Log.e(TAG, e.getMessage());
            }
        }, UsersListFragment.class.getSimpleName() + ": Postman Daemon");
        postmanDaemon.setDaemon(true);
        postmanDaemon.start();
    }

    public synchronized void stopPostmanDaemon() {
        if (postmanDaemon != null)
            postmanDaemon.interrupt();
    }
}
