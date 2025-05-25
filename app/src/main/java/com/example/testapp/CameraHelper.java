package com.example.testapp;

import android.graphics.SurfaceTexture;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.content.Context;

public class CameraHelper implements TextureView.SurfaceTextureListener {
    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private TextureView textureView;
    private Context context;
    private ImageAnalysis.Analyzer frameAnalyzer;
    private SurfaceTexture surfaceTexture;
    private Surface surface;

    public CameraHelper(Context context, TextureView textureView) {
        this.context = context;
        this.textureView = textureView;
        this.cameraExecutor = Executors.newSingleThreadExecutor();

        // Set up surface texture listener
        textureView.setSurfaceTextureListener(this);
    }

    public void setFrameAnalyzer(ImageAnalysis.Analyzer analyzer) {
        this.frameAnalyzer = analyzer;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        this.surfaceTexture = surfaceTexture;
        this.surface = new Surface(surfaceTexture);
        setupCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        // Reconfigure camera if needed
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        if (surface != null) {
            surface.release();
            surface = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        // Frame update callback
    }

    private void setupCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(context));
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null || surface == null) {
            return;
        }

        // Unbind previous use cases
        cameraProvider.unbindAll();

        // Camera selector (back camera)
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // Preview use case
        Preview preview = new Preview.Builder()
                .setTargetResolution(new Size(1280, 720))
                .build();

        // Set the surface for preview
        preview.setSurfaceProvider(request -> {
            request.provideSurface(surface, ContextCompat.getMainExecutor(context),
                    result -> {
                        // Handle surface request result
                    });
        });

        // Image analysis use case
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(new Size(640, 480))
                .build();

        if (frameAnalyzer != null) {
            imageAnalysis.setAnalyzer(cameraExecutor, frameAnalyzer);
        }

        // Bind use cases to camera
        camera = cameraProvider.bindToLifecycle(
                (LifecycleOwner)context,
                cameraSelector,
                preview,
                imageAnalysis);
    }

    public void shutdown() {
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        if (surface != null) {
            surface.release();
            surface = null;
        }
    }
}