#include <jni.h>
#include <string>
#include <android/log.h>
#include <iostream>
#include <bitset>
#include <pthread.h>
#include <unistd.h>
#include "common.h"
#include "pigeon.h"

#define START 0x01
#define RUNNING 0x02
#define COMPLETE 0x03


/**
 * java层native方法实现
 * 调用gmp库用于计算圆周率
 */

//保存java类为全局变量，用来在不同的线程中使用
jobject gObject;
JavaVM *gVm;

//计算标志位
bool running = JNI_TRUE;

jint JNICALL native_compute_pi(JNIEnv *env, jobject thiz, jint n, jobject listener);

void JNICALL native_stop(JNIEnv *env, jobject thiz);

// 在java层申明的类名（包名+类名）
const char *classPathName = "com/yihaobeta/picomputor/function/NativeHelper";

// 所有Java层的Native方法与底层实现接口函数的对应关系
static JNINativeMethod methods[] = {
        {"computePiByNative", "(ILcom/yihaobeta/picomputor/function/PiComputeListener;)I", (void *) native_compute_pi},
        {"stopCompute",       "()V",                                              (void *) native_stop},
};


// 注册Native方法
jboolean registerPICoreMethods(JNIEnv *env) {
    jclass clazz = env->FindClass(classPathName);
    if (clazz == NULL) {
        return JNI_FALSE;
    }

    if (env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0])) < 0) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

/**
 * 计算线程
 * @param args
 * @return
 */
void *compute(void *args) {
    LOGD("thread");
    LOGD("in thread: n = %d\n", (int) args);
    bool mNeedDetach = JNI_FALSE;
    JNIEnv *localEnv;
    //获取当前native线程是否有没有被附加到jvm环境中
    int getEnvStat = gVm->GetEnv((void **) &localEnv, JNI_VERSION_1_6);
    if (getEnvStat == JNI_EDETACHED) {
        //如果没有， 主动附加到jvm环境中，获取到env
        if (gVm->AttachCurrentThread(&localEnv, NULL) != JNI_OK) {
            LOGD("attach fail");
            pthread_exit(nullptr);
        }
        mNeedDetach = JNI_TRUE;
    }
    assert(localEnv != NULL);
    jclass clazz = localEnv->GetObjectClass(gObject);
    if (clazz == NULL) {
        LOGD("Unable to find class");
        gVm->DetachCurrentThread();
        pthread_exit(nullptr);
    }
    jmethodID methodId = localEnv->GetMethodID(clazz, "onCompute", "(II)V");
    if (methodId == NULL) {
        LOGD("Unable to find method:onCompute");
        pthread_exit(nullptr);
    }

    localEnv->CallVoidMethod(gObject, methodId, START, -1);

    //环境正常，开始计算
    pigeon pg((size_t) args);
    for (int i : pg) {
        localEnv->CallVoidMethod(gObject, methodId, RUNNING, i);
        if (!running) {
            break;
        }
    }
    pg = NULL;

    localEnv->CallVoidMethod(gObject, methodId, COMPLETE, -1);
    //释放当前线程
    if (mNeedDetach) {
        gVm->DetachCurrentThread();
    }

    //释放全局引用
    localEnv->DeleteGlobalRef(gObject);
    localEnv = NULL;

    pthread_exit(nullptr);
}

jint JNICALL native_compute_pi(JNIEnv *env, jobject thiz, jint n, jobject listener) {
    LOGD("request:%d\n", n);
    env->GetJavaVM(&gVm);
    gObject = env->NewGlobalRef(listener);
    running = JNI_TRUE;
    pthread_t thread;
    //创建新的线程用来完成耗时的计算任务
    pthread_create(&thread, NULL, compute, (void *) n);
    return 1;
}

/**
 * 停止计算任务
 * @param env
 * @param thiz
 */
void JNICALL native_stop(JNIEnv *env, jobject thiz) {
    running = JNI_FALSE;
}









