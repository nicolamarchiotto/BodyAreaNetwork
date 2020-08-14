package com.example.progettogio.models;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.couchbase.lite.MutableArray;
import com.couchbase.lite.MutableDictionary;
import com.example.progettogio.callback.SubSectionCallback;

public class PhonePeriodSample implements SensorEventListener {

    private static final String TAG = "PhonePeriodSample";

    private SensorManager mPhoneSensorManager;
    private Sensor mAccelerometer;
    private Sensor mLinearAccelerometer;
    private Sensor mGyroscope;
    private Sensor mMagneto;

    private MutableArray phoneAccelerometerMutableArray;
    private MutableArray phoneLinearAccelerometerMutableArray;
    private MutableArray phoneGyroscopeMutableArray;
    private MutableArray phoneMagnetoMutableArray;

    private int subsession=0;
    private SubSectionCallback callback;


    public PhonePeriodSample(SensorManager phoneSensorManager, SubSectionCallback subsessionCallback){
        mPhoneSensorManager=phoneSensorManager;
        callback=subsessionCallback;
        mAccelerometer = mPhoneSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mLinearAccelerometer = mPhoneSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGyroscope = mPhoneSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mMagneto = mPhoneSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (mAccelerometer != null) {
            mPhoneSensorManager.registerListener(this, mAccelerometer, android.hardware.SensorManager.SENSOR_DELAY_GAME);
        }
        if (mGyroscope != null) {
            mPhoneSensorManager.registerListener(this, mGyroscope, android.hardware.SensorManager.SENSOR_DELAY_GAME);
        }
        if (mLinearAccelerometer != null) {
            mPhoneSensorManager.registerListener(this, mLinearAccelerometer, android.hardware.SensorManager.SENSOR_DELAY_GAME);
        }
        if (mMagneto != null) {
            mPhoneSensorManager.registerListener(this, mMagneto, android.hardware.SensorManager.SENSOR_DELAY_GAME);
        }
        phoneAccelerometerMutableArray=new MutableArray();
        phoneLinearAccelerometerMutableArray=new MutableArray();
        phoneGyroscopeMutableArray=new MutableArray();
        phoneMagnetoMutableArray=new MutableArray();
    }

    public void unRegisterPhoneListeners(){
        mPhoneSensorManager.unregisterListener(this,mAccelerometer);
        mPhoneSensorManager.unregisterListener(this,mGyroscope);
        mPhoneSensorManager.unregisterListener(this,mLinearAccelerometer);
        mPhoneSensorManager.unregisterListener(this,mMagneto);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setDouble("W",0);
        dictionary.setDouble("X",event.values[0]);
        dictionary.setDouble("Y",event.values[1]);
        dictionary.setDouble("Z",event.values[2]);
        dictionary.setDouble("TimeStamp",event.timestamp);
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                phoneAccelerometerMutableArray.addDictionary(dictionary);
                break;
            case Sensor.TYPE_GYROSCOPE:
                phoneGyroscopeMutableArray.addDictionary(dictionary);
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                phoneLinearAccelerometerMutableArray.addDictionary(dictionary);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                phoneMagnetoMutableArray.addDictionary(dictionary);
                break;
            default:
                break;
        }
        checkPhoneSize();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public MutableArray getPhoneAccelerometerMutableArray() {
        MutableArray array=phoneAccelerometerMutableArray;
        phoneAccelerometerMutableArray=new MutableArray();
        return array;
    }

    public MutableArray getPhoneLinearAccelerometerMutableArray() {
        MutableArray array=phoneLinearAccelerometerMutableArray;
        phoneLinearAccelerometerMutableArray=new MutableArray();
        return array;
    }

    public MutableArray getPhoneGyroscopeMutableArray() {
        MutableArray array=phoneGyroscopeMutableArray;
        phoneGyroscopeMutableArray=new MutableArray();
        return array;

    }

    public MutableArray getPhoneMagnetoMutableArray() {
        MutableArray array=phoneMagnetoMutableArray;
        phoneMagnetoMutableArray=new MutableArray();
        return array;
    }

    public int getSubsession() {
        return subsession;
    }

    private void nextSubsession(){
        subsession+=1;
    }

    private void checkPhoneSize() {
        int size=phoneAccelerometerMutableArray.count()+
                phoneMagnetoMutableArray.count()+
                phoneGyroscopeMutableArray.count()+
                phoneLinearAccelerometerMutableArray.count();

        Log.d(TAG, "checkPhoneSize: "+size);

        if(size>1000){
            Log.d(TAG, "checkPhoneSize: ");
            callback.doPhoneSubsection();
            nextSubsession();
        }
    }


}
