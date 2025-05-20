package com.example.medicineapp.BlueTooths;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.medicineapp.Mains.MainActivity;
import com.example.medicineapp.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BTheartbeats extends AppCompatActivity {

    private String deviceName = null;
    private String deviceAddress;

    public static Handler handler;
    public static BluetoothSocket mmSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;

    private final static int CONNECTING_STATUS = 1;
    private final static int MESSAGE_READ = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btheartbeats);

        // Initialize UI components
        final Button buttonConnect = findViewById(R.id.buttonConnect);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        final TextView textViewInfo = findViewById(R.id.textViewInfo);
        final TextView textViewHeartRate = findViewById(R.id.textViewHeartRate);
        final ImageView imageViewStatus = findViewById(R.id.imageViewStatus);
        final Button btnBTback = findViewById(R.id.btnBTback);
        final Button buttonToggle = findViewById(R.id.buttonToggle);
        buttonToggle.setEnabled(false);
        progressBar.setVisibility(View.GONE);

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

            createConnectThread = new CreateConnectThread(bluetoothAdapter, deviceAddress);
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
        setupHandler(toolbar, progressBar, textViewInfo, textViewHeartRate, imageViewStatus, buttonConnect, buttonToggle);


        btnBTback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                // Navigate to MainActivity and tell it to show ChooseService fragment
                Intent intent = new Intent(BTheartbeats.this, MainActivity.class);
                intent.putExtra("fragment", "ChooseService"); // Optional: tells MainActivity which fragment to load
                startActivity(intent);
                finish(); // Optional: close BTheartbeats activity
            }
        });

        // Connect button
        buttonConnect.setOnClickListener(v -> {
            startActivity(new Intent(BTheartbeats.this, SelectDeviceActivity.class));
            finish();
        });

        // Toggle measurement
        buttonToggle.setOnClickListener(v -> {
            String cmdText = "";
            String btnState = buttonToggle.getText().toString().toLowerCase();

            switch (btnState) {
                case "start":
                    buttonToggle.setText("STOP");
                    cmdText = "O";
                    break;
                case "stop":
                    buttonToggle.setText("START");
                    cmdText = "F";
                    break;
            }

            if (connectedThread != null) {
                connectedThread.write(cmdText);
            } else {
                textViewInfo.setText("Not connected");
            }
        });
    }

    private void setupHandler(Toolbar toolbar, ProgressBar progressBar, TextView textViewInfo,
                              TextView textViewHeartRate, ImageView imageViewStatus,
                              Button buttonConnect, Button buttonToggle) {

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
                        String arduinoMsg = msg.obj.toString();
                        Log.d(TAG, "Received: " + arduinoMsg);

                        if (textViewHeartRate == null || textViewInfo == null || imageViewStatus == null) {
                            Log.e(TAG, "One or more views are null");
                            return;
                        }

                        if (arduinoMsg.startsWith("GOOD:")) {
                            String bpmStr = arduinoMsg.replace("GOOD:", "");
                            textViewHeartRate.setText("Heart Rate: " + bpmStr + " BPM");
                            textViewInfo.setText("Heart Rate Status: Good!");

                        } else if (arduinoMsg.startsWith("BAD:")) {
                            String bpmStr = arduinoMsg.replace("BAD:", "");
                            textViewHeartRate.setText("Heart Rate: " + bpmStr + " BPM");
                            textViewInfo.setText("Heart Rate Status: Too Fast/Slow!");

                        } else if (arduinoMsg.equals("STOPPED")) {
                            textViewHeartRate.setText("Heart Rate: -- BPM");
                            textViewInfo.setText("Measurement Stopped");
                            imageViewStatus.setImageResource(R.drawable.heart);
                        }
                        break;
                }
            }
        };
    }

    /* ============================ Thread to Create Bluetooth Connection =================================== */
    public static class CreateConnectThread extends Thread {
        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address) {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            UUID uuid = device.getUuids()[0].getUuid();
            try {
                mmSocket = device.createInsecureRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.e(TAG, "Socket creation failed", e);
            }
        }

        public void run() {
            try {
                mmSocket.connect();
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close socket", closeException);
                }
                return;
            }

            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.start();
        }

        public void cancel() {
            try {
                if (mmSocket != null && mmSocket.isConnected()) {
                    mmSocket.close();
                }
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
                Log.e(TAG, "Stream initialization failed", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes = 0;

            while (true) {
                try {
                    buffer[bytes] = (byte) mmInStream.read();
                    if (buffer[bytes] == '\n') {
                        String message = new String(buffer, 0, bytes);
                        Log.d(TAG, "Arduino Message: " + message);
                        handler.obtainMessage(MESSAGE_READ, message).sendToTarget();
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
                Log.e(TAG, "Unable to send message", e);
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
            connectedThread.write("F"); // Stop command
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