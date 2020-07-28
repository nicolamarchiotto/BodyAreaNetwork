package com.example.progettogio.views;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.progettogio.R;
import com.example.progettogio.adapters.SensorsAdapter;
import com.example.progettogio.databinding.FragmentDeviceSettingsBinding;
import com.example.progettogio.models.NordicSensorList;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import no.nordicsemi.android.thingylib.ThingySdkManager;

public class DeviceSettingsFragment extends BottomSheetDialogFragment {

    private FragmentDeviceSettingsBinding binding;
    private NordicSensorList mNordicSensors;
    private String devAddress = "";
    private SensorsAdapter mSensorAdapter;
    private RecyclerView mSensorsRecyclerView;
    private ThingySdkManager thingySdkManager;

    public DeviceSettingsFragment(String devAddress, NordicSensorList sensorList, ThingySdkManager thingySdkManager) {
        this.devAddress = devAddress;
        this.mNordicSensors = sensorList;
        this.thingySdkManager=thingySdkManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_device_settings, container, false);
        View view = binding.getRoot();

        binding.fragmentDeviceSettingsTxtAddr.setText(devAddress);

        // bind recyclervie

        mSensorsRecyclerView = binding.fragmentSensorsRecyclerView;
        LinearLayoutManager linearLayoutManagerTags = new LinearLayoutManager(this.getContext());
        linearLayoutManagerTags.setOrientation(RecyclerView.VERTICAL);
        mSensorsRecyclerView.setLayoutManager(linearLayoutManagerTags);
        mSensorAdapter = new SensorsAdapter(mNordicSensors);
        mSensorsRecyclerView.setAdapter(mSensorAdapter);
        return view;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        thingySdkManager.enableTemperatureNotifications(mNordicSensors.getBluetoothDevice(),mNordicSensors.get(0).getState());
        thingySdkManager.enableHumidityNotifications(mNordicSensors.getBluetoothDevice(),mNordicSensors.get(1).getState());
        thingySdkManager.enablePressureNotifications(mNordicSensors.getBluetoothDevice(),mNordicSensors.get(2).getState());
        thingySdkManager.enableAirQualityNotifications(mNordicSensors.getBluetoothDevice(),mNordicSensors.get(3).getState());
        thingySdkManager.enableColorNotifications(mNordicSensors.getBluetoothDevice(),mNordicSensors.get(4).getState());
        thingySdkManager.enableTapNotifications(mNordicSensors.getBluetoothDevice(),mNordicSensors.get(5).getState());
        thingySdkManager.enableOrientationNotifications(mNordicSensors.getBluetoothDevice(),mNordicSensors.get(6).getState());
        thingySdkManager.enablePedometerNotifications(mNordicSensors.getBluetoothDevice(),mNordicSensors.get(7).getState());
        thingySdkManager.enableQuaternionNotifications(mNordicSensors.getBluetoothDevice(),mNordicSensors.get(8).getState());
        thingySdkManager.enableEulerNotifications(mNordicSensors.getBluetoothDevice(),mNordicSensors.get(9).getState());
        thingySdkManager.enableRotationMatrixNotifications(mNordicSensors.getBluetoothDevice(),mNordicSensors.get(10).getState());
        thingySdkManager.enableGravityVectorNotifications(mNordicSensors.getBluetoothDevice(),mNordicSensors.get(11).getState());
        thingySdkManager.enableHeadingNotifications(mNordicSensors.getBluetoothDevice(),mNordicSensors.get(12).getState());
        thingySdkManager.enableRawDataNotifications(mNordicSensors.getBluetoothDevice(),mNordicSensors.get(13).getState());
        thingySdkManager.enableSpeakerStatusNotifications(mNordicSensors.getBluetoothDevice(),mNordicSensors.get(14).getState());
        thingySdkManager.enableThingyMicrophone(mNordicSensors.getBluetoothDevice(),mNordicSensors.get(15).getState());
    }

    //    @Override
//    public void onItemClick(NordicEvents event) {
//        // add or remove the props
//
//    }
}
