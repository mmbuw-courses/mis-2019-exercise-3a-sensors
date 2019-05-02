package com.example.mis_2019_exercise_3a_sensors;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.opengles.GL10;

// https://developer.android.com/training/graphics/opengl/environment.html
// https://developer.android.com/training/graphics/opengl/draw
// https://developer.android.com/training/graphics/opengl/projection
public class GLAccelerometerRenderer implements GLSurfaceView.Renderer {
    public Line xAxis;
    public Line yAxis;
    public Line zAxis;
    public Line magnitude;

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    public float[] linear_acceleration = new float[4];
    private final float[] vPMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private float[] rotationMatrix = new float[16];

    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        // x-axis in crimson red
        xAxis = new Line(0.0f,  0.0f, 0.0f, 0.5f, 0.0f, 0.0f, 220.0f/255.0f, 20.0f/255.0f, 60.0f/255.0f);
        // y-axis in lime green
        yAxis = new Line(0.0f,  0.0f, 0.0f, 0.0f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f);
        // z-axis in cyan
        zAxis = new Line(0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.5f, 0.0f, 1.0f, 1.0f);
        // magnitude in white
        magnitude = new Line(0.0f, 0.0f, 0.0f, -0.5f, 0.0f, 0.0f, 240.0f/255.0f, 248.0f/255.0f, 255.0f/255.0f);
    }

    public void onDrawFrame(GL10 unused) {
        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        float[] scratch = new float[16];

        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        float angle = 45.0f;
        Matrix.setRotateM(rotationMatrix, 0, angle, 1.0f, -1.0f, 1.0f);

        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0);
        Matrix.scaleM(scratch, 0, 0.2f + 0.5f*linear_acceleration[0], 0.2f + 0.5f*linear_acceleration[1], 0.2f + 0.5f*linear_acceleration[2]);

        Matrix.translateM(vPMatrix, 0, 1.0f, -0.8f, 0.0f);
        Matrix.scaleM(vPMatrix, 0, 0.3f + 0.5f*linear_acceleration[3], 0.0f, 0.0f);

        xAxis.draw(scratch);
        yAxis.draw(scratch);
        zAxis.draw(scratch);
        magnitude.draw(vPMatrix);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    public static int loadShader(int type, String shaderCode){
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}
