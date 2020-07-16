package com.example.progettogio.models;

import android.util.Log;

import com.couchbase.lite.MutableArray;
import com.couchbase.lite.MutableDictionary;

import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * Oggetto che contiene le liste di ogni sensore, contenente i dati della registrazione.
 */
public class NordicPeriodSample {

    private String id;
    private String deviceName;
    private MutableArray thingyQuaternionMutableArray;
    private MutableArray thingyAccellerometerMutableArray;
    private MutableArray thingyGyroscopeMutableArray;
    private MutableArray thingyCompassMutableArray;
    private MutableArray thingyEulerAngleMutableArray;
    private MutableArray thingyGravityVectorMutableArray;

    public String getId() {
        return id;
    }

    public NordicPeriodSample(){
        thingyQuaternionMutableArray=new MutableArray();
        thingyAccellerometerMutableArray=new MutableArray();
        thingyGyroscopeMutableArray=new MutableArray();
        thingyCompassMutableArray=new MutableArray();
        thingyEulerAngleMutableArray=new MutableArray();
        thingyGravityVectorMutableArray=new MutableArray();
        id="prova3";


    }

    public void addThingyQuaternionData(String bluetoothDeviceAddress, float w, float x, float y, float z, long timestamp) {
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setDouble("W",w);
        dictionary.setDouble("X",x);
        dictionary.setDouble("Y",y);
        dictionary.setDouble("Z",z);
        dictionary.setDouble("TimeStamp",timestamp);
        thingyQuaternionMutableArray.addValue(dictionary);

    }

    public void addThingyAccellerometerData(String bluetoothDeviceAddress, float x, float y, float z,long timestamp) {
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setDouble("X",x);
        dictionary.setDouble("Y",y);
        dictionary.setDouble("Z",z);
        dictionary.setDouble("TimeStamp",timestamp);
        thingyAccellerometerMutableArray.addValue(dictionary);

    }

    public void addThingyGyroscopeData(String bluetoothDeviceAddress, float x, float y, float z,long timestamp) {
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setDouble("X",x);
        dictionary.setDouble("Y",y);
        dictionary.setDouble("Z",z);
        dictionary.setDouble("TimeStamp",timestamp);
        thingyGyroscopeMutableArray.addValue(dictionary);

    }

    public void addThingyCompassData(String bluetoothDeviceAddress, float x, float y, float z,long timestamp) {
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setDouble("X",x);
        dictionary.setDouble("Y",y);
        dictionary.setDouble("Z",z);
        dictionary.setDouble("TimeStamp",timestamp);
        thingyCompassMutableArray.addValue(dictionary);

    }

    public void addThingyEulerAngleData(String bluetoothDeviceAddress, float roll, float pitch, float yaw,long timestamp) {
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setDouble("ROLL",roll);
        dictionary.setDouble("PITCH",pitch);
        dictionary.setDouble("YAW",yaw);
        dictionary.setDouble("TimeStamp",timestamp);
        thingyEulerAngleMutableArray.addValue(dictionary);
    }

    public void addThingyGravityVectorData(String bluetoothDeviceAddress, float x, float y, float z, long timestamp) {
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setDouble("X",x);
        dictionary.setDouble("Y",y);
        dictionary.setDouble("Z",z);
        dictionary.setDouble("TimeStamp",timestamp);
        thingyGravityVectorMutableArray.addValue(dictionary);
        Log.d(TAG, "addThingyGravityVectorData: Vector Size: "+thingyGravityVectorMutableArray.count()+" X:"+dictionary.getDouble("X")+" Y:"+dictionary.getDouble("Y")
                +" Z:"+dictionary.getDouble("Z")+" Timestamp"+dictionary.getLong("TimeStamp"));
    }
    public MutableArray getThingyQuaternionMutableArray() {
        return thingyQuaternionMutableArray;
    }

    public MutableArray getThingyAccellerometerMutableArray() {
        return thingyAccellerometerMutableArray;
    }

    public MutableArray getThingyGyroscopeMutableArray() {
        return thingyGyroscopeMutableArray;
    }

    public MutableArray getThingyCompassMutableArray() {
        return thingyCompassMutableArray;
    }

    public MutableArray getThingyEulerAngleMutableArray() {
        return thingyEulerAngleMutableArray;
    }

    public MutableArray getThingyGravityVectorMutableArray() {
        return thingyGravityVectorMutableArray;
    }

}
