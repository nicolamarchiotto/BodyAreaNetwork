package com.example.progettogio.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.progettogio.views.MainActivity;
import com.google.android.gms.wearable.Wearable;
import com.wagoo.wgcom.WagooGlassesInterface;

import java.util.ArrayList;

public class ClosingService extends Service {

    private static final String TAG = "ClosingService";

    private boolean wearState=false;
    private ArrayList<String> nodeIds;
    private WagooGlassesInterface wagooGlassesInterface;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(closingReceiver, new IntentFilter("closingMessage"));
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        // Handle application closing
        Log.d(TAG, "onTaskRemoved: ");
        if(wearState){
            for (String node : nodeIds)
                Wearable.getMessageClient(getApplicationContext()).sendMessage(
                        node, "/connection", "STOP".getBytes());
            for (String node : nodeIds)
                Wearable.getMessageClient(getApplicationContext()).sendMessage(
                        node, "/connection", "DISCONNECTED".getBytes());
        }
        if(wagooGlassesInterface.isConnected()){
            wagooGlassesInterface.set_lights(0.0f,0,false,false,false);
            wagooGlassesInterface.disconnect();
        }

        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(closingReceiver);
        //Forced kill of the application, bad way, there's something which does not close but can't find what
        android.os.Process.killProcess(android.os.Process.myPid());
        // Destroy the service
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        wagooGlassesInterface= MainActivity.getWagooGlassesInterface();
        return START_STICKY;

    }

    private BroadcastReceiver closingReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            wearState = intent.getBooleanExtra("wearState",false);
            nodeIds=intent.getStringArrayListExtra("nodesId");
            Log.d(TAG, "onReceive: ");
        }
    };
}
