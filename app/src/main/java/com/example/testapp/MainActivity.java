package com.example.testapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.camera.core.CameraSelector;
import android.util.Log;
import java.nio.ByteBuffer;
import com.google.common.util.concurrent.ListenableFuture;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private ExecutorService cameraExecutor;
    Boolean applyFilter = false;
    Button filterButton;
    TextView fpsCounter;
    GLSurfaceView glSurfaceView;
    MyGLRenderer renderer;

    // For performance measurement
    long startTime = 0;
    private int frameCount = 0;

    // Load native library
    public native void processFrame(byte[] frameData, int width, int height, boolean applyFilter);
    static {
        System.loadLibrary("testapp");
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Your layout with TextureView (optional)

        cameraExecutor = Executors.newSingleThreadExecutor();
        glSurfaceView = findViewById(R.id.grayImage);
        glSurfaceView.setEGLContextClientVersion(2);
        assert glSurfaceView != null;
        renderer = new MyGLRenderer();
        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        filterButton = findViewById(R.id.filterButton);
        fpsCounter = findViewById(R.id.fpsCounter);
        fpsCounter.setVisibility(View.GONE);

        filterButton.setOnClickListener(v -> {
            applyFilter = !applyFilter;
            if (applyFilter) {
                filterButton.setText("Disable Filter");
                fpsCounter.setVisibility(View.VISIBLE);
            } else {
                filterButton.setText("Enable Filter");
                fpsCounter.setVisibility(View.GONE);

            }
        });


        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CODE
            );
        }
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, image -> {
                    frameCount++;

                    long currentTime = System.currentTimeMillis();

                    if (frameCount == 10) {
                        long elapsedTime = currentTime - startTime;
                        float fps = (float) (1000 * frameCount) / elapsedTime;
                        Log.i("FPS", "Average FPS: " + fps);
                        runOnUiThread(() -> fpsCounter.setText(String.format("FPS: %.2f", fps)));

                        frameCount = 0;
                        startTime = currentTime;
                    }

                    ImageProxy.PlaneProxy plane = image.getPlanes()[0];
                    ByteBuffer buffer = plane.getBuffer();

                    byte[] rgba = new byte[buffer.remaining()];
                    buffer.get(rgba);

                    processFrame(rgba, image.getWidth(), image.getHeight(), applyFilter);

                    // for directly displaying the image in ImageView (if needed)
//                    Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
//                    bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(rgba));
//
//                    runOnUiThread(() -> imageView.setImageBitmap(bitmap));


                    renderer.updateFrame(rgba, image.getWidth(), image.getHeight());
                    runOnUiThread(() -> {
                        if (glSurfaceView != null) {
                            glSurfaceView.requestRender();
                        }
                    });

                    image.close();
                });

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis);

            } catch (Exception e) {
                Log.e("CameraX", "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
