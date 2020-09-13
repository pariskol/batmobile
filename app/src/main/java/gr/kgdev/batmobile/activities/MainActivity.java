package gr.kgdev.batmobile.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONException;

import gr.kgdev.batmobile.R;
import gr.kgdev.batmobile.fragments.ChatFragment;
import gr.kgdev.batmobile.fragments.UsersListFragment;
import gr.kgdev.batmobile.models.User;
import gr.kgdev.batmobile.utils.AppCache;
import gr.kgdev.batmobile.services.NotificationsService;
import gr.kgdev.batmobile.utils.HTTPClient;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private static final String TAG = MainActivity.class.getName();

    private BroadcastReceiver pong = new BroadcastReceiver(){
        public void onReceive (Context context, Intent intent) {
            notificationServiceRunning = true;
        }
    };

    private boolean notificationServiceRunning = false;

    public void initMediaPlayer() {
        Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, defaultRingtoneUri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
            mediaPlayer.prepare();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public synchronized void playNotificationSound() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.start();
            } catch (Throwable e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    public void startNotificationService() {
        startService(new Intent(this, NotificationsService.class));
    }

    public void stopNotificationService() {
        stopService(new Intent(this, NotificationsService.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new UsersListFragment())
                    .commitNow();
        }

        NotificationsService.enableNotifications(false);
        startNotificationService();
        initMediaPlayer();

        // keep service alive
        LocalBroadcastManager.getInstance(this).registerReceiver(pong, new IntentFilter("pong"));
        LocalBroadcastManager.getInstance(this).sendBroadcastSync(new Intent("ping"));

        if(!notificationServiceRunning){
            startNotificationService();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        NotificationsService.enableNotifications(true);
        logout();
    }

    @Override
    protected void onStop() {
        super.onStop();
        NotificationsService.enableNotifications(true);
        logout();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotificationsService.enableNotifications(true);
        logout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationsService.enableNotifications(false);
        login();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            getSupportActionBar().setTitle("BATMobile");
        }
        else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            NotificationsService.enableNotifications(true);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);
        switch (item.getItemId())
        {
            case R.id.item_one:
                Toast.makeText(this, "Hello " +
                        AppCache.getAppUser().getUsername() + "!", Toast.LENGTH_SHORT).show();
            case R.id.search:
            default:
                return false;
        }
    }

    public void loadChatFragment(User user) throws JSONException {
        getSupportActionBar().setTitle("Chating with " + user.getUsername());
        Fragment newFragment = new ChatFragment(AppCache.getAppUser(), user);
        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void loadUsersListFragment() {
        Fragment newFragment = new UsersListFragment();
        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void login() {
        HTTPClient.executeAsync(() -> {
            try {
                HTTPClient.POST(HTTPClient.BASE_URL + "/login", null);
            } catch (Throwable e) {
                Log.e(TAG, e.getMessage(), e);
            }
        });
    }

    private void logout() {
        HTTPClient.executeAsync(() -> {
            try {
                HTTPClient.POST(HTTPClient.BASE_URL + "/logout", null);
            } catch (Throwable e) {
                Log.e(TAG, e.getMessage(), e);
            }
        });
    }
}
