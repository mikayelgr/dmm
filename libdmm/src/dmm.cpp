#include <Eigen/Dense>
#include <jni.h>

extern "C" JNIEXPORT jdoubleArray JNICALL
Java_com_mikayel_grigoryan_Bindings_mul(JNIEnv *env, jclass,
    jint lRows, jint lCols, jdoubleArray lData,
    jint rRows, jint rCols, jdoubleArray rData
)
{
    jdouble* leftPtr = env->GetDoubleArrayElements(lData, nullptr);
    jdouble* rightPtr = env->GetDoubleArrayElements(rData, nullptr);
    Eigen::Map<const Eigen::Matrix<double, Eigen::Dynamic, Eigen::Dynamic, Eigen::RowMajor>> leftMat(leftPtr, lRows, lCols);
    Eigen::Map<const Eigen::Matrix<double, Eigen::Dynamic, Eigen::Dynamic, Eigen::RowMajor>> rightMat(rightPtr, rRows, rCols);
    Eigen::Matrix<double, Eigen::Dynamic, Eigen::Dynamic, Eigen::RowMajor> resultMat = leftMat * rightMat;
    jdoubleArray resultData = env->NewDoubleArray(resultMat.size());
    env->SetDoubleArrayRegion(resultData, 0, resultMat.size(), resultMat.data());
    env->ReleaseDoubleArrayElements(lData, leftPtr, 0);
    env->ReleaseDoubleArrayElements(rData, rightPtr, 0);
    return resultData;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_mikayel_grigoryan_Bindings_eq(JNIEnv *env, jclass,
    jint lRows, jint lCols, jdoubleArray lData,
    jint rRows, jint rCols, jdoubleArray rData
)
{
    jdouble* leftPtr = env->GetDoubleArrayElements(lData, nullptr);
    jdouble* rightPtr = env->GetDoubleArrayElements(rData, nullptr);
    Eigen::Map<const Eigen::Matrix<double, Eigen::Dynamic, Eigen::Dynamic, Eigen::RowMajor>> leftMat(leftPtr, lRows, lCols);
    Eigen::Map<const Eigen::Matrix<double, Eigen::Dynamic, Eigen::Dynamic, Eigen::RowMajor>> rightMat(rightPtr, rRows, rCols);

    jboolean result = JNI_FALSE;
    if (lRows == rRows && lCols == rCols) {
        // Use Eigen's isApprox with a small tolerance to compare floating-point matrices
        result = leftMat.isApprox(rightMat, 1e-4) ? JNI_TRUE : JNI_FALSE;
    }

    env->ReleaseDoubleArrayElements(lData, leftPtr, 0);
    env->ReleaseDoubleArrayElements(rData, rightPtr, 0);
    return result;
}
