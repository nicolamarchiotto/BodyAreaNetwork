package com.example.wear;

import android.os.Bundle;
import android.os.PowerManager;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.wear.vibration.SoundVibrationThread;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.Set;

public class MainActivity extends WearableActivity {

    private static final String TAG = "MainActivityWear";
    
    private TextView mTextView;
    private Set<Node> nodes;

    private String text = "Paired with:\n";
    private SoundVibrationThread mSoundVibrationThread;
    private PowerManager.WakeLock mWakeLock;
    private PowerManager mPowerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text);

        // Enables Always-on
//        setAmbientEnabled();

        mTextView = (TextView) findViewById(R.id.text);
        //Wear
        Task<CapabilityInfo> capabilityInfoTask = Wearable.getCapabilityClient(this)
                .getCapability("watch_server", CapabilityClient.FILTER_ALL);
        Log.d(TAG, "onCreate: reached");

        capabilityInfoTask.addOnCompleteListener(new OnCompleteListener<CapabilityInfo>() {
            @Override
            public void onComplete(Task<CapabilityInfo> task) {
                if (task.isSuccessful()) {
                    CapabilityInfo capabilityInfo = task.getResult();
                    nodes = capabilityInfo.getNodes();
                    for (Node node : nodes){
                        text += node.getDisplayName() + "\ncioa";
                        Log.d(TAG, "onComplete: "+node.getDisplayName());
                    }
                    mTextView.setText(text);
                } else {
                    Log.d(TAG, "Capability request failed to return any results.");
                }

            }
        });

        Wearable.getMessageClient(getApplicationContext()).addListener(new MessageClient.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(@NonNull MessageEvent messageEvent) {
                MainActivity.this.vibrate(new String(messageEvent.getData()));
            }
        });
        // Enables Always-on
        setAmbientEnabled();
//        vibrate("30");
    }

    public void vibrate(String message) {
        if (!message.equals("FREEZE")) {
            if (mWakeLock != null)
                mWakeLock.acquire(5 * 60 * 1000);
            int bpm = Integer.parseInt(message);
            mSoundVibrationThread = new SoundVibrationThread(getApplicationContext(), bpm);
            mSoundVibrationThread.start();
        } else if (mSoundVibrationThread != null) {
            mSoundVibrationThread.end();
            if (mWakeLock != null)
                if (mWakeLock.isHeld())
                    mWakeLock.release();
        }
    }

}
