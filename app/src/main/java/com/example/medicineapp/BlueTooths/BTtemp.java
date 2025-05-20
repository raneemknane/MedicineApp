package com.example.medicineapp.BlueTooths;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static android.content.ContentValues.TAG;

import com.example.medicineapp.Mains.MainActivity;
import com.example.medicineapp.R;

public class BTtemp extends AppCompatActivity {

    private String deviceName = null;
    private String deviceAddress;

    public static Handler handler;
    public static BluetoothSocket mmSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;

    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bttemp);

        // UI Initialization
        final Button buttonConnect = findViewById(R.id.buttonConnect2);
        final Toolbar toolbar = findViewById(R.id.toolbar2);
        final ProgressBar progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.GONE);
        final TextView textViewInfo = findViewById(R.id.textViewInfo2);
        final TextView textViewTemp = findViewById(R.id.textViewTemp);
        final ImageView imageViewStatus = findViewById(R.id.imageViewStatus2);
        final Button btnBTback = findViewById(R.id.btnBTback2);
        final Button buttonToggle = findViewById(R.id.buttonToggle2);
        progressBar.setVisibility(View.GONE);
        buttonToggle.setEnabled(false);



        // Get Intent extras
        Intent intent = getIntent();
        String newDeviceName = intent.getStringExtra("deviceName");
        String newDeviceAddress = intent.getStringExtra("deviceAddress");

        // Only update device info if new data is provided
        if (newDeviceName != null && newDeviceAddress != null) {
            this.deviceName = newDeviceName;
            this.deviceAddress = newDeviceAddress;

            toolbar.setSubtitle("Connecting to " + deviceName + "...");
            progressBar.setVisibility(View.VISIBLE);
            buttonConnect.setEnabled(false);

            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                textViewInfo.setText("Bluetooth not supported");
                finish();
                return;
            }

            createConnectThread = new BTtemp.CreateConnectThread(bluetoothAdapter, deviceAddress);
            createConnectThread.start();
        } else {
            // If no new device info, check if already connected
            if (deviceName != null) {
                toolbar.setSubtitle("Connected to " + deviceName);
                buttonConnect.setEnabled(true);
                buttonToggle.setEnabled(true);
            } else {
                toolbar.setSubtitle("No Device");
                textViewInfo.setText("Select a Bluetooth device");
                buttonConnect.setEnabled(true);
            }
        }

        // Setup Handler
        setupHandler(toolbar, progressBar, textViewInfo, textViewTemp, imageViewStatus, buttonConnect, buttonToggle);

        btnBTback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                // Navigate to MainActivity and tell it to show ChooseService fragment
                Intent intent = new Intent(BTtemp.this, MainActivity.class);
                intent.putExtra("fragment", "ChooseService"); // Optional: tells MainActivity which fragment to load
                startActivity(intent);
                finish(); // Optional: close BTheartbeats activity
            }
        });

        // Connect button
        buttonConnect.setOnClickListener(v -> {
            startActivity(new Intent(BTtemp.this, SelectDeviceActivity2.class));
            finish();
        });

        // Toggle measurement
        buttonToggle.setOnClickListener(v -> {
            String cmdText = "";
            String btnState = buttonToggle.getText().toString().toLowerCase();

            switch (btnState) {
                case "start":
                    buttonToggle.setText("STOP");
                    cmdText = "1";
                    break;
                case "stop":
                    buttonToggle.setText("START");
                    cmdText = "0";
                    break;
            }

            if (connectedThread != null) {
                connectedThread.write(cmdText);
            } else {
                textViewInfo.setText("Not connected");
            }
        });
    }

    private void setupHandler(Toolbar toolbar, ProgressBar progressBar,
                              TextView textViewInfo, TextView textViewTemp,
                              ImageView imageViewStatus, Button buttonConnect,
                              Button buttonToggle) {

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (isFinishing()) return;

                switch (msg.what) {
                    case CONNECTING_STATUS:
                        switch (msg.arg1) {
                            case 1:
                                toolbar.setSubtitle("Connected to " + deviceName);
                                progressBar.setVisibility(View.GONE);
                                buttonConnect.setEnabled(true);
                                buttonToggle.setEnabled(true);
                                break;
                            case -1:
                                toolbar.setSubtitle("Device fails to connect");
                                progressBar.setVisibility(View.GONE);
                                buttonConnect.setEnabled(true);
                                textViewInfo.setText("Connection failed");
                                break;
                        }
                        break;

                    case MESSAGE_READ:
                        String arduinoMsg = msg.obj.toString(); // Read message from Arduino
                        Log.d(TAG, "Received: " + arduinoMsg);

                        if (arduinoMsg.startsWith("GOOD:")) {
                            String tempStr = arduinoMsg.replace("GOOD:", "");
                            textViewTemp.setText("Temp: " + tempStr + "°C");
                            textViewInfo.setText("Temp Status: Good!");

                        } else if (arduinoMsg.startsWith("BAD:")) {
                            String tempStr = arduinoMsg.replace("BAD:", "");
                            textViewTemp.setText("Temp: " + tempStr + "°C");
                            textViewInfo.setText("Temp Status: Too Low/High!");

                        } else if (arduinoMsg.equals("STOPPED")) {
                            textViewTemp.setText("Temp: -- °C");
                            textViewInfo.setText("Measurement Stopped");
                            imageViewStatus.setImageResource(R.drawable.temp); // Default icon
                        }
                        break;
                }
            }
        };
    }

    /* ============================ Thread to Create Bluetooth Connection =================================== */
    public static class CreateConnectThread extends Thread {
        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address) {
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();

            try {
                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.e(TAG, "Socket creation failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            try {
                mmSocket.connect();
                Log.e("Status", "Device connected");
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                    Log.e("Status", "Cannot connect to device");
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.start();
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing socket", e);
            }
        }
    }

    /* =============================== Thread for Data Transfer =========================================== */
    public static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "InputStream or OutputStream failed", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes = 0;

            while (!isInterrupted()) {
                try {
                    buffer[bytes] = (byte) mmInStream.read();
                    String readMessage;

                    if (buffer[bytes] == '\n') {
                        readMessage = new String(buffer, 0, bytes);
                        Log.e("Arduino Message", readMessage);
                        handler.obtainMessage(MESSAGE_READ, readMessage).sendToTarget();
                        bytes = 0;
                    } else {
                        bytes++;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Bluetooth read error", e);
                    break;
                }
            }
        }

        public void write(String input) {
            byte[] bytes = input.getBytes();
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("Send Error", "Unable to send message", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing socket", e);
            }
        }
    }

    /* ============================ Terminate Connection at BackPress ====================== */
    @Override
    public void onBackPressed() {
        // Step 1: Tell Arduino to stop measuring
        if (connectedThread != null) {
            connectedThread.write("0"); // Stop command
        }

        // Step 2: Cancel threads
        if (createConnectThread != null) {
            createConnectThread.cancel();
            createConnectThread = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        // Step 3: Close socket
        try {
            if (mmSocket != null && mmSocket.isConnected()) {
                mmSocket.close();
            }
            mmSocket = null;
        } catch (IOException e) {
            Log.e(TAG, "Error closing socket", e);
        }

        // Step 4: Finish activity
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        // Always clear static references on destroy
        createConnectThread = null;
        connectedThread = null;
        mmSocket = null;
        handler = null;

        super.onDestroy();
    }
}