package com.example.todoapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BluetoothShareActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothHelper bluetoothHelper;
    private BluetoothDeviceAdapter deviceAdapter;
    private List<BluetoothDevice> deviceList;
    private DatabaseHelper db;
    private TextView statusText;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    deviceAdapter.addDevice(device);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_share);

        db = new DatabaseHelper(this);
        bluetoothHelper = new BluetoothHelper();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceList = new ArrayList<>();
        statusText = findViewById(R.id.statusText);

        RecyclerView recyclerView = findViewById(R.id.deviceRecyclerView);
        deviceAdapter = new BluetoothDeviceAdapter(this, deviceList, new BluetoothDeviceAdapter.OnDeviceClickListener() {
            @Override
            public void onDeviceClick(BluetoothDevice device) {
                sendTaskList(device);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(deviceAdapter);

        Button btnScan = findViewById(R.id.btnScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanning();
            }
        });

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    private void startScanning() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        deviceList.clear();
        deviceAdapter.notifyDataSetChanged();
        statusText.setText("Scanning...");
        bluetoothAdapter.startDiscovery();
    }

    private void sendTaskList(BluetoothDevice device) {
        String data = buildShareString();
        statusText.setText("Connecting to " + device.getName() + "...");

        bluetoothHelper.sendData(device, data, new BluetoothHelper.BluetoothListener() {
            @Override
            public void onConnectionSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusText.setText("Sending list...");
                    }
                });
            }

            @Override
            public void onMessageReceived(String message) {}

            @Override
            public void onConnectionFailed(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusText.setText("Failed to connect.");
                        Toast.makeText(BluetoothShareActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private String buildShareString() {
        List<Task> tasks = db.getAllTasks();
        StringBuilder sb = new StringBuilder("TODO_SHARE");
        for (Task task : tasks) {
            sb.append("::").append(task.getTitle());
        }
        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        bluetoothHelper.closeSocket();
        bluetoothAdapter.cancelDiscovery();
    }
}