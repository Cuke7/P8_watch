package com.example.p8companion;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // UI elements
    private Button mStopServiceButton, mSettingsButton, mSetTimeButton, mSetMeteoButton;
    private TextView LogText;
    private Spinner LuminositySpinner;

    // Contain all logs to display
    private String logs ="Logs : ";

    // A reference to the foreground service, assigned during binding
    private ForegroundService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    private boolean test = false;

    // Display or not on the watch the notifications received
    private boolean DisplayNotification = false;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ForegroundService.LocalBinder binder = (ForegroundService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            // Initialise bluetooth
            if (!mService.initializeBLE()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }else{
                mService.connectBLE("E3:7F:48:82:0E:1D");
            }

            // Register the receiver for bluetooth events from the service
            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG,"Service disconnected");
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStopServiceButton = findViewById(R.id.StopServiceButton);
        mSettingsButton = findViewById(R.id.SettingsButton);
        mSetTimeButton = findViewById(R.id.SetTimeButton);
        mSetMeteoButton = findViewById(R.id.SetMeteoButton);
        LuminositySpinner = findViewById(R.id.LuminostitySpinner);
        // Text view we'll use as log
        LogText = findViewById(R.id.LogText);

        mStopServiceButton.setOnClickListener(view -> mService.customStopService());

        mSettingsButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        mSetTimeButton.setOnClickListener(view -> {
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
            String now = df.format(new Date());
            mService.send("AT+DT=" + now + "\r\n");
        });

        mSetMeteoButton.setOnClickListener(view -> {
            mService.sendMeteo();
        });

        // Get les options du spinner dans strings.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.Luminosity, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        LuminositySpinner.setAdapter(adapter);

        LuminositySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(mBound){
                    if(test){
                        switch(position) {
                            case 0:
                                mService.send("AT+CONTRAST=100\r\n");
                                break;
                            case 1:
                                mService.send("AT+CONTRAST=175\r\n");
                                break;
                            case 2:
                                mService.send("AT+CONTRAST=200\r\n");
                                break;
                        }
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        // Setup the broadcast used to receive the notifications
        LocalBroadcastManager.getInstance(this).registerReceiver(MyReceiver, new IntentFilter(NotificationListener.ACTION_STATUS_BROADCAST));

        // Ensures that the settings are properly initialized with their default values
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);

        if (!checkPermissions()) {
            requestPermissions();
            getApplicationContext().startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    @Override
    protected void onStart() {
        if(!mBound){
            bindService(new Intent(getApplicationContext(), ForegroundService.class), mServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }
        super.onStart();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        DisplayNotification = sharedPref.getBoolean(SettingsActivity.KEY_NOTIFICATION_SWITCH, false);
    }

    @Override
    protected void onStop() {
        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }
        super.onStop();
    }

    private boolean checkPermissions() {
        return  PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestPermissions() {
        Log.i(TAG, "Requesting permission");
        String[] PERMISSIONS = {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN,
                android.Manifest.permission.INTERNET
        };
        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    // Bluetooth stuff

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read or notification operations.
    public final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ForegroundService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.d(TAG,"Bluetooth connected !");
                mService.mConnected = true;
                log("P8 watch connected !");
                mService.customStartService();
                test = true;
            } else if (ForegroundService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d(TAG,"Bluetooth disconnected !");
                mService.mConnected = false;
            } else if (ForegroundService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Configure the GattService we'll use as a notification GATT
                mService.enableCharacteristicNotification();
            } else if (ForegroundService.ACTION_DATA_AVAILABLE.equals(action)) {
                String data = intent.getStringExtra(ForegroundService.EXTRA_DATA);
                Log.d(TAG, "Bluetooth data received: " + data);
                log(data);
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ForegroundService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(ForegroundService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ForegroundService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(ForegroundService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    // Notification stuff

    // Is called when NotificationListener.java receive a new notification
    private BroadcastReceiver MyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra("title");
            String text = intent.getStringExtra("text");
            if(DisplayNotification) {
                mService.send("AT+HTTP="+title+"\n"+text+"\r\n");
                log("Sending notification");
            }
        }
    };

    private void log(String text){
        logs = logs + "\n" + text;
        LogText.setText(logs);
    }
}