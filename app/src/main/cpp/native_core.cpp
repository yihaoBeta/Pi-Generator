/**
 * Created by yihao
 * Native层的通用入口
 * 用来注册java层定义的native方法
**/

#include <jni.h>
#include <string>
#include <android/log.h>
#include "common.h"

//JavaVM *gVm;

/**
 * 注册Java层定义的native方法
 * @param env
 * @return
 */
static int registerNatives(JNIEnv *env) {
    if (!registerPICoreMethods(env)) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

/**
 * NDK入口函数
 * @param vm
 * @param reserved
 * @return
 */
jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        LOGD("GetEnvfailed\n");
        return -1;
    }
    //注册
    if (!registerNatives(env)) {
        LOGD("register error");
        return -1;
    }
    // gVm = vm;
    return JNI_VERSION_1_6;
}