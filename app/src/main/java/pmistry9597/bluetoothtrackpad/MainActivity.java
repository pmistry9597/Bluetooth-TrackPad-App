package pmistry9597.bluetoothtrackpad;

/*
 * ---------- Created by Preet Mistry on December 3, 2017 -------------
 * This program communicates with a bluetooth server on a computer that sends mouse movement and click commands
 * so the server can interpret these commands and use them to control the computer
 *
 * scrolling and keyboard might be added
 *
 * --------- Notes: ----------
 * - server or computer refers to the bluetooth server running on the host
 *      (sometimes it might refer to the actual host running the server)
 * - BluetoothService is a class created by me to handle communications with the server
 */

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    // communicate with bluetooth thread
    Handler fromBtHandler;
    // handler send data to bluetooth thread
    Handler toBtHandler;
    Button leftButton; // left button reference
    Button rightButton;// right button reference
    public static final String MSG_KEY = "msg key blahblahblah";// key for retrieving message
    public static final String GET_KEY = "msgtypeblahblah"; // key that indicates sending message
    View trackPad;// stores trackpad view reference

    BluetoothService btService;// bluetooth thread

    // these variables store variables to the previous x and y coordinates
    // which is required to calculate change from previous coords
    float prevX;
    float prevY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // retrieve the trackpad
        trackPad = findViewById(R.id.trackpad);
        leftButton = findViewById(R.id.leftClck); // retrieve the left button
        rightButton = findViewById(R.id.rightClck); // retrieve right button

        fromBtHandler = new Handler() { // handles messages from bt thread
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String msgType = msg.getData().getString(GET_KEY);// get the kind of message sent
                if (msgType.equals(MSG_KEY)) { // if the message was a toast to the gui
                    // display the message in a toast
                    String message = msg.getData().getString(MSG_KEY);
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                }
            }
        };

        // handle touch input for the trackpad
        // that will handle sending data to the bluetooth service about events that occurred
        trackPad.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // print out coordinates of the touch relative to the screen
                float x = motionEvent.getX();
                float y = motionEvent.getY();
                 // get action type
                int action = motionEvent.getActionMasked();

                // react according to action type that occurred
                switch (action) {
                    case MotionEvent.ACTION_DOWN: // handle touch event
                        break;
                    case MotionEvent.ACTION_MOVE: // handle movement gestures
                        // run function that will handle sending data to target device
                        // JUST TO BE CLEAR THE X AND Y COORDS IN THIS CASE REPRESENT CHANGE IN X AND Y, NOT THE ACTUAL
                        // X AND Y COORD
                        // first get the change in x and y coordinates
                        float dx = x - prevX;
                        float dy = y - prevY;
                        // send data to bluetooth device using function to package it properly
                        // and handle sending to bt thread
                        sendMovementData(dx, dy);
                        break;

                }
                // update prevX and prevY to new coords
                prevX = x;
                prevY = y;
                return true;
            }
        });

        // left button touch handler - this will send command to server to send left button down
        // on button down on android and left button up command to server on button up on android
        leftButton.setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // this will send a left button down command on ACTION_DOWN
                // and left button up command on ACTION_UP
                int action = motionEvent.getAction();
                // stores msg to be sent to server
                String msg;
                // react according to type of event that occurred
                switch (action) {
                    case MotionEvent.ACTION_DOWN: // when button goes down
                        view.setPressed(true); // make button view go down - we have to do this
                        // since we're overriding a button touch event
                        // create string message that carries a left down command like this:
                        // "LD"
                        msg = "LD";
                        // send it
                        sendData(BluetoothService.BUTTON_EVENT, msg);
                        break;
                    case MotionEvent.ACTION_UP: // when button goes up
                        view.setPressed(false); // make button view go up - we have to this
                        // since we're overriding a button touch event
                        // create string message that carries a left up command like this:
                        // "LU"
                        msg = "LU";
                        // send it
                        sendData(BluetoothService.BUTTON_EVENT, msg);
                        break;
                }
                return true;
            }
        });

        // right button handler - this will send command to server to send right button down
        // on button down on android and right button up command to server on button up on android
        rightButton.setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // this will send a left button down command on ACTION_DOWN
                // and left button up command on ACTION_UP
                int action = motionEvent.getAction();
                // stores msg to be sent to server
                String msg;
                // react according to type of event that occurred
                switch (action) {
                    case MotionEvent.ACTION_DOWN: // when button goes down
                        view.setPressed(true); // make button view go down - we have to do this
                        // since we're overriding a button touch event
                        // create string message that carries a left down command like this:
                        // "RD"
                        msg = "RD";
                        // send it
                        sendData(BluetoothService.BUTTON_EVENT, msg);
                        break;
                    case MotionEvent.ACTION_UP: // when button goes up
                        view.setPressed(false); // make button view go up - we have to this
                        // since we're overriding a button touch event
                        // create string message that carries a left up command like this:
                        // "RU"
                        msg = "RU";
                        // send it
                        sendData(BluetoothService.BUTTON_EVENT, msg);
                        break;
                }
                return true;
            }
        });

        btService = new BluetoothService(fromBtHandler); // pass handler to this thread
        // get handler to send data to this thread
        fromBtHandler = btService.getHandler();
        btService.start(); // start the service
    }

    // send movement data to bluetooth thread
    void sendMovementData(float xf, float yf) {
        // the data will have a string in form of this:
        // [+/-]XXX[+/-]YYY
        // where each X is a x coord digit and each Y is a y coord digit
        // and each [+/-] is a single character that is either
        // + or -
        // so we don't lose any resolution with negative numbers
        // first get 4 character integer in a string to represent each coordinate value
        // JUST TO BE CLEAR THE X AND Y COORDS REPRESENT CHANGE IN X AND Y, NOT THE ACTUAL
        // X AND Y COORD
        String xCords = floatTo4Char(xf);
        String yCords = floatTo4Char(yf);
        // combine the coordinates together to get final product
        String finalCoord = xCords + yCords;
        // send data to server
        sendData(BluetoothService.MOVEMENT_EVENT, finalCoord);
    }
    // send data to BluetoothService to be sent
    void sendData(int msgType, String info) {
        // pack it up in a bundle with the msg type and coordinates for the message
        /// msg type is movement_event in this case as it indicates command to move cursor
        Bundle b = new Bundle();
        b.putInt(BluetoothService.MSG_TYPE, msgType); // indicate this is
        // a movement command
        b.putString(BluetoothService.MSG_INFO, info); // put coords under MSG_INFO tag
        // as this is the info behind the command to move
        // pack it up a message object
        Message msg = Message.obtain();
        msg.setData(b);
        // send it to the bluetooth thread
        btService.getHandler().sendMessage(msg);
    }
    // convert float to 3 digit integer in the form of a string
    String floatTo4Char(float f) {
        int num = (int) f;// convert float to integer to remove decimals
        // first part is converting the magnitude of the number to
        // a three digit number string
        // ----- 3 DIGIT MAG CONVERSION ----
        // first add two zeroes to the string derived from the integer
        String numberStr = Integer.toString(Math.abs(num));
        numberStr = "00" + numberStr;
        // now trim off all but the last three characters
        numberStr = numberStr.substring(numberStr.length() - 3);
        // ----- END OF 3 DIGIT CONVERSION -----
        // now add a negative sign or positive based on whether the number was negative or positive (or equal to zero)
        if (num >= 0) {
            numberStr = "+" + numberStr;
        } else {
            numberStr = "-" + numberStr;
        }
        return numberStr;
    }
}
// --------------- Author's Notes --------------
//  - BluetoothService is a class made by me, not a builtin Android system class
//  - BluetoothService objects handle bluetooth communication on separate thread to stop the UI from freezing
//  - i'm tired
//  - i'm bored


// --------------- GOALS FOR IMPROVING PROGRAM -------
// - make the Bluetooth thread handle packing the data into a string for sending
// - make a method to get paired devices and select one for pairing
// - add scrolling
// - add options for sensitivity (options on server as well)
//      - keep sensitivity synchronized in both server and app?
// - add pinch zoom