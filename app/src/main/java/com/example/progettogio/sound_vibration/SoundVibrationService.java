package com.example.progettogio.sound_vibration;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class SoundVibrationService extends Service {

    private Metronome mMetronome;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean phoneVibration = intent.getExtras().getBoolean("phoneVibration");
        boolean phoneSound = intent.getExtras().getBoolean("phoneSound");
        int bpm = intent.getExtras().getInt("bpm");
        this.mMetronome = new Metronome(this, phoneVibration, phoneSound);
        mMetronome.setBpm(bpm);
        mMetronome.play();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMetronome.stop();
        Toast.makeText(this, "Service destroyed by user.", Toast.LENGTH_LONG).show();

    }
}