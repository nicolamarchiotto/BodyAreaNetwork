package com.example.progettogio.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.progettogio.R;
import com.example.progettogio.db.AppExecutors;
import com.example.progettogio.db.DataMapper;
import com.example.progettogio.models.NordicPeriodSample;
import com.example.progettogio.views.MainActivity;

import java.util.HashMap;

import no.nordicsemi.android.thingylib.BaseThingyService;
import no.nordicsemi.android.thingylib.ThingyListener;
import no.nordicsemi.android.thingylib.ThingyListenerHelper;
import no.nordicsemi.android.thingylib.ThingySdkManager;

public class DataCollectionService extends Service implements ThingySdkManager.ServiceConnectionListener {

    private static final String TAG = "DataCollectionService";

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    private BluetoothGatt mBluetoothGatt;
    private ThingySdkManager thingySdkManager;
    private BaseThingyService.BaseThingyBinder mBinder;
//    private List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();
//    private Database database;

    private HashMap<String,NordicPeriodSample> nordicHashMap;



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");
//        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Collecting Data")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_thingy_gray)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
        //do heavy work on a background thread
        //stopSelf();
        return START_STICKY;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "DataCollectionDiscoverService Created");

        thingySdkManager = ThingySdkManager.getInstance();
        mBinder = thingySdkManager.getThingyBinder();




        nordicHashMap=new HashMap<>();


        startCollection();
    }


    private void startCollection(){
        for(BluetoothDevice device : thingySdkManager.getConnectedDevices()) {
            Log.d(TAG, "startCollection: connettendo il device "+device.getAddress()+" al listener in DataCollectorService");

            nordicHashMap.put(device.getAddress(),new NordicPeriodSample());

            ThingyListenerHelper.registerThingyListener(getApplicationContext(), thingyListener, device);
//            thingySdkManager.enableMotionNotifications(device, true);
//            thingySdkManager.enableAirQualityNotifications(device, true);
//            thingySdkManager.enableBatteryLevelNotifications(device, true);
//            thingySdkManager.enableButtonStateNotification(device, true);
//            thingySdkManager.enableColorNotifications(device, true);
//            thingySdkManager.enableEnvironmentNotifications(device, true);
//            thingySdkManager.enableEulerNotifications(device, true);
            thingySdkManager.enableGravityVectorNotifications(device, true);
//            thingySdkManager.enableHeadingNotifications(device, true);
//            thingySdkManager.enableHumidityNotifications(device, true);
//            thingySdkManager.enableOrientationNotifications(device, true);
//            thingySdkManager.enablePedometerNotifications(device, true);
//            thingySdkManager.enablePressureNotifications(device, true);
//            thingySdkManager.enableQuaternionNotifications(device, true);
//            thingySdkManager.enableRawDataNotifications(device, true);
//            thingySdkManager.enableRotationMatrixNotifications(device, true);
//            thingySdkManager.enableSoundNotifications(device, true);
//            thingySdkManager.enableSpeakerStatusNotifications(device, true);
//            thingySdkManager.enableTapNotifications(device, true);
//            thingySdkManager.enableThingyMicrophone(device, true);
//            thingySdkManager.enableUiNotifications(device, true);
            // thingySdkManager.setMotionProcessingFrequency(device, ThingyUtils.MPU_FREQ_MIN_INTERVAL);
            Log.d(TAG, "startCollection: fine attivazione sensori per device "+device.getAddress());
        }

    }


    private void stopCollection(){

    }


    @Override
    public void onDestroy() {
        for(BluetoothDevice device:thingySdkManager.getConnectedDevices()){
            DataMapper.getInstance().saveNordicPeriodSampleIntoDbLocal(device.getAddress(),nordicHashMap.get(device.getAddress()));
            ThingyListenerHelper.unregisterThingyListener(getApplicationContext(),thingyListener);
        }
        super.onDestroy();
    }


    private final ThingyListener thingyListener = new ThingyListener() {
        @Override
        public void onDeviceConnected(BluetoothDevice device, int connectionState) {

        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, int connectionState) {

        }

        @Override
        public void onServiceDiscoveryCompleted(BluetoothDevice device) {

        }

        @Override
        public void onBatteryLevelChanged(BluetoothDevice bluetoothDevice, int batteryLevel) {

        }

        @Override
        public void onTemperatureValueChangedEvent(BluetoothDevice bluetoothDevice, String temperature) {

        }

        @Override
        public void onPressureValueChangedEvent(BluetoothDevice bluetoothDevice, String pressure) {

        }

        @Override
        public void onHumidityValueChangedEvent(BluetoothDevice bluetoothDevice, String humidity) {

        }

        @Override
        public void onAirQualityValueChangedEvent(BluetoothDevice bluetoothDevice, int eco2, int tvoc) {

        }

        @Override
        public void onColorIntensityValueChangedEvent(BluetoothDevice bluetoothDevice, float red, float green, float blue, float alpha) {

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
}
