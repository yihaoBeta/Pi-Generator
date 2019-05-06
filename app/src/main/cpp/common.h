//
// Created by yihao on 2019/5/3.
//

#ifndef PICOMPUTOR_COMMON_H
#define PICOMPUTOR_COMMON_H

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,"TAG",__VA_ARGS__)

/**
 * 圆周率计算模块的所有方法注册函数
 * @param env
 * @return
 */
jboolean registerPICoreMethods(JNIEnv *env);

#endif //PICOMPUTOR_COMMON_H
