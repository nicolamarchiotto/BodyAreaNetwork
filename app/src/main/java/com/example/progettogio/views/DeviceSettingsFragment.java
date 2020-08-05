package com.example.progettogio.views;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.progettogio.R;
import com.example.progettogio.adapters.DevsSensorsListener;
import com.example.progettogio.adapters.SensorsAdapter;
import com.example.progettogio.databinding.FragmentDeviceSettingsBinding;
import com.example.progettogio.models.NordicSensorList;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import no.nordicsemi.android.thingylib.ThingySdkManager;

public class DeviceSettingsFragment extends BottomSheetDialogFragment implements DevsSensorsListener {

    private static final String TAG = "DeviceSettingsFragment";

    private FragmentDeviceSettingsBinding binding;
    private NordicSensorList mNordicSensors;
    private String devAddress = "";
    private SensorsAdapter mSensorAdapter;
    private RecyclerView mSensorsRecyclerView;
    private ThingySdkManager thingySdkManager;
    private BluetoothDevice thingyBleDevice;

    public DeviceSettingsFragment(NordicSensorList sensorList, ThingySdkManager thingySdkManager) {
        this.thingyBleDevice=sensorList.getBluetoothDevice();
        this.devAddress = thingyBleDevice.getAddress();
        this.mNordicSensors = sensorList;
        this.thingySdkManager=thingySdkManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_device_settings, container, false);
        View view = binding.getRoot();

        binding.fragmentDeviceSettingsTxtAddr.setText(devAddress);
        binding.fragmentDeviceSettingsTxtName.setText(thingyBleDevice.getName());

        mSensorsRecyclerView = binding.fragmentSensorsRecyclerView;
        LinearLayoutManager linearLayoutManagerTags = new LinearLayoutManager(this.getContext());
        linearLayoutManagerTags.setOrientation(RecyclerView.VERTICAL);
        mSensorsRecyclerView.setLayoutManager(linearLayoutManagerTags);
        mSensorAdapter = new SensorsAdapter(mNordicSensors,this);
        mSensorsRecyclerView.setAdapter(mSensorAdapter);
        return view;
    }

    @Override
    public void onCheckBoxClicked(int position, boolean state) {
        switch (position){
            case 0: thingySdkManager.enableTemperatureNotifications(thingyBleDevice,state);
                mNordicSensors.get(0).setState(state);
                Log.d(TAG, "onCheckBoxClicked: enableTemperatureNotifications:"+thingyBleDevice.getAddress());
                break;
            case 1: thingySdkManager.enableHumidityNotifications(thingyBleDevice,state);
                mNordicSensors.get(1).setState(state);
                break;
            case 2: thingySdkManager.enablePressureNotifications(thingyBleDevice,state);
                mNordicSensors.get(2).setState(state);
                break;
            case 3: thingySdkManager.enableAirQualityNotifications(thingyBleDevice,state);
                mNordicSensors.get(3).setState(state);
                break;
            case 4: thingySdkManager.enableColorNotifications(thingyBleDevice,state);
                mNordicSensors.get(4).setState(state);
                break;
            case 5: thingySdkManager.enableTapNotifications(thingyBleDevice,state);
                mNordicSensors.get(5).setState(state);
                break;
            case 6: thingySdkManager.enableButtonStateNotification(thingyBleDevice,state);
                mNordicSensors.get(6).setState(state);
                break;
            case 7: thingySdkManager.enableOrientationNotifications(thingyBleDevice,state);
                mNordicSensors.get(7).setState(state);
                break;
            case 8: thingySdkManager.enablePedometerNotifications(thingyBleDevice,state);
                mNordicSensors.get(8).setState(state);
                break;
            case 9: thingySdkManager.enableAirQualityNotifications(thingyBleDevice,state);
                mNordicSensors.get(9).setState(state);
                break;
            case 10: thingySdkManager.enableEulerNotifications(thingyBleDevice,state);
                mNordicSensors.get(10).setState(state);
                break;
            case 11: thingySdkManager.enableRotationMatrixNotifications(thingyBleDevice,state);
                mNordicSensors.get(11).setState(state);
                break;
            case 12: thingySdkManager.enableGravityVectorNotifications(thingyBleDevice,state);
                mNordicSensors.get(12).setState(state);
                break;
            case 13: thingySdkManager.enableHeadingNotifications(thingyBleDevice,state);
                mNordicSensors.get(13).setState(state);
                break;
            case 14: thingySdkManager.enableRawDataNotifications(thingyBleDevice,state);
                mNordicSensors.get(14).setState(state);
                break;
            case 15: thingySdkManager.enableSpeakerStatusNotifications(thingyBleDevice,state);
                mNordicSensors.get(15).setState(state);
                break;
            case 16: thingySdkManager.enableThingyMicrophone(thingyBleDevice,state);
                mNordicSensors.get(16).setState(state);
                break;
        }
    }
}
