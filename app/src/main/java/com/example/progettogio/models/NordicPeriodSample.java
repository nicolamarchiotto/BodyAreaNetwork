package com.example.progettogio.models;

import android.util.Log;

import com.couchbase.lite.MutableDictionary;
import com.example.progettogio.interfaces.SubSectionCallback;

import java.sql.Timestamp;

public class NordicPeriodSample {

    private static final String TAG = "NordicPeriodSample";

    private static int ARRAYDIMENSION=1000;

    private String nordicAddress;
    private int subsession;
    private String nordicName;
    private SubSectionCallback callback;
    private int nordicNumber;

    //    private MutableDictionary[] myArray=new MutableDictionary[500];
    private MutableDictionary[] quaternionArray;
    private MutableDictionary[] accelerometerArray;
    private MutableDictionary[] gyroscopeArray;
    private MutableDictionary[] compassArray;
    private MutableDictionary[] eulerAngleArray;
    private MutableDictionary[] headingArray;
    private MutableDictionary[] gravityVectorArray;
    private int quaternionIndex;
    private int gyroscopeIndex;
    private int accelerometerIndex;
    private int compassIndex;
    private int eulerAngleIndex;
    private int headingIndex;
    private int gravityVectorIndex;

    private MutableDictionary[] quaternionSupportArray;
    private MutableDictionary[] gyroscopeSupportArray;
    private MutableDictionary[] accelerometerSupportArray;
    private MutableDictionary[] compassSupportArray;
    private MutableDictionary[] eulerSupportArray;
    private MutableDictionary[] headingSupportArray;
    private MutableDictionary[] gravityVectorSupportArray;

    private Boolean creatingSupportArray=false;


    public NordicPeriodSample(String thingyName, String deviceAddress, int nordicNumber, SubSectionCallback subsessionCallback,int arrayDimension) {
        nordicAddress = deviceAddress;
        subsession = 0;
        nordicName = thingyName;
        callback = subsessionCallback;
        ARRAYDIMENSION=arrayDimension;
        this.nordicNumber=nordicNumber;

        quaternionArray = new MutableDictionary[ARRAYDIMENSION];
        accelerometerArray = new MutableDictionary[ARRAYDIMENSION];
        gyroscopeArray = new MutableDictionary[ARRAYDIMENSION];
        compassArray = new MutableDictionary[ARRAYDIMENSION];
        eulerAngleArray = new MutableDictionary[ARRAYDIMENSION];
        headingArray = new MutableDictionary[ARRAYDIMENSION];
        gravityVectorArray = new MutableDictionary[ARRAYDIMENSION];

        quaternionIndex = 0;
        gyroscopeIndex = 0;
        accelerometerIndex = 0;
        compassIndex = 0;
        eulerAngleIndex = 0;
        headingIndex = 0;
        gravityVectorIndex = 0;
    }

    public void addThingyQuaternionData(String bluetoothDeviceAddress, float w, float x, float y, float z, Timestamp timestamp) {
        MutableDictionary dictionary = new MutableDictionary();
        dictionary.setDouble("W", w);
        dictionary.setDouble("X", x);
        dictionary.setDouble("Y", y);
        dictionary.setDouble("Z", z);
        dictionary.setString("TimeStamp", timestamp.toString());
        try {
            quaternionArray[quaternionIndex] = dictionary;
            quaternionIndex += 1;
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.d(TAG, "addThingyQuaternionData: ArrayIndexOutOfBoundsException");
            doSubsection();
        }
    }

    public void addThingyAccelerometerData(String bluetoothDeviceAddress, float x, float y, float z, Timestamp timestamp) {
        MutableDictionary dictionary = new MutableDictionary();
        dictionary.setDouble("X", x);
        dictionary.setDouble("Y", y);
        dictionary.setDouble("Z", z);
        dictionary.setString("TimeStamp", timestamp.toString());
        try {
            accelerometerArray[accelerometerIndex] = dictionary;
            accelerometerIndex += 1;
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.d(TAG, "addThingyAccelerometerData: ArrayIndexOutOfBoundsException");
            doSubsection();
        }

    }

    public void addThingyGyroscopeData(String bluetoothDeviceAddress, float x, float y, float z, Timestamp timestamp) {
        MutableDictionary dictionary = new MutableDictionary();
        dictionary.setDouble("X", x);
        dictionary.setDouble("Y", y);
        dictionary.setDouble("Z", z);
        dictionary.setString("TimeStamp", timestamp.toString());
        try {
            gyroscopeArray[gyroscopeIndex] = dictionary;
            gyroscopeIndex += 1;
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.d(TAG, "addThingyGyroscopeData: ArrayIndexOutOfBoundsException");
            doSubsection();
        }
    }

    public void addThingyCompassData(String bluetoothDeviceAddress, float x, float y, float z, Timestamp timestamp) {
        MutableDictionary dictionary = new MutableDictionary();
        dictionary.setDouble("X", x);
        dictionary.setDouble("Y", y);
        dictionary.setDouble("Z", z);
        dictionary.setString("TimeStamp", timestamp.toString());
        try {
            compassArray[compassIndex] = dictionary;
            compassIndex += 1;
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.d(TAG, "addThingyCompassData: ArrayIndexOutOfBoundsException");
            doSubsection();
        }
    }

    public void addThingyEulerAngleData(String bluetoothDeviceAddress, float roll, float pitch, float yaw, Timestamp timestamp) {
        MutableDictionary dictionary = new MutableDictionary();
        dictionary.setDouble("ROLL", roll);
        dictionary.setDouble("PITCH", pitch);
        dictionary.setDouble("YAW", yaw);
        dictionary.setString("TimeStamp", timestamp.toString());
        try {
            eulerAngleArray[eulerAngleIndex] = dictionary;
            eulerAngleIndex += 1;
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.d(TAG, "addThingyEulerAngleData: ArrayIndexOutOfBoundsException");
            doSubsection();
        }
    }

    public void addThingyHeadingData(String bluetoothDeviceAddress, float heading, Timestamp timestamp) {
        MutableDictionary dictionary = new MutableDictionary();
        dictionary.setDouble("Heading", heading);
        dictionary.setString("TimeStamp", timestamp.toString());
        try {
            headingArray[headingIndex] = dictionary;
            headingIndex += 1;
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.d(TAG, "addThingyHeadingData: ArrayIndexOutOfBoundsException");
            doSubsection();
        }
    }

    public void addThingyGravityVectorData(String bluetoothDeviceAddress, float x, float y, float z, Timestamp timestamp) {
        MutableDictionary dictionary = new MutableDictionary();
        dictionary.setDouble("X", x);
        dictionary.setDouble("Y", y);
        dictionary.setDouble("Z", z);
        dictionary.setString("TimeStamp", timestamp.toString());
        try {
            gravityVectorArray[gravityVectorIndex] = dictionary;
            gravityVectorIndex += 1;
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.d(TAG, "addThingyGravityVectorData: ArrayIndexOutOfBoundsException");
            doSubsection();
        }
//        Log.d(TAG, "addThingyGravityVectorData: Vector Size: "+gravityVectorArray.length+" X:"+dictionary.getDouble("X")+" Y:"+dictionary.getDouble("Y")
//                +" Z:"+dictionary.getDouble("Z")+" Timestamp"+dictionary.getLong("TimeStamp"));
    }

    public void doSubsection(){
        if(!creatingSupportArray){
            creatingSupportArray=true;

            quaternionSupportArray = quaternionArray;
            quaternionArray = new MutableDictionary[ARRAYDIMENSION];
            quaternionIndex = 0;

            gyroscopeSupportArray = accelerometerArray;
            accelerometerArray = new MutableDictionary[ARRAYDIMENSION];
            accelerometerIndex = 0;

            accelerometerSupportArray = gyroscopeArray;
            gyroscopeArray = new MutableDictionary[ARRAYDIMENSION];
            gyroscopeIndex = 0;

            compassSupportArray = compassArray;
            compassArray = new MutableDictionary[ARRAYDIMENSION];
            compassIndex = 0;

            eulerSupportArray = eulerAngleArray;
            eulerAngleArray = new MutableDictionary[ARRAYDIMENSION];
            eulerAngleIndex = 0;

            headingSupportArray = headingArray;
            headingArray = new MutableDictionary[ARRAYDIMENSION];
            headingIndex = 0;

            gravityVectorSupportArray = gravityVectorArray;
            gravityVectorArray = new MutableDictionary[ARRAYDIMENSION];
            gravityVectorIndex = 0;

            callback.doNordicSubsection(nordicAddress,subsession);
            subsession+=1;

            creatingSupportArray=false;
        }
        else
            return;

    }

    public MutableDictionary[] getQuaternionArray() {
        return quaternionArray;
    }

    public MutableDictionary[] getAccelerometerArray() {
        return accelerometerArray;
    }

    public MutableDictionary[] getGyroscopeArray() {
        return gyroscopeArray;
    }

    public MutableDictionary[] getCompassArray() {
        return compassArray;
    }

    public MutableDictionary[] getEulerAngleArray() {
        return eulerAngleArray;
    }

    public MutableDictionary[] getHeadingArray() {
        return headingArray;
    }

    public MutableDictionary[] getGravityVectorArray() {
        return gravityVectorArray;
    }

    //support

    public MutableDictionary[] getQuaternionSupportArray() {
        return quaternionSupportArray;
    }

    public MutableDictionary[] getGyroscopeSupportArray() {
        return gyroscopeSupportArray;
    }

    public MutableDictionary[] getAccelerometerSupportArray() {
        return accelerometerSupportArray;
    }

    public MutableDictionary[] getCompassSupportArray() {
        return compassSupportArray;
    }

    public MutableDictionary[] getEulerSupportArray() {
        return eulerSupportArray;
    }

    public MutableDictionary[] getHeadingSupportArray() {
        return headingSupportArray;
    }

    public MutableDictionary[] getGravityVectorSupportArray() {
        return gravityVectorSupportArray;
    }

    public String getNordicName() {
        return nordicName;
    }

    public String getNordicAddress() {
        return nordicAddress;
    }

    public int getNordicNumber() { return nordicNumber; }

    public void setNordicAddress(String nordicAddress) {
        this.nordicAddress = nordicAddress;
    }

    public int getSubsection() {
        return subsession;
    }


}