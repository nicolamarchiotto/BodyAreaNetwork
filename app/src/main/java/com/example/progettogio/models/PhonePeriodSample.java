package com.example.progettogio.models;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.couchbase.lite.MutableDictionary;
import com.example.progettogio.interfaces.SubSectionCallback;

import java.sql.Timestamp;

public class PhonePeriodSample implements SensorEventListener {

    private static final String TAG = "PhonePeriodSample";

    private static int ARRAYDIMENSION=1000;

    private SensorManager mPhoneSensorManager;
    private Sensor mAccelerometer;
    private Sensor mLinearAccelerometer;
    private Sensor mGyroscope;
    private Sensor mMagneto;

    private MutableDictionary[] phoneAccelerometerMutableArray;
    private MutableDictionary[] phoneLinearAccelerometerMutableArray;
    private MutableDictionary[] phoneGyroscopeMutableArray;
    private MutableDictionary[] phoneMagnetoMutableArray;

    private MutableDictionary[] phoneAccelerometerSupportMutableArray;
    private MutableDictionary[] phoneLinearAccelerometerSupportMutableArray;
    private MutableDictionary[] phoneGyroscopeSupportMutableArray;
    private MutableDictionary[] phoneMagnetoSupportMutableArray;

    private int accelerometerIndex=0;
    private int linearAccelerometerIndex=0;
    private int gyroscopeIndex=0;
    private int magnetoIndex=0;

    private Boolean creatingSupportArray=false;

    private int subSession =0;
    private SubSectionCallback callback;


    public PhonePeriodSample(SensorManager phoneSensorManager, SubSectionCallback subsessionCallback,int arrayDimension){
        mPhoneSensorManager=phoneSensorManager;
        callback=subsessionCallback;
        ARRAYDIMENSION=arrayDimension;
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
        phoneAccelerometerMutableArray=new MutableDictionary[ARRAYDIMENSION];
        phoneLinearAccelerometerMutableArray=new MutableDictionary[ARRAYDIMENSION];
        phoneGyroscopeMutableArray=new MutableDictionary[ARRAYDIMENSION];
        phoneMagnetoMutableArray=new MutableDictionary[ARRAYDIMENSION];
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
//        dictionary.setDouble("W",0);
        dictionary.setDouble("X",event.values[0]);
        dictionary.setDouble("Y",event.values[1]);
        dictionary.setDouble("Z",event.values[2]);
        dictionary.setString("TimeStamp", String.valueOf(new Timestamp(System.currentTimeMillis())));
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                try{
                    phoneAccelerometerMutableArray[accelerometerIndex]=dictionary;
                    accelerometerIndex+=1;
                }
                catch (ArrayIndexOutOfBoundsException e){
                    Log.d(TAG, "onSensorChanged: phoneAccelerometerMutableArray");
                    doSubsection();
                }
                break;
            case Sensor.TYPE_GYROSCOPE:
                try{
                    phoneGyroscopeMutableArray[gyroscopeIndex]=dictionary;
                    gyroscopeIndex+=1;
                }
                catch (ArrayIndexOutOfBoundsException e){
                    Log.d(TAG, "onSensorChanged: phoneGyroscopeMutableArray");
                    doSubsection();
                }
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                try{
                    phoneLinearAccelerometerMutableArray[linearAccelerometerIndex]=dictionary;
                    linearAccelerometerIndex+=1;
                }
                catch (ArrayIndexOutOfBoundsException e){
                    Log.d(TAG, "onSensorChanged: phoneLinearAccelerometerMutableArray");
                    doSubsection();
                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                try{
                    phoneMagnetoMutableArray[magnetoIndex]=dictionary;
                    magnetoIndex+=1;
                }
                catch (ArrayIndexOutOfBoundsException e){
                    Log.d(TAG, "onSensorChanged: phoneMagnetoMutableArray");
                    doSubsection();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void doSubsection(){
        if(!creatingSupportArray){
            creatingSupportArray=true;

            phoneAccelerometerSupportMutableArray=phoneAccelerometerMutableArray;
            phoneAccelerometerMutableArray=new MutableDictionary[ARRAYDIMENSION];
            accelerometerIndex=0;

            phoneLinearAccelerometerSupportMutableArray=phoneLinearAccelerometerMutableArray;
            phoneLinearAccelerometerMutableArray=new MutableDictionary[ARRAYDIMENSION];
            linearAccelerometerIndex=0;

            phoneGyroscopeSupportMutableArray=phoneGyroscopeMutableArray;
            phoneGyroscopeMutableArray=new MutableDictionary[ARRAYDIMENSION];
            gyroscopeIndex=0;

            phoneMagnetoSupportMutableArray=phoneMagnetoMutableArray;
            phoneMagnetoMutableArray=new MutableDictionary[ARRAYDIMENSION];
            magnetoIndex=0;

            callback.doPhoneSubsection(subSession);
            subSession +=1;

            creatingSupportArray=false;
        }
    }

    public MutableDictionary[] getPhoneAccelerometerMutableArray() {
        return phoneAccelerometerMutableArray;
    }

    public MutableDictionary[] getPhoneLinearAccelerometerMutableArray() {
        return phoneLinearAccelerometerMutableArray;
    }

    public MutableDictionary[] getPhoneGyroscopeMutableArray() {
        return phoneGyroscopeMutableArray;
    }

    public MutableDictionary[] getPhoneMagnetoMutableArray() {
        return phoneMagnetoMutableArray;
    }

    public MutableDictionary[] getPhoneAccelerometerSupportMutableArray() {
        return phoneAccelerometerSupportMutableArray;
    }

    public MutableDictionary[] getPhoneLinearAccelerometerSupportMutableArray() {
        return phoneLinearAccelerometerSupportMutableArray;
    }

    public MutableDictionary[] getPhoneGyroscopeSupportMutableArray() {
        return phoneGyroscopeSupportMutableArray;
    }

    public MutableDictionary[] getPhoneMagnetoSupportMutableArray() {
        return phoneMagnetoSupportMutableArray;
    }

    public int getSubSession() {
        return subSession   ;
    }

    private void nextSubsession(){
        subSession +=1;
    }




}
