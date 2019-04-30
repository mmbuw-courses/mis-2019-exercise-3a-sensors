package com.example.mis_2019_exercise_3a_sensors;

import android.content.Context;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.hardware.SensorManager;
import android.hardware.Sensor;

import android.widget.Toast;
import android.widget.SeekBar;

// https://developer.android.com/guide/topics/sensors/sensors_overview.html

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private Context context;

    // storage for accelerometer data
    private float[] linear_acceleration = {0.0f, 0.0f, 0.0f};

    private GLAccelerometerSurfaceView glView;
    private int duration = Toast.LENGTH_SHORT;;
    private SeekBar refreshRate;
    private SeekBar windowSizeFFT;

    // needed for measuring sensor input
    private SensorManager sensorManager;
    private Sensor sensorAcc;

    private boolean accelerometerAva;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_main);

        // OpenGL visualization of AccelerometerService data
        glView = (GLAccelerometerSurfaceView) findViewById(R.id.accelerometerView);

        // link SeekBars
        refreshRate = (SeekBar) findViewById(R.id.seekBar);
        windowSizeFFT = (SeekBar) findViewById(R.id.seekBar2);

        // instance of SensorManager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // check if the AccelerometerService is available
        // only then start the Service needed read data from the accelerometer
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometerAva = true;
            Toast.makeText(context, "Accelerometer found", duration).show();
            sensorAcc = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        }
        else {
            duration = Toast.LENGTH_LONG;
            accelerometerAva = false;
            Toast.makeText(context, "No Accelerometer", duration).show();
        }
    }

    protected void onResume() {
        super.onResume();
        // start accelerometer listener
        if(accelerometerAva){
            sensorManager.registerListener(this, sensorAcc, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    protected void onPause() {
        super.onPause();
        if(accelerometerAva){
            sensorManager.unregisterListener(this);
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        // get the acceleration values from the sensors
        linear_acceleration[0] = event.values[0];
        linear_acceleration[1] = event.values[1];
        linear_acceleration[2] = event.values[2];


        // only positive vibes allowed, "negative" acceleration cannot be displayed
        for(int i = 0; i < linear_acceleration.length; i++){
            if (linear_acceleration[i] < 1){
                linear_acceleration[i] = linear_acceleration[i]*-1;
            }
        }

        // update glView with new data
        glView.renderer.linear_acceleration = linear_acceleration;
    }
}

/*
    // implement refresh rate of sensor
        refreshRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    });

        windowSizeFFT.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    });
}*/