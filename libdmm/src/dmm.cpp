#include <Eigen/Dense>
#include <jni.h>

extern "C" JNIEXPORT jdouble JNICALL
Java_com_mikayel_grigoryan_Bindings_multiplyDouble(JNIEnv *env, jclass, jobject m1, jobject m2)
{
    jclass listClass = env->FindClass("java/util/List");
    jclass doubleClass = env->FindClass("java/lang/Double");
    return 0.0;
}
