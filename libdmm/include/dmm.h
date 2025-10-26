#pragma once
#include <jni.h>

extern "C" JNIEXPORT jdoubleArray JNICALL
Java_com_mikayel_grigoryan_Bindings_mul(JNIEnv *env, jclass,
                                        jint lRows, jint lCols, jdoubleArray lData,
                                        jint rRows, jint rCols, jdoubleArray rData);

extern "C" JNIEXPORT jboolean JNICALL
Java_com_mikayel_grigoryan_Bindings_eq(JNIEnv *env, jclass,
                                       jint lRows, jint lCols, jdoubleArray lData,
                                       jint rRows, jint rCols, jdoubleArray rData,
                                       jdouble errorTolerance);