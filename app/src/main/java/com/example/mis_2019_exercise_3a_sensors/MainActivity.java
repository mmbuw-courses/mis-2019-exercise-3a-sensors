package com.example.mis_2019_exercise_3a_sensors;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Toast;

import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;


// https://developer.android.com/guide/topics/sensors/sensors_overview.html
public class MainActivity extends AppCompatActivity {

    private Context context;
    private int duration;
    private SensorManager sensorManager;
    private Toast toast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        sensorManager = (SensorManager) getSystemService(context.SENSOR_SERVICE);

        // check if the Accelerometer is available
        if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            // Accelerometer found
            duration = Toast.LENGTH_LONG;
            toast.makeText(context, "Accelerometer found", duration).show();
        }
        else {
            duration = Toast.LENGTH_LONG;
            toast.makeText(context, "No Accelerometer", duration).show();
        }
    }
}
