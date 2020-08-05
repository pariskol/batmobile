package gr.kgdev.batmobile.fragments;

import android.os.Build;
import android.os.Bundle;
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
import java.util.stream.Collectors;

import gr.kgdev.batmobile.R;
import gr.kgdev.batmobile.activities.MainViewModel;
import gr.kgdev.batmobile.models.User;
import gr.kgdev.batmobile.utils.AppCache;
import gr.kgdev.batmobile.utils.HTTPClient;

@RequiresApi(api = Build.VERSION_CODES.N)
public class UsersListFragment extends Fragment {

    private MainViewModel mViewModel;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private SearchView searchView;
    private ArrayList<User> users;

    public static UsersListFragment newInstance() {
        return new UsersListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        recyclerView = (RecyclerView) getView().findViewById(R.id.my_recycler_view);
        searchView = (SearchView) getView().findViewById(R.id.search);
        searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String newText) {
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

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        setAdapter();
    }

    private void setAdapter() {
        HTTPClient.executeAsync(() -> {
            try {
                JSONArray usersJson = new JSONArray(HTTPClient.GET(HTTPClient.BASE_URL + "/get/active_users_details"));
                users = new ArrayList<>();
                for (int i = 0; i < usersJson.length(); i++) {
                    if (!AppCache.getAppUser().getId().equals(usersJson.getJSONObject(i).getInt("ID")))
                        users.add(new User(usersJson.getJSONObject(i)));
                }

                mAdapter = new UsersAdapter(users);
                getActivity().runOnUiThread(() -> recyclerView.setAdapter(mAdapter));
            } catch (Throwable e) {
                e.printStackTrace();
            }

        });
    }

    private void filterUsers(String query) {
        List<User> filteredUsers = users.stream().filter(user -> {
            Boolean answer = user.getUsername().toLowerCase().contains(query);
            return answer;
        }).collect(Collectors.toList());
        mAdapter = new UsersAdapter((ArrayList<User>) filteredUsers);
        getActivity().runOnUiThread(() -> recyclerView.setAdapter(mAdapter));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        // TODO: Use the ViewModel
    }

}
