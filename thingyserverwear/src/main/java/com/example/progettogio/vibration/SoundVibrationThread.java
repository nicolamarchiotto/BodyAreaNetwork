package com.example.progettogio.vibration;

import android.content.Context;

/**
 * Thread che si occupa di emettere un suono di duration e bpm definiti dal medico.
 * Credit to: https://masterex.github.io/archive/2012/05/28/android-audio-synthesis.html
 */
public class SoundVibrationThread extends Thread {

    private static final String TAG = "SoundVibrationThread" ;
    private Metronome mMetronome;
    private int mBpm;
    private Context mContext;

    public SoundVibrationThread(Context context, int bpm) {
        this.mBpm = bpm;
        this.mContext = context;
        this.mMetronome = new Metronome(mContext);
        this.mMetronome.setBpm(mBpm);
    }


    public void run() {
        mMetronome.play();
    }

    public void end(){
        mMetronome.stop();
    }

    public void setBpm(int bpm){
        this.mBpm=bpm;
        mMetronome.setBpm(bpm);
    }
}


