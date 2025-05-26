# Real-Time Edge Detection App (Android + OpenCV + OpenGL ES)

This app performs real-time edge detection using the Android camera (CameraX), processes frames with OpenCV via JNI (NDK), and renders the results using OpenGL ES for efficient GPU-based display.

---

## Features Implemented

-  Real-time camera preview using CameraX
-  Frame processing using OpenCV C++ (via JNI)
-  Display processed frames using OpenGL ES with texture rendering
-  Optimized for performance (RGBA conversion, grayscale toggle)
-  FPS counter displayed on screen
-  Multithreaded image analysis using `ImageAnalysis`
---

## Architecture Explanation
1. CameraX captures frames in RBGA_8888 format using the imageanalysis analyzer callback
2. Frame data in bytes array is passed to c++ code using jni(bridge between java and c++).
3. Frame processing(grayscale) is performed in c++
4. Opengl then uses the frames to create textures to render on the glSurfaceView whenever requestRender() is called.

---

## Screenshots / GIF

| Camera Feed | OpenGL Processed Output |
|-------------|-------------------------|
| ![Camera](screenshots/camera_preview.jpg) | ![Processed](screenshots/processed_frame.jpg) |

---

##  Setup Instructions

### Prerequisites

- Android Studio - newer versions are recommended
- Android NDK
- CMake and LLDB (installed via SDK Tools) - will be done automatically by gradle
- OpenCV Android SDK

  Note: Everything will be installed/downloaded by the android studio. Just Clone the repo and let the studio do its magic. All you need is a emulator or a physical device to run the app


###  Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/EdgeDetectApp.git
