package com.example.todoapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothHelper {

    private static final UUID APP_UUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final String APP_NAME = "TodoApp";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private BluetoothServerSocket serverSocket;

    public interface BluetoothListener {
        void onMessageReceived(String message);
        void onConnectionFailed(String error);
        void onConnectionSuccess();
    }

    public BluetoothHelper() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean isBluetoothAvailable() {
        return bluetoothAdapter != null;
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    // SENDER — connects to receiver and sends data
    public void sendData(final BluetoothDevice device, final String data, final BluetoothListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = device.createRfcommSocketToServiceRecord(APP_UUID);
                    bluetoothAdapter.cancelDiscovery();
                    socket.connect();
                    listener.onConnectionSuccess();

                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write(data.getBytes());
                    outputStream.flush();
                    closeSocket();

                } catch (IOException e) {
                    listener.onConnectionFailed(e.getMessage());
                    closeSocket();
                }
            }
        }).start();
    }

    // RECEIVER — listens for incoming connection and reads data
    public void receiveData(final BluetoothListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, APP_UUID);
                    socket = serverSocket.accept();
                    listener.onConnectionSuccess();

                    InputStream inputStream = socket.getInputStream();
                    byte[] buffer = new byte[1024];
                    int bytes = inputStream.read(buffer);
                    String received = new String(buffer, 0, bytes);
                    listener.onMessageReceived(received);

                    closeServerSocket();
                    closeSocket();

                } catch (IOException e) {
                    listener.onConnectionFailed(e.getMessage());
                    closeServerSocket();
                }
            }
        }).start();
    }

    public void closeSocket() {
        try { if (socket != null) socket.close(); } catch (IOException e) { e.printStackTrace(); }
    }

    public void closeServerSocket() {
        try { if (serverSocket != null) serverSocket.close(); } catch (IOException e) { e.printStackTrace(); }
    }
}