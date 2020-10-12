package com.example.progettogio.services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Created by User on 12/21/2016.
 */

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionService";

    private static final String appName = "bodyAreaNetwork_App";

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("e385235a-a94c-43ea-ae18-e44dcc2061e3");

    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;

    private AcceptThread mInsecureAcceptThread;
    private ConnectedThread mConnectedThread;

    private BluetoothDevice mmDevice;
    private UUID deviceUUID;

    public BluetoothConnectionService(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode.
     */
    public synchronized void start() {
        Log.d(TAG, "start");
        // Cancel any thread attempting to make a connection
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }
    /**
     AcceptThread starts and sits waiting for a connection.
     Then ConnectThread starts and attempts to make a connection with the other devices AcceptThread.
     **/

//    public void startClient(BluetoothDevice device,UUID uuid){
//        Log.d(TAG, "startClient: Started.");
//
//        mConnectThread = new ConnectThread(device, uuid);
//        mConnectThread.start();
//    }

    public void closeAcceptThread(){
        if(!(mInsecureAcceptThread ==null))
            mInsecureAcceptThread.cancel();
    }

    public void closeConnectedThread(){
        if(!(mConnectedThread ==null)){
            mConnectedThread.cancel();
            mConnectedThread.interrupt();
        }
    }


    private void connected(BluetoothSocket mmSocket) {
        Log.d(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();

    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {

        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try{
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);

                Log.d(TAG, "AcceptThread: Setting up Server using: " + MY_UUID_INSECURE);
            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
            }

            mmServerSocket = tmp;
        }

        public void run(){
            Log.d(TAG, "run: AcceptThread Running.");

            BluetoothSocket socket = null;

            try{
                // This is a blocking call and will only return on a
                // successful connection or an exception, another device start his ConnecteThread
                //and grabs the mmServerSocket
                Log.d(TAG, "run: AcceptThread server socket start.....");
//
//                Intent messageToActivityIntent=new Intent("incomingMessage");
//                messageToActivityIntent.putExtra("theMessage","Waiting for Doctor Phone");
//                LocalBroadcastManager.getInstance(mContext).sendBroadcast(messageToActivityIntent);


                socket = mmServerSocket.accept();

                Log.d(TAG, "run: AcceptThread server socket accepted connection.");

                Intent incomingMessageIntent=new Intent("incomingMessage");
                incomingMessageIntent.putExtra("theMessage","Connected to Doctor Phone");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntent);

            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
            }

            //Incoming request from another device, method to managing the connection
            if(socket != null){
                connected(socket);
            }
            Log.i(TAG, "END mAcceptThread ");
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage() );
            }
        }

    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
//    private class ConnectThread extends Thread {
//        private BluetoothSocket mmSocket;
//
//        public ConnectThread(BluetoothDevice device, UUID uuid) {
//            Log.d(TAG, "ConnectThread: started.");
//            mmDevice = device;
//            deviceUUID = uuid;
//        }
//
//        public void run(){
//            BluetoothSocket tmp = null;
//            Log.i(TAG, "RUN mConnectThread ");
//
//            // Get a BluetoothSocket for a connection with the
//            // given BluetoothDevice
//            try {
//                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "
//                        +MY_UUID_INSECURE );
//                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
//            } catch (IOException e) {
//                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
//            }
//
//            mmSocket = tmp;
//
//            // Always cancel discovery because it will slow down a connection
//            mBluetoothAdapter.cancelDiscovery();
//
//            // Make a connection to the BluetoothSocket
//
//            try {
//                // This is a blocking call and will only return on a
//                // successful connection or an exception
//                mmSocket.connect();
//
//                Log.d(TAG, "run: ConnectThread connected.");
//            } catch (IOException e) {
//                // Close the socket
//                try {
//                    mmSocket.close();
//                    Log.d(TAG, "run: Closed Socket.");
//                } catch (IOException e1) {
//                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
//                }
//                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_UUID_INSECURE );
//            }
//
//            //will talk about this in the 3rd video
//            connected(mmSocket);
//        }
//        public void cancel() {
//            try {
//                Log.d(TAG, "cancel: Closing Client Socket.");
//                mmSocket.close();
//            } catch (IOException e) {
//                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
//            }
//        }
//    }


    /**
     The ConnectedThread which is responsible for maintaining the BTConnection, Sending the data, and
     receiving incoming data through input/output streams respectively.
     **/
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];  // buffer store for the stream

            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            Intent messageIntent=new Intent("incomingMessage");
            messageIntent.putExtra("theMessage","connectedThreadCommunication_connectedThreadReady");
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(messageIntent);

            while (true) {
                // Read from the InputStream
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);
                    if(incomingMessage.equals("56")){
                        Intent fiveMessageIntent=new Intent("incomingMessage");
                        fiveMessageIntent.putExtra("theMessage","5");
                        sleep(1000);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(fiveMessageIntent);
                        Intent sixMessageIntent=new Intent("incomingMessage");
                        sixMessageIntent.putExtra("theMessage","6");
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(sixMessageIntent);
                    }
                    else{
                        incomingMessage="connectedThreadCommunication_"+incomingMessage;

                        Intent incomingMessageIntent=new Intent("incomingMessage");
                        incomingMessageIntent.putExtra("theMessage",incomingMessage);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntent);
                    }

                } catch (IOException | InterruptedException e) {
                    Log.e(TAG, "read: Error reading Input Stream: " + e.getMessage() );
                    Intent incomingMessageIntent=new Intent("incomingMessage");
                    incomingMessageIntent.putExtra("theMessage","connectedThreadCommunication_connectedThreadClosed");
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntent);
                    break;
                }
            }
        }

        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG,  "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage() );
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    public void write(byte[] out) {
        // Create temporary object

        // Synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: Write Called.");
        //perform the write
        mConnectedThread.write(out);
    }
}
