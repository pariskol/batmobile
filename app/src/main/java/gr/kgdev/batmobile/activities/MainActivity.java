package gr.kgdev.batmobile.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import org.json.JSONException;

import gr.kgdev.batmobile.utils.AppCache;
import gr.kgdev.batmobile.R;
import gr.kgdev.batmobile.fragments.ChatFragment;
import gr.kgdev.batmobile.fragments.LoginFragment;
import gr.kgdev.batmobile.fragments.UsersListFragment;
import gr.kgdev.batmobile.models.User;

@RequiresApi(api = Build.VERSION_CODES.N)
public class MainActivity extends AppCompatActivity {

    private static AppCompatActivity instance;
    private boolean twice = false;
    private MainViewModel mViewModel;

    public static AppCompatActivity getInstance() {
        return instance;
    }

    public static void loadChatFragment(User user) throws JSONException {
        if (instance == null)
            throw new NullPointerException("MainActivity has not been initialized yet!");

        Fragment newFragment = new ChatFragment(AppCache.getAppUser(), user);
        FragmentTransaction transaction = MainActivity.getInstance().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void loadUsersListFragment() {
        Fragment newFragment = new UsersListFragment();
        FragmentTransaction transaction = MainActivity.getInstance().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
//        getSupportActionBar().setIcon(R.drawable.batmanlogo);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new UsersListFragment())
                    .commitNow();
        }
        instance = this;
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }
        else {
            if (twice == true) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                System.exit(0);
            }
            twice = true;
            //if we push the back button twice in 3s we exit the app,otherwise app will continue to run
            Toast.makeText(this, "Please press BACK again to exit", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() -> twice = false, 2000);
        }

    }
}
