/**
 * Provides a Kotlin-friendly, zero-copy wrapper for a native (C/C++) dense matrix implementation,
 * presumably using the Eigen library via C interoperability.
 */
package com.mikayel.grigoryan

import kotlinx.cinterop.*
import com.mikayel.grigoryan.libdmmbase.cinterop.eq as bindingsEq
import com.mikayel.grigoryan.libdmmbase.cinterop.mul as bindingsMul
import com.mikayel.grigoryan.libdmmbase.cinterop.free_matrix as bindingsFreeMatrix
import com.mikayel.grigoryan.libdmmbase.cinterop.MatrixHandle as BindingsMatrixHandle

/**
 * A Kotlin wrapper for a native dense matrix structure.
 *
 * This class provides direct, zero-copy access to the matrix data held in native memory.
 * It implements [AutoCloseable] to allow for deterministic cleanup of the underlying
 * native resources, which is crucial when the matrix is created and managed by the native library
 * (e.g., as a result of a multiplication).
 *
 * @property rows The number of rows in the matrix.
 * @property cols The number of columns in the matrix.
 * @property dataPtr A raw C pointer to the first element (row 0, col 0) of the matrix data,
 * stored in row-major order.
 * @property nativeHandlePtr An optional C pointer to the native structure that "owns" this matrix data.
 * If non-null, [close] will use this handle to free the native resources.
 */
@Suppress("EqualsOrHashCode")
@OptIn(ExperimentalForeignApi::class)
class DenseMatrix internal constructor(
    val rows: Int,
    val cols: Int,
    internal val dataPtr: CPointer<DoubleVar>,
    internal val nativeHandlePtr: CPointer<BindingsMatrixHandle>? = null
) : AutoCloseable {
    /**
     * Reads an element directly from the native memory buffer.
     *
     * @param row The row index (0-based).
     * @param col The column index (0-based).
     * @return The [Double] value at the specified position.
     * @throws IllegalArgumentException if the indices are out of bounds.
     */
    operator fun get(row: Int, col: Int): Double {
        require(row in 0 until this.rows && col in 0 until this.cols)
        return this.dataPtr[row * this.cols + col]
    }

    /**
     * Writes an element directly to the native memory buffer.
     *
     * @param row The row index (0-based).
     * @param col The column index (0-based).
     * @param value The [Double] value to write.
     * @throws IllegalArgumentException if the indices are out of bounds.
     */
    operator fun set(row: Int, col: Int, value: Double) {
        require(row in 0 until this.rows && col in 0 until this.cols)
        this.dataPtr[row * this.cols + col] = value
    }

    /**
     * Frees the underlying native matrix resources, if this [DenseMatrix] instance
     * was created with a [nativeHandlePtr].
     *
     * This allows for deterministic cleanup, which is useful in resource-sensitive
     * contexts like loops or benchmarks. If this instance does not own a native handle
     * (e.g., it wraps data allocated elsewhere), this method has no effect.
     */
    override fun close() {
        this.nativeHandlePtr?.let {
            bindingsFreeMatrix(it)
        }
    }

    /**
     * Generates a string representation of the matrix, with elements separated by tabs
     * and rows separated by newlines.
     */
    override fun toString(): String {
        val sb = StringBuilder()
        for (i in 0 until this.rows) {
            for (j in 0 until this.cols) sb.append("${get(i, j)}\t")
            sb.appendLine()
        }
        return sb.toString()
    }

    /**
     * Checks for approximate equality with another object.
     *
     * If [other] is a [DenseMatrix], this delegates to [equalsApprox] using
     * the default tolerance.
     *
     * @see equalsApprox
     */
    override fun equals(other: Any?): Boolean {
        if (other is DenseMatrix) return equalsApprox(other)
        return super.equals(other)
    }
}

/**
 * Performs matrix multiplication ($C = A \times B$) using the native library (e.g., Eigen).
 *
 * The resulting [DenseMatrix] is a new instance that wraps the memory
 * allocated by the native library. This new instance "owns" the native resource
 * and is responsible for freeing it via its [AutoCloseable.close] method.
 *
 * @param left The left-hand side matrix (A).
 * @param right The right-hand side matrix (B).
 * @return A new [DenseMatrix] (C) containing the result of the multiplication.
 * @throws IllegalArgumentException if the inner dimensions do not match
 * (i.e., `left.cols != right.rows`).
 */
@OptIn(ExperimentalForeignApi::class)
fun mul(left: DenseMatrix, right: DenseMatrix): DenseMatrix {
    if (left.cols != right.rows) {
        throw IllegalArgumentException("Incompatible dimensions: left.cols=${left.cols}, right.rows=${right.rows}")
    }

    // We assume that the pointer will never be null, since all the parameters are
    // passed down correctly. The only edge case which might happen is the computer
    // runs out of memory, which is less probable.
    val nativeHandle = bindingsMul(
        left.rows, left.cols, left.dataPtr,
        right.rows, right.cols, right.dataPtr
    )!!

    val rows = nativeHandle.pointed.rows
    val cols = nativeHandle.pointed.cols
    val dataPtr = nativeHandle.pointed.data!!
    // Automatic cleanup registered by DenseMatrix constructor
    return DenseMatrix(rows, cols, dataPtr, nativeHandle)
}

/**
 * Compares two matrices for approximate equality using the native library's
 * `isApprox` functionality (tolerance-aware).
 *
 * This is the preferred method for comparing floating-point matrices.
 *
 * @receiver The first matrix.
 * @param other The second matrix to compare against.
 * @param tolerance The maximum absolute difference allowed between corresponding elements.
 * @return `true` if the matrices have the same dimensions and all corresponding
 * elements are within the specified [tolerance], `false` otherwise.
 */
@OptIn(ExperimentalForeignApi::class)
fun DenseMatrix.equalsApprox(other: DenseMatrix, tolerance: Double = 1e-6): Boolean {
    return bindingsEq(
        this.rows, this.cols, this.dataPtr,
        other.rows, other.cols, other.dataPtr,
        tolerance
    ) != 0
}