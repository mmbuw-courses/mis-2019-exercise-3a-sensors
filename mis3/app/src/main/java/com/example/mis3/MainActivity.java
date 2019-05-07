package com.example.mis3;



import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import org.apache.commons.lang3.ArrayUtils;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensor;

    private double last_x = 0.0f;
    private double last_y = 0.0f;
    private double last_z = 0.0f;
    private static int SHAKE_THRESHOLD = 10000;
    ArrayList<Double> magnitudeData = new ArrayList<>();
    private long lastUpdate = 0;
    LineGraphSeries<DataPoint> xline;
    LineGraphSeries<DataPoint> yline;
    LineGraphSeries<DataPoint> zline;
    LineGraphSeries<DataPoint> magline;
    private int size_Win = 64;
    private int size_Sam = 64;
    private int samplerate = 0;
    GraphView AccGraphView;
    GraphView FFTGraphView;
    TextView textWindow;
    TextView textSample;

    SeekBar sampleSB;
    SeekBar winSB;
    TextView idgesture1;
    TextView idgesture2;
    TextView idgesture3;
    TextView idgesture4;
    private double[] Eventvalues;
    int tempstate = 10000;
    int RATE_VALUE;



    public MainActivity() {
        RATE_VALUE = 20000;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialization of all components
        sampleSB = findViewById(R.id.sampleSB);
        sampleSB.setMax(1000000);
        sampleSB.setProgress(0);
        sampleSB.incrementProgressBy(10000);
        textSample = findViewById(R.id.textSRate);

        winSB = findViewById(R.id.winSB);
        winSB.setProgress(64);
        winSB.setMax(1024);
        textWindow = findViewById(R.id.textWin);
        http://www.javased.com/index.php?api=android.widget.SeekBar.OnSeekBarChangeListener
        sampleSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int state, boolean fromUser) {
                // https://stackoverflow.com/questions/7329166/changing-step-values-in-seekbar
                state = state / tempstate;
                state = state * tempstate;
                String text = "Sample Rate " + state + " ms";
                textSample.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                int state = seekBar.getProgress();
                state /= 10000;
                state *= 10000;
                seekBar.setProgress(state );

                String text = "samplerate size: " + state  + " ms";
                textSample.setText(text);
                RATE_VALUE = state;
                updateSampleRate(state);
            }
        });
        //http://www.javased.com/index.php?api=android.widget.SeekBar.OnSeekBarChangeListener
        winSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int state, boolean fromUser) {

                String text = "Window size: " + state;
                textWindow.setText(text);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int state = seekBar.getProgress();
                seekBar.setProgress(state);
                String text = "Window size: " + state;
                textWindow.setText(text);
                size_Win = state;
                FFTGraphView.getViewport().setMaxX(size_Win + 5);
            }
        });

        //https://www.vogella.com/tutorials/AndroidSensor/article.html
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        } else {

            Log.i("Sensor Manager", "SensorManager not available");
        }
        if (sensor == null) {

            Log.i("Sensor Manager", "SensorManager not available");
        } else {
            sensorManager.registerListener(this, sensor, RATE_VALUE);
        }

         //Calling two funtions for Live accelorometer data and Transformed FFT
         liveAccelerometer();
         transformedFFT();
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override

    //https://www.programcreek.com/java-api-examples/android.hardware.SensorEvent - Example 2
    public final void onSensorChanged(SensorEvent event) {

        double mag;

        mag = (float) Math.sqrt(Math.pow(event.values[0],2) + Math.pow(event.values[1],2) + Math.pow(event.values[2],2));
        magnitudeData.add(mag);
        samplerate++;
        updateGraph(samplerate, event.values[0], event.values[1], event.values[2], mag);
        //activityRecognition();

    }

    @Override
    protected void onResume() {

        super.onResume();
        sensorManager.registerListener(this, sensor, RATE_VALUE);

    }

    @Override
    protected void onPause() {

        super.onPause();

    }

    //Code Inspired from https://github.com/jjoe64/GraphView/blob/master/src/main/java/com/jjoe64/graphview/series/LineGraphSeries.java
    private void  liveAccelerometer() {
        AccGraphView = findViewById(R.id.AccGraphView);

        AccGraphView.setBackgroundColor(Color.WHITE);

        xline = new LineGraphSeries<>();
        yline = new LineGraphSeries<>();
        zline = new LineGraphSeries<>();
        magline = new LineGraphSeries<>();

        xline.setColor(Color.RED);
        yline.setColor(Color.GREEN);
        zline.setColor(Color.BLUE);
        // Set to gray in order to see it better
        magline.setColor(Color.GRAY);

        AccGraphView.addSeries(xline);
        AccGraphView.addSeries(yline);
        AccGraphView.addSeries(zline);
        AccGraphView.addSeries(magline);

        AccGraphView.getGridLabelRenderer().setHorizontalAxisTitle("samplerate");
        AccGraphView.getGridLabelRenderer().setVerticalAxisTitle("Acceleration");
        AccGraphView.getGridLabelRenderer().setHorizontalLabelsVisible(true);

        // add legend
        xline.setTitle("x-axis");
        yline.setTitle("y-axis");
        zline.setTitle("z-axis");
        magline.setTitle("Magnitude");
        AccGraphView.getLegendRenderer().setVisible(true);
        AccGraphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        // set axes
        AccGraphView.getViewport().setScalable(true);
        AccGraphView.getViewport().setScalableY(true);
        AccGraphView.getViewport().setScrollableY(true);
        AccGraphView.getViewport().setScrollable(true);
        AccGraphView.getViewport().setXAxisBoundsManual(true);
        AccGraphView.getViewport().setMinX(0);
        AccGraphView.getViewport().setMaxX(size_Sam);

    }

    private void transformedFFT() {

        FFTGraphView = findViewById(R.id.FFTGraphView);

        FFTGraphView.setBackgroundColor(Color.WHITE);

        FFTGraphView.getViewport().setScalable(true);
        FFTGraphView.getViewport().setScalableY(true);
        FFTGraphView.getViewport().setScrollableY(true);
        FFTGraphView.getViewport().setScrollable(true);

        FFTGraphView.getGridLabelRenderer().setHorizontalAxisTitle("samplerate");
        FFTGraphView.getGridLabelRenderer().setVerticalAxisTitle("Magnitude");
        FFTGraphView.getGridLabelRenderer().setHorizontalLabelsVisible(true);

        FFTGraphView.getViewport().setXAxisBoundsManual(true);
        FFTGraphView.getViewport().setMinX(0);
        FFTGraphView.getViewport().setMaxX(size_Win + 5);

    }

    private void updateSampleRate(int rate){

        sensorManager.unregisterListener( this);
        sensorManager.registerListener(this, sensor, rate);
    }

    private void updateGraph(final int samplerate, final double x, final double y, final double z, final double mag) {

        xline.appendData(new DataPoint(samplerate, x), true, 1000);
        yline.appendData(new DataPoint(samplerate, y), true, 1000);
        zline.appendData(new DataPoint(samplerate, z), true, 1000);
        magline.appendData(new DataPoint(samplerate, mag), true, 1000);
        AccGraphView.onDataChanged(true, true);
        Double[] FFTValues = magnitudeData.toArray(new Double[magnitudeData.size()]);
        if(FFTValues.length == size_Win)
            new FFTAsynchronousTask(size_Win).execute(ArrayUtils.toPrimitive(FFTValues));

        if (FFTValues.length > size_Win)
            magnitudeData.clear();
    }
    /**
     * Implements the fft functionality as an async task
     * FFT(int n): constructor with fft length
     * fft(double[] x, double[] y)
     */

    private class FFTAsynchronousTask extends AsyncTask<double[], Void, double[]> {

        private int wsize;
        FFTAsynchronousTask(int wsize) {
            this.wsize = wsize;
        }

        @Override
        protected double[] doInBackground(double[]... values) {

            double[] realPart = values[0].clone(); // actual acceleration values
            double[] imagPart = new double[wsize]; // init empty

            /**
             * Init the FFT class with given window size and run it with your input.
             * The fft() function overrides the realPart and imagPart arrays!
             */
            FFT fft = new FFT(wsize);
            fft.fft(realPart, imagPart);
            //init new double array for magnitude (e.g. frequency count)
            double[] magnitude = new double[wsize];


            //fill array with magnitude values of the distribution
            for (int i = 0; wsize > i ; i++) {
                magnitude[i] = Math.sqrt(Math.pow(realPart[i], 2) + Math.pow(imagPart[i], 2));
            }

            return magnitude;


        }

        @Override
        protected void onPostExecute(double[] values) {

            Eventvalues = values;

            DataPoint[] dataPoints = new DataPoint[Eventvalues.length - 1];
            for (int i = 0; i < Eventvalues.length - 1; i++)
                dataPoints[i] = new DataPoint(i, Eventvalues[i + 1]);
            LineGraphSeries<DataPoint> fft = new LineGraphSeries<>(dataPoints);
            fft.setColor(Color.YELLOW);
            FFTGraphView.removeAllSeries();
            FFTGraphView.addSeries(fft);
            fft.setTitle("Magnitude");
            FFTGraphView.getLegendRenderer().setVisible(true);
            FFTGraphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
            activityRecognition();

        }
    }

    private void activityRecognition() {
        //double temp = 0;

        idgesture1 = findViewById(R.id.idgesture1);
        idgesture2 = findViewById(R.id.idgesture2);
        idgesture3 = findViewById(R.id.idgesture3);
        idgesture4 = findViewById(R.id.idgesture4);
        double x = Eventvalues[0];
        double y = Eventvalues[1];
        double z = Eventvalues[2];

        /*float ztemp = (float) Eventvalues[2];
        if (ztemp >9 && ztemp < 10)
            idgesture2.setText("Flip : Face UP" );
        else if (ztemp > -10 && ztemp < -9)
            idgesture2.setText("Flip : Face DOWN" );*/


        /*if (z >= 0){
            idgesture2.setText("Flip : Face UP" );

        }
        else {
            idgesture2.setText("Flip : Face DOWN" );
        }*/

        double vectorSum = Math.sqrt(x * x + y * y + z * z);
        if (vectorSum > 11) {
           idgesture4.setText("Picked the Device : Yes");
        }
        else
        {
            idgesture4.setText("Picked the Device : No");
        }
        long curTime = System.currentTimeMillis();
        // only allow one update every 100ms.
        if (curTime - lastUpdate > 100) {
            long diffTime = curTime - lastUpdate;
            lastUpdate = curTime;
            double speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

            if (speed > SHAKE_THRESHOLD) {

                idgesture3.setText("Shake Speed: "+ speed);

            }
            else
            {
                idgesture3.setText("Shake Speed: No Shaking");
            }
            last_x = x;
            last_y = y;
            last_z = z;
        }

            double norm_Of_g = Math.sqrt(Eventvalues[0] * Eventvalues[0] + Eventvalues[1] * Eventvalues[1] + Eventvalues[2] * Eventvalues[2]);

        // Normalize the accelerometer vector
        Eventvalues[0] = Eventvalues[0] / norm_Of_g;
        Eventvalues[1] = Eventvalues[1] / norm_Of_g;
        Eventvalues[2] = Eventvalues[2] / norm_Of_g;

        int inclination = (int) Math.round(Math.toDegrees(Math.acos(Eventvalues[2])));
        int rotation = (int) Math.round(Math.toDegrees(Math.atan2(Eventvalues[0], Eventvalues[1])));
        if (inclination < 25 || inclination > 155)
        {
            // device is flat
            idgesture2.setText("Device is on flat Surface");
        }
        else
        {
            // device is not flat
            idgesture2.setText("Device is Not flat Surface");
        }
        if(rotation ==0){
            idgesture1.setText("Rotation: Normal Position" );

        }
        else
        {

            idgesture1.setText("Rotation:"+rotation);
        }

    }

}