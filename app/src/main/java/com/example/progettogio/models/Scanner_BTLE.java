package com.example.progettogio.models;

import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;
import no.nordicsemi.android.thingylib.utils.ThingyUtils;

public class Scanner_BTLE {

    private static final String TAG = "Scanner_BTLE";

    private static Scanner_BTLE instance = null;
    private BluetoothLeScannerCompat scanner;

    /**
     *
     * @return
     */
    public static Scanner_BTLE getInstance(){
        if (instance == null)
            instance = new Scanner_BTLE();

        return instance;
    }


    private Scanner_BTLE(){
        scanner = BluetoothLeScannerCompat.getScanner();
    }


    /**
     *
     * @param scanCallback
     */
    public void startScan(ScanCallback scanCallback){

        //SCANNER SETTINGS
        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(500)
                .setUseHardwareBatchingIfSupported(false)
                .build();

        //FILTER
        List<ScanFilter> filters = new ArrayList<>();
        ParcelUuid thingyUuid = new ParcelUuid(ThingyUtils.THINGY_BASE_UUID);
        filters.add(new ScanFilter.Builder().setServiceUuid(thingyUuid).build());

        ParcelUuid wagooUuid = ParcelUuid.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
        filters.add(new ScanFilter.Builder().setServiceUuid(wagooUuid).build());

        //START SCAN
        scanner.startScan(filters, scanSettings, scanCallback);

    }


    /**
     *
     * @param scanCallback
     */
    public void stopScan(ScanCallback scanCallback){

        //STOP SCAN
        scanner.stopScan(scanCallback);

    }
}
