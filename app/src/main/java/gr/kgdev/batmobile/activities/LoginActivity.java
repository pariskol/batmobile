package gr.kgdev.batmobile.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import gr.kgdev.batmobile.utils.AppCache;
import gr.kgdev.batmobile.R;
import gr.kgdev.batmobile.models.User;
import gr.kgdev.batmobile.utils.HTTPClient;

public class LoginActivity extends AppCompatActivity {

    private static AppCompatActivity instance;
    private boolean twice = false;
    private MainViewModel mViewModel;

    public static AppCompatActivity getInstance() {
        return instance;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_fragment);
        TextView usernameTextView = (TextView) this.findViewById(R.id.username);
        TextView passwordTextView = (TextView) this.findViewById(R.id.password);
        Button loginButton = (Button) this.findViewById(R.id.login_button);
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

                //TODO go to other acivity
                try {
                    Intent i = new Intent(this, MainActivity.class);
                    startActivity(i);
                } catch(Exception e) {
                    e.printStackTrace();
                }

            } catch (Throwable e) {
                e.printStackTrace();
               this.runOnUiThread(() -> Toast.makeText(this, "Failed to login!", Toast.LENGTH_SHORT).show());
            }
        });
    }
    @Override
    public void onBackPressed() {
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
