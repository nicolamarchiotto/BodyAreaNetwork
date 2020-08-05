package com.example.progettogio.models;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.List;

public class NordicSensorList {

    private BluetoothDevice bluetoothDevice;
    private ArrayList<NordicSensor> sensorList;

    public NordicSensorList(BluetoothDevice bluetoothDevice) {
        ArrayList<NordicSensor> list=new ArrayList<>();

        list.add(0,new NordicSensor("temperature"));
        list.add(1,new NordicSensor("humidity"));
        list.add(2,new NordicSensor("pressure"));
        list.add(3,new NordicSensor("airQuality"));
        list.add(4,new NordicSensor("colorAndLightIntensity"));
        list.add(5,new NordicSensor("buttonState"));
        list.add(6,new NordicSensor("tapDetection"));
        list.add(7,new NordicSensor("orientation"));
        list.add(8,new NordicSensor("stepCounter"));
        list.add(9,new NordicSensor("quaternions"));
        list.add(10,new NordicSensor("eulerAngles"));
        list.add(11,new NordicSensor("rotationMatrix"));
        list.add(12,new NordicSensor("gravityVector"));
        list.add(13,new NordicSensor("compassHeading"));
        list.add(14,new NordicSensor("rawAccelGyroCompassData"));
        list.add(15,new NordicSensor("speaker"));
        list.add(16,new NordicSensor("microphone"));


        list.get(9).setState(true);
        list.get(10).setState(true);
        list.get(12).setState(true);
        list.get(13).setState(true);
        list.get(14).setState(true);
//
//        list.setSensorState(9,true);
//        list.setSensorState(11,true);
//        list.setSensorState(12,true);
//        list.setSensorState(13,true);


        this.sensorList=list;
        this.bluetoothDevice=bluetoothDevice;
    }

    public int size(){
        return sensorList.size();
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setSensorState(int index, Boolean state){
        sensorList.get(index).setState(state);
    }

    public List<String> getSensorNames(){
        List<String> list=new ArrayList<>();
        list.add(0,"temperature");
        list.add(1,"humidity");
        list.add(2,"pressure");
        list.add(3,"airQuality");
        list.add(4,"colorAndLightIntensity");
        list.add(5,"buttonState");
        list.add(6,"tapDetection");
        list.add(7,"orientation");
        list.add(8,"stepCounter");
        list.add(9,"quaternions");
        list.add(10,"eulerAngles");
        list.add(11,"rotationMatrix");
        list.add(12,"gravityVector");
        list.add(13,"compassHeading");
        list.add(14,"rawAccelGyroCompassData");
        list.add(15,"speaker");
        list.add(16,"microphone");


        return list;
    }

    public NordicSensor get(int position) {
        return sensorList.get(position);
    }

    public class NordicSensor{
        private String name;
        private boolean state;

        private NordicSensor(String name){
            this.name=name;
            this.state=false;
        }

        public String getName() {
            return name;
        }

        public boolean getState() {
            return state;
        }

        public void setState(boolean state) {
            this.state = state;
        }
    }
}
