package com.example.ruedaslocas;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private Button btnConnectBluetooth;
    private Button btnUp;
    private Button btnDown;
    private Button btnLeft;
    private Button btnRight;
    private Button btnStop;
    private TextView txtX;
    private TextView txtY;
    private TextView txtZ;
    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private List<BluetoothDevice> deviceList;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    String msg;
    private boolean repeatFlag = false;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 2;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        btnConnectBluetooth = findViewById(R.id.btnConnectBluetooth);
        btnUp = findViewById(R.id.btnUp);
        btnDown = findViewById(R.id.btnDown);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        btnStop = findViewById(R.id.btnDetener);
        txtX = findViewById(R.id.txtX);
        txtY = findViewById(R.id.txtY);
        txtZ = findViewById(R.id.txtZ);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "No tienes Bluetooth XD", Toast.LENGTH_SHORT).show();
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            // Si no se tienen los permisos, solicitarlos al usuario
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_BLUETOOTH_PERMISSION);
        }

        if (!bluetoothAdapter.isEnabled()) {
            // Si el Bluetooth está deshabilitado, solicitar al usuario que lo habilite
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        btnConnectBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectBluetooth();
            }
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        } else {
            Toast.makeText(this, "No tienes Sensor", Toast.LENGTH_SHORT).show();
        }

        btnUp.setBackgroundColor(Color.WHITE);
        btnDown.setBackgroundColor(Color.WHITE);
        btnLeft.setBackgroundColor(Color.WHITE);
        btnRight.setBackgroundColor(Color.WHITE);

        btnConnectBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectBluetooth();
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBluetoothCommand("5");
            }
        });
        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBluetoothCommand(msg);
            }
        });
        btnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBluetoothCommand(msg);
            }
        });
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBluetoothCommand(msg);
            }
        });
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBluetoothCommand(msg);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        x = Math.round(x * 100) / 100.0f;
        y = Math.round(y * 100) / 100.0f;
        z = Math.round(z * 100) / 100.0f;

        txtX.setText("X: " + String.valueOf(x));
        txtY.setText("Y: " + String.valueOf(y));
        txtZ.setText("Z: " + String.valueOf(z));

        if (y > 4) {
            btnDown.setBackgroundColor(Color.YELLOW);
            msg="3";
            btnDown.performClick();
        } else {
            btnDown.setBackgroundColor(Color.WHITE);
        }
        if (y < -4) {
            btnUp.setBackgroundColor(Color.YELLOW);
            msg="1";
            btnUp.performClick();
        } else {
            btnUp.setBackgroundColor(Color.WHITE);
        }
        if (x > 4) {
            btnLeft.setBackgroundColor(Color.YELLOW);
            btnLeft.performClick();
            msg="2";
        } else {
            btnLeft.setBackgroundColor(Color.WHITE);
        }
        if (x < -4) {
            btnRight.setBackgroundColor(Color.YELLOW);
            btnRight.performClick();
            msg="4";
        } else {
            btnRight.setBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void connectBluetooth() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        pairedDevices = bluetoothAdapter.getBondedDevices();
        deviceList = new ArrayList<>();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceList.add(device);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Selecciona un Bluetooth");
            builder.setCancelable(true);

            CharSequence[] deviceNames = new CharSequence[deviceList.size()];
            for (int i = 0; i < deviceList.size(); i++) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                deviceNames[i] = deviceList.get(i).getName();
            }

            builder.setItems(deviceNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    BluetoothDevice selectedDevice = deviceList.get(which);
                    try {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        bluetoothSocket = selectedDevice.createRfcommSocketToServiceRecord(MY_UUID);
                        bluetoothSocket.connect();
                        outputStream = bluetoothSocket.getOutputStream();
                        Toast.makeText(MainActivity.this, "Conectado", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Error al conectar", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            Toast.makeText(this, "No hay dispositivos ", Toast.LENGTH_SHORT).show();
        }
    }
    private void sendBluetoothCommand(String command) {
        if (outputStream != null) {
            try {
                outputStream.write(command.getBytes());
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error ", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No hay conexión ", Toast.LENGTH_SHORT).show();
        }
    }
}