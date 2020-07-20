package com.example.progettogio.models;

import android.util.Log;

import com.couchbase.lite.MutableArray;
import com.couchbase.lite.MutableDictionary;
import com.example.progettogio.callback.SubSectionCallback;

/**
 * Oggetto che contiene le liste di ogni sensore, contenente i dati della registrazione.
 */
public class NordicPeriodSample {

    private static final String TAG = "NordicPeriodSample";

    private String id;
    private String nordicAddress;
    private MutableArray thingyQuaternionMutableArray;
    private MutableArray thingyAccellerometerMutableArray;
    private MutableArray thingyGyroscopeMutableArray;
    private MutableArray thingyCompassMutableArray;
    private MutableArray thingyEulerAngleMutableArray;
    private MutableArray thingyGravityVectorMutableArray;
    private int subsession;
    private int nordic_num;
    private SubSectionCallback callback;

    public NordicPeriodSample(int nordic_number, String deviceAddress, SubSectionCallback subsessionCallback){
        thingyQuaternionMutableArray=new MutableArray();
        thingyAccellerometerMutableArray=new MutableArray();
        thingyGyroscopeMutableArray=new MutableArray();
        thingyCompassMutableArray=new MutableArray();
        thingyEulerAngleMutableArray=new MutableArray();
        thingyGravityVectorMutableArray=new MutableArray();
//        id=session_id;
        nordicAddress=deviceAddress;
        subsession=0;
        nordic_num=nordic_number;
        callback=subsessionCallback;
    }

    public void addThingyQuaternionData(String q, float w, float x, float y, float z, long timestamp) {
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setDouble("W",w);
        dictionary.setDouble("X",x);
        dictionary.setDouble("Y",y);
        dictionary.setDouble("Z",z);
        dictionary.setDouble("TimeStamp",timestamp);
        thingyQuaternionMutableArray.addValue(dictionary);
        checksize();

    }

    public void addThingyAccellerometerData(String bluetoothDeviceAddress, float x, float y, float z,long timestamp) {
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setDouble("X",x);
        dictionary.setDouble("Y",y);
        dictionary.setDouble("Z",z);
        dictionary.setDouble("TimeStamp",timestamp);
        thingyAccellerometerMutableArray.addValue(dictionary);
        checksize();

    }

    public void addThingyGyroscopeData(String bluetoothDeviceAddress, float x, float y, float z,long timestamp) {
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setDouble("X",x);
        dictionary.setDouble("Y",y);
        dictionary.setDouble("Z",z);
        dictionary.setDouble("TimeStamp",timestamp);
        thingyGyroscopeMutableArray.addValue(dictionary);
        checksize();

    }

    public void addThingyCompassData(String bluetoothDeviceAddress, float x, float y, float z,long timestamp) {
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setDouble("X",x);
        dictionary.setDouble("Y",y);
        dictionary.setDouble("Z",z);
        dictionary.setDouble("TimeStamp",timestamp);
        thingyCompassMutableArray.addValue(dictionary);
        checksize();

    }

    public void addThingyEulerAngleData(String bluetoothDeviceAddress, float roll, float pitch, float yaw,long timestamp) {
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setDouble("ROLL",roll);
        dictionary.setDouble("PITCH",pitch);
        dictionary.setDouble("YAW",yaw);
        dictionary.setDouble("TimeStamp",timestamp);
        thingyEulerAngleMutableArray.addValue(dictionary);
        checksize();
    }

    public void addThingyGravityVectorData(String bluetoothDeviceAddress, float x, float y, float z, long timestamp) {
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setDouble("X",x);
        dictionary.setDouble("Y",y);
        dictionary.setDouble("Z",z);
        dictionary.setDouble("TimeStamp",timestamp);
        thingyGravityVectorMutableArray.addValue(dictionary);
        checksize();
        Log.d(TAG, "addThingyGravityVectorData: Vector Size: "+thingyGravityVectorMutableArray.count()+" X:"+dictionary.getDouble("X")+" Y:"+dictionary.getDouble("Y")
                +" Z:"+dictionary.getDouble("Z")+" Timestamp"+dictionary.getLong("TimeStamp"));
    }
    public MutableArray getThingyQuaternionMutableArray() {
        MutableArray array=thingyQuaternionMutableArray;
        thingyQuaternionMutableArray=new MutableArray();
        return array;
    }

    public MutableArray getThingyAccellerometerMutableArray() {
        MutableArray array=thingyAccellerometerMutableArray;
        thingyAccellerometerMutableArray=new MutableArray();
        return array;
    }

    public MutableArray getThingyGyroscopeMutableArray() {
        MutableArray array=thingyGyroscopeMutableArray;
        thingyGyroscopeMutableArray=new MutableArray();
        return array;
    }

    public MutableArray getThingyCompassMutableArray() {
        MutableArray array=thingyCompassMutableArray;
        thingyCompassMutableArray=new MutableArray();
        return array;
    }

    public MutableArray getThingyEulerAngleMutableArray() {
        MutableArray array=thingyEulerAngleMutableArray;
        thingyEulerAngleMutableArray=new MutableArray();
        return array;
    }

    public MutableArray getThingyGravityVectorMutableArray() {
        MutableArray array=thingyGravityVectorMutableArray;
        thingyGravityVectorMutableArray=new MutableArray();
        return array;
    }

    public String getNordicAddress() { return nordicAddress; }

    public int getSubsession() { return subsession; }

    public void nextSubsession(){
        subsession+=1;
        Log.d(TAG, "nextSubsession: "+subsession);
    }

    public int getNordic_num() {
        return nordic_num;
    }

    public void checksize(){
        int size=thingyQuaternionMutableArray.count()+
                thingyAccellerometerMutableArray.count()+
                thingyGyroscopeMutableArray.count()+
                thingyCompassMutableArray.count()+
                thingyEulerAngleMutableArray.count()+
                thingyGravityVectorMutableArray.count();
        Log.d(TAG, "checksize: size: "+size);

        if (size>500) {
            Log.d(TAG, "checksize: ");
            callback.doNordicSubsection(nordicAddress);
            nextSubsession();
        }
    }
}
