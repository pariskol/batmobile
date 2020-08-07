package gr.kgdev.batmobile.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ServiceEchoReceiver extends BroadcastReceiver {

    private Service service;

    public ServiceEchoReceiver(Service service) {
        this.service = service;
    }

    public void onReceive (Context context, Intent intent) {
        LocalBroadcastManager
                .getInstance(service)
                .sendBroadcastSync(new Intent("pong"));
    }
}
