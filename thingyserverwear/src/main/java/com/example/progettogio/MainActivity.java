package com.example.progettogio;

import android.os.Bundle;
import android.os.PowerManager;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;

import com.example.progettogio.vibration.SoundVibrationThread;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.Set;

public class MainActivity extends WearableActivity {

    private static final String TAG = "MainActivity";
    private TextView mTextView;
    private Set<Node> nodes;
    private String text="Paired with:\n";
    private SoundVibrationThread mSoundVibrationThread;
    private PowerManager.WakeLock mWakeLock;
    private PowerManager mPowerManager;

    private String phoneName="";
    private boolean connectedToSmarphone=false;
    private boolean soundVibrationOn=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SignalsServer::SensorsWakeLock");
        mTextView = (TextView) findViewById(R.id.text);
        //Wear
        pairWithSmartphone();

        // Enables Always-on
        setAmbientEnabled();
    }

    private void pairWithSmartphone(){
        phoneName="";
        Task<CapabilityInfo> capabilityInfoTask = Wearable.getCapabilityClient(getApplicationContext())
                .getCapability("watch_server", CapabilityClient.FILTER_REACHABLE);

        capabilityInfoTask.addOnCompleteListener(new OnCompleteListener<CapabilityInfo>() {
            @Override
            public void onComplete(Task<CapabilityInfo> task) {
                if (task.isSuccessful()) {
                    CapabilityInfo capabilityInfo = task.getResult();
                    nodes = capabilityInfo.getNodes();
                    for (Node node : nodes)
                        if(!phoneName.equals(node.getDisplayName()))
                            phoneName = node.getDisplayName();
                    mTextView.setText(text+phoneName);
                } else {
                    Log.d("MainActivityWear", "Capability request failed to return any results.");
                }

            }
        });

        Wearable.getMessageClient(getApplicationContext()).addListener((messageEvent) -> {
            manageMessage(new String(messageEvent.getData()));
        });
    }

    public void manageMessage(String message) {
        if (message.equals("STOP")) {
            soundVibrationOn=false;
            if(mSoundVibrationThread!=null)
                mSoundVibrationThread.end();
            if (mWakeLock != null)
                if (mWakeLock.isHeld())
                    mWakeLock.release();
        } else if (message.equals("CONNECTED")) {
            mTextView.setText("Connected to\n"+phoneName);
            connectedToSmarphone=true;
        }else if (message.equals("DISCONNECTED")) {
            connectedToSmarphone=false;
            pairWithSmartphone();
        }
        else if (message.equals("PING")){
            Log.d(TAG, "ping: ");
        }else{
            if(!soundVibrationOn){
                soundVibrationOn=true;
                if (mWakeLock != null)
                    mWakeLock.acquire(5 * 60 * 1000);
                int bpm = Integer.parseInt(message);
                mSoundVibrationThread = new SoundVibrationThread(getApplicationContext(), bpm);
                mSoundVibrationThread.start();

            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(!connectedToSmarphone)
            pairWithSmartphone();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

