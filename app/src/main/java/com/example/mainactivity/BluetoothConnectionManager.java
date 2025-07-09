package com.example.mainactivity;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
// THIS CALSS IS USED TO PASS TEH BLUETOOTH CONNECTION FROM ACTIVITY TO ANOTHER!!!!!!!
// TO PASS THE SOCKET ITSELF WITHOUT HAVING TO USE INTENTS.
public class BluetoothConnectionManager {
    private static BluetoothConnectionManager instance;
    private BluetoothSocket socket;

    private BluetoothConnectionManager() {}

    public static synchronized BluetoothConnectionManager getInstance() {
        if (instance == null) {
            instance = new BluetoothConnectionManager();
        }
        return instance;
    }

    public void setSocket(BluetoothSocket socket) {
        this.socket = socket;
    }

    public BluetoothSocket getSocket() {
        return socket;
    }

    public void closeSocket() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket = null;
    }
}