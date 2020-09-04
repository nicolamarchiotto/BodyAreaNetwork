package com.example.progettogio.vibration;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;


public class Metronome {

    private int bpm;
    private Vibrator mVibrator;
    boolean play = false;

    public Metronome(Context context) {
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }


    public void play() {
        if (!play) {
            double lengthPulse = (60d / (bpm * 2)) * 1000d;
            long lenghtPulseL = Math.round(lengthPulse);
            long[] mVibratePattern = new long[]{0, lenghtPulseL, lenghtPulseL};
            int[] mAmplitudes = new int[]{0, 255, 0};
            play = true;
            mVibrator.vibrate(VibrationEffect.createWaveform(mVibratePattern, mAmplitudes, 1));
        }

    }

    public void stop() {
        if (play) {
            mVibrator.cancel();
            play = false;
        }
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

}