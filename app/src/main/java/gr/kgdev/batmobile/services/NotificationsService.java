package gr.kgdev.batmobile.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONArray;

import gr.kgdev.batmobile.R;
import gr.kgdev.batmobile.activities.MainActivity;
import gr.kgdev.batmobile.utils.AppCache;
import gr.kgdev.batmobile.utils.HTTPClient;

public class NotificationsService extends Service {
    private ServiceEchoReceiver broadcastReceiver;
    Thread postmanDaemon;
    private static int PREVIOUS_COUNT = 0;
    private static boolean ENABLE_DAEMON = true;

    public NotificationsService() {
    }

    public synchronized static void enableNotifications(boolean enabled) {
        ENABLE_DAEMON = enabled;
        PREVIOUS_COUNT = 0;
    }

    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("Notifications Service started");
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startPostmanDaemon();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        // register a broadcast receiver this is a hack to keep service alive
        broadcastReceiver = new ServiceEchoReceiver(this);
        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(broadcastReceiver, new IntentFilter("ping"));
        //do not forget to deregister the receiver when the service is destroyed to avoid
        //any potential memory leaks
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        stopPostmanDaemon();
    }

    private void startPostmanDaemon() {
        postmanDaemon = new Thread(() -> {
            try {
                while (!postmanDaemon.isInterrupted()) {
                    if (ENABLE_DAEMON)
                        getUnreadMessagesCount();
                    postmanDaemon.sleep(5000);
                }
            } catch (InterruptedException e) {
                System.out.println(postmanDaemon.getName() + " is now exiting...");
            }
        }, getClass().getSimpleName() + ": Postman Daemon");
        postmanDaemon.setDaemon(true);
        postmanDaemon.start();
    }

    public void stopPostmanDaemon() {
        if (postmanDaemon != null)
            postmanDaemon.interrupt();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getUnreadMessagesCount() {
        HTTPClient.executeAsync(() -> {
            try {
                String url = HTTPClient.BASE_URL + "/get/unread_messages?TO_USER=" + AppCache.getAppUser().getId();
                JSONArray unreadMessages = new JSONArray(HTTPClient.GET(url));
                int count = 0;
                for (int i = 0; i < unreadMessages.length(); i++)
                     count = unreadMessages.getJSONObject(i).getInt("UNREAD_NUM");

                if (count > 0 && count > PREVIOUS_COUNT)
                    createNotification();

                PREVIOUS_COUNT = count;

            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this);
        Notification notification = builder
                .setContentTitle("New messages")
                .setContentText("BATMobile")
                .setSmallIcon(R.drawable.batmobile)
                .setContentIntent(pIntent)
                .setAutoCancel(true).build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "Your_channel_id";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "BATMobile Notifications",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }

        notificationManager.notify(0, notification);
    }
}
