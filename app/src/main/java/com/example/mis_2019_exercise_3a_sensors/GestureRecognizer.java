package com.example.mis_2019_exercise_3a_sensors;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;

import android.view.Gravity;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;


public class GestureRecognizer extends IntentService {
    public GestureRecognizer(){
        super("GestureRecognizer");
    }

    public GestureRecognizer(String name){
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(ActivityRecognitionResult.hasResult(intent)) {
            final ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            // create a handler to post detected gesture to the main thread
            Handler mHandler = new Handler(getMainLooper());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    String txt;
                    switch( result.getMostProbableActivity().getType() ) {
                        case DetectedActivity.ON_FOOT: {
                            txt = "Phone was on foot";
                            Toast toast = Toast.makeText(getApplicationContext(), txt, Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0,0);
                            toast.show();
                            break;
                        }
                        case DetectedActivity.RUNNING: {
                            txt = "Phone was running";
                            Toast toast = Toast.makeText(getApplicationContext(), txt, Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0,0);
                            toast.show();
                            break;
                        }
                        case DetectedActivity.TILTING: {
                            txt = "Phone was rotating";
                            Toast toast = Toast.makeText(getApplicationContext(), txt, Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0,0);
                            toast.show();
                            break;
                        }
                        case DetectedActivity.WALKING: {
                            txt = "Phone was on walking";
                            Toast toast = Toast.makeText(getApplicationContext(), txt, Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0,0);
                            toast.show();
                            break;
                        }
                        case DetectedActivity.UNKNOWN: {
                            txt = "Phone was doing unknown things";
                            Toast toast = Toast.makeText(getApplicationContext(), txt, Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0,0);
                            toast.show();
                        }
                    }
                }
            });
        }
    }
}
