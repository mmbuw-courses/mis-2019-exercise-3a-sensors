package com.example.mis_2019_exercise_3a_sensors;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.hardware.SensorManager;
import android.hardware.Sensor;

import android.opengl.GLSurfaceView;

import android.widget.Toast;
import android.widget.SeekBar;


// https://developer.android.com/guide/topics/sensors/sensors_overview.html
public class MainActivity extends AppCompatActivity {

    private Context context;
    private GLSurfaceView glView;
    private int duration = Toast.LENGTH_LONG;;
    private SeekBar refreshRate;
    private SeekBar windowSizeFFT;
    private Sensor sensorAcc;
    private SensorManager sensorManager;
    private Toast toast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();

        setContentView(R.layout.activity_main);

        glView = (GLAccelerometerSurfaceView) findViewById(R.id.accelerometerView);

        sensorManager = (SensorManager) getSystemService(context.SENSOR_SERVICE);

        // check if the Accelerometer is available
        if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            // Accelerometer found
            toast.makeText(context, "Accelerometer found", duration).show();
            sensorAcc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        else {
            duration = Toast.LENGTH_LONG;
            toast.makeText(context, "No Accelerometer", duration).show();
        }
    }
}
