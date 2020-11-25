package com.example.p8companion;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // UI elements
    private Button mStartServiceButton, mStopServiceButton;

    // A reference to the foreground service, assigned during binding
    private ForegroundService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    // Bluetooth stuff
    private BluetoothAdapter mBluetoothAdapter;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ForegroundService.LocalBinder binder = (ForegroundService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            Log.d(TAG,"Service bound!");

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

        mStartServiceButton = findViewById(R.id.StartServiceButton);
        mStopServiceButton = findViewById(R.id.StopServiceButton);

        mStartServiceButton.setOnClickListener(view -> {
            if (!checkPermissions()) {
                requestPermissions();
            } else {
                startService();
                // Then bind to the service
                bindService(new Intent(getApplicationContext(), ForegroundService.class), mServiceConnection,
                        Context.BIND_AUTO_CREATE);
                // The bluetooth operations are in the ServiceConnection callback

            }
        });

        mStopServiceButton.setOnClickListener(view -> stopService());

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    public void startService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
        ContextCompat.startForegroundService(this, serviceIntent);
    }
    public void stopService() {
        mService.stopSelf();
        unbindService(mServiceConnection);
        mBound = false;
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
                android.Manifest.permission.BLUETOOTH_ADMIN
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
            } else if (ForegroundService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d(TAG,"Bluetooth disconnected !");
            } else if (ForegroundService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Configure the GattService we'll use as a notification GATT
                mService.enableCharacteristicNotification();
            } else if (ForegroundService.ACTION_DATA_AVAILABLE.equals(action)) {
                String data = intent.getStringExtra(ForegroundService.EXTRA_DATA);
                Log.d(TAG, "Bluetooth data received: " + data);
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
}