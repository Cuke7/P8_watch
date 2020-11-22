package com.google.android.gms.location.sample.locationupdatesforegroundservice;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.ByteArrayOutputStream;

public class NotificationListener extends NotificationListenerService {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String ACTION_STATUS_BROADCAST = "ACTION_STATUS_BROADCAST";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"SERVICE STARTED !");

        super.onStartCommand(intent, flags, startId);

        // NOTE: We return STICKY to prevent the automatic service termination
        return START_STICKY;
    }


   /* private void broadcastStatus() {
        Intent i1 = new  Intent(ACTION_STATUS_BROADCAST);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i1);
        sendBroadcast(i1);

    }*/

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        String text = "";
        String title = "";
        Bundle extras = sbn.getNotification().extras;
        if(extras.getCharSequence("android.text") != null) {
            text = extras.getCharSequence("android.text").toString();
            title = extras.getString("android.title");
            Intent i = new Intent(ACTION_STATUS_BROADCAST);
            i.putExtra("title", title);
            i.putExtra("text", text);
            LocalBroadcastManager.getInstance(this).sendBroadcast(i);
            sendBroadcast(i);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }

}