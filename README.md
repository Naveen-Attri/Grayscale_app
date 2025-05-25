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

## Screenshots / GIF

> Add actual media here

| Camera Feed | OpenGL Processed Output |
|-------------|-------------------------|
| ![Camera](screenshots/camera_preview.jpg) | ![Processed](screenshots/processed_frame.jpg) |

---

##  Setup Instructions

### Prerequisites

- Android Studio (Arctic Fox or newer)
- Android NDK (version 23 or later recommended)
- CMake and LLDB (installed via SDK Tools)
- OpenCV Android SDK

###  Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/EdgeDetectApp.git
   cd EdgeDetectApp
