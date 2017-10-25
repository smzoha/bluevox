package com.zedapps.bluevox;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * ConnectDevice class
 * A component of BlueVox application
 *
 * @author Shamah M Zoha
 * @github https://bitbucket.org/smzoha/
 * @since 05/12/2015
 */

public class ConnectDevice extends ActionBarActivity {
    private static final int CONNECTION_SUCCESS = 0;
    private static final int MESSAGE_READ = 1;
    private static final int SPEECH_RESULT = 5;

    private final String[] commandArray = {"FORWARD", "BACKWARD", "LEFT", "RIGHT", "STOP", "CLOCKWISE", "ANTI CLOCKWISE"};
    private final List<String> commandList = Arrays.asList(commandArray);

    ConnectThread connThread;
    ConnectedThread connctdThread;

    Button voiceCmd;
    Button manCmd;

    main mainObj;

    Set<BluetoothDevice> pairedDeviceSet;
    ArrayList<String> pairedDeviceList;
    ArrayAdapter<String> PDListAdapter;
    ListView LVPairedDevices;

    static Handler msgHandler;

    String outMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_device);

        // initializing the buttons
        voiceCmd = (Button) findViewById(R.id.btnVoiceCmd);
        manCmd = (Button) findViewById(R.id.btnManCmd);

        // obtaining an object of the previous activity to eradicate overhead of BTAdapter
        Intent mainInt = getIntent();
        mainObj = (main) mainInt.getSerializableExtra("passObject");

        // initializing the lists and the adapters, and assigning the listview objects
        pairedDeviceList = new ArrayList<String>();
        PDListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pairedDeviceList);
        LVPairedDevices = (ListView) findViewById(R.id.LVPairedDevices);
        LVPairedDevices.setAdapter(PDListAdapter);

        // initializing the handler, which performs action based on messages sent by the threads
        msgHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (msg.what == CONNECTION_SUCCESS) {
                    Toast.makeText(getApplicationContext(), "Successfully connected to device!", Toast.LENGTH_SHORT).show();
                }

                if (msg.what == MESSAGE_READ) {
                    System.out.println(connctdThread.bSocket.isConnected());
                    try {
                        byte[] readBuff = (byte[]) msg.obj;
                        String rdMsg = new String(readBuff);
                        Toast.makeText(getApplicationContext(), "Message received: " + rdMsg, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Please connect to the device first.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        // generating the paired devices list
        populatePairedDeviceList();

        // on item click, the paired device will start a new connect thread, depending on whichever
        // device is clicked upon from the list
        LVPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // if adapter is still discovering, cancel discovery to speed up process
                if (main.BLAdpt.isDiscovering()) {
                    main.BLAdpt.cancelDiscovery();
                }

                final int deviceIndx = position;

                // display a confirmation dialog
                AlertDialog.Builder confDiagBuilder = new AlertDialog.Builder(ConnectDevice.this);
                confDiagBuilder.setTitle("Do you wish to connect to this device?");

                // if yes is clicked, the connection thread is initialized
                confDiagBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Object[] pdArray = pairedDeviceSet.toArray();
                        BluetoothDevice selectedDevice = (BluetoothDevice) pdArray[deviceIndx];
                        connThread = new ConnectThread(selectedDevice);
                        connThread.start();
                    }
                });

                // if no is clicked, do nothing and display a toast message
                confDiagBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Connection was not made with the device", Toast.LENGTH_SHORT).show();
                    }
                });

                confDiagBuilder.show();
            }
        });

        // on voice command button click, show the speech recognition dialog and seek user input
        // if valid input is found, action is taken based on what is defined in onActivityResult method
        voiceCmd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent speechToText = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                // specify what the user is going to say
                speechToText.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                // define the language for the recognizer
                speechToText.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

                try {
                    startActivityForResult(speechToText, SPEECH_RESULT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // on clicking manual control button, a dialog is displayed to the user, with available options
        manCmd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // display a dialog for manual controls
                AlertDialog.Builder manControlDiag = new AlertDialog.Builder(ConnectDevice.this);
                manControlDiag.setTitle("Manual Controls");
                manControlDiag.setMessage("Press one of the buttons below to send command to the device:");

                // retrieve application context to reduce redundancy
                Context appContext = getApplicationContext();

                // create layout to add buttons to the dialog
                LinearLayout diagLayout = new LinearLayout(appContext);
                diagLayout.setOrientation(LinearLayout.VERTICAL);

                // populate the view with buttons and add OnClickListener to them
                final Button fwdBtn = new Button(appContext);
                fwdBtn.setText("FORWARD");
                diagLayout.addView(fwdBtn);
                fwdBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        outMsg = "FORWARD";
                        connctdThread.write(outMsg);
                    }
                });

                final Button backBtn = new Button(appContext);
                backBtn.setText("BACKWARD");
                diagLayout.addView(backBtn);
                backBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        outMsg = "BACKWARD";
                        connctdThread.write(outMsg);
                    }
                });

                final Button leftBtn = new Button(appContext);
                leftBtn.setText("LEFT");
                diagLayout.addView(leftBtn);
                leftBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        outMsg = "LEFT";
                        connctdThread.write(outMsg);
                    }
                });

                final Button rightBtn = new Button(appContext);
                rightBtn.setText("RIGHT");
                diagLayout.addView(rightBtn);
                rightBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        outMsg = "RIGHT";
                        connctdThread.write(outMsg);
                    }
                });

                final Button cwBtn = new Button(appContext);
                cwBtn.setText("CLOCKWISE");
                diagLayout.addView(cwBtn);
                cwBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        outMsg = "CLOCKWISE";
                        connctdThread.write(outMsg);
                    }
                });

                final Button acwBtn = new Button(appContext);
                acwBtn.setText("ANTI-CLOCKWISE");
                diagLayout.addView(acwBtn);
                acwBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        outMsg = "ANTI CLOCKWISE";
                        connctdThread.write(outMsg);
                    }
                });

                final Button stopBtn = new Button(appContext);
                stopBtn.setText("STOP");
                diagLayout.addView(stopBtn);
                stopBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        outMsg = "STOP";
                        connctdThread.write(outMsg);
                    }
                });

                // add layout to dialog
                manControlDiag.setView(diagLayout);

                // if cancelled by the user, show a toast message indicating such
                manControlDiag.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Operation was cancelled by the user", Toast.LENGTH_SHORT).show();
                    }
                });

                // finally, show the dialog
                manControlDiag.show();
            }
        });
    }


    @Override
    // if the request code matches speech result code, the result code is checked if it has yield ok
    // if the result is ok and data is not null, the text is taken in and displayed as a toast
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == SPEECH_RESULT) {
            if (resultCode == RESULT_OK && intent != null) {
                ArrayList<String> textFromSpeech = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                outMsg = textFromSpeech.get(0).toUpperCase();

                // if the command is valid, proceed, else display toast message as declination
                if (commandList.contains(outMsg)) {

                    // upon retrieving message, a confirmation dialog is displayed
                    AlertDialog.Builder msgConfDiag = new AlertDialog.Builder(ConnectDevice.this);
                    msgConfDiag.setTitle("Confirmation");
                    msgConfDiag.setMessage("Do you wish to send the following command to the device?\n '" + outMsg + "'");

                    // upon positive response, pass the text to the thread to write to socket
                    msgConfDiag.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            connctdThread.write(outMsg);
                        }
                    });

                    // show a toast message to indicate cancellation
                    msgConfDiag.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), "Operation was cancelled by the user", Toast.LENGTH_SHORT).show();
                        }
                    });

                    // finally, show the dialog
                    msgConfDiag.show();

                } else {
                    Toast.makeText(getApplicationContext(), outMsg + " is not a valid command!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // add the already paired devices in the paired device list
    private void populatePairedDeviceList() {
        pairedDeviceList.clear();

        pairedDeviceSet = mainObj.BLAdpt.getBondedDevices();
        if (pairedDeviceSet.size() > 0) {
            for (Iterator<BluetoothDevice> i = pairedDeviceSet.iterator(); i.hasNext(); ) {
                BluetoothDevice tempo = i.next();
                pairedDeviceList.add(tempo.getName());
            }
        }

        PDListAdapter.notifyDataSetChanged();
    }

    /**
     * ConnectThread Class
     * Used to create a socket that will connect to the bluetooth device.
     * Helps in allowing multi-tasking in the app.
     * Much thanks to Bluetooth tutorial over @ developer.android.com for helping out with the class.
     */

    class ConnectThread extends Thread {
        private final BluetoothSocket bSocket;
        private final BluetoothDevice bDevice;
        private final UUID UUID_VAR = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


        // initialize thread with a provided device and the UUID value declared in this class
        public ConnectThread(BluetoothDevice bluetoothDevice) {
            BluetoothSocket tmpSocket = null;
            bDevice = bluetoothDevice;
            try {
                tmpSocket = bDevice.createRfcommSocketToServiceRecord(UUID_VAR);
            } catch (Exception e) {
                e.printStackTrace();
            }

            bSocket = tmpSocket;
        }

        // start the thread by connecting the socket to the server device. if unable, close socket
        public void run() {
            try {
                bSocket.connect();
            } catch (Exception e) {
                try {
                    bSocket.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                e.printStackTrace();
            }

            connctdThread = new ConnectedThread(bSocket);
            connctdThread.start();
            msgHandler.obtainMessage(CONNECTION_SUCCESS, bSocket).sendToTarget();
        }

        // cancel the on-going connection and close the socket
        public void cancel() {
            try {
                bSocket.close();
            } catch (Exception e) {
                e.getStackTrace();
            }
        }
    }

    /**
     * ConnectedThread Class
     * Using the socket established above, the I/O streams are created, followed by a few methods
     * to send and receive data over BT.
     * Much thanks to Bluetooth tutorial over @ developer.android.com for helping out with the class.
     */

    class ConnectedThread extends Thread {
        private final BluetoothSocket bSocket;
        private final InputStream bIStream;
        private final OutputStream bOStream;

        // initializing thread with a socket, which is used to create a set of IO streams
        public ConnectedThread(BluetoothSocket bsoc) {
            bSocket = bsoc;
            InputStream tmpIS = null;
            OutputStream tmpOS = null;

            try {
                tmpIS = bSocket.getInputStream();
                tmpOS = bSocket.getOutputStream();
            } catch (Exception e) {
                e.printStackTrace();
            }

            bIStream = tmpIS;
            bOStream = tmpOS;
        }

        // run method - used to constantly read from the socket for incoming data
        public void run() {
            byte[] buffer;
            int bytes;

            while (true) {
                try {
                    buffer = new byte[1024];
                    bytes = bIStream.read(buffer);
                    msgHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        // write method - using string provided, convert it to a byte array and pass it to the connected device through socket
        public void write(String wString) {
            byte[] wByte = wString.getBytes();
            try {
                bOStream.write(wByte);
                bOStream.flush();
                Toast.makeText(getApplicationContext(), outMsg + " sent to device!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // cancel method - close the socket upon cancellation of thread
        public void cancel() {
            try {
                bSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}