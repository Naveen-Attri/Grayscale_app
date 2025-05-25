
#include <jni.h>
#include <android/bitmap.h>
#include <opencv2/opencv.hpp>

using namespace cv;

jobject matToBitmap(JNIEnv *env, Mat &mat) {
    // Create a new Bitmap with the same dimensions
    jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
    jmethodID createBitmap = env->GetStaticMethodID(bitmapClass,
                                                    "createBitmap", "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");

    jstring configName = env->NewStringUTF("ARGB_8888");
    jclass bitmapConfigClass = env->FindClass("android/graphics/Bitmap$Config");
    jmethodID valueOf = env->GetStaticMethodID(bitmapConfigClass,
                                               "valueOf", "(Ljava/lang/String;)Landroid/graphics/Bitmap$Config;");
    jobject bitmapConfig = env->CallStaticObjectMethod(bitmapConfigClass, valueOf, configName);

    jobject bitmap = env->CallStaticObjectMethod(bitmapClass, createBitmap,
                                                 mat.cols, mat.rows, bitmapConfig);

    // Copy the data to the Bitmap
    void* pixels;
    AndroidBitmap_lockPixels(env, bitmap, &pixels);
    memcpy(pixels, mat.data, mat.total() * mat.elemSize());
    AndroidBitmap_unlockPixels(env, bitmap);

    return bitmap;
}

Mat imageToMat(JNIEnv *env, jobject image) {
    // Get the YUV data from Image
    // Note: This is a simplified version - you'll need proper YUV to RGB conversion
    // for a production app

    // For now, we'll assume we're getting a Bitmap
    AndroidBitmapInfo info;
    void* pixels;

    if (AndroidBitmap_getInfo(env, image, &info) < 0) {
        return Mat();
    }

    if (AndroidBitmap_lockPixels(env, image, &pixels) < 0) {
        return Mat();
    }

    Mat mat(info.height, info.width, CV_8UC4, pixels);
    AndroidBitmap_unlockPixels(env, image);

    return mat;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_example_testapp_MainActivity_processFrameWithEdgeDetection(JNIEnv *env, jobject thiz, jobject bitmapIn) {

    AndroidBitmapInfo info;
    void* pixels;

    // Lock the bitmap to get the raw pixels
    if (AndroidBitmap_getInfo(env, bitmapIn, &info) != ANDROID_BITMAP_RESULT_SUCCESS) {
        return nullptr;
    }

    if (AndroidBitmap_lockPixels(env, bitmapIn, &pixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        return nullptr;
    }

    // Create OpenCV Mat from the bitmap
    Mat mat;
    if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
        mat = Mat(info.height, info.width, CV_8UC4, pixels);
    } else if (info.format == ANDROID_BITMAP_FORMAT_RGB_565) {
        mat = Mat(info.height, info.width, CV_8UC2, pixels);
    } else {
        AndroidBitmap_unlockPixels(env, bitmapIn);
        return nullptr;
    }

    // Convert to grayscale
    Mat gray;
    cvtColor(mat, gray, COLOR_RGBA2GRAY);

    // Convert back to RGBA for display
    Mat result;
    cvtColor(gray, result, COLOR_GRAY2RGBA);

    // Unlock the bitmap
    AndroidBitmap_unlockPixels(env, bitmapIn);

    // Create output bitmap
    jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
    jmethodID createBitmap = env->GetStaticMethodID(bitmapClass,
                                                    "createBitmap", "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");

    jstring configName = env->NewStringUTF("ARGB_8888");
    jclass bitmapConfigClass = env->FindClass("android/graphics/Bitmap$Config");
    jmethodID valueOf = env->GetStaticMethodID(bitmapConfigClass,
                                               "valueOf", "(Ljava/lang/String;)Landroid/graphics/Bitmap$Config;");
    jobject bitmapConfig = env->CallStaticObjectMethod(bitmapConfigClass, valueOf, configName);

    jobject bitmapOut = env->CallStaticObjectMethod(bitmapClass, createBitmap,
                                                    result.cols, result.rows, bitmapConfig);

    // Copy the result to output bitmap
    void* outPixels;
    AndroidBitmap_lockPixels(env, bitmapOut, &outPixels);
    memcpy(outPixels, result.data, result.total() * result.elemSize());
    AndroidBitmap_unlockPixels(env, bitmapOut);

    return bitmapOut;
}


extern "C" JNIEXPORT jobject JNICALL
Java_com_example_testapp_MainActivity_processFrameRaw(JNIEnv *env, jobject thiz, jobject image) {

    // Just pass through the original image
    Mat inputMat = imageToMat(env, image);
    if (inputMat.empty()) {
        return nullptr;
    }

    return matToBitmap(env, inputMat);
}




extern "C"
JNIEXPORT void JNICALL
Java_com_example_testapp_MainActivity_processFrame__Landroid_graphics_Bitmap_2Landroid_graphics_Bitmap_2(JNIEnv *env, jobject thiz, jobject input, jobject output) {

}