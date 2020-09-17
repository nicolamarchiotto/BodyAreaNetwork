package com.example.progettogio.views;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
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
import com.example.progettogio.models.GeneralDevice;
import com.example.progettogio.models.Scanner_BTLE;
import com.example.progettogio.services.BluetoothConnectionService;
import com.example.progettogio.services.DataCollectionService;
import com.example.progettogio.services.ThingyService;
import com.example.progettogio.sound_vibration.SoundVibrationThread;
import com.example.progettogio.utils.DevicesEnum;
import com.example.progettogio.utils.PermissionUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.wagoo.wgcom.WagooGlassesInterface;
import com.wagoo.wgcom.connection.WagooConnectionHandler;
import com.wagoo.wgcom.connection.WagooDevice;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.thingylib.BaseThingyService;
import no.nordicsemi.android.thingylib.ThingyListener;
import no.nordicsemi.android.thingylib.ThingyListenerHelper;
import no.nordicsemi.android.thingylib.ThingySdkManager;
import no.nordicsemi.android.thingylib.utils.ThingyUtils;

public class MainActivity extends AppCompatActivity implements ThingySdkManager.ServiceConnectionListener,
        DevsScanListener,
        DevsSelectedListener,
        WaitingForDoctorDialogFragment.WaitingDialogListener,
        ChangeThingyNameDialogFragment.ChangeThingyNameListener {


    private final static String TAG = "MainActivity";

    private ActivityMainBinding activityMainBinding;

    //
    private Switch scanSwitch;
    private Switch collectionSwitch;
    private Switch readySwitch;

    //Nordic stuff
    private ThingySdkManager thingySdkManager;

    //Scan stuff
    private Scanner_BTLE scannerBLE;
    private final ParcelUuid thingyParcelUuid=new ParcelUuid(ThingyUtils.THINGY_BASE_UUID);
    private boolean isScanning;

    private final ParcelUuid wagooParcelUuid= ParcelUuid.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");

    private BluetoothAdapter bluetoothAdapter=null;
    //data collection stuff
    private BaseThingyService.BaseThingyBinder binder;
    private Boolean isCollectingData=false;

    //scan devs list
    private List<GeneralDevice> scanDevicesList = new ArrayList<>();
    private RecyclerView scanRecyclerView;
    private DevicesScanAdapters devicesScanAdapters;

    //selected devs list
    private List<GeneralDevice> selectedDeviceList = new ArrayList<>();
    private RecyclerView selectedRecyclerView;
    private DevicesSelectedAdapters devicesSelectedAdapters;

    private static WagooGlassesInterface wagooGlassesInterface=null;

    private Toolbar toolbar;

    private SoundVibrationThread mSoundVibrationThread;
    private Button soundVibrationButton;
    private boolean soundVibrationOn=false;
    /**
     * timestamp of the session, same for every app start
     * session_id: varies for every collection of datas
     */
    private static int session_id=1;
    private Timestamp timestamp;

    private BluetoothConnectionService mBluetoothConnectionService=null;

    //wear
    private Set<Node> nodes;
    private boolean searchAndConnectSmartWatch;
    private boolean smartWatchConnected=false;
    private static final int REQUEST_CODE_SMARTWATCH=0;
    private static final int REQUEST_DISCOVERABILITY=1;
    private static final int REQUEST_ENABLE_BT=2;

    private DialogFragment waitingDialogFragment;
    private DialogFragment sessionOngoingDialogFragment;
    private ChangeThingyNameDialogFragment changeThingyNameDialogFragment;


    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;


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
        preferenceChangeListener=new SharedPreferences.OnSharedPreferenceChangeListener(){
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals("smartwatch"))
                    searchAndConnectSmartWatch=sharedPreferences.getBoolean(key,true);
            }
        };
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        //Nordic SDK
        thingySdkManager = ThingySdkManager.getInstance();

        wagooGlassesInterface = WagooGlassesInterface.Companion.bleAutoInit(
                getApplicationContext(), new WagooConnectionHandler() {

                    @Override
                    public void onDisconnected(WagooGlassesInterface wagooInterface) {
                        wagooGlassesInterface.disable_collect_mode();
                        wagooGlassesInterface.set_lights(0.0f,0,false,false,false);
                        Log.d(TAG, "onDisconnected: Wagoo glasses disconnected");
                        Intent messageToActivityIntent=new Intent("incomingMessage");
                        messageToActivityIntent.putExtra("theMessage","wagooglassesdisconnected");
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageToActivityIntent);
                    }

                    @Override
                    public void onConnecting(WagooGlassesInterface wagooInterface) {
                        Log.d(TAG, "onConnecting: WagooGlassesInterface");
                    }

                    @Override
                    public void onConnected(WagooGlassesInterface wagooInterface) {
                        Log.d(TAG, "onConnected: Wagoo glasses connected ");
                        Intent messageToActivityIntent=new Intent("incomingMessage");
                        messageToActivityIntent.putExtra("theMessage","wagooglassesconnected");
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageToActivityIntent);
                    }

                    @Override
                    public void onDeviceFound(WagooGlassesInterface wagooInterface, WagooDevice device) {
                        Log.d(TAG, "onDeviceFound: WagooGlasses Found");
                    }
                },
                null);


        //get scan
        scannerBLE = Scanner_BTLE.getInstance();


        //ask for permission
        PermissionUtils.askForPermissions(this);


        //get bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


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
                isScanning=true;
                Toast.makeText(getApplicationContext(), "BLE scan started.", Toast.LENGTH_SHORT).show();
            }else {

                stopScan();
                isScanning=false;
                Toast.makeText(getApplicationContext(), "BLE scan stopped.", Toast.LENGTH_SHORT).show();
            }
        });

        collectionSwitch=activityMainBinding.collectionSwitch;
        collectionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "collectionswitch listener called: ");
            if (isChecked){
                if(thingySdkManager.getConnectedDevices().size()==0 && !wagooGlassesInterface.isConnected() && !smartWatchConnected){
                    Toast.makeText(getApplicationContext(),"You have to connect at least one device before starting the data collection", Toast.LENGTH_SHORT).show();
                    buttonView.setChecked(false);
                }
                else{
                    startDataCollection();
                }
            }else {
                stopDataCollection();
                isCollectingData=false;
            }
        });

        readySwitch=activityMainBinding.readySwitch;
        readySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "readySwitch listener called: ");
            if(isChecked){
                if(thingySdkManager.getConnectedDevices().size()==0 && !wagooGlassesInterface.isConnected() && !smartWatchConnected){
                    Toast.makeText(getApplicationContext(),"You have to connect at least one device before starting the data collection", Toast.LENGTH_SHORT).show();
                    buttonView.setChecked(false);
                }
                else
                    bluetoothEnableDiscoverability();
            }
            else{
                bluetoothDisableDiscoverability();
                if(mBluetoothConnectionService!=null)
                    mBluetoothConnectionService.closeAcceptThread();
            }
        });

        soundVibrationButton=activityMainBinding.vibrationButton;
        soundVibrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(soundVibrationOn){
                    mSoundVibrationThread.end();
                    soundVibrationOn=false;
                    if(wagooGlassesInterface.isConnected()){
                        wagooGlassesInterface.set_lights(0.0f, 0,false,false,false);
                    }
                    if(smartWatchConnected){
                        for (Node node : nodes)
                            Wearable.getMessageClient(getApplicationContext()).sendMessage(
                                    node.getId(), "/stop", "STOP".getBytes());
                    }
                }
                else {
                    if(smartWatchConnected){
                        if (nodes != null) {
                            for (Node node : nodes)
                                Wearable.getMessageClient(getApplicationContext()).sendMessage(
                                        node.getId(), "/vibration", "30".getBytes());
                        }
                    }
                    if(wagooGlassesInterface.isConnected()){
                        wagooGlassesInterface.set_lights(1.0f, 1000,true,true,true);
                    }
                    mSoundVibrationThread = new SoundVibrationThread(getApplicationContext(), true, true, 30);
                    mSoundVibrationThread.start();

                    soundVibrationOn=true;
                }
            }
        });

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        searchAndConnectSmartWatch=pref.getBoolean("smartwatch",true);
        if(searchAndConnectSmartWatch){
            connectToWearable();
        }

        DataMapper.getInstance().setContext(getApplicationContext());

    }

    public void showWaitingForDoctorDialogFragment(){
            waitingDialogFragment = new WaitingForDoctorDialogFragment(this);
            waitingDialogFragment.setCancelable(false);
            waitingDialogFragment.show(getSupportFragmentManager(), "WaitForDoctor");
    }

    public void showSessionOngoingDialogFragment(){
        sessionOngoingDialogFragment = new SessionOngoingDialogFragment();
        sessionOngoingDialogFragment.setCancelable(false);
        sessionOngoingDialogFragment.show(getSupportFragmentManager(), "WaitForDoctor");
    }

    public void showChangeThingyNameDialogFragment(String thingyAddress, String oldName){
        changeThingyNameDialogFragment = new ChangeThingyNameDialogFragment(this,oldName,thingyAddress);
        changeThingyNameDialogFragment.show(getSupportFragmentManager(), "WaitForDoctor");
    }



    public static WagooGlassesInterface getWagooGlassesInterface() {
        return wagooGlassesInterface;
    }

    public void connectToWearable() {
        Task<CapabilityInfo> capabilityInfoTask = Wearable.getCapabilityClient(getApplicationContext())
                .getCapability("watch_client", CapabilityClient.FILTER_REACHABLE);

        capabilityInfoTask.addOnCompleteListener(new OnCompleteListener<CapabilityInfo>() {
            @Override
            public void onComplete(Task<CapabilityInfo> task) {

                if (task.isSuccessful()) {
                    CapabilityInfo capabilityInfo = task.getResult();
                    nodes = capabilityInfo.getNodes();
                    Log.d(TAG, "onComplete: "+nodes);
                    if(nodes!=null){
                        Intent messageToActivityIntent=new Intent("incomingMessage");
                        messageToActivityIntent.putExtra("theMessage","smartwatchconnected");
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageToActivityIntent);
                    }
                } else {
                    Log.d(TAG, "Capability request failed to return any results.");
                }

            }
        });

        Wearable.getMessageClient(getApplicationContext()).addListener((messageEvent) -> {

        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SMARTWATCH && resultCode==RESULT_OK) {
            if (searchAndConnectSmartWatch && !smartWatchConnected){
                connectToWearable();
                return;
            }
            if(!searchAndConnectSmartWatch)
                smartWatchConnected=false;
        }
        if(requestCode==REQUEST_DISCOVERABILITY){
            if(resultCode==RESULT_CANCELED){
                readySwitch.setChecked(false);
            }
            if(resultCode==300){
                mBluetoothConnectionService=new BluetoothConnectionService(this);
                showWaitingForDoctorDialogFragment();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.settings) {
            Intent i = new Intent(this, MyPreferencesActivity.class);
            startActivityForResult(i,REQUEST_CODE_SMARTWATCH);
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

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,new IntentFilter("incomingMessage"));
 }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wagooGlassesInterface.isConnected()){
            wagooGlassesInterface.set_lights(0.0f,0,false,false,false);
            wagooGlassesInterface.disconnect();
        }
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
        if(!(mBluetoothConnectionService ==null)){
            mBluetoothConnectionService.closeAcceptThread();
            mBluetoothConnectionService.closeConnectedThread();
        }
    }

    public void enableBt(){
        if (bluetoothAdapter == null) {
            Log.d(TAG, "enableBt: Device does not support Bluetooth");
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public void bluetoothEnableDiscoverability() {
        Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds.");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivityForResult(discoverableIntent,REQUEST_DISCOVERABILITY);
//        Toast.makeText(this, "Waiting for doctor phone", Toast.LENGTH_SHORT).show();
    }


    public void bluetoothDisableDiscoverability() {
        //method copied and paste, it works
        BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
        try {
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode =BluetoothAdapter.class.getMethod("setScanMode", int.class,int.class);
            setScanMode.setAccessible(true);

            setDiscoverableTimeout.invoke(adapter, 1);
            setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE,1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onServiceConnected() {
        binder = (ThingyService.ThingyBinder) thingySdkManager.getThingyBinder();
    }

    private void startScan(){
        scanDevicesList.clear();
        scannerBLE.startScan(scanCallback);
    }

    private void stopScan(){
        scannerBLE.stopScan(scanCallback);
        scanSwitch.setChecked(false);

        devicesScanAdapters.notifyDataSetChanged();

    }

    /**
     * CallBack chiamata dallo Scanner_BLE quando trova un dispositivo
     */
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, @NonNull ScanResult result) {
            super.onScanResult(callbackType, result);
        }

        @Override
        public void onBatchScanResults(@NonNull List<ScanResult> results) {
            super.onBatchScanResults(results);

            for (ScanResult result : results){
                Log.d(TAG, "onBatchScanResults: "+result.getDevice().getAddress()+" - "+result.getDevice().getName() +" trovato");
                BluetoothDevice device = result.getDevice();

                if(result.getScanRecord().getServiceUuids()==null)
                    return;
                ParcelUuid mUuid=result.getScanRecord().getServiceUuids().get(0);
                Log.d(TAG, "UUID "+mUuid+" onBatchScanResult: "+result.getDevice().getAddress());
                //controllo per vedere se device non è già presente all'interno delle due liste,
                //callback ha frequenza molto alta

                for(GeneralDevice generalDevice:selectedDeviceList){
                    if(generalDevice.getAddress().equals(device.getAddress())){
                        Log.d(TAG, "onBatchScanResults: device already in selectedDeviceList ");
                        return;
                    }
                }
                for(GeneralDevice generalDevice:scanDevicesList){
                    if(generalDevice.getAddress().equals(device.getAddress())){
//                        if(generalDevice.getConnectionState().equals(ConnectionState.CONNECTING)){
//                            scanDevicesList.remove(generalDevice);
//                        }
                        Log.d(TAG, "onBatchScanResults: device already in scanDeviceList ");
                        return;
                    }
                }

                if (thingyParcelUuid.equals(mUuid)){
                    scanDevicesList.add(new GeneralDevice(device, DevicesEnum.NORDIC,0,device.getName()));
                    devicesScanAdapters.notifyDataSetChanged();
                }
                else if(wagooParcelUuid.equals(mUuid)){
//                    Log.d(TAG, "onBatchScanResults: "+mUuid+" - "+wagooParcelUuid);
                    scanDevicesList.add(new GeneralDevice(device, DevicesEnum.WAGOOGLASSES,0, "WagooGLasses"));
                    devicesScanAdapters.notifyDataSetChanged();
                }
            }


        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    private ThingyListener thingyListener = new ThingyListener() {

        @Override
        public void onDeviceConnected(BluetoothDevice device, int connectionState) {
            Log.d(TAG, "onDeviceConnected: "+device.getAddress()+" connectionState: "+connectionState);

            thingySdkManager.enableBatteryLevelNotifications(device,true);

            devicesSelectedAdapters.notifyDataSetChanged();

            thingySdkManager.setMotionProcessingFrequency(device,5000);

            Toast.makeText(getApplicationContext(), "Thingy "+device.getAddress()+" connected ", Toast.LENGTH_SHORT).show();


        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, int connectionState) {
            Log.d(TAG, "onDeviceDisconnected: "+device.getAddress()+" connectionState: "+connectionState);
            Toast.makeText(getApplicationContext(), "Thingy "+device.getAddress() +" disconnected ", Toast.LENGTH_SHORT).show();

            for(GeneralDevice device1:selectedDeviceList){
                if(device1.getAddress().equals(device.getAddress())){
                    selectedDeviceList.remove(device1);
                    devicesSelectedAdapters.notifyDataSetChanged();
                    return;
                }
            }
        }

        @Override
        public void onServiceDiscoveryCompleted(BluetoothDevice device) {

        }

        @Override
        public void onBatteryLevelChanged(BluetoothDevice bluetoothDevice, int batteryLevel) {
            Log.d(TAG, "onBatteryLevelChanged: "+bluetoothDevice.getAddress()+" batteryLevel: "+batteryLevel);
            for(GeneralDevice device:selectedDeviceList){
                if(device.getAddress().equals(bluetoothDevice.getAddress())){
                    device.setBatteryLevel(batteryLevel);
                }
            }
            devicesSelectedAdapters.notifyDataSetChanged();
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
            String message = intent.getStringExtra("theMessage");
            switch (message){
                case "wagooglassesconnected":
                    devicesSelectedAdapters.notifyDataSetChanged();
                    break;
                case "wagooglassesdisconnected":
                    for(GeneralDevice device:selectedDeviceList){
                        if(device.getType().equals(DevicesEnum.WAGOOGLASSES)){
                            selectedDeviceList.remove(device);
                            devicesSelectedAdapters.notifyDataSetChanged();
                            return;
                        }
                    }
                    break;
                case "smartwatchconnected":
                    Toast.makeText(getApplicationContext(),"Smart Watch Paired",Toast.LENGTH_SHORT).show();
                    smartWatchConnected=true;
                    break;
                case "connectedThreadReady":
                    Log.d(TAG, "onReceive: ConnectedThread Ready");
                    mBluetoothConnectionService.write("testMessage".getBytes());
                    if(waitingDialogFragment!=null){
                        waitingDialogFragment.dismiss();
                    }
                    showSessionOngoingDialogFragment();
                    break;
                default:
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onReceive: " + message);
                    break;
            }
        }
    };


    /**
     * listener per per la recyclerview dei device scansionati
     * @param address
     */
    @Override
    public void onScannedDeviceClick(String address) {
        for (GeneralDevice device : scanDevicesList){
            if (device.getAddress().equals(address) && (device.getType().equals(DevicesEnum.NORDIC))){
                Log.d(TAG, "onScannedDeviceClick: "+address);
                scanDevicesList.remove(device);
                selectedDeviceList.add(device);
                devicesScanAdapters.notifyDataSetChanged();
                thingySdkManager.connectToThingy(this, device.getBluetoothDevice(), ThingyService.class);
                break;
            }
            else if(device.getAddress().equals(address) && (device.getType().equals(DevicesEnum.WAGOOGLASSES))){
                Log.d(TAG, "onScannedDeviceClick: "+address);
                scanDevicesList.remove(device);
                selectedDeviceList.add(device);
                wagooGlassesInterface.connect();
                devicesScanAdapters.notifyDataSetChanged();
                break;
            }
        }
    }

    /**
     * listener per la recyclerview dei device connessi
     * @param address
     */

    @Override
    public void onSelectedDeviceClick(String address) {
        for(GeneralDevice device: selectedDeviceList){
            if(device.getAddress().equals(address) && device.getType().equals(DevicesEnum.NORDIC)){
              Toast.makeText(this,device.getBluetoothDevice().getName()+" " +device.getAddress()+" is connected: "+thingySdkManager.isConnected(device.getBluetoothDevice()),Toast.LENGTH_SHORT).show();
//                Log.d(TAG, "onSelectedDeviceClick: name:"+device.getBluetoothDevice().getName());
//                showChangeThingyNameDialogFragment(address,device.getDeviceName());

            }
            else if(device.getAddress().equals(address) && device.getType().equals(DevicesEnum.WAGOOGLASSES)){
                Toast.makeText(this,"Wagoo Glasses is connected: "+wagooGlassesInterface.isConnected(),Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSelectedDeviceLongClick(String address) {
            for (GeneralDevice device : selectedDeviceList){
                if (device.getAddress().equals(address) && (device.getType().equals(DevicesEnum.NORDIC))){
                    thingySdkManager.disconnectFromThingy(device.getBluetoothDevice());
                }
                else if(device.getAddress().equals(address) && (device.getType().equals(DevicesEnum.WAGOOGLASSES))){
                    Toast.makeText(getApplicationContext(), "Disconnected WagooGlasses" + address, Toast.LENGTH_SHORT).show();
                    selectedDeviceList.remove(device);
                    wagooGlassesInterface.disconnect();
                    devicesSelectedAdapters.notifyDataSetChanged();
                }
            }
    }

    public void startDataCollection(){
        Log.d(TAG, "startDataCollection: ");
        Toast.makeText(getApplicationContext(),"Data Collection Started", Toast.LENGTH_SHORT).show();
//        ThingyListenerHelper.unregisterThingyListener(this, thingyListener);


        //Default sensors
        for(BluetoothDevice device:thingySdkManager.getConnectedDevices()){
            thingySdkManager.enableQuaternionNotifications(device,true);
            thingySdkManager.enableEulerNotifications(device,true);
            thingySdkManager.enableGravityVectorNotifications(device,true);
            thingySdkManager.enableHeadingNotifications(device,true);
            thingySdkManager.enableRawDataNotifications(device,true);
        }

        wagooGlassesInterface.enable_collect_mode();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean phoneSensorOn = pref.getBoolean("phone_sensors", false);

        Log.d(TAG, "startDataCollection: phoneSensorOn:"+phoneSensorOn);
        Log.d(TAG, "startDataCollection: wagooGlasses Connected: "+wagooGlassesInterface.isConnected());

        Intent beaconDiscoveryService = new Intent(this, DataCollectionService.class);
        String value=timestamp+" session:"+session_id;
        Log.d(TAG, "startDataCollection: "+value);

        beaconDiscoveryService.putExtra("SESSION_ID",value);
        beaconDiscoveryService.putExtra("PHONE_SENSORS_ON",phoneSensorOn);
        beaconDiscoveryService.putExtra("WAGOO_GLASSES_CONNECTED",wagooGlassesInterface.isConnected());

        session_id+=1;


        startForegroundService(beaconDiscoveryService);
    }

    private void stopDataCollection() {

        Log.d(TAG, "stopDataCollection: ");
        Toast.makeText(getApplicationContext(),"Data Collection Stopped", Toast.LENGTH_SHORT).show();
        Intent beaconDiscoveryService = new Intent(this, DataCollectionService.class);
        stopService(beaconDiscoveryService);
    }

    @Override
    public void onWaitingDialogPositiveClick() {
        //not used in the dialog fragment
    }

    @Override
    public void onWaitingDialogNegativeClick() {
        readySwitch.setChecked(false);
    }

    @Override
    public void onThingyNameChanged(String thingyAddress, String newName) {
//        ThingySdkManager mmThingySdkManager=ThingySdkManager.getInstance();
//        AppExecutors mAppExecutor= new AppExecutors();

        for(GeneralDevice device:selectedDeviceList){
            if(device.getAddress().equals(thingyAddress)){

                if(device.getBluetoothDevice()!=null)
                    thingySdkManager.setDeviceName(device.getBluetoothDevice(),newName);
                device.setDeviceName(newName);
            }
        }
        devicesSelectedAdapters.notifyDataSetChanged();
    }
}