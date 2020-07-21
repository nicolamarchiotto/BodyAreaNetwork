package com.example.progettogio.sound_vibration;
import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class Metronome {

    private double bpm;
    private int silence;
    private double sound = 1000; //tipo di suono tick
    private final int tick = 1000; // samples of tick durata tick
    private Vibrator vibrator;
    private boolean play = true;
    private boolean phoneVibration;



    private boolean phoneSound;

    private AudioGenerator audioGenerator = new AudioGenerator(8000);

    public Metronome(Context context, boolean phoneVibration, boolean phoneSound) {
        this.phoneVibration = phoneVibration;
        this.phoneSound = phoneSound;
        if(phoneSound)
            audioGenerator.createPlayer();
        if(phoneVibration)
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void calcSilence() {
        silence = (int) (((60/bpm)*8000)-tick);
    }

    public void play() {
        if(isPhoneVibration()) {
            double lengthPulse = (60d / (bpm * 2)) * 1000d;
            long lenghtPulseL = Math.round(lengthPulse);
            long[] mVibratePattern = new long[]{0, lenghtPulseL, lenghtPulseL};
            int[] mAmplitudes = new int[]{0, 255, 0};
            vibrator.vibrate(VibrationEffect.createWaveform(mVibratePattern, mAmplitudes, 1));
        }

        if(isPhoneSound()) {
            calcSilence();
            double[] tock =
                    audioGenerator.getSineWave(this.tick, 8000, sound);
            double silence = 0;
            double[] sound = new double[8000];
            int t = 0, s = 0, b = 0;
            do {
                for (int i = 0; i < sound.length && play; i++) {
                    if (t < this.tick) {
                        sound[i] = tock[t];
                        t++;
                    } else {
                        sound[i] = silence;
                        s++;
                        if (s >= this.silence) {
                            t = 0;
                            s = 0;
                        }
                    }
                }
                audioGenerator.writeSound(sound);
            } while (play);
        }
    }

    public void stop() {
        if(isPhoneSound()){
            play = false;
            audioGenerator.destroyAudioTrack();
        }
        if(isPhoneVibration()){
            vibrator.cancel();
        }
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

    private boolean isPhoneVibration() {
        return phoneVibration;
    }


    private boolean isPhoneSound() {
        return phoneSound;
    }



}