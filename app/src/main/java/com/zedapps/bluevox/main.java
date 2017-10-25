package com.zedapps.bluevox;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.io.Serializable;

/**
 * BlueVox application
 *
 * @author Shamah M Zoha
 * @github https://bitbucket.org/smzoha/
 * @since 20/11/2015
 */

public class main extends ActionBarActivity implements Serializable {
    Button pairDevice;
    Button connect;
    Button powerUp;

    static BluetoothAdapter BLAdpt;

    final static int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        powerUp = (Button) findViewById(R.id.btnPowerUp);
        pairDevice = (Button) findViewById(R.id.btnPairDevice);
        connect = (Button) findViewById(R.id.btnConnect);

        powerUp.setEnabled(false);
        pairDevice.setEnabled(false);
        connect.setEnabled(false);

        // try to obtain bluetooth adapter. message if adapter not found.
        BLAdpt = BluetoothAdapter.getDefaultAdapter();
        if (BLAdpt == null) {
            Toast.makeText(getApplicationContext(), "Your device does not support bluetooth. The application will not work.",
                    Toast.LENGTH_SHORT).show();
        }

        // check if BT is enabled. if enabled, disable power up and enable pair & connect
        // else enable power up only
        if (BLAdpt.isEnabled()) {
            Toast.makeText(getApplicationContext(), "Bluetooth is turned on. Proceed to connect to device.",
                    Toast.LENGTH_SHORT).show();
            pairDevice.setEnabled(true);
            connect.setEnabled(true);
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth is not turned on. You can turn on Bluetooth " +
                            "by simply pressing 'Pair Device' button above.",
                    Toast.LENGTH_SHORT).show();
            powerUp.setEnabled(true);
        }

        // powerUp on click listener which gives user bluetooth prompt
        powerUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                powerUpBT();
            }
        });

        // pairing on click listener
        pairDevice.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoBLIntent();
            }
        });

        connect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoConnIntent();
            }
        });
    }

    // method for the powerUp button
    // provides power-up dialog to user and depending on the choice by user, the log is updated
    // in onActivityResult method.
    public void powerUpBT() {
        Intent enBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enBTIntent, REQUEST_ENABLE_BT);
    }

    // shift to Bluetooth settings, such to pair the device
    public void gotoBLIntent() {
        Intent gotoBLSettings = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(gotoBLSettings);
    }

    // shift to ConnectDevice intent to connect to a device
    private void gotoConnIntent() {
        Intent ConnIntent = new Intent(this, ConnectDevice.class);
        startActivity(ConnIntent);
    }


    @Override
    // checks request code against the action code and result code against request ok, and performs action.
    // for bluetooth power up, if bluetooth has been turned on, enables pair and connect
    // buttons, else keep as is.
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth has successfully been turned on.", Toast.LENGTH_SHORT).show();
                powerUp.setEnabled(false);
                pairDevice.setEnabled(true);
                connect.setEnabled(true);
            }

            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Bluetooth must be enabled to proceed further.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}