package com.example.todoapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class BluetoothReceiveActivity extends AppCompatActivity {

    private BluetoothHelper bluetoothHelper;
    private DatabaseHelper db;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_receive);

        db = new DatabaseHelper(this);
        bluetoothHelper = new BluetoothHelper();
        statusText = findViewById(R.id.statusText);

        Button btnListen = findViewById(R.id.btnListen);
        btnListen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startListening();
            }
        });
    }

    private void startListening() {
        statusText.setText("Waiting for connection...");

        bluetoothHelper.receiveData(new BluetoothHelper.BluetoothListener() {
            @Override
            public void onConnectionSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusText.setText("Connected! Receiving list...");
                    }
                });
            }

            @Override
            public void onMessageReceived(String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (message.startsWith("TODO_SHARE")) {
                            importTaskList(message);
                        } else {
                            statusText.setText("Invalid data received.");
                        }
                    }
                });
            }

            @Override
            public void onConnectionFailed(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusText.setText("Connection failed.");
                        Toast.makeText(BluetoothReceiveActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void importTaskList(String data) {
        // Full override — wipe existing tasks
        db.deleteAllTasks();

        // Parse incoming string
        String[] parts = data.split("::");
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                db.addTask(parts[i]);
            }
        }

        statusText.setText("List received successfully!");
        Toast.makeText(this, "Task list updated!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothHelper.closeServerSocket();
        bluetoothHelper.closeSocket();
    }
}