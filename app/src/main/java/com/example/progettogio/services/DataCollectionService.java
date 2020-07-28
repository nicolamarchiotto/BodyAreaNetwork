package com.example.progettogio.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.progettogio.R;
import com.example.progettogio.callback.SubSectionCallback;
import com.example.progettogio.db.DataMapper;
import com.example.progettogio.models.NordicPeriodSample;
import com.example.progettogio.models.PhonePeriodSample;
import com.example.progettogio.views.MainActivity;

import java.util.HashMap;

import no.nordicsemi.android.thingylib.BaseThingyService;
import no.nordicsemi.android.thingylib.ThingyListener;
import no.nordicsemi.android.thingylib.ThingyListenerHelper;
import no.nordicsemi.android.thingylib.ThingySdkManager;

public class DataCollectionService extends Service implements ThingySdkManager.ServiceConnectionListener, SubSectionCallback {

    private static final String TAG = "DataCollectionService";

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    private BluetoothGatt mBluetoothGatt;
    private ThingySdkManager thingySdkManager;
    private BaseThingyService.BaseThingyBinder mBinder;
//    private List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();

    SensorManager mPhoneSensorManager;
    PhonePeriodSample phonePeriodSample;

    private HashMap<String,NordicPeriodSample> nordicHashMap;
    String session_id;



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        thingySdkManager = ThingySdkManager.getInstance();
        mBinder = thingySdkManager.getThingyBinder();

        mPhoneSensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);

        phonePeriodSample=new PhonePeriodSample(mPhoneSensorManager,this);
        nordicHashMap=new HashMap<>();
        
        int nordic_number=1;
        for(BluetoothDevice device : thingySdkManager.getConnectedDevices()) {
            Log.d(TAG, "startCollection: connettendo il device "+device.getAddress()+" al listener in DataCollectorService");
            ThingyListenerHelper.registerThingyListener(getApplicationContext(),thingyListener,device);
            nordicHashMap.put(device.getAddress(),new NordicPeriodSample(nordic_number,device.getAddress(),this));
            nordic_number+=1;

//            thingySdkManager.enableEnvironmentNotifications(device,true);

            thingySdkManager.enableGravityVectorNotifications(device,true);

            thingySdkManager.enableUiNotifications(device, true);
            thingySdkManager.enableSoundNotifications(device, true);
            thingySdkManager.enableMotionNotifications(device, true);

            Log.d(TAG, "startCollection: fine attivazione sensori per device "+device.getAddress());
        }



    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        session_id=intent.getStringExtra("SESSION_ID");
//        Log.d(TAG, "onStartCommand: SESSION_ID: "+session_id);
        DataMapper.getInstance().setReplicator();
        setNotification("Data collecting", "Running");
        startCollection();
        return Service.START_STICKY;
    }



    private void startCollection(){
        int nordic_number=1;
//        for(BluetoothDevice device : thingySdkManager.getConnectedDevices()) {
//            Log.d(TAG, "startCollection: connettendo il device "+device.getAddress()+" al listener in DataCollectorService");
//            ThingyListenerHelper.registerThingyListener(getApplicationContext(),thingyListener,device);
//            nordicHashMap.put(device.getAddress(),new NordicPeriodSample(nordic_number,device.getAddress(),this));
//            nordic_number+=1;
//
//            thingySdkManager.enableEnvironmentNotifications(device, true);
//            thingySdkManager.enableUiNotifications(device, true);
//            thingySdkManager.enableSoundNotifications(device, true);
//            thingySdkManager.enableMotionNotifications(device, true);
//
//            Log.d(TAG, "startCollection: fine attivazione sensori per device "+device.getAddress());
//        }
//

    }
    private void setNotification(String title, String descr) {
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, "misc")
                .setContentTitle(title)
                .setContentText(descr)
                .setSmallIcon(R.drawable.ic_thingy_gray)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "misc",
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }



    private void stopCollection(){

    }


    @Override
    public void onDestroy() {
        for(BluetoothDevice device:thingySdkManager.getConnectedDevices()){
            DataMapper.getInstance().saveNordicPeriodSampleIntoDbLocal(nordicHashMap.get(device.getAddress()),session_id);
            thingySdkManager.enableEnvironmentNotifications(device, false);
            thingySdkManager.enableUiNotifications(device, false);
            thingySdkManager.enableSoundNotifications(device, false);
            thingySdkManager.enableMotionNotifications(device, false);
            ThingyListenerHelper.unregisterThingyListener(getApplicationContext(),thingyListener);
            Log.d(TAG, "onDestroy: unregisting listener for nordic: "+device.getAddress());
        }
        phonePeriodSample.unRegisterPhoneListeners();
        DataMapper.getInstance().savePhonePeriodSampleIntoDbLocal(phonePeriodSample,session_id);
        DataMapper.getInstance().startReplication();
        super.onDestroy();
    }


    private final ThingyListener thingyListener = new ThingyListener() {
        @Override
        public void onDeviceConnected(BluetoothDevice device, int connectionState) {
            Log.d(TAG, "onDeviceConnected: "+device.getAddress());
        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, int connectionState) {
            Log.d(TAG, "onDeviceDisconnected: "+device.getAddress()+" connectionState: "+connectionState);
        }

        @Override
        public void onServiceDiscoveryCompleted(BluetoothDevice device) {

        }

        @Override
        public void onBatteryLevelChanged(BluetoothDevice bluetoothDevice, int batteryLevel) {

        }

        @Override
        public void onTemperatureValueChangedEvent(BluetoothDevice bluetoothDevice, String temperature) {
            Log.d(TAG, "onTemperatureValueChangedEvent: "+temperature);
        }

        @Override
        public void onPressureValueChangedEvent(BluetoothDevice bluetoothDevice, String pressure) {
            Log.d(TAG, "onPressureValueChangedEvent: "+pressure);
        }

        @Override
        public void onHumidityValueChangedEvent(BluetoothDevice bluetoothDevice, String humidity) {
            Log.d(TAG, "onHumidityValueChangedEvent: "+humidity);
        }

        @Override
        public void onAirQualityValueChangedEvent(BluetoothDevice bluetoothDevice, int eco2, int tvoc) {
            Log.d(TAG, "onAirQualityValueChangedEvent: "+eco2+" "+tvoc);
        }

        @Override
        public void onColorIntensityValueChangedEvent(BluetoothDevice bluetoothDevice, float red, float green, float blue, float alpha) {
            Log.d(TAG, "onColorIntensityValueChangedEvent: ");
        }

        @Override
        public void onButtonStateChangedEvent(BluetoothDevice bluetoothDevice, int buttonState) {

        }

        @Override
        public void onTapValueChangedEvent(BluetoothDevice bluetoothDevice, int direction, int count) {

        }

        @Override
        public void onOrientationValueChangedEvent(BluetoothDevice bluetoothDevice, int orientation) {

        }

        @Override
        public void onQuaternionValueChangedEvent(BluetoothDevice bluetoothDevice, float w, float x, float y, float z) {
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyQuaternionData(bluetoothDevice.getAddress(),w,x,y,z, SystemClock.elapsedRealtimeNanos());
        }

        @Override
        public void onPedometerValueChangedEvent(BluetoothDevice bluetoothDevice, int steps, long duration) {

        }

        @Override
        public void onAccelerometerValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyAccellerometerData(bluetoothDevice.getAddress(),x,y,z, SystemClock.elapsedRealtimeNanos());
        }

        @Override
        public void onGyroscopeValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyGyroscopeData(bluetoothDevice.getAddress(),x,y,z, SystemClock.elapsedRealtimeNanos());
        }

        @Override
        public void onCompassValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyCompassData(bluetoothDevice.getAddress(),x,y,z, SystemClock.elapsedRealtimeNanos());
        }

        @Override
        public void onEulerAngleChangedEvent(BluetoothDevice bluetoothDevice, float roll, float pitch, float yaw) {
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyEulerAngleData(bluetoothDevice.getAddress(),roll,pitch,yaw, SystemClock.elapsedRealtimeNanos());
        }

        @Override
        public void onRotationMatrixValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] matrix) {

        }

        @Override
        public void onHeadingValueChangedEvent(BluetoothDevice bluetoothDevice, float heading) {

        }

        @Override
        public void onGravityVectorChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyGravityVectorData(bluetoothDevice.getAddress(),x,y,z, SystemClock.elapsedRealtimeNanos());
            Log.d(TAG, "onGravityVectorChangedEvent: "+bluetoothDevice.getAddress());
        }

        @Override
        public void onSpeakerStatusValueChangedEvent(BluetoothDevice bluetoothDevice, int status) {

        }

        @Override
        public void onMicrophoneValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] data) {

        }
    };


    @Override
    public void onServiceConnected() {
        mBinder = thingySdkManager.getThingyBinder();
    }

    @Override
    public void doNordicSubsection(String address) {
        Log.d(TAG, "doNordicSubsection: ");
        DataMapper.getInstance().saveNordicPeriodSampleIntoDbLocal(nordicHashMap.get(address),session_id);
        DataMapper.getInstance().startReplication();
    }

    @Override
    public void doPhoneSubsection() {
        Log.d(TAG, "doPhoneSubsection: ");
        DataMapper.getInstance().savePhonePeriodSampleIntoDbLocal(phonePeriodSample,session_id);
        DataMapper.getInstance().startReplication();
    }

    @Override
    public void doGlassesSubsection() {
        //to implemet once we get the glasses API
    }
}
