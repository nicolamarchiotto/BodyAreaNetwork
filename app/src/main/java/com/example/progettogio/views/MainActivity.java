package com.example.progettogio.views;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.progettogio.R;
import com.example.progettogio.adapters.DevicesScanAdapters;
import com.example.progettogio.adapters.DevicesSelectedAdapters;
import com.example.progettogio.adapters.DevsScanListener;
import com.example.progettogio.adapters.DevsSelectedListener;
import com.example.progettogio.databinding.ActivityMainBinding;
import com.example.progettogio.db.DataMapper;
import com.example.progettogio.models.NordicSensorList;
import com.example.progettogio.models.Scanner_BTLE;
import com.example.progettogio.services.BluetoothConnectionService;
import com.example.progettogio.services.DataCollectionService;
import com.example.progettogio.services.ThingyService;
import com.example.progettogio.utils.PermissionUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.thingylib.BaseThingyService;
import no.nordicsemi.android.thingylib.ThingyListener;
import no.nordicsemi.android.thingylib.ThingyListenerHelper;
import no.nordicsemi.android.thingylib.ThingySdkManager;

public class MainActivity extends AppCompatActivity implements ThingySdkManager.ServiceConnectionListener,
        DevsScanListener,
        DevsSelectedListener {


    private final static String TAG = "MainActivity";


    private ActivityMainBinding activityMainBinding;

    //
    private Switch scanSwitch;
    private Switch collectionSwitch;

    //Nordic stuff
    private ThingySdkManager thingySdkManager;

    //Scan stuff
    private Scanner_BTLE scannerBLE;

    private BluetoothAdapter bluetoothAdapter=null;
    public static final int REQUEST_BT_ENABLE = 1;

    //data collection stuff
    private BaseThingyService.BaseThingyBinder binder;
    private Boolean isCollectingData=false;

    //scan devs list
    private List<BluetoothDevice> scanDevicesList = new ArrayList<>();
    private RecyclerView scanRecyclerView;
    private DevicesScanAdapters devicesScanAdapters;

    //selected devs list
    private List<BluetoothDevice> selectedDeviceList = new ArrayList<>();
    private RecyclerView selectedRecyclerView;
    private DevicesSelectedAdapters devicesSelectedAdapters;

    //hasmMap of sensorsList of connected nordic devices

    private HashMap<String,NordicSensorList> sensorHashMap;

    private Toolbar toolbar;

    /**
     * timestamp of the session, same for every app start
     * session_id: varies for every collection of datas
     */
    private static int session_id=1;
    private Timestamp timestamp;

    private BluetoothConnectionService mBluetoothConnectionService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        timestamp = new Timestamp(System.currentTimeMillis());
        Log.d(TAG, "onCreate: "+timestamp);

        //view binding
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        toolbar =  (Toolbar) activityMainBinding.toolbar;
        setSupportActionBar(toolbar);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);

        //Nordic SDK
        thingySdkManager = ThingySdkManager.getInstance();

        //get scan
        scannerBLE = Scanner_BTLE.getInstance();
        
        //ask for permission
        PermissionUtils.askForPermissions(this);


        //get bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,new IntentFilter("incomingMessage"));

        enableBt();

        //set scan recyclerview
        scanRecyclerView = activityMainBinding.devScanRecyclerview;
        scanRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        devicesScanAdapters = new DevicesScanAdapters(scanDevicesList, this);
        scanRecyclerView.setAdapter(devicesScanAdapters);

        //set selected recyclerview
        selectedRecyclerView = activityMainBinding.selectedDevRecyclerview;
        selectedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        devicesSelectedAdapters = new DevicesSelectedAdapters(selectedDeviceList, this);
        //devicesSelectedAdapters = new DevicesSelectedAdapters(thingySdkManager.getConnectedDevices(), this);
        selectedRecyclerView.setAdapter(devicesSelectedAdapters);

        //sensorHashMap
        sensorHashMap=new HashMap<>();

        //set toolbar
        scanSwitch=activityMainBinding.scanSwitch;
        scanSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                enableBt();
                if (!bluetoothAdapter.isEnabled()){
                    buttonView.setChecked(false);
                    return;
                }
                startScan();
                Toast.makeText(getApplicationContext(), "BLE scan started.", Toast.LENGTH_SHORT).show();
            }else {

                stopScan();
                Toast.makeText(getApplicationContext(), "BLE scan stopped.", Toast.LENGTH_SHORT).show();
            }
        });

        collectionSwitch=activityMainBinding.collectionSwitch;
        collectionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "collectionswitch listener called: ");
            if (isChecked){
                if(thingySdkManager.getConnectedDevices().size()==0){
                    Toast.makeText(getApplicationContext(),"You have to connect at least one device before starting the data collection", Toast.LENGTH_SHORT).show();
                    buttonView.setChecked(false);
                }
                else{
                    startDataCollection();
                    Toast.makeText(getApplicationContext(), "Data Collection Started", Toast.LENGTH_SHORT).show();
//                    mBluetoothConnectionService=new BluetoothConnectionService(this);
                }
            }else {
                stopDataCollection();
                isCollectingData=false;
                Toast.makeText(getApplicationContext(), "Data Collection Stopped", Toast.LENGTH_SHORT).show();
            }
        });


        DataMapper.getInstance().setContext(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.database_settings) {
            Intent i = new Intent(this, MyPreferencesActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //thingy service
        thingySdkManager.bindService(this, ThingyService.class);

        //thingy listener
        ThingyListenerHelper.registerThingyListener(this, thingyListener);

    }

    @Override
    protected void onStop() {
        super.onStop();

        //thingy service
        thingySdkManager.unbindService(this);

        //thingy listener
//        ThingyListenerHelper.unregisterThingyListener(this, thingyListener);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);

        //stop scan
        stopScan();
        scanSwitch.setChecked(false);
    }

    public void enableBt(){
        if (bluetoothAdapter == null) {
            Log.d(TAG, "enableBt: Device does not support Bluetooth");
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_BT_ENABLE);
        }
    }


    @Override
    public void onServiceConnected() {
        binder = (ThingyService.ThingyBinder) thingySdkManager.getThingyBinder();
    }

    private void startScan(){
        scannerBLE.startScan(scanCallback);
    }

    private void stopScan(){
        scannerBLE.stopScan(scanCallback);

        scanDevicesList.clear();
        devicesScanAdapters.notifyDataSetChanged();

    }


    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, @NonNull ScanResult result) {
            super.onScanResult(callbackType, result);
        }

        @Override
        public void onBatchScanResults(@NonNull List<ScanResult> results) {
            super.onBatchScanResults(results);

            for (ScanResult result : results){
                Log.d(TAG, "onBatchScanResults:"+result.getDevice().getAddress()+" trovato");
                BluetoothDevice device = result.getDevice();

               if (!scanDevicesList.contains(device)){
                   scanDevicesList.add(device);
               }
            }

            devicesScanAdapters.notifyDataSetChanged();
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };



    /**
     * listener per per la recyclerview dei device scansionati
     * @param address
     */
    @Override
    public void onDeviceSelected(String address) {
        for (BluetoothDevice device : scanDevicesList){
            if (device.getAddress().equals(address)){
                thingySdkManager.connectToThingy(this, device, ThingyService.class);
                break;
            }
        }
    }

    private ThingyListener thingyListener = new ThingyListener() {

        @Override
        public void onDeviceConnected(BluetoothDevice device, int connectionState) {

            Log.d(TAG, "onDeviceConnected: ");
            Toast.makeText(getApplicationContext(), "Connected " + device.getName(), Toast.LENGTH_SHORT).show();

            selectedDeviceList.add(device);
            devicesSelectedAdapters.notifyDataSetChanged();

            sensorHashMap.put(device.getAddress(),new NordicSensorList(device));
            setNordicSensors(sensorHashMap.get(device.getAddress()));

            scanDevicesList.remove(device);
            devicesScanAdapters.notifyDataSetChanged();

        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, int connectionState) {
            Log.d(TAG, "onDeviceDisconnected: ");
            Toast.makeText(getApplicationContext(), "Disconnected " + device.getName(), Toast.LENGTH_SHORT).show();

            sensorHashMap.remove(device.getAddress());

            selectedDeviceList.remove(device);
            devicesSelectedAdapters.notifyDataSetChanged();
        }

        @Override
        public void onServiceDiscoveryCompleted(BluetoothDevice device) {

        }

        @Override
        public void onBatteryLevelChanged(BluetoothDevice bluetoothDevice, int batteryLevel) {

        }

        @Override
        public void onTemperatureValueChangedEvent(BluetoothDevice bluetoothDevice, String temperature) {

        }

        @Override
        public void onPressureValueChangedEvent(BluetoothDevice bluetoothDevice, String pressure) {

        }

        @Override
        public void onHumidityValueChangedEvent(BluetoothDevice bluetoothDevice, String humidity) {

        }

        @Override
        public void onAirQualityValueChangedEvent(BluetoothDevice bluetoothDevice, int eco2, int tvoc) {

        }

        @Override
        public void onColorIntensityValueChangedEvent(BluetoothDevice bluetoothDevice, float red, float green, float blue, float alpha) {

        }

        @Override
        public void onButtonStateChangedEvent(BluetoothDevice bluetoothDevice, int buttonState) {

        }

        @Override
        public void onTapValueChangedEvent(BluetoothDevice bluetoothDevice, int direction, int count) {

        }

        @Override
        public void onOrientationValueChangedEvent(BluetoothDevice bluetoothDevice, int orientation) {

        }

        @Override
        public void onQuaternionValueChangedEvent(BluetoothDevice bluetoothDevice, float w, float x, float y, float z) {

        }

        @Override
        public void onPedometerValueChangedEvent(BluetoothDevice bluetoothDevice, int steps, long duration) {

        }

        @Override
        public void onAccelerometerValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onGyroscopeValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onCompassValueChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onEulerAngleChangedEvent(BluetoothDevice bluetoothDevice, float roll, float pitch, float yaw) {

        }

        @Override
        public void onRotationMatrixValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] matrix) {

        }

        @Override
        public void onHeadingValueChangedEvent(BluetoothDevice bluetoothDevice, float heading) {

        }

        @Override
        public void onGravityVectorChangedEvent(BluetoothDevice bluetoothDevice, float x, float y, float z) {

        }

        @Override
        public void onSpeakerStatusValueChangedEvent(BluetoothDevice bluetoothDevice, int status) {

        }

        @Override
        public void onMicrophoneValueChangedEvent(BluetoothDevice bluetoothDevice, byte[] data) {

        }
    };


    private BroadcastReceiver mReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message=intent.getStringExtra("theMessage");
            Log.d(TAG, "onReceive: "+message);
        }
    };


    /**
     * listener per la recyclerview dei device connessi
     * @param address
     */

    @Override
    public void onDevSelectecClick(String address) {
//        Toast.makeText(getApplicationContext(), "Connected " + address, Toast.LENGTH_SHORT).show();
        startDetailDialog(address,sensorHashMap.get(address));
    }

    @Override
    public void onDevSelectedLongClick(String address) {
            for (BluetoothDevice device : selectedDeviceList){
                if (device.getAddress().equals(address)){
                    thingySdkManager.disconnectFromThingy(device);
                    Toast.makeText(getApplicationContext(), "Disconnected " + address, Toast.LENGTH_SHORT).show();
                    break;
                }
            }
    }

    private void setNordicSensors(NordicSensorList sensorsList){
        thingySdkManager.enableTemperatureNotifications(sensorsList.getBluetoothDevice(),sensorsList.get(0).getState());
        thingySdkManager.enableHumidityNotifications(sensorsList.getBluetoothDevice(),sensorsList.get(1).getState());
        thingySdkManager.enablePressureNotifications(sensorsList.getBluetoothDevice(),sensorsList.get(2).getState());
        thingySdkManager.enableAirQualityNotifications(sensorsList.getBluetoothDevice(),sensorsList.get(3).getState());
        thingySdkManager.enableColorNotifications(sensorsList.getBluetoothDevice(),sensorsList.get(4).getState());
        thingySdkManager.enableTapNotifications(sensorsList.getBluetoothDevice(),sensorsList.get(5).getState());
        thingySdkManager.enableOrientationNotifications(sensorsList.getBluetoothDevice(),sensorsList.get(6).getState());
        thingySdkManager.enablePedometerNotifications(sensorsList.getBluetoothDevice(),sensorsList.get(7).getState());
        thingySdkManager.enableQuaternionNotifications(sensorsList.getBluetoothDevice(),sensorsList.get(8).getState());
        thingySdkManager.enableEulerNotifications(sensorsList.getBluetoothDevice(),sensorsList.get(9).getState());
        thingySdkManager.enableRotationMatrixNotifications(sensorsList.getBluetoothDevice(),sensorsList.get(10).getState());
        thingySdkManager.enableGravityVectorNotifications(sensorsList.getBluetoothDevice(),sensorsList.get(11).getState());
        thingySdkManager.enableHeadingNotifications(sensorsList.getBluetoothDevice(),sensorsList.get(12).getState());
        thingySdkManager.enableRawDataNotifications(sensorsList.getBluetoothDevice(),sensorsList.get(13).getState());
        thingySdkManager.enableSpeakerStatusNotifications(sensorsList.getBluetoothDevice(),sensorsList.get(14).getState());
        thingySdkManager.enableThingyMicrophone(sensorsList.getBluetoothDevice(),sensorsList.get(15).getState());
    }

    private void startDetailDialog(String devAddr, NordicSensorList sensorList) {
        DeviceSettingsFragment deviceSettingsFragment = new DeviceSettingsFragment(devAddr,sensorList,thingySdkManager);
        deviceSettingsFragment.show(getSupportFragmentManager(), "device_sensors");
    }

    public void startDataCollection(){
        Log.d(TAG, "startDataCollection: ");
        Toast.makeText(getApplicationContext(),"Data Collection Started", Toast.LENGTH_SHORT).show();
//        ThingyListenerHelper.unregisterThingyListener(this, thingyListener);

        Intent beaconDiscoveryService = new Intent(this, DataCollectionService.class);
        String value=timestamp+" session:"+session_id;
        Log.d(TAG, "startDataCollection: "+value);
        beaconDiscoveryService.putExtra("SESSION_ID",value);

        session_id+=1;

        startForegroundService(beaconDiscoveryService);
    }

    private void stopDataCollection() {

        Log.d(TAG, "stopDataCollection: ");
        Toast.makeText(getApplicationContext(),"Data Collection Stopped", Toast.LENGTH_SHORT).show();
        Intent beaconDiscoveryService = new Intent(this, DataCollectionService.class);
        stopService(beaconDiscoveryService);
    }
}