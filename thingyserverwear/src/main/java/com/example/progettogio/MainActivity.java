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
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SignalsServer::SensorsWakeLock");
        mWakeLock.acquire();
        mTextView = (TextView) findViewById(R.id.text);
        //Wear
        Task<CapabilityInfo> capabilityInfoTask = Wearable.getCapabilityClient(getApplicationContext())
                .getCapability("watch_server", CapabilityClient.FILTER_REACHABLE);

        capabilityInfoTask.addOnCompleteListener(new OnCompleteListener<CapabilityInfo>() {
            @Override
            public void onComplete(Task<CapabilityInfo> task) {
                if (task.isSuccessful()) {
                    CapabilityInfo capabilityInfo = task.getResult();
                    nodes = capabilityInfo.getNodes();
                    for (Node node : nodes)
                        text += node.getDisplayName() + "\n";
                    mTextView.setText(text);
                } else {
                    Log.d("MainActivityWear", "Capability request failed to return any results.");
                }

            }
        });

        Wearable.getMessageClient(getApplicationContext()).addListener((messageEvent) -> {
            vibrate(new String(messageEvent.getData()));
        });

        // Enables Always-on
        setAmbientEnabled();
    }


    public void vibrate(String message) {
        if (message.equals("STOP")) {
            if(mSoundVibrationThread!=null)
                mSoundVibrationThread.end();
            if (mWakeLock != null)
                if (mWakeLock.isHeld())
                    mWakeLock.release();
        } else if (message.equals("DISCONNECT")) {
            //not used
            text="Paired with:\n";
            mTextView.setText(text);
        } else{
            if (mWakeLock != null)
                mWakeLock.acquire(5 * 60 * 1000);
            int bpm = Integer.parseInt(message);
            mSoundVibrationThread = new SoundVibrationThread(getApplicationContext(), bpm);
            mSoundVibrationThread.start();
        }
    }
}

//public class MainActivity extends WearableActivity {
//
//    private static final String TAG = "MainActivity";
//
//    private TextView mTextView;
//    private Set<Node> nodes;
//    private String text = "Pairing...\n";
//    private SoundVibrationThread mSoundVibrationThread=null;
//    private PowerManager.WakeLock mWakeLock;
//    private PowerManager mPowerManager;
//
//    private boolean vibrationOn;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
////        mWakeLock = mPowerManager.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, "SignalsServer::SensorsWakeLock");
//        mWakeLock = mPowerManager.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, "SignalsServer::SensorsWakeLock");
//        mTextView = (TextView) findViewById(R.id.text);
//
//        vibrationOn = false;
//
//
//        //Wear
//
//        Task<CapabilityInfo> capabilityInfoTask = Wearable.getCapabilityClient(getApplicationContext())
//                .getCapability("watch_server", CapabilityClient.FILTER_REACHABLE);
//
//        capabilityInfoTask.addOnCompleteListener(new OnCompleteListener<CapabilityInfo>() {
//            @Override
//            public void onComplete(Task<CapabilityInfo> task) {
//                if (task.isSuccessful()) {
//                    CapabilityInfo capabilityInfo = task.getResult();
//                    nodes = capabilityInfo.getNodes();
//                    for (Node node : nodes)
//                        text = "Paired with:\n" + node.getDisplayName();
//                    mTextView.setText(text);
//                } else {
//                    Log.d("MainActivityWear", "Capability request failed to return any results.");
//                }
//
//            }
//        });
//        mSoundVibrationThread.start();
//
//        Wearable.getMessageClient(getApplicationContext()).addListener((messageEvent) -> {
//            onMessageRecieve(messageEvent);
//        });
//
//        // Enables Always-on
//        setAmbientEnabled();
//    }
//
//    public void vibrate2(String message){
//        if(vibrationOn){
//            vibrationOn=false;
//            mSoundVibrationThread.pausa();
//        }
//        else{
//            vibrationOn=true;
//            int bpm = Integer.parseInt(message);
//            if(mSoundVibrationThread!=null){
////                mSoundVibrationThread.setBpm(bpm);
//                mSoundVibrationThread.riprendi(bpm);
//            }
////            mSoundVibrationThread = new SoundVibrationThread(getApplicationContext(), bpm);
////            mSoundVibrationThread.start();
//        }
//    }
//
//    public void vibrate(String message) {
//        if (vibrationOn) {
//            vibrationOn=false;
//            mSoundVibrationThread.end();
//            if (mWakeLock != null)
//                if (mWakeLock.isHeld())
//                    mWakeLock.release();
//        }
//        else{
//            if (mWakeLock != null)
//                mWakeLock.acquire(5 * 60 * 1000);
//            int bpm = Integer.parseInt(message);
//            mSoundVibrationThread = new SoundVibrationThread(getApplicationContext(), bpm);
//            mSoundVibrationThread.start();
//            vibrationOn=true;
//        }
////        if (!message.equals("FREEZE")) {
////            if (mWakeLock != null)
////                mWakeLock.acquire(5 * 60 * 1000);
////            int bpm = Integer.parseInt(message);
////            mSoundVibrationThread = new SoundVibrationThread(getApplicationContext(), bpm);
////            mSoundVibrationThread.start();
////        } else if (mSoundVibrationThread != null) {
////            mSoundVibrationThread.end();
////            if (mWakeLock != null)
////                if (mWakeLock.isHeld())
////                    mWakeLock.release();
////        }
//    }
//
//    public void onMessageRecieve(MessageEvent messageEvent) {
//        if (messageEvent.getPath().equals("/vibration") && !vibrationOn) {
//            if (mWakeLock != null)
//                mWakeLock.acquire(5 * 60 * 1000); //5 minutes
//            vibrationOn = true;
//            int bpm = Integer.parseInt(new String(messageEvent.getData()));
//            mSoundVibrationThread = new SoundVibrationThread(getApplicationContext(), bpm);
//            mSoundVibrationThread.start();
//            mTextView.setText("Freeze");
//        }
//        else{
//            if (mWakeLock != null && mWakeLock.isHeld()) {
//                    mWakeLock.release();
//            }
//            if(mSoundVibrationThread!=null){
//                mSoundVibrationThread.end();
//            }
//            vibrationOn = false;
//            mTextView.setText(text);
//        }
//
//    }
//}
