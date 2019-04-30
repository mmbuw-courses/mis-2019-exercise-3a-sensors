package com.example.mis_2019_exercise_3a_sensors;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.lang.Math;

// https://developer.android.com/training/graphics/opengl/shapes
public class Line {

    private FloatBuffer vertexBuffer;
    private int mProgram;
    private int positionHandle;
    private int colorHandle;

    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    // Use to access and set the view transformation
    private int vPMatrixHandle;

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    // number of coordinates per Vertex in this array
    static final int COORDS_PER_VERTEX = 3;

    float[] lineCoords = new float[6];
    float[] color = new float[6];

    private final int vertexCount = lineCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    public Line(){
        lineCoords[0] = 0.0f;
        lineCoords[1] = 0.0f;
        lineCoords[2] = 0.0f;
        lineCoords[3] = 1.0f;
        lineCoords[4] = 1.0f;
        lineCoords[5] = 1.0f;
        color[0] = 1.0f;
        color[1] = 1.0f;
        color[2] = 1.0f;
        initLineGL();
    }

    public Line(float f1, float f2, float f3, float f4, float f5, float f6){
        lineCoords[0] = f1;
        lineCoords[1] = f2;
        lineCoords[2] = f3;
        lineCoords[3] = f4;
        lineCoords[4] = f5;
        lineCoords[5] = f6;
        initLineGL();
    }

    public Line(float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9){
        lineCoords[0] = f1;
        lineCoords[1] = f2;
        lineCoords[2] = f3;
        lineCoords[3] = f4;
        lineCoords[4] = f5;
        lineCoords[5] = f6;
        color[0] = f7;
        color[1] = f8;
        color[2] = f9;
        initLineGL();
    }

    public void initLineGL(){
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                lineCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(lineCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);

        // link the shader code to this class' instances
        int vertexShader = GLAccelerometerRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = GLAccelerometerRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();
        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);
        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);
        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);
    }


    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the line vertices
        GLES20.glEnableVertexAttribArray(positionHandle);

        // Prepare the line coordinate data
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the line
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0);


        // Draw the line
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    public void setLine(float f1, float f2, float f3, float f4, float f5, float f6){
        lineCoords[0] = f1;
        lineCoords[1] = f2;
        lineCoords[2] = f3;
        lineCoords[3] = f4;
        lineCoords[4] = f5;
        lineCoords[5] = f6;
    }

    public void setColor(float f1, float f2, float f3){
        color[0] = f1;
        color[1] = f2;
        color[2] = f3;
    }

    public double lineLength(){
        return Math.sqrt(Math.pow((lineCoords[3]-lineCoords[0]), 2) + Math.pow((lineCoords[1] - lineCoords[4]), 2) + Math.pow((lineCoords[2] - lineCoords [5]), 2));
    }
}
