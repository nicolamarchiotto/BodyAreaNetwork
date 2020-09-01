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
import com.example.progettogio.models.WagooPeriodSample;
import com.example.progettogio.views.MainActivity;
import com.wagoo.wgcom.WagooGlassesInterface;
import com.wagoo.wgcom.functions.base_functions.AccelGyroInfo;

import java.util.HashMap;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
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

    private SensorManager mPhoneSensorManager;
    private PhonePeriodSample phonePeriodSample;

    private HashMap<String,NordicPeriodSample> nordicHashMap;
    private String session_id;
    private Boolean phone_sensors_on;

    private WagooGlassesInterface mWagooGlassesInterface;
    private Boolean wagoo_glasses_connected;
    private Function1<AccelGyroInfo, Unit> wagooFunctionCallback;
    private WagooPeriodSample wagooPeriodSample;



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
        nordicHashMap=new HashMap<>();



        for(BluetoothDevice device : thingySdkManager.getConnectedDevices()) {
            Log.d(TAG, "startCollection: connettendo il device "+device.getAddress()+" al listener in DataCollectorService");
            ThingyListenerHelper.registerThingyListener(getApplicationContext(),thingyListener,device);
            nordicHashMap.put(device.getAddress(),new NordicPeriodSample(device.getName(),device.getAddress(),this));
//            thingySdkManager.enableEnvironmentNotifications(device,true);
//            thingySdkManager.enableMotionNotifications(device,true);
//            thingySdkManager.setMotionProcessingFrequency(device,500000000);

//            thingySdkManager.setAdvertisingIntervalUnits(device,50000);
        }

        wagooFunctionCallback=(accelGyroInfo) -> {
            wagooPeriodSample.addDataEntry(accelGyroInfo.getAccl().getX(),accelGyroInfo.getAccl().getY(),accelGyroInfo.getAccl().getZ(),
                    accelGyroInfo.getGyro().getPitch(),accelGyroInfo.getGyro().getPitch(),accelGyroInfo.getGyro().getRoll(),accelGyroInfo.getTimestamp());

            Log.d(TAG, "wagooFunctionCallback: called");
            return Unit.INSTANCE;
        };

        mWagooGlassesInterface=MainActivity.getWagooGlassesInterface();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        session_id=intent.getStringExtra("SESSION_ID");
        phone_sensors_on=intent.getBooleanExtra("PHONE_SENSORS_ON",false);
        wagoo_glasses_connected=intent.getBooleanExtra("WAGOO_GLASSES_CONNECTED",false);

        if(phone_sensors_on)
            phonePeriodSample=new PhonePeriodSample(mPhoneSensorManager,this);

        if(wagoo_glasses_connected){
            mWagooGlassesInterface.register_collect_sensors_callback(wagooFunctionCallback);
            wagooPeriodSample=new WagooPeriodSample(this);
        }

        DataMapper.getInstance().setReplicator();
        setNotification("Data collecting", "Running");
        return Service.START_STICKY;
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
        stopSelf();
    }


    @Override
    public void onDestroy() {
        for(BluetoothDevice device:thingySdkManager.getConnectedDevices()){
            ThingyListenerHelper.unregisterThingyListener(getApplicationContext(),thingyListener);
            DataMapper.getInstance().saveNordicPeriodSampleIntoDbLocal(nordicHashMap.get(device.getAddress()),session_id);
//            Log.d(TAG, "onDestroy: unregisting listener for nordic: "+device.getAddress());
        }
        if(phone_sensors_on){
            phonePeriodSample.unRegisterPhoneListeners();
            DataMapper.getInstance().savePhonePeriodSampleIntoDbLocal(phonePeriodSample,session_id);
        }

        if(wagoo_glasses_connected){
            mWagooGlassesInterface.unregister_collect_sensors_callback(wagooFunctionCallback);
            DataMapper.getInstance().saveWagooPeriodSampleIntoDbLocal(wagooPeriodSample,session_id);
        }
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
            stopSelf();
        }

        @Override
        public void onServiceDiscoveryCompleted(BluetoothDevice device) {
            Log.d(TAG, "onServiceDiscoveryCompleted: ");
        }

        @Override
        public void onBatteryLevelChanged(BluetoothDevice bluetoothDevice, int batteryLevel) {
            Log.d(TAG, "onBatteryLevelChanged: ");
        }

        @Override
        public void onTemperatureValueChangedEvent(BluetoothDevice bluetoothDevice, String temperature) {
            Log.d(TAG, "onTemperatureValueChangedEvent: "+temperature);
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyTemperatureData(bluetoothDevice.getAddress(),temperature,SystemClock.elapsedRealtimeNanos());
        }

        @Override
        public void onPressureValueChangedEvent(BluetoothDevice bluetoothDevice, String pressure) {
            Log.d(TAG, "onPressureValueChangedEvent: "+pressure);
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyPressureData(bluetoothDevice.getAddress(),pressure,SystemClock.elapsedRealtimeNanos());
        }

        @Override
        public void onHumidityValueChangedEvent(BluetoothDevice bluetoothDevice, String humidity) {
            Log.d(TAG, "onHumidityValueChangedEvent: "+humidity);
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyHumidityData(bluetoothDevice.getAddress(),humidity,SystemClock.elapsedRealtimeNanos());
        }

        @Override
        public void onAirQualityValueChangedEvent(BluetoothDevice bluetoothDevice, int eco2, int tvoc) {
            Log.d(TAG, "onAirQualityValueChangedEvent: "+eco2+" "+tvoc);
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyAirQualityData(bluetoothDevice.getAddress(),eco2,tvoc,SystemClock.elapsedRealtimeNanos());
        }

        @Override
        public void onColorIntensityValueChangedEvent(BluetoothDevice bluetoothDevice, float red, float green, float blue, float alpha) {
            Log.d(TAG, "onColorIntensityValueChangedEvent: ");
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyColorIntensityData(bluetoothDevice.getAddress(),red,green,blue,alpha,SystemClock.elapsedRealtimeNanos());
        }


        //TODO: onButtonStateChangedEvent requires enable tap notifications, so what are the Taps Value???
        @Override
        public void onButtonStateChangedEvent(BluetoothDevice bluetoothDevice, int buttonState) {
            Log.d(TAG, "onButtonStateChangedEvent: ");
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyButtonStateData(bluetoothDevice.getAddress(),buttonState,SystemClock.elapsedRealtimeNanos());
        }

        @Override
        public void onTapValueChangedEvent(BluetoothDevice bluetoothDevice, int direction, int count) {
            Log.d(TAG, "onTapValueChangedEvent: ");
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyTapData(bluetoothDevice.getAddress(),direction,count,SystemClock.elapsedRealtimeNanos());
        }

        @Override
        public void onOrientationValueChangedEvent(BluetoothDevice bluetoothDevice, int orientation) {
            Log.d(TAG, "onOrientationValueChangedEvent: ");
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyOrientationData(bluetoothDevice.getAddress(),orientation,SystemClock.elapsedRealtimeNanos());
        }

        @Override
        public void onQuaternionValueChangedEvent(BluetoothDevice bluetoothDevice, float w, float x, float y, float z) {
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyQuaternionData(bluetoothDevice.getAddress(),w,x,y,z, SystemClock.elapsedRealtimeNanos());
            Log.d(TAG, "onQuaternionValueChangedEvent: ");
        }

        @Override
        public void onPedometerValueChangedEvent(BluetoothDevice bluetoothDevice, int steps, long duration) {
            Log.d(TAG, "onPedometerValueChangedEvent: ");
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyPedometerData(bluetoothDevice.getAddress(),steps,duration,SystemClock.elapsedRealtimeNanos());
        }

        @Override
        public void onAccelerometerValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyAccelerometerData(bluetoothDevice.getAddress(),x,y,z, SystemClock.elapsedRealtimeNanos());
            Log.d(TAG, "onAccelerometerValueChangedEvent: ");    
        }

        @Override
        public void onGyroscopeValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyGyroscopeData(bluetoothDevice.getAddress(),x,y,z, SystemClock.elapsedRealtimeNanos());
            Log.d(TAG, "onGyroscopeValueChangedEvent: ");
        }

        @Override
        public void onCompassValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyCompassData(bluetoothDevice.getAddress(),x,y,z, SystemClock.elapsedRealtimeNanos());
            Log.d(TAG, "onCompassValueChangedEvent: ");
        }

        @Override
        public void onEulerAngleChangedEvent(BluetoothDevice bluetoothDevice, float roll, float pitch, float yaw) {
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyEulerAngleData(bluetoothDevice.getAddress(),roll,pitch,yaw, SystemClock.elapsedRealtimeNanos());
            Log.d(TAG, "onEulerAngleChangedEvent: ");
        }

        //TODO come gestire una matrice in un documento couchBase??? Vedi anche onMicrophoneValueChangeEvent

        @Override
        public void onRotationMatrixValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] matrix) {
            if(matrix==null)
                Log.d(TAG, "onRotationMatrixValueChangedEvent: matrix is null");
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyRotationMatrixData(bluetoothDevice.getAddress(),matrix,SystemClock.elapsedRealtimeNanos());
        }

        //TODO prosegui da qua

        @Override
        public void onHeadingValueChangedEvent(BluetoothDevice bluetoothDevice, float heading) {
            Log.d(TAG, "onHeadingValueChangedEvent: ");
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyHeadingData(bluetoothDevice.getAddress(),heading,SystemClock.elapsedRealtimeNanos());
        }

        @Override
        public void onGravityVectorChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyGravityVectorData(bluetoothDevice.getAddress(),x,y,z, SystemClock.elapsedRealtimeNanos());
            Log.d(TAG, "onGravityVectorChangedEvent: "+bluetoothDevice.getAddress());
        }

        @Override
        public void onSpeakerStatusValueChangedEvent(BluetoothDevice bluetoothDevice, int status) {
            Log.d(TAG, "onSpeakerStatusValueChangedEvent: ");
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingySpeakerStatusData(bluetoothDevice.getAddress(),status,SystemClock.elapsedRealtimeNanos());
        }

        @Override
        public void onMicrophoneValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] data) {
            Log.d(TAG, "onMicrophoneValueChangedEvent: ");
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyMicrophoneData(bluetoothDevice.getAddress(),data,SystemClock.elapsedRealtimeNanos());
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
        Log.d(TAG, "doGlassesSubsection: ");
        DataMapper.getInstance().saveWagooPeriodSampleIntoDbLocal(wagooPeriodSample,session_id);
        DataMapper.getInstance().startReplication();

    }

}
