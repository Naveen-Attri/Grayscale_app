#include <jni.h>
#include <opencv2/opencv.hpp>
#include <android/log.h>

#define TAG "NativeCode"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

extern "C"
JNIEXPORT void JNICALL
Java_com_example_testapp_MainActivity_processFrame(JNIEnv *env, jobject thiz, jbyteArray frameData,
                                                   jint width, jint height, jboolean applyFilter) {
    jbyte* data = env->GetByteArrayElements(frameData, nullptr);

    cv::Mat rgba(height, width, CV_8UC4, reinterpret_cast<uchar*>(data));

    // Convert RGBA to Grayscale
    if( applyFilter != false ) {
        cv::Mat gray;
        cv::cvtColor(rgba, gray, cv::COLOR_RGBA2GRAY);
        cv::Mat grayRGBA;
        cv::cvtColor(gray, grayRGBA, cv::COLOR_GRAY2RGBA);
        cv::rotate(grayRGBA, grayRGBA, cv::ROTATE_90_CLOCKWISE);
        std::memcpy(rgba.data, grayRGBA.data, grayRGBA.total() * grayRGBA.elemSize());
    }
    else {
        cv::rotate(rgba, rgba, cv::ROTATE_90_CLOCKWISE);
    }


    env->ReleaseByteArrayElements(frameData, data, 0);

}