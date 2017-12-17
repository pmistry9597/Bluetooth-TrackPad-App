package pmistry9597.bluetoothtrackpad;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Preet Mistry on 2017-12-03.
 */

public class BluetoothService extends Thread {
    public static final int MOVEMENT_EVENT = 1002; // this indicates a movement message
    public static final int BUTTON_EVENT = 1003; // this indicates a mouse button event
    public static final String MSG_TYPE = "msgtypeblahblahstufstufk;;k"; // key for retrieving message type
    public static final String MSG_INFO = "ms info fblahblaf";
    Handler toMainHandler; // handler for communicating with main thread
    Handler recvHandler; // handler for receiving messages
    BluetoothAdapter btAdapter; // main bluetooth adapter
    BluetoothSocket targetSocket; // socket with target device
    OutputStream targetOutStream; // stream to send data to target device
    public final String targetName = "LAPTOP1"; // name of the target device (this will be changed so
    // the user can find the paired device to connect to)
    public final UUID targetUUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");// uuid for connecting
    // with target device (this uuid is preset on the bluetooth server)

    // movement events will be sent like this:
    // "mXXXXYYYY\0"

    // button events will be sent like this:
    // "b[L/R][D/U]\0"
    // L means left, R means right
    // D means down, U means up

    // method that returns the handler to send messages to this thread
    public Handler getHandler() {
        return recvHandler;
    }

    // this handler will process messages such as movement gestures and clicks
    static class BTServiceHandler extends Handler {
        // reference to BluetoothService Object
        BluetoothService btService;
        public BTServiceHandler(BluetoothService btService) {
            this.btService = btService;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // retrieve the message type
            int msgType = msg.getData().getInt(MSG_TYPE);
            // retrieve message contents
            String msgInfo = msg.getData().getString(MSG_INFO);
            // end result of data processing for sending will be stored here
            String data;
            // handle different events based on retrieved message type
            switch (msgType) {
                case MOVEMENT_EVENT: // this indicates an event to move cursor on screen
                    // in this case change in position is recorded like this in
                    // msg_info data:
                    // XXXYYY
                    // where X are x coord digits an Y are y coord digits
                    // in this case we will send entire block as a string with proper
                    // wrapper to indicate an end message:
                    // so first we will properly wrap the message:
                    data = "m" + msgInfo + '\0';
                    // show us what is being sent
                    //System.out.println(e);
                    // now we will send the end result
                    btService.sendData(data);
                    break;
                case BUTTON_EVENT: // means a button event - can be up or down
                    // data coming into this handler for button event will be like this:
                    // [L/R][D/U]
                    // L is right, R is up
                    // D is down, U is up
                    // now we need to add a wrapper to indicate the event and signify termination
                    data = "b" + msgInfo + '\0';
                    // show us what is being sent
                    System.out.println(data);
                    // send it
                    // now we will send the end result
                    btService.sendData(data);
                    break;
            }
        }
    }

    public BluetoothService(Handler handler) {
        super();
        toMainHandler = handler;
        btAdapter = BluetoothAdapter.getDefaultAdapter(); // get main bluetooth adapter used by device
        // this handler will process messages such as movement gestures and clicks
        recvHandler = new BTServiceHandler(this);
    }

    // send message as a string over bluetooth from inputted string
    void sendData(String s) {
        // check if there is a stream to write to
        if (targetOutStream != null) {
            // first convert to byte array
            byte data[] = s.getBytes();
            // send the data to target device
            try {
                targetOutStream.write(data);
            } catch (IOException io) {
                // tell the main thread of the failure
                Bundle b = new Bundle();
                b.putString(MainActivity.GET_KEY, MainActivity.MSG_KEY);
                b.putString(MainActivity.MSG_KEY, "Error sending command!");
                // pack it a message and send it
                Message msg = Message.obtain();
                msg.setData(b);
                toMainHandler.sendMessage(msg);
            }
        }
    }

    @Override
    public void run() {
        super.run();
        // connect to the target device
            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) { // loop through all paired devices
                if (device.getName().equals(targetName)) {
                    // if the code reached this point, then its the right device
                    btSetup(device);
                }
            }
        // create looper to communicate if connect was successful
        Looper.prepare(); // prepare message looper
        Looper.loop(); // wait for messages
    }

    // this runs when the right device has been detected
    void btSetup(BluetoothDevice targetDevice) {
        // create the socket
        try {
            // create socket to handle connection
            targetSocket = targetDevice.createInsecureRfcommSocketToServiceRecord(targetUUID);
            targetSocket.connect(); // try connecting
            targetOutStream = targetSocket.getOutputStream(); // get the output stream
        } catch (IOException e) {
            // send message to gui what happened
            Message msg = Message.obtain(); // safely obtain a message object
            Bundle b = new Bundle();
            b.putString(MainActivity.GET_KEY, MainActivity.MSG_KEY); // this tells kind of msg being sent
            b.putString(MainActivity.MSG_KEY, "Error while connecting!"); // actual message
            msg.setData(b); // pack it up
            toMainHandler.sendMessage(msg); // send it
        }
    }
}
