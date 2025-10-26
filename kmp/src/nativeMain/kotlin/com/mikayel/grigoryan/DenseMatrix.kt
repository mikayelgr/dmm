package com.mikayel.grigoryan

import kotlinx.cinterop.*
import com.mikayel.grigoryan.libdmmbase.cinterop.eq as bindingsEq
import com.mikayel.grigoryan.libdmmbase.cinterop.mul as bindingsMul
import com.mikayel.grigoryan.libdmmbase.cinterop.free_matrix as bindingsFreeMatrix
import com.mikayel.grigoryan.libdmmbase.cinterop.MatrixHandle as BindingsMatrixHandle

@Suppress("EqualsOrHashCode")
@OptIn(ExperimentalForeignApi::class)
class DenseMatrix internal constructor(
    val rows: Int,
    val cols: Int,
    internal val dataPtr: CPointer<DoubleVar>,
    private val nativeHandle: CPointer<BindingsMatrixHandle>? = null
) : AutoCloseable {
    /** Read element (row, col) directly from native memory. */
    operator fun get(row: Int, col: Int): Double {
        require(row in 0 until this.rows && col in 0 until this.cols)
        return this.dataPtr[row * this.cols + col]
    }

    /** Write element (row, col) directly to native memory. */
    operator fun set(row: Int, col: Int, value: Double) {
        require(row in 0 until rows && col in 0 until cols)
        this.dataPtr[row * this.cols + col] = value
    }

    /** Optional deterministic cleanup (e.g., inside loops or benchmarks). */
    override fun close() {
        this.nativeHandle?.let {
            bindingsFreeMatrix(it)
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for (i in 0 until this.rows) {
            for (j in 0 until this.cols) sb.append("${get(i, j)}\t")
            sb.appendLine()
        }
        return sb.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (other is DenseMatrix) return equalsApprox(other)
        return super.equals(other)
    }
}

/** Performs matrix multiplication via native Eigen, zero-copy result. */
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

/** Compares two matrices using Eigen::isApprox (tolerance-aware). */
@OptIn(ExperimentalForeignApi::class)
fun DenseMatrix.equalsApprox(other: DenseMatrix, tolerance: Double = 1e-6): Boolean {
    return bindingsEq(
        this.rows, this.cols, this.dataPtr,
        other.rows, other.cols, other.dataPtr,
        tolerance
    ) != 0
}
