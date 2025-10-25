package com.mikayel.grigoryan;

/**
 * This class exposes the system-level bindings to the low-level Eigen library
 * functions exposed by the JNI C++ code in `libdmm`. This code is not meant to
 * be used directly. Instead, you must use the {@link DenseMatrix}
 * which is a safe Matrix wrapper for this class.
 */
final class Bindings {
    static final String LIBRARY_NAME = "dmm";

    static {
        // Since the dynamic library is already included in the Java's path in the
        // build step, we can load it directly with its name by calling loadLibrary.
        System.loadLibrary(LIBRARY_NAME);
    }

    /**
     * This is the method which is implemented externally in the C++ dynamic library
     * using the Eigen library under the hood. The method takes two matrices and performs
     * matrix multiplication via a JNI call.
     * <p>
     * The assumptions that this function makes are the following:
     * <li>`lRows`, `rRows`, `lCols`, and `rCols` are non-negative
     * <li>`left` and `right` double arrays are not `null`
     * <li>`lCols` is equal to `rRows`
     *
     * @param left  The left-hand side matrix
     * @param right The right-hand side matrix
     * @return If the operation succeeds, the function will return a flattened
     * final matrix as the final result.
     */
    public static native double[] mul(
            int lRows, int lCols, double[] left,
            int rRows, int rCols, double[] right);

    /**
     * Compares two matrices for element-wise equality using native Eigen functions.
     * <p>
     * This method checks whether two matrices are identical in both shape and content.
     * The operation is implemented in C++ and exposed to Java via JNI. The comparison
     * is typically performed with a small floating-point tolerance on the native side
     * to account for potential numerical precision differences.
     * </p>
     *
     * <p>Assumptions:</p>
     * <ul>
     *     <li>{@code lRows}, {@code lCols}, {@code rRows}, and {@code rCols} are non-negative.</li>
     *     <li>{@code left} and {@code right} are non-{@code null} arrays of compatible length.</li>
     *     <li>{@code lRows == rRows} and {@code lCols == rCols} must hold true for a valid comparison.</li>
     * </ul>
     *
     * @param lRows  the number of rows in the left-hand matrix
     * @param lCols  the number of columns in the left-hand matrix
     * @param left   the flattened row-major data of the left-hand matrix
     * @param rRows  the number of rows in the right-hand matrix
     * @param rCols  the number of columns in the right-hand matrix
     * @param right  the flattened row-major data of the right-hand matrix
     * @return {@code true} if both matrices are equal within the defined numerical tolerance;
     *         {@code false} otherwise
     */
    public static native boolean eq(
            int lRows, int lCols, double[] left,
            int rRows, int rCols, double[] right);
}
