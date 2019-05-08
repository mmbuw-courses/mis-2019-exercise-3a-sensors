package com.example.mis.sensor;

import android.content.Context;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.example.mis.sensor.FFT;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import android.view.View;
import android.widget.SeekBar;
import android.widget.Switch;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final float N = 1.0f / 1000000000.0f;


    private double[] magnitude_Values;

    private double[] counts_freq;

    private double[] rndAccExamplevalues;


    private SensorManager sensor_manager;
    private Sensor acceleroMeter;

    //get codes from
    //https://github.com/jjoe64/GraphView

    private GraphView accelerometerGraph;

    private LineGraphSeries<DataPoint> Line_x, Line_y, Line_z, magnitude_line, FFTLine;

    private int windowSize = 200;
    private int sampleRate = SensorManager.SENSOR_DELAY_NORMAL;
    private int iFFT = 0;

    private Switch switchFFT;
    private SeekBar windowSizeBar;
    private SeekBar sampleRateBar;


    public float get_Magnitude(float x, float y, float z) {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }


    public void clearGraph(GraphView graph)

    {
        graph.removeAllSeries();
        DataPoint[] clear_array = new DataPoint[0];

        Line_x.resetData(clear_array);
        Line_y.resetData(clear_array);
        Line_z.resetData(clear_array);
        magnitude_line.resetData(clear_array);
        FFTLine.resetData(clear_array);

        if (!switchFFT.isChecked())

        {
            accelerometerGraph.addSeries(Line_x);
            accelerometerGraph.addSeries(Line_y);
            accelerometerGraph.addSeries(Line_z);
            accelerometerGraph.addSeries(magnitude_line);
        } else {
            accelerometerGraph.addSeries(FFTLine);
        }
    }


    public void Seekbar_toggle(View view) {

        if (!switchFFT.isChecked())
        {
            windowSizeBar.setVisibility(View.GONE);
            sampleRateBar.setVisibility(View.GONE);
        }
        else {
            windowSizeBar.setVisibility(View.VISIBLE);
            sampleRateBar.setVisibility(View.VISIBLE);
        }

        clearGraph(accelerometerGraph);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        acceleroMeter = null;
        sensor_manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // code got from
        // https://code.tutsplus.com/tutorials/using-the-accelerometer-on-android--mobile-22125
        //https://github.com/jjoe64/GraphView
        if (sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            acceleroMeter = sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            // register listeners
            sensor_manager.registerListener((SensorEventListener) this, acceleroMeter, SensorManager.SENSOR_DELAY_NORMAL);

            switchFFT = findViewById(R.id.switchFFT);
            windowSizeBar = findViewById(R.id.modFFT);
            sampleRateBar = findViewById(R.id.modSample);
        }
        if (!switchFFT.isChecked()) {
            windowSizeBar.setVisibility(View.GONE);
            sampleRateBar.setVisibility(View.GONE);
        }
        accelerometerGraph = findViewById(R.id.accelerometer);
        accelerometerGraph.setBackgroundColor(Color.LTGRAY);
        Viewport viewport = accelerometerGraph.getViewport();
        viewport.setScalable(true);
        viewport.setScalableY(true);
        viewport.setScrollable(true);
        viewport.setScrollableY(true);


        Line_x = new LineGraphSeries<>();
        Line_x.setTitle("x data");
        Line_x.setColor(Color.RED);
        accelerometerGraph.addSeries(Line_x);

        Line_y = new LineGraphSeries<>();
        Line_y.setTitle("y data");
        Line_y.setColor(Color.GREEN);
        accelerometerGraph.addSeries(Line_y);

        Line_z = new LineGraphSeries<>();
        Line_z.setTitle("z data");
        Line_z.setColor(Color.BLUE);
        accelerometerGraph.addSeries(Line_z);

        magnitude_line = new LineGraphSeries<>();
        magnitude_line.setTitle("magnitude data");
        magnitude_line.setColor(Color.WHITE);
        accelerometerGraph.addSeries(magnitude_line);

        FFTLine = new LineGraphSeries<>();
        FFTLine.setTitle("transformed magnitude data");
        FFTLine.setColor(Color.CYAN);
        accelerometerGraph.addSeries(FFTLine);



        windowSize = (int) Math.pow(2, windowSizeBar.getProgress() + 3);
        windowSizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int barProgress = 0;

            // https://developer.android.com/reference/android/widget/SeekBar.OnSeekBarChangeListener
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                barProgress = progress;
                progress = progress + 3;
                windowSize = (int) Math.pow(2, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                magnitude_Values = new double[windowSize];
            }
        });

        magnitude_Values = new double[windowSize];

        String delay = "";

        //code from below
        // https://stackoverflow.com/questions/17337504/need-to-read-android-sensors-with-fixed-sampling-rate
        //https://github.com/jjoe64/GraphView

        sampleRateBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int barProgress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                barProgress = progress;

                // code from below
                // https://stackoverflow.com/questions/17337504/need-to-read-android-sensors-with-fixed-sampling-rate
                switch (barProgress) {
                    case 0:
                        sampleRate = SensorManager.SENSOR_DELAY_FASTEST;
                        break;
                    case 1:
                        sampleRate = SensorManager.SENSOR_DELAY_GAME;
                        break;
                    case 2:
                        sampleRate = SensorManager.SENSOR_DELAY_NORMAL;
                        break;
                    case 3:
                        sampleRate = SensorManager.SENSOR_DELAY_UI;
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sensor_manager.unregisterListener(MainActivity.this);
                sensor_manager.registerListener(MainActivity.this, acceleroMeter, sampleRate);
            }
        });


        rndAccExamplevalues = new double[64];
        randomFill(rndAccExamplevalues);
        new FFTAsynctask(64).execute(rndAccExamplevalues);

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensor_manager.unregisterListener((SensorEventListener) this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensor_manager.registerListener((SensorEventListener) this, acceleroMeter, sampleRate);
    }



    //https://stackoverflow.com/a/2441702
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        windowSize = savedInstanceState.getInt("windowSize");
        sampleRate = savedInstanceState.getInt("sampleRate");
        switchFFT.setChecked(savedInstanceState.getBoolean("FFT"));
        sampleRateBar.setProgress(savedInstanceState.getInt("sampleRateBar_progress"));
        windowSizeBar.setProgress(savedInstanceState.getInt("windowSizeBar_progress"));
        sampleRateBar.setVisibility(savedInstanceState.getInt("sampleRateBar_visibility"));
        windowSizeBar.setVisibility(savedInstanceState.getInt("windowSizeBar_visibility"));
    }

    //https://stackoverflow.com/a/2441702
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("windowSize", windowSize);
        outState.putInt("sampleRate", sampleRate);
        outState.putBoolean("FFT", switchFFT.isChecked());
        outState.putInt("sampleRateBar_progress", sampleRateBar.getProgress());
        outState.putInt("windowSizeBar_progress", windowSizeBar.getProgress());
        outState.putInt("sampleRateBar_visibility", sampleRateBar.getVisibility());
        outState.putInt("windowSizeBar_visibility", windowSizeBar.getVisibility());
    }



    @Override
    public void onSensorChanged(final SensorEvent event) {
        Sensor sensor = event.sensor;

        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER && acceleroMeter != null) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (!switchFFT.isChecked()) {
                        Line_x.appendData(new DataPoint(event.timestamp * N, event.values[0]), true, 250);
                        Line_y.appendData(new DataPoint(event.timestamp * N, event.values[1]), true, 250);
                        Line_z.appendData(new DataPoint(event.timestamp * N, event.values[2]), true, 250);
                        magnitude_line.appendData(new DataPoint(event.timestamp * N, get_Magnitude(event.values[0], event.values[1], event.values[2])), true, 40);
                    } else {
                        if (iFFT > windowSize) {
                            magnitude_Values = new double[windowSize];
                            iFFT = 0;
                        } else if (iFFT < windowSize) {
                            magnitude_Values[iFFT] = get_Magnitude(event.values[0], event.values[1], event.values[2]);
                        } else {
                            new FFTAsynctask(windowSize).execute(magnitude_Values);
                        }
                        ++iFFT;
                    }

                }
            });
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {


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

    private class FFTAsynctask extends AsyncTask<double[], Void, double[]> {

        private int wsize;


        FFTAsynctask(int wsize) {
            this.wsize = wsize;
        }

        @Override
        protected double[] doInBackground(double[]... values) {


            double[] realPart = values[0].clone();
            double[] imagPart = new double[wsize];
            /**
             * Init the FFT class with given window size and run it with your input.
             * The fft() function overrides the realPart and imagPart arrays!
             */

            FFT fft = new FFT(wsize);
            fft.fft(realPart, imagPart);

            double[] magnitude = new double[wsize];



            for (int i = 0; wsize > i ; i++) {
                magnitude[i] = Math.sqrt(Math.pow(realPart[i], 2) + Math.pow(imagPart[i], 2));
            }

            return magnitude;

        }


        @Override
        protected void onPostExecute(double[] values) {

            counts_freq = values;

            DataPoint[] fft_point = new DataPoint[this.wsize];
            for (int i = 0; i < this.wsize; ++i) {
                fft_point[i] = new DataPoint(i, counts_freq[i]);
            }
            FFTLine.resetData(fft_point);
        }

    }


}