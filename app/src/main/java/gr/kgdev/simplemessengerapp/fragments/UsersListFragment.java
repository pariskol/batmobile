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

import gr.kgdev.simplemessengerapp.R;
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
                JSONArray users = new JSONArray(HTTPClient.GET("http://192.168.2.6:8080/get/active_users_details"));
                String[][] usersArr = new String[users.length()][2];
                for (int i = 0; i < users.length(); i++) {
                    usersArr[i][0] = users.getJSONObject(i).getString("USERNAME");
                    usersArr[i][1] = users.getJSONObject(i).getString("active");
                }
                mAdapter = new UsersAdapter(usersArr);
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
