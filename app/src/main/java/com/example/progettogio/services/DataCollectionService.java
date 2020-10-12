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
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.progettogio.db.DataMapper;
import com.example.progettogio.interfaces.SubSectionCallback;
import com.example.progettogio.models.NordicPeriodSample;
import com.example.progettogio.models.PhonePeriodSample;
import com.example.progettogio.models.WagooPeriodSample;
import com.example.progettogio.views.MainActivity;
import com.wagoo.wgcom.WagooGlassesInterface;
import com.wagoo.wgcom.functions.base_functions.AccelGyroInfo;

import java.sql.Timestamp;
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
    private int nordicNumber;
    private int thingy_motion_frequency;

    private SensorManager mPhoneSensorManager;
    private PhonePeriodSample phonePeriodSample;

    private HashMap<String, NordicPeriodSample> nordicHashMap;
    private String session_id;
    private Boolean phone_sensors_on;

    private WagooGlassesInterface mWagooGlassesInterface;
    private Boolean wagoo_glasses_connected;
    private Function1<AccelGyroInfo, Unit> wagooFunctionCallback;
    private WagooPeriodSample wagooPeriodSample;

    private int ARRAYDIMENSION=1000;

    private Boolean firstWagooData=true;
    private long tempoDaQunadoAccesoWagoo=0;
    private long timeStampCorrispettivoATempoAccensioneWaggo=0;




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
        nordicNumber=0;



        for(BluetoothDevice device : thingySdkManager.getConnectedDevices()) {
            Log.d(TAG, "startCollection: connettendo il device "+device.getAddress()+" al listener in DataCollectorService");
            ThingyListenerHelper.registerThingyListener(getApplicationContext(),thingyListener,device);
            nordicHashMap.put(device.getAddress(),new NordicPeriodSample("N"+nordicNumber+"-"+device.getName(),device.getAddress(), this,ARRAYDIMENSION));
            nordicNumber+=1;
            thingySdkManager.enableMotionNotifications(device,true);
            Log.d(TAG, "onCreate: "+device.getAddress()+" - wake state "+thingySdkManager.getWakeOnMotionState(device));
        }

        wagooFunctionCallback=(accelGyroInfo) -> {
            if(firstWagooData){
                tempoDaQunadoAccesoWagoo=accelGyroInfo.getTimestamp()/16000;
                timeStampCorrispettivoATempoAccensioneWaggo=System.currentTimeMillis();
                firstWagooData=false;
            }
            wagooPeriodSample.addDataEntry(accelGyroInfo.getAccl().getX(),accelGyroInfo.getAccl().getY(),accelGyroInfo.getAccl().getZ(),
                    accelGyroInfo.getGyro().getPitch(),accelGyroInfo.getGyro().getPitch(),accelGyroInfo.getGyro().getRoll(),new Timestamp(timeStampCorrispettivoATempoAccensioneWaggo-tempoDaQunadoAccesoWagoo+(accelGyroInfo.getTimestamp()/16000)));


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
            phonePeriodSample=new PhonePeriodSample(mPhoneSensorManager,this,ARRAYDIMENSION);

        if(wagoo_glasses_connected){
            mWagooGlassesInterface.register_collect_sensors_callback(wagooFunctionCallback);
            wagooPeriodSample=new WagooPeriodSample(this,ARRAYDIMENSION);
        }

//        DataMapper.getInstance().setReplicator();
        setNotification("", "");
        return Service.START_STICKY;
    }

    private void setNotification(String title, String descr) {
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, "misc")
                .setContentTitle(title)
                .setContentText(descr)
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
            DataMapper.getInstance().saveNordicLastPeriodSampleIntoLocalDb(nordicHashMap.get(device.getAddress()),session_id);
//            Log.d(TAG, "onDestroy: unregisting listener for nordic: "+device.getAddress());
        }
        if(phone_sensors_on){
            phonePeriodSample.unRegisterPhoneListeners();
            DataMapper.getInstance().savePhoneLastPeriodSampleIntoDbLocal(phonePeriodSample,session_id);
        }

        if(wagoo_glasses_connected){
            mWagooGlassesInterface.unregister_collect_sensors_callback(wagooFunctionCallback);
            DataMapper.getInstance().saveWagooLastPeriodSampleIntoDbLocal(wagooPeriodSample,session_id);
        }
//        DataMapper.getInstance().startReplication();
//        DataMapper.getInstance().waitForPreviousFileToBeSaveAndStarReplication();
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
            Log.d(TAG, "onBatteryLevelChanged: ");
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
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyQuaternionData(bluetoothDevice.getAddress(),w,x,y,z, new Timestamp(System.currentTimeMillis()));
        }

        @Override
        public void onPedometerValueChangedEvent(BluetoothDevice bluetoothDevice, int steps, long duration) {
         }

        @Override
        public void onAccelerometerValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyAccelerometerData(bluetoothDevice.getAddress(),x,y,z, new Timestamp(System.currentTimeMillis()));
            Log.d(TAG, "onAccelerometerValueChangedEvent: ");    
        }

        @Override
        public void onGyroscopeValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyGyroscopeData(bluetoothDevice.getAddress(),x,y,z, new Timestamp(System.currentTimeMillis()));
            Log.d(TAG, "onGyroscopeValueChangedEvent: ");
        }

        @Override
        public void onCompassValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyCompassData(bluetoothDevice.getAddress(),x,y,z, new Timestamp(System.currentTimeMillis()));
            Log.d(TAG, "onCompassValueChangedEvent: ");
        }

        @Override
        public void onEulerAngleChangedEvent(BluetoothDevice bluetoothDevice, float roll, float pitch, float yaw) {
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyEulerAngleData(bluetoothDevice.getAddress(),roll,pitch,yaw, new Timestamp(System.currentTimeMillis()));
            Log.d(TAG, "onEulerAngleChangedEvent: ");
        }

        @Override
        public void onRotationMatrixValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] matrix) {
        }

        @Override
        public void onHeadingValueChangedEvent(BluetoothDevice bluetoothDevice, float heading) {
            Log.d(TAG, "onHeadingValueChangedEvent: ");
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyHeadingData(bluetoothDevice.getAddress(),heading,new Timestamp(System.currentTimeMillis()));
        }

        @Override
        public void onGravityVectorChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {
            nordicHashMap.get(bluetoothDevice.getAddress()).addThingyGravityVectorData(bluetoothDevice.getAddress(),x,y,z, new Timestamp(System.currentTimeMillis()));
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
    public void doPhoneSubsection(int subSession) {
        Log.d(TAG, "doPhoneSubsection: ");
        DataMapper.getInstance().savePhonePeriodSampleIntoDbLocal(phonePeriodSample,session_id,subSession);
//        DataMapper.getInstance().startReplication();
    }

    @Override
    public void doGlassesSubsection(int subSession) {
        Log.d(TAG, "doGlassesSubsection: ");
        DataMapper.getInstance().saveWagooPeriodSampleIntoDbLocal(wagooPeriodSample,session_id,subSession);
//        DataMapper.getInstance().startReplication();
    }

    @Override
    public void doNordicSubsection(String address, int subSection) {
        Log.d(TAG, "doNordicSubsection2: ");
        DataMapper.getInstance().saveNordicPeriodSampleIntoLocalDb(nordicHashMap.get(address),session_id,subSection);
//        DataMapper.getInstance().startReplication();
    }
}
