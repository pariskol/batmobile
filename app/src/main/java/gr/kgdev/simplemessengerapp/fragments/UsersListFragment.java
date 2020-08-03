package gr.kgdev.simplemessengerapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import gr.kgdev.simplemessengerapp.R;
import gr.kgdev.simplemessengerapp.models.User;
import gr.kgdev.simplemessengerapp.utils.HTTPClient;
import gr.kgdev.simplemessengerapp.MainViewModel;

public class UsersListFragment extends Fragment {

    private MainViewModel mViewModel;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

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
                JSONArray usersJson = new JSONArray(HTTPClient.GET("http://192.168.2.6:8080/get/active_users_details"));
                ArrayList<User> users = new ArrayList<>();
                for (int i = 0; i < usersJson.length(); i++)
                    users.add(new User(usersJson.getJSONObject(i)));

                mAdapter = new UsersAdapter(users);
                getActivity().runOnUiThread(() -> recyclerView.setAdapter(mAdapter));
            } catch (Throwable e) {
                e.printStackTrace();
            }

        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        // TODO: Use the ViewModel
    }

}
