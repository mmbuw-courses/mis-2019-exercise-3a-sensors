package com.example.mis.sensor;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.CheckBox;

import com.example.mis.sensor.FFT;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

public abstract class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener, SeekBar.OnSeekBarChangeListener {

    //example variables
    private double[] rndAccExamplevalues;
    private double[] freqCounts;

    // For accelerator's sensor
    private SensorManager sensorManager;
    private Sensor sensor;

    // For line chart
    // Source: "https://github.com/PhilJay/MPAndroidChart/tree/master/MPChartExample/src/main/java/com/xxmassdeveloper/mpchartexample"
    // Getting values for x, y, and z axis
    private List<Entry> xAxis = new ArrayList<>();
    private List<Entry> yAxis = new ArrayList<>();
    private List<Entry> zAxis = new ArrayList<>();
    private List<Entry> magnitudeChart = new ArrayList<>();
    private int graph = 100;

    // For Seekbars
    private SeekBar samplerateSeekbar, fftSeekbar;
    //private TextView locationSpeed;
    private int samplerate, xAxisInitial = 0, windowSize, magnitudeInitial = 0;
    private double[] magnitudeArray = new double[windowSize];

    // Checking location to determine acceleration
    private boolean locationGranted;
    private LocationManager locationManager;
    private UserActivity userActivity = UserActivity.STATIC;
    //private UserActivity userActivity = UserActivity.moving;
    //private UserActivity userActivity;
    private Location lastLocation;
    private boolean accelerationChanges = false;
    public TextView activityText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setting up the initial values for sample-rate and windows size
        // Also initialising seekbar, sensor, and location functions
        // Sample-rate is written in milisec.
        samplerate = 100000;
        windowSize = 10;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        activityText = findViewById(R.id.textView);
        textView();
        getSeekbar();
        getSensor();
        getLocation();


        //initiate and fill example array with random values
        rndAccExamplevalues = new double[64];
        randomFill(rndAccExamplevalues);
        new FFTAsynctask(64).execute(rndAccExamplevalues);

    }

    // Function for getting seekbar value
    // Sources: "https://developer.android.com/reference/android/widget/SeekBar.html"
    public void getSeekbar() {

        samplerateSeekbar = findViewById(R.id.samplerate);
        fftSeekbar = findViewById(R.id.seekbarFFT);
        samplerateSeekbar.incrementProgressBy(1);

        // Listener function for the seekbars
        samplerateSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if( progress == 0 ){
                    progress = 1;
                }
                float temp = progress;
                temp /= 10;
                seekBar.setProgress( progress );
                samplerate = progress * 100000;
                Log.d("###sampleRate: " , String.valueOf(temp));
                sensorManager.unregisterListener(MainActivity.this);
                sensorManager.registerListener(MainActivity.this, sensor, samplerate);
                updateLineChart();
            }

            private void updateLineChart() {

            }
        });

        // // Listener function for the seekbars (FFT chart)
        fftSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (magnitudeInitial > windowSize) {
                    magnitudeArray = new double[windowSize];
                    magnitudeInitial = 0;
                } else if (magnitudeInitial < windowSize) {
                    double[] tempArray = new double[windowSize];
                    tempArray = magnitudeArray;
                    magnitudeArray = new double[windowSize];
                    for (int i = 0; i <= magnitudeInitial; i++) {
                        magnitudeArray[i] = tempArray[i];
                    }
                }
            }
        });

    }

    // Source: "https://developer.android.com/guide/topics/sensors/sensors_motion.html"
    private void updateLineChart(SensorEvent event) {
        float x, y, z, magnitude;
        x = event.values[0];
        y = event.values[1];
        z = event.values[2];
        magnitude = (float) Math.sqrt((x * x) + (y * y) + (z * z));

        if (xAxis.size() >= graph) xAxis.remove(0);
        if (yAxis.size() >= graph) yAxis.remove(0);
        if (zAxis.size() >= graph) zAxis.remove(0);
        if (magnitudeChart.size() >= graph) magnitudeChart.remove(0);

        if ( magnitudeInitial < magnitudeArray.length) {
            if (magnitudeArray.length <= windowSize) {
                magnitudeArray[magnitudeInitial] =  magnitude;
                magnitudeInitial++;
            }
        }
        else{
            magnitudeInitial = 0;
            magnitudeArray = new double[windowSize];
        }
        xAxisInitial += 1;
        xAxis.add( new Entry( xAxisInitial, x));
        yAxis.add( new Entry( xAxisInitial, y));
        zAxis.add( new Entry( xAxisInitial, z));
        magnitudeChart.add( new Entry( xAxisInitial, magnitude));
        updateChartData();
    }

    public void updateChartData(){
        LineChart graph = findViewById(R.id.dataGraph);
        LineData lineData = new LineData();
        lineData.addDataSet(addLineData( xAxis, "X" , Color.RED));
        lineData.addDataSet(addLineData( yAxis, "Y" , Color.GREEN ));
        lineData.addDataSet(addLineData( zAxis, "Z" , Color.BLUE));
        lineData.addDataSet(addLineData( magnitudeChart, "Magnitude" , Color.WHITE));
        graph.setBackgroundColor(Color.DKGRAY);
        graph.setDescription(new Description());
        graph.setGridBackgroundColor(Color.BLACK);
        graph.setData(lineData);
        graph.notifyDataSetChanged();
        graph.invalidate();
    }

    private void updateFFTChart() {
        LineChart fftGraph = findViewById(R.id.fftGraph);
        List<Entry> magnitudeEntryList = new ArrayList<>();
        for (int i = 0; i < freqCounts.length; i++){
            magnitudeEntryList.add( new Entry(i, (float) freqCounts[i]));
        }
        LineData lineData = new LineData();
        lineData.addDataSet(addLineData( magnitudeEntryList, "X" , Color.DKGRAY));
        fftGraph.setData(lineData);
        fftGraph.notifyDataSetChanged();
        fftGraph.invalidate();
        fftGraph.setDescription(new Description());
    }

    public LineDataSet addLineData( List<Entry> valueList, String label, int lineColor  ){
        LineDataSet lineDataSet = new LineDataSet(valueList, label);
        lineDataSet.setColor(lineColor);
        lineDataSet.setValueTextColor(lineColor);
        lineDataSet.setValueTextSize(10);
        return lineDataSet;
    }

    // Function to get sensor data/value
    public boolean getSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null
                && sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null){
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            sensorManager.registerListener(this, sensor, samplerate);
            return true;
        }
        return false;
    }

    // Function to get location service
    public void getLocation() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION
            },
                    1);
        }
        locationGranted = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (locationGranted) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
            intent.putExtra("enabled", true);
        }
    }

    // Setting boolean value when the location checkbox is ticked
    public void onCheckboxClicked(View v) {
        locationGranted = ((CheckBox) v).isChecked();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (magnitudeInitial == windowSize) {
            FFTAsynctask fftAsyncTask = new FFTAsynctask(windowSize);
            fftAsyncTask.execute(magnitudeArray);
        }
        updateLineChart(event);
    }

    /*
    public void updateLineChart(SensorEvent event) {

    }
    */

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onLocationChanged(Location location) {
        double beforeSpeed = 0;
        double afterSpeed = 0;
        if (lastLocation != null) {
        double elapseTime = (location.getTime() - lastLocation.getTime()) / 1000;
        afterSpeed = lastLocation.distanceTo(location) / elapseTime;
        }
        lastLocation = location;
        beforeSpeed = location.hasSpeed() ? location.getSpeed() : afterSpeed;
        if (location != null) {
            afterSpeed = location.getSpeed();
        }
        if (beforeSpeed <= 3) {
            userActivity = UserActivity.STATIC;
        }
        if (beforeSpeed > 3) {
            userActivity = UserActivity.MOVING;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
    /*
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }
    */
    /*
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }
    */
    /*
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
    */


    /**
     * Implements the fft functionality as an async task
     * FFT(int n): constructor with fft length
     * fft(double[] x, double[] y)
     */

    private class FFTAsynctask extends AsyncTask<double[], Void, double[]> {

        private int windowSize; //window size must be power of 2

        // constructor to set window size
        FFTAsynctask(int windowSize) {
            this.windowSize = windowSize;
        }

        @Override
        protected double[] doInBackground(double[]... values) {


            double[] realPart = values[0].clone(); // actual acceleration values
            double[] imagPart = new double[windowSize]; // init empty

            /**
             * Init the FFT class with given window size and run it with your input.
             * The fft() function overrides the realPart and imagPart arrays!
             */
            FFT fft = new FFT(windowSize);
            fft.fft(realPart, imagPart);
            //init new double array for magnitude (e.g. frequency count)
            double[] magnitude = new double[windowSize];


            //fill array with magnitude values of the distribution
            for (int i = 0; windowSize > i ; i++) {
                magnitude[i] = Math.sqrt(Math.pow(realPart[i], 2) + Math.pow(imagPart[i], 2));
            }

            return magnitude;

        }

        @Override
        protected void onPostExecute(double[] values) {
            //hand over values to global variable after background task is finished
            freqCounts = values;
            updateFFTChart();
        }
    }




    /**
     * little helper function to fill example with random double values
     */
    public void randomFill(double[] array){
        Random rand = new Random();
        for(int i = 0; array.length > i; i++){
            array[i] = rand.nextDouble();
        }
    }


    private void textView() {
        activityText = findViewById(R.id.textView);
        try {
                if (accelerationChanges) {
                    switch (userActivity) {
                        case MOVING:
                            activityText.setText("Moving");
                            break;
                    }
                } else {
                    switch (userActivity) {
                        case STATIC:
                            activityText.setText("Static");
                            }
                }

        } catch (IllegalStateException ex) {
            System.out.print("IllegalStateException: " + ex.getLocalizedMessage());
        }
    }

}
// Source: "https://docs.oracle.com/javase/tutorial/java/javaOO/enum.html"
// Source: "https://stackoverflow.com/questions/6391777/switch-on-enum-in-java"
enum UserActivity {
    MOVING, STATIC
}
