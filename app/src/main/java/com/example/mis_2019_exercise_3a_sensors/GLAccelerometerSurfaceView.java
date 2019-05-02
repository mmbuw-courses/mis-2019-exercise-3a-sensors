package com.example.mis_2019_exercise_3a_sensors;

import android.content.Context;
import android.hardware.Sensor;

import android.opengl.GLSurfaceView;

import android.util.AttributeSet;

// https://developer.android.com/training/graphics/opengl/environment.html
public class GLAccelerometerSurfaceView extends GLSurfaceView{
    public GLAccelerometerRenderer renderer;

    public GLAccelerometerSurfaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        renderer = new GLAccelerometerRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer);
    }
}
