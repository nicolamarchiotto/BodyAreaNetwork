package com.example.progettogio.sound_vibration;

import android.content.Context;

/**
 * Thread che si occupa di emettere un suono di duration e bpm definiti dal medico.
 * Credit to: https://masterex.github.io/archive/2012/05/28/android-audio-synthesis.html
 */
public class SoundVibrationThread extends Thread {

    private static final String TAG = "SoundVibrationThread" ;
    private Metronome mMetronome;

    public SoundVibrationThread(Context context,boolean phoneVibration, boolean phoneSound, int bpm) {
        this.mMetronome = new Metronome(context, phoneVibration, phoneSound);
        this.mMetronome.setBpm(bpm);
    }


    public void run() {
        mMetronome.play();
    }

    public void end(){
        mMetronome.stop();
    }
}


