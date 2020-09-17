package com.example.progettogio.models;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.Nullable;

import com.example.progettogio.utils.DevicesEnum;

public class GeneralDevice {
    private BluetoothDevice bluetoothDevice;
    private DevicesEnum type;
    private int batteryLevel;
    private String deviceName;

    @Override
    public boolean equals(@Nullable Object obj) {
        return ((GeneralDevice) this).bluetoothDevice.getAddress().equals(((GeneralDevice) obj).getAddress());
    }

    public GeneralDevice(BluetoothDevice bluetoothDevice, DevicesEnum type, int batteryLevel,String deviceName) {
        this.bluetoothDevice = bluetoothDevice;
        this.type = type;
        this.batteryLevel = batteryLevel;
        this.deviceName=deviceName;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }


    public void setType(DevicesEnum type) {
        this.type = type;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public DevicesEnum getType() {
        return type;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public String getAddress(){
        return bluetoothDevice.getAddress();
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }
}
