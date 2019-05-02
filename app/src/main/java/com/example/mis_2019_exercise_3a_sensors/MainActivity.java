package com.example.mis_2019_exercise_3a_sensors;

import android.content.Context;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import android.graphics.Color;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.hardware.SensorManager;
import android.hardware.Sensor;

import android.util.Log;
import android.widget.Toast;
import android.widget.SeekBar;

// https://github.com/jjoe64/GraphView
// https://developer.android.com/guide/topics/sensors/sensors_overview.html
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private Context context;

    // storage for accelerometer data
    private float[] linear_acceleration = {0.0f, 0.0f, 0.0f, 0.0f};
    private GLAccelerometerSurfaceView glView;

    // Toast length
    private int duration = Toast.LENGTH_SHORT;;

    // change between the 3 differenct frequencies of refresh rates
    private SeekBar refreshRate;
    private int sensorRefresh;

    // for visualization of fft magniute data
    private double[] frequency;
    private double[] magnitudeValuesTotal;
    private GraphView mGraph;
    private int windowSize = 32;
    private int fftIndex = 0;
    private LineGraphSeries<DataPoint> fftMagnitude;
    private SeekBar windowSizeFFT;

    // needed for measuring sensor input
    private Sensor sensorAcc;
    private SensorManager sensorManager;

    // check if accelerometer available or not
    private boolean accelerometerAva;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_main);

        // OpenGL visualization of AccelerometerService data
        glView = (GLAccelerometerSurfaceView) findViewById(R.id.accelerometerView);
        refreshRate = (SeekBar) findViewById(R.id.seekBar2);

        // FFT Magnitude and its representation
        mGraph = (GraphView) findViewById(R.id.graph);
        mGraph.setTitle("FFT Magnitude");
        mGraph.setBackgroundColor(Color.LTGRAY);
        mGraph.setTitleColor(Color.YELLOW);
        mGraph.getViewport().setXAxisBoundsManual(true);
        mGraph.getViewport().setYAxisBoundsManual(true);
        mGraph.getViewport().setMinX(0.0);
        mGraph.getViewport().setMaxX(32.0);
        mGraph.getViewport().setMinY(0.0);
        mGraph.getViewport().setMaxY(64.0);
        mGraph.getViewport().setScalable(true);
        mGraph.getViewport().setScrollable(true);

        fftMagnitude = new LineGraphSeries<>();
        fftMagnitude.setColor(Color.YELLOW);
        mGraph.addSeries(fftMagnitude);
        windowSizeFFT = (SeekBar) findViewById(R.id.seekBar);

        // instance of SensorManager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorRefresh = sensorManager.SENSOR_DELAY_NORMAL;

        // check if the AccelerometerService is available
        // only then start the Service needed read data from the accelerometer
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometerAva = true;
            Toast.makeText(context, "Accelerometer found", duration).show();
            sensorAcc = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            startListener(sensorRefresh);
        }
        else {
            duration = Toast.LENGTH_LONG;
            accelerometerAva = false;
            Toast.makeText(context, "No Accelerometer", duration).show();
        }

        // only do this when accelerometer is available
        if(accelerometerAva){
            // implement refresh rate of sensor
            refreshRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(progress < 33){
                        sensorRefresh = sensorManager.SENSOR_DELAY_NORMAL;
                    }
                    else if(progress >= 33 && progress < 66){
                        sensorRefresh = sensorManager.SENSOR_DELAY_GAME;
                    }
                    else if(progress >= 66){
                        sensorRefresh = sensorManager.SENSOR_DELAY_FASTEST;
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    stopListener();
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    startListener(sensorRefresh);
                }
            });

            // implement scalability of fft window
            windowSizeFFT.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                int mProgress = 0;
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    float pr = (float)progress;
                    mProgress = (int)(pr/12.5);
                    windowSize = (int) Math.pow(2, mProgress);
                    magnitudeValuesTotal = new double[windowSize];
                    mGraph.getViewport().setMaxX(windowSize);
                    Log.i("Progress for FFT", Integer.toString(progress));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            magnitudeValuesTotal = new double[windowSize];
        }
    }

    protected void onResume() {
        super.onResume();
        if(accelerometerAva){
            startListener(sensorRefresh);
        }
    }

    protected void onPause() {
        super.onPause();
        if(accelerometerAva){
            stopListener();
        }
    }

    public void startListener(int refresh){
        sensorManager.registerListener(this, sensorAcc, refresh);
    }

    public void stopListener(){
        sensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(final SensorEvent event) {
        // magnitude
        double tmp = calcMagnitude(event.values[0], event.values[1], event.values[2]);
        linear_acceleration[3] = (float) tmp;
        // get the acceleration values from the sensors, normalize it
        linear_acceleration[0] = event.values[0];
        // for some reasion the Y values seem off the charts if the phone is held upright
        linear_acceleration[1] = event.values[1];
        linear_acceleration[2] = event.values[2];

        // only positive vibes allowed, "negative" acceleration cannot be displayed
        // magnitude always positive
        for(int i = 0; i < linear_acceleration.length; i++){
            if (linear_acceleration[i] < 1){
                linear_acceleration[i] = linear_acceleration[i]*-1;
            }
        }
        // update glView with new data
        glView.renderer.linear_acceleration = linear_acceleration;

        // do FFT stuff here
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(fftIndex > windowSize){
                    magnitudeValuesTotal = new double[windowSize];
                    fftIndex = 0;
                }
                else if(fftIndex < windowSize){
                    magnitudeValuesTotal[fftIndex] = calcMagnitude(event.values[0], event.values[1], event.values[2]);
                }
                else{
                    new FFTAsync(windowSize).execute(magnitudeValuesTotal);
                }
                ++fftIndex;
            }
        });

    }

    // fft needs to run on a separate thread because it's quite resource intense
    private class FFTAsync extends AsyncTask<double[], Void, double[]>{
        private int sizeW;

        FFTAsync(int size){
            sizeW = size;
        }

        @Override
        protected double[] doInBackground(double[]... values){
            // clone, otherwise it's just a reference
            double[] rea = values[0].clone();
            double[] img = new double[sizeW];
            double[] magnitude = new  double[sizeW];

            // start the fft with window size
            FFT fft = new FFT(sizeW);
            fft.fft(rea, img);

            for(int i = 0; i < sizeW; i++){
                magnitude[i] = Math.sqrt(rea[i] * rea[i] + img[i] * img[i]);
            }

         return magnitude;
        }

        @Override
        protected void onPostExecute(double[] values){
            frequency = values;

            DataPoint[] fftPoints = new DataPoint[sizeW];
            for(int i = 0; i < sizeW; i++){
                fftPoints[i] = new DataPoint(i, frequency[i]);
            }
            fftMagnitude.resetData(fftPoints);
        }
    }

    public double calcMagnitude(double v1, double v2, double v3){
        return Math.sqrt(v1 * v1 + v2 * v2 + v3 * v3);
    }
}

