package com.example.progettogio.models;

import android.util.Log;

import com.couchbase.lite.MutableArray;
import com.couchbase.lite.MutableDictionary;
import com.example.progettogio.interfaces.SubSectionCallback;

/**
 * Oggetto che contiene le liste di ogni sensore, contenente i dati della registrazione.
 */
public class NordicPeriodSample {

    private static final String TAG = "NordicPeriodSample";

    private String nordicAddress;
    private int subsession;
    private String nordicName;
    private SubSectionCallback callback;

    private MutableArray thingyTemperatureMutableArray;
    private MutableArray thingyPressureMutableArray;
    private MutableArray thingyHumidityMutableArray;
    private MutableArray thingyAirQualityMutableArray;
    private MutableArray thingyColorIntensityMutableArray;
    private MutableArray thingyButtonStateMutableArray;
    private MutableArray thingyTapMutableArray;
    private MutableArray thingyOrientationMutableArray;
    private MutableArray thingyQuaternionMutableArray;
    private MutableArray thingyPedometerMutableArray;
    private MutableArray thingyAccelerometerMutableArray;
    private MutableArray thingyGyroscopeMutableArray;
    private MutableArray thingyCompassMutableArray;
    private MutableArray thingyEulerAngleMutableArray;
    private MutableArray thingyRotationMatrixMutableArray;
    private MutableArray thingyHeadingMutableArray;
    private MutableArray thingyGravityVectorMutableArray;
    private MutableArray thingySpeakerMutableArray;
    private MutableArray thingyMicrophoneMutableArray;


    public NordicPeriodSample(String thingyName, String deviceAddress, SubSectionCallback subsessionCallback){
        nordicAddress=deviceAddress;
        subsession=0;
        nordicName=thingyName;
        callback=subsessionCallback;

        thingyTemperatureMutableArray=new MutableArray();
        thingyPressureMutableArray=new MutableArray();
        thingyHumidityMutableArray=new MutableArray();
        thingyAirQualityMutableArray=new MutableArray();
        thingyColorIntensityMutableArray=new MutableArray();
        thingyButtonStateMutableArray=new MutableArray();
        thingyTapMutableArray=new MutableArray();
        thingyOrientationMutableArray=new MutableArray();
        thingyQuaternionMutableArray=new MutableArray();
        thingyPedometerMutableArray=new MutableArray();
        thingyAccelerometerMutableArray=new MutableArray();
        thingyGyroscopeMutableArray=new MutableArray();
        thingyCompassMutableArray=new MutableArray();
        thingyEulerAngleMutableArray=new MutableArray();
        thingyRotationMatrixMutableArray=new MutableArray();
        thingyHeadingMutableArray=new MutableArray();
        thingyGravityVectorMutableArray=new MutableArray();
        thingySpeakerMutableArray=new MutableArray();
        thingyMicrophoneMutableArray=new MutableArray();
    }

    public void addThingyTemperatureData(String bluetoothDeviceAddress,String temperature,long timestamp){
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setString("Temperature",temperature);
        dictionary.setDouble("TimeStamp",timestamp);
        thingyTemperatureMutableArray.addValue(dictionary);
        checksize();
    }
    public void addThingyPressureData(String bluetoothDeviceAddress,String pressure,long timestamp){
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setString("Pressure",pressure);
        dictionary.setDouble("TimeStamp",timestamp);
        thingyPressureMutableArray.addValue(dictionary);
        checksize();
    }
    public void addThingyHumidityData(String bluetoothDeviceAddress,String humidity,long timestamp){
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setString("Humidity",humidity);
        dictionary.setDouble("TimeStamp",timestamp);
        thingyHumidityMutableArray.addValue(dictionary);
        checksize();
    }
    public void addThingyAirQualityData(String bluetoothDeviceAddress,int eco2,int tvoc,long timestamp){
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setInt("Eco2",eco2);
        dictionary.setInt("Tvoc",tvoc);
        dictionary.setDouble("TimeStamp",timestamp);
        thingyAirQualityMutableArray.addValue(dictionary);
        checksize();
    }
    public void addThingyColorIntensityData(String bluetoothDeviceAddress, float red, float green, float blue, float alpha, long timestamp) {
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setDouble("R",red);
        dictionary.setDouble("G",green);
        dictionary.setDouble("B",blue);
        dictionary.setDouble("Alpha",alpha);
        dictionary.setDouble("TimeStamp",timestamp);
        thingyColorIntensityMutableArray.addValue(dictionary);
        checksize();
    }
    public void addThingyButtonStateData(String bluetoothDeviceAddress, int buttonState, long timestamp) {
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setInt("ButtonState",buttonState);
        dictionary.setDouble("TimeStamp",timestamp);
        thingyButtonStateMutableArray.addValue(dictionary);
        checksize();
    }
    public void addThingyTapData(String bluetoothDeviceAddress,int direction,int count,long timestamp){
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setInt("Direction",direction);
        dictionary.setInt("Count",count);
        dictionary.setDouble("TimeStamp",timestamp);
        thingyTapMutableArray.addValue(dictionary);
        checksize();
    }
    public void addThingyOrientationData(String bluetoothDeviceAddress,int orientation,long timestamp){
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setInt("Orientation",orientation);
        dictionary.setDouble("TimeStamp",timestamp);
        thingyOrientationMutableArray.addValue(dictionary);
        checksize();
    }

    public void addThingyQuaternionData(String bluetoothDeviceAddress, float w, float x, float y, float z, long timestamp) {
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setDouble("W",w);
        dictionary.setDouble("X",x);
        dictionary.setDouble("Y",y);
        dictionary.setDouble("Z",z);
        dictionary.setDouble("TimeStamp",timestamp);
        thingyQuaternionMutableArray.addValue(dictionary);
        checksize();
    }
    public void addThingyPedometerData(String bluetoothDeviceAddress,int steps,long duration,long timestamp){
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setInt("Steps",steps);
        dictionary.setLong("Duration",duration);
        dictionary.setDouble("TimeStamp",timestamp);
        thingyPedometerMutableArray.addValue(dictionary);
        checksize();
    }

    public void addThingyAccelerometerData(String bluetoothDeviceAddress, float x, float y, float z, long timestamp) {
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setDouble("X",x);
        dictionary.setDouble("Y",y);
        dictionary.setDouble("Z",z);
        dictionary.setDouble("TimeStamp",timestamp);
        thingyAccelerometerMutableArray.addValue(dictionary);
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

    //TODO come gestire una matrice in un documento couchBase??? Vedi anche onMicrophoneValueChangeEvent
    public void addThingyRotationMatrixData(String bluetoothDeviceAddress, byte matrix[],long timestamp) {
//        Log.d(TAG, "addThingyRotationMatrixData: "+new String(matrix));
//        MutableDictionary dictionary=new MutableDictionary();
//        dictionary.setString("RotationMatrix",new String(matrix));
//        dictionary.setDouble("TimeStamp",timestamp);
//        thingyRotationMatrixMutableArray.addValue(dictionary);
//        checksize();
    }
    public void addThingyHeadingData(String bluetoothDeviceAddress, float heading,long timestamp) {
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setDouble("Heading",heading);
        dictionary.setDouble("TimeStamp",timestamp);
        thingyHeadingMutableArray.addValue(dictionary);
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
    public void addThingySpeakerStatusData(String bluetoothDeviceAddress, int status, long timestamp) {
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setInt("Status",status);
        dictionary.setDouble("TimeStamp",timestamp);
        thingySpeakerMutableArray.addValue(dictionary);
        checksize();
    }
    public void addThingyMicrophoneData(String bluetoothDeviceAddress, byte data[], long timestamp) {
        MutableDictionary dictionary=new MutableDictionary();
        dictionary.setValue("Data",data);
        dictionary.setDouble("TimeStamp",timestamp);
        thingyMicrophoneMutableArray.addValue(dictionary);
        checksize();
    }

    public MutableArray getThingyTemperatureMutableArray() {
        MutableArray array=thingyTemperatureMutableArray;
        thingyTemperatureMutableArray=new MutableArray();
        return array;
    }

    public MutableArray getThingyPressureMutableArray() {
        MutableArray array=thingyPressureMutableArray;
        thingyPressureMutableArray=new MutableArray();
        return array;
    }

    public MutableArray getThingyHumidityMutableArray() {
        MutableArray array=thingyHumidityMutableArray;
        thingyHumidityMutableArray=new MutableArray();
        return array;
    }

    public MutableArray getThingyAirQualityMutableArray() {
        MutableArray array=thingyAirQualityMutableArray;
        thingyAirQualityMutableArray=new MutableArray();
        return array;
    }

    public MutableArray getThingyColorIntensityMutableArray() {
        MutableArray array=thingyColorIntensityMutableArray;
        thingyColorIntensityMutableArray=new MutableArray();
        return array;
    }

    public MutableArray getThingyButtonStateMutableArray() {
        MutableArray array=thingyButtonStateMutableArray;
        thingyButtonStateMutableArray=new MutableArray();
        return array;
    }

    public MutableArray getThingyTapMutableArray() {
        MutableArray array=thingyTapMutableArray;
        thingyTapMutableArray=new MutableArray();
        return array;
    }

    public MutableArray getThingyOrientationMutableArray() {
        MutableArray array=thingyOrientationMutableArray;
        thingyOrientationMutableArray=new MutableArray();
        return array;
    }

    public MutableArray getThingyPedometerMutableArray() {
        MutableArray array=thingyPedometerMutableArray;
        thingyPedometerMutableArray=new MutableArray();
        return array;
    }

    public MutableArray getThingyAccelerometerMutableArray() {
        MutableArray array=thingyAccelerometerMutableArray;
        thingyAccelerometerMutableArray=new MutableArray();
        return array;
    }

    public MutableArray getThingyRotationMatrixMutableArray() {
        MutableArray array=thingyRotationMatrixMutableArray;
        thingyRotationMatrixMutableArray=new MutableArray();
        return array;
    }

    public MutableArray getThingyHeadingMutableArray() {
        MutableArray array=thingyHeadingMutableArray;
        thingyHeadingMutableArray=new MutableArray();
        return array;
    }

    public MutableArray getThingySpeakerMutableArray() {
        MutableArray array=thingySpeakerMutableArray;
        thingySpeakerMutableArray=new MutableArray();
        return array;
    }

    public MutableArray getThingyMicrophoneMutableArray() {
        MutableArray array=thingyMicrophoneMutableArray;
        thingyMicrophoneMutableArray=new MutableArray();
        return array;
    }

    public MutableArray getThingyQuaternionMutableArray() {
        MutableArray array=thingyQuaternionMutableArray;
        thingyQuaternionMutableArray=new MutableArray();
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

    public String getNordicName() {
        return nordicName;
    }

    public void checksize(){
        int size= thingyTemperatureMutableArray.count()+
                    thingyPressureMutableArray.count()+
                    thingyHumidityMutableArray.count()+
                    thingyAirQualityMutableArray.count()+
                    thingyColorIntensityMutableArray.count()+
                    thingyButtonStateMutableArray.count()+
                    thingyTapMutableArray.count()+
                    thingyOrientationMutableArray.count()+
                    thingyQuaternionMutableArray.count()+
                    thingyPedometerMutableArray.count()+
                    thingyAccelerometerMutableArray.count()+
                    thingyGyroscopeMutableArray.count()+
                    thingyCompassMutableArray.count()+
                    thingyEulerAngleMutableArray.count()+
                    thingyRotationMatrixMutableArray.count()+
                    thingyHeadingMutableArray.count()+
                    thingyGravityVectorMutableArray.count()+
                    thingySpeakerMutableArray.count()+
                    thingyMicrophoneMutableArray.count();

        Log.d(TAG, "checksize: size: "+size);

        if (size>5000) {
            Log.d(TAG, "checksize: ");
            callback.doNordicSubsection(nordicAddress);
            nextSubsession();
        }
    }
}
