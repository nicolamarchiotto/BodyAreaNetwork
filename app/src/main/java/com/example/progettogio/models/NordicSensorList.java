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
        list.add(5,new NordicSensor("tapDetection"));
        list.add(6,new NordicSensor("orientation"));
        list.add(7,new NordicSensor("stepCounter"));
        list.add(8,new NordicSensor("quaternions"));
        list.add(9,new NordicSensor("eulerAngles"));
        list.add(10,new NordicSensor("rotationMatrix"));
        list.add(11,new NordicSensor("gravityVector"));
        list.add(12,new NordicSensor("compassHeading"));
        list.add(13,new NordicSensor("rawAccelGyroCompassData"));
        list.add(14,new NordicSensor("speaker"));
        list.add(15,new NordicSensor("microphone"));

        list.get(8).setState(true);
        list.get(9).setState(true);
        list.get(11).setState(true);
        list.get(12).setState(true);
        list.get(13).setState(true);


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
        list.add(5,"tapDetection");
        list.add(6,"orientation");
        list.add(7,"stepCounter");
        list.add(8,"quaternions");
        list.add(9,"eulerAngles");
        list.add(10,"rotationMatrix");
        list.add(11,"gravityVector");
        list.add(12,"compassHeading");
        list.add(13,"rawAccelGyroCompassData");
        list.add(14,"speaker");
        list.add(15,"microphone");

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
