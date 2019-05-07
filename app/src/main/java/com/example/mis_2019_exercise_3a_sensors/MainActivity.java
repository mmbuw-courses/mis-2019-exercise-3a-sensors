package com.example.mis_2019_exercise_3a_sensors;

import android.Manifest;
import android.app.PendingIntent;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import android.graphics.Color;

import android.hardware.Camera;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Sensor;

import android.os.AsyncTask;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;

import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;
import android.widget.SeekBar;

// https://github.com/jjoe64/GraphView
// https://developer.android.com/guide/topics/sensors/sensors_overview.html
// https://code.tutsplus.com/tutorials/how-to-recognize-user-activity-with-activity-recognition--cms-25851
public class MainActivity extends AppCompatActivity implements SensorEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Context context;

    // storage for accelerometer data
    private float[] linear_acceleration = {0.0f, 0.0f, 0.0f, 0.0f};
    private GLAccelerometerSurfaceView glView;

    // Toast length
    private int duration = Toast.LENGTH_SHORT;;

    // change between the 3 differenct frequencies of refresh rates
    private SeekBar refreshRate;
    private int sensorRefresh;

    // for visualization of fft magnitude data
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

    // rotation recognition
    private CheckBox checkBox;
    public GoogleApiClient mApiClient;
    private GestureRecognizer mGestureRecognizer;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_CAMERA = 2;
    private boolean mLocationPermissionGranted = false;
    private boolean mCameraPermissionGranted = false;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION};

    // pattern recognition on phone
    private long timeStart = 0;
    private long timePassed = 0;
    private long timeBetween = 0;
    private List<TimeCounter> timeList = new ArrayList<TimeCounter>();
    private int clickCounter = 0;
    private boolean tapRec = false;
    private boolean cameraAva = false;
    Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_main);
        // check permissions
        checkLocationPermission();
        checkCameraPermission();

        // lock activity in portrait mode
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // check for camera
        checkCameraHardware(context);

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
        } else {
            accelerometerAva = false;
            Toast.makeText(context, "No Accelerometer", duration).show();
        }

        // only do this when accelerometer is available
        if (accelerometerAva) {
            // implement refresh rate of sensor
            refreshRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress < 33) {
                        sensorRefresh = sensorManager.SENSOR_DELAY_NORMAL;
                    } else if (progress >= 33 && progress < 66) {
                        sensorRefresh = sensorManager.SENSOR_DELAY_GAME;
                    } else if (progress >= 66) {
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
                    float pr = (float) progress;
                    mProgress = (int) (pr / 12.5);
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

        // initialize Google Play Client for rotation recognition
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // use checkbox to trigger gesture recognizer
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check which checkbox was clicked
                if (checkBox.isChecked()) {
                    tapRec = true;
                    // initialize Google Play Client for rotation recognition
                    iniApiClient();
                } else {
                    tapRec = false;
                    mApiClient.disconnect();
                }
            }
        });

        // only do this when camera permission was granted
        if(mCameraPermissionGranted) {
            // listen to click events on screen
            ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.mainLayout);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (tapRec) {
                        // add first element to time list
                        if (timeList.size() == 0) {
                            TimeCounter tC = new TimeCounter(System.currentTimeMillis(), 0);
                            timeList.add(tC);
                        }
                        // add all following elements to time list
                        else if (timeList.size() >= 1) {
                            TimeCounter tC = new TimeCounter(System.currentTimeMillis(), System.currentTimeMillis() - timeList.get(clickCounter - 1).a);
                            timeList.add(tC);
                        }
                        // count up clicks
                        clickCounter += 1;

                        // check if more than 3 seconds have passed, if yes, repeat pattern
                        if (timeList.get(clickCounter - 1).b > 3000) {
                            lightUp();
                        }
                    }

                    if (!tapRec) {
                        // clean the list for later use
                        timeList.clear();
                        clickCounter = 0;
                    }
                }
            });
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
        if(accelerometerAva && !tapRec) {
            // get the acceleration values from the sensors, normalize it
            linear_acceleration[0] = event.values[0];
            // for some reason the Y values seem off the charts if the phone is held upright
            linear_acceleration[1] = event.values[1];
            linear_acceleration[2] = event.values[2];
            // magnitude
            linear_acceleration[3] = (float) calcMagnitude(event.values[0], event.values[1], event.values[2]);

            // "negative" acceleration cannot be displayed
            // magnitude always positive
            for (int i = 0; i < linear_acceleration.length - 1; i++) {
                if (linear_acceleration[i] < 1) {
                    linear_acceleration[i] = linear_acceleration[i] * -1;
                }
            }
            // update glView with new data
            glView.renderer.linear_acceleration = linear_acceleration;

            // do FFT on a separate thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // restart
                    if (fftIndex > windowSize) {
                        magnitudeValuesTotal = new double[windowSize];
                        fftIndex = 0;
                    }
                    // fill with magnitude values
                    else if (fftIndex < windowSize) {
                        magnitudeValuesTotal[fftIndex] = calcMagnitude(event.values[0], event.values[1], event.values[2]);
                    }
                    // perform fft on magnitude values
                    else {
                        new FFTAsync(windowSize).execute(magnitudeValuesTotal);
                    }
                    ++fftIndex;
                }
            });
        }
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

    // calculate magnitude of event data
    public double calcMagnitude(double v1, double v2, double v3){
        return Math.sqrt(v1 * v1 + v2 * v2 + v3 * v3);
    }

    // implement interfaces for google api
    public void onConnected(@Nullable Bundle bundle){
        Intent intent = new Intent( this, GestureRecognizer.class );
        PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mApiClient, 1000, pendingIntent );
    }

    public void onConnectionSuspended(int i){
    }

    public void onConnectionFailed(@NonNull ConnectionResult connectionResult){
    }

    public void iniApiClient(){
        mApiClient.connect();
    }

    //https://developer.android.com/training/permissions/requesting
    public void checkLocationPermission()
    {
        // if not granted ask for permission to use location data
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        // if permission granted, set it on true
        else {
            mLocationPermissionGranted = true;
        }
    }

    public void checkCameraPermission()
    {
        // if not granted ask for permission to use location data
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_ACCESS_CAMERA);
        }
        // if permission granted, set it on true
        else {
            mCameraPermissionGranted = true;
        }
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    // repeat pattern that was tapped on the screen without last element
    // https://www.viralandroid.com/2016/01/turn-on-and-off-camera-led-flashlight-programmatically.html
    public void lightUp(){
        camera = Camera.open();
        for(int i = 0; i < timeList.size()-1; i++){
            try {
                Thread.sleep(timeList.get(i).b);
                Camera.Parameters parameters = camera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(parameters);
                camera.startPreview();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(parameters);
                camera.stopPreview();
            }
            catch (Exception e){
                Toast.makeText(context, e.getMessage(), duration).show();
            }
        }

        String txt = "Pattern has been repeated. Time for something new";
        Toast toast = Toast.makeText(context, txt, duration);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0,0);
        toast.show();
        // close camera & clean list for later use
        camera.release();
        timeList.clear();
        clickCounter = 0;
    }
}

