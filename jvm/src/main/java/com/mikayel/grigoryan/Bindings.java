package com.mikayel.grigoryan;

import java.util.List;

/**
 * This class exposes the system-level bindings to the low-level Eigen library
 * functions exposed by the JNI C++ code in `libdmm`. This code is not meant to
 * be used directly. Instead, you must use the {@link com.mikayel.grigoryan.DMM}
 * which is the wrapper around this class.
 */
final class Bindings {
    static {
        System.loadLibrary("dmm");
    }

    public static native double multiplyDouble(List<List<Double>> left, List<List<Double>> right);
    public static native int multiplyInt(List<List<Integer>> left, List<List<Integer>> right);
}
