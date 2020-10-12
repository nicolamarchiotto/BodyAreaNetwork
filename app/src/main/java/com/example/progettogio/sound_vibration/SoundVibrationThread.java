package com.example.progettogio.sound_vibration;

import android.content.Context;

import com.wagoo.wgcom.WagooGlassesInterface;

import java.util.ArrayList;

/**
 * Thread che si occupa di emettere un suono di duration e bpm definiti dal medico.
 * Credit to: https://masterex.github.io/archive/2012/05/28/android-audio-synthesis.html
 */
public class SoundVibrationThread extends Thread {

    private static final String TAG = "SoundVibrationThread" ;
    private Metronome mMetronome;

    private WagooGlassesInterface mWagooGlassesInterface;
    private ArrayList<String> mNodes;
    private int mBpm=0;
    private boolean mWatchVibration=false;
    private boolean mWagooLights=false;
    private Context mContext;

    public SoundVibrationThread(Context context, boolean phoneVibration, boolean phoneSound, int bpm) {
        this.mMetronome = new Metronome(context, phoneVibration, phoneSound);
        mContext=context;
        this.mMetronome.setBpm(bpm);
//        mWagooGlassesInterface=wagooGlassesInterface;
//        mNodes=nodes;
//        mBpm=bpm;
//        mWagooLights=wagooLights;
//        mWatchVibration=watchVibration;

    }


    public void run() {
        if(!isInterrupted()){
//            if(mWagooLights && mWagooGlassesInterface!=null)
//                mWagooGlassesInterface.set_lights(1.0f,(int) (60000/(mBpm*2)),true,true,true);
//            if(mWatchVibration && mNodes!=null){
//                for (String node : mNodes)
//                    Wearable.getMessageClient(mContext).sendMessage(
//                            node, "/connection", String.valueOf(mBpm).getBytes());
//            }
            mMetronome.play();

        }
    }


    public void end(){
//        if(mWagooGlassesInterface!=null)
//            mWagooGlassesInterface.set_lights(0,0,false,false,false);
//        if(mNodes!=null) {
//            for (String node : mNodes)
//                Wearable.getMessageClient(mContext).sendMessage(
//                        node, "/connection", "STOP".getBytes());
//        }
        mMetronome.stop();
        interrupt();
    }
}


