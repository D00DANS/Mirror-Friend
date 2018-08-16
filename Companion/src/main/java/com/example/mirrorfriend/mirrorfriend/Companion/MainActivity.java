package com.example.mirrorfriend.mirrorfriend.Companion;

import android.Manifest;
import android.Manifest.permission;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;

import java.nio.charset.Charset;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    BluetoothAdapter mBluetoothAdapter;
    Blue2 mBluetoothConnection;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    BluetoothDevice mBTDevice;

    String pLat, pLng, pTime, txtSize, tColor, bColor;

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    mBTDevice = mDevice;
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        startConnection();

        /*  Location section: Variable content to obtain - Latitude, Longitude, Time.   */
        if (checkLocationPermission()) {
            //Make an instance of LocationManager called lm(short for locationManager)
            LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            //Call lm to provide the last known location of the phone via network provider called lkl(short for lastKnownLocation)
            assert lm != null;
            Location lkl = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            //Make variables to hold the value of Latitude, Longitude, and Time. To be sent to mirror.
            double phoneLat = lkl.getLatitude();
            double phoneLng = lkl.getLongitude();
            pLat = Double.toString(phoneLat);
            pLng = Double.toString(phoneLng);
        }/*  End Location Section.    */

        final int[] RR = {0};

        /*  SeekBar Section: Variable content to obtain - refreshRate integer.  */
        //Make a SeekBar called refreshRate.
        SeekBar refreshRate = findViewById(R.id.seekBarRefreshRate);
        refreshRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // Determine the value of the Seek bar for Refresh Rate by taking the initial value and modifying for how the user has moved the cursor on the bar. Send this value to the mirror.
            @Override
            public void onProgressChanged(SeekBar refreshRate, int progress, boolean fromUser) {
                RR[0] = ((progress + 1) * 50000);
             }

            @Override
            public void onStartTrackingTouch(SeekBar refreshRate) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar refreshRate) {
            }
        });/*  End SeekBar Section. */

        /*  Switch Section: Variable content to obtain - textSizeValue Boolean.  */
        //Make a boolean called textSizeValue to hold the true or false value that will determine text size on mirror.
        final boolean[] textSizeValue = {false};
        Switch textSize = findViewById(R.id.switchTextSize);
        textSize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            // The value of the switch is a boolean with it's default state as false. Check which position the switch is in and return it so that it can be sent to the mirror.
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                textSizeValue[0] = isChecked;
                // If the switch is checked set this variable to true.
                int tSize = isChecked ? 1 : 0;
                txtSize = Integer.toString(tSize);
            }
        });

        /*  End Switch Section.  */
        /*  Text Color Spinner Section: Variable content to obtain - Text color string. */
        Spinner spinnerTextColor = findViewById(R.id.spinnerTextColor);
        //Create an instance of spinner that is connected to the spinnerTextColor.
        ArrayAdapter<CharSequence> adapterTextColor = ArrayAdapter.createFromResource(this,
                R.array.TextBackgroundColors, android.R.layout.simple_spinner_item);
        // Create an Array Adapter to attach the array of colors to the spinner, call layout.
        adapterTextColor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the contents of the array to the spinner and apply the layout.
        spinnerTextColor.setAdapter(adapterTextColor);
        // Apply the previous actions to the spinnerTextColor.
        int spinnerTextColorValue = (int) spinnerTextColor.getSelectedItem();
        tColor = Integer.toString(spinnerTextColorValue);
        // Record the selected item as an int to be sent to mirror.
        /*  End Text Color Spinner Section. */

        /*  Background Color Spinner Section: Variable content to obtain - Background color string. */
        Spinner spinnerBackgroundColor = findViewById(R.id.spinnerBackgroundColor);
        ArrayAdapter<CharSequence> adapterBackgroundColor = ArrayAdapter.createFromResource(this,
                R.array.TextBackgroundColors, android.R.layout.simple_spinner_item);
        adapterBackgroundColor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBackgroundColor.setAdapter(adapterBackgroundColor);
        int spinnerBackgroundColorValue = (int) spinnerBackgroundColor.getSelectedItem();
        bColor = Integer.toString(spinnerBackgroundColorValue);
        // See spinnerTextColor above and apply to second spinnerBackgroundColor.
        /*  End Background Color Spinner Section. */

        Timer timer = new Timer();
        timer.schedule(new refresh(), 0, RR[0]);
    }

    public void startConnection() {
        startBTConnection(mBTDevice, MY_UUID);
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid) {
        mBluetoothConnection.startClient(device, uuid);
    }

    public boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION);
                }
            }
        }
    }

    public class refresh extends TimerTask {
        public void run() {

            byte[] byteLat = pLat.getBytes(Charset.defaultCharset());
            byte[] byteLng = pLng.getBytes(Charset.defaultCharset());
            byte[] byteSize = txtSize.getBytes(Charset.defaultCharset());
            byte[] byteTC = tColor.getBytes(Charset.defaultCharset());
            byte[] byteBGC = bColor.getBytes(Charset.defaultCharset());

            mBluetoothConnection.write(byteLat);
            mBluetoothConnection.write(byteLng);
            mBluetoothConnection.write(byteSize);
            mBluetoothConnection.write(byteTC);
            mBluetoothConnection.write(byteBGC);
        }
    }

}
