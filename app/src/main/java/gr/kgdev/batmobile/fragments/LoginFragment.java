package gr.kgdev.batmobile.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import org.json.JSONObject;

import gr.kgdev.batmobile.utils.AppCache;
import gr.kgdev.batmobile.activities.MainActivity;
import gr.kgdev.batmobile.activities.MainViewModel;
import gr.kgdev.batmobile.R;
import gr.kgdev.batmobile.models.User;
import gr.kgdev.batmobile.utils.HTTPClient;

public class LoginFragment extends Fragment {

    private MainViewModel mViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login_fragment, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView usernameTextView = (TextView) getView().findViewById(R.id.username);
        TextView passwordTextView = (TextView) getView().findViewById(R.id.password);
        Button loginButton = (Button) getView().findViewById(R.id.login_button);
        loginButton.setOnClickListener(v -> login(usernameTextView, passwordTextView));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void login(TextView usernameTextView, TextView passwordTextView) {
        HTTPClient.setBasicAuthCredentials(usernameTextView.getText().toString(), passwordTextView.getText().toString());
        HTTPClient.executeAsync(() -> {
            try {
                JSONObject json = new JSONObject(HTTPClient.POST(HTTPClient.BASE_URL + "/login", null));
                User appUser = new User(json);
                AppCache.setAppUser(appUser);

                if (getActivity() instanceof MainActivity)
                    ((MainActivity)getActivity()).loadUsersListFragment();

            } catch (Throwable e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Failed to login!", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
    }
}
