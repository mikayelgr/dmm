package com.mikayel.grigoryan

import kotlinx.cinterop.*
import com.mikayel.grigoryan.libdmmbase.cinterop.*
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.Cleaner

@OptIn(ExperimentalForeignApi::class)
class DenseMatrix internal constructor(
    val rows: Int,
    val cols: Int,
    internal val dataPtr: CPointer<DoubleVar>,
    private val nativeHandle: CPointer<MatrixHandle>? = null
) {
    /** Read element (row, col) directly from native memory. */
    operator fun get(row: Int, col: Int): Double {
        require(row in 0 until rows && col in 0 until cols)
        return dataPtr[row * cols + col]
    }

    /** Write element (row, col) directly to native memory. */
    operator fun set(row: Int, col: Int, value: Double) {
        require(row in 0 until rows && col in 0 until cols)
        dataPtr[row * cols + col] = value
    }

    // Cleaner that frees the matrix when the object is collected
    @OptIn(ExperimentalNativeApi::class)
    private val cleaner: Cleaner? = nativeHandle?.let { handle ->
        Cleaner.create(this) {
            // Will be executed once the DenseMatrix is unreachable
            free_matrix(handle)
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for (i in 0 until rows) {
            for (j in 0 until cols) sb.append(get(i, j).toString() + "\t")
            sb.appendLine()
        }
        return sb.toString()
    }
}

/** Performs matrix multiplication via native Eigen, zero-copy result. */
@OptIn(ExperimentalForeignApi::class)
fun mul(left: DenseMatrix, right: DenseMatrix): DenseMatrix {
    require(left.cols == right.rows) {
        "Incompatible dimensions: left.cols=${left.cols}, right.rows=${right.rows}"
    }

    val handlePtr = mul(
        left.rows, left.cols, left.dataPtr,
        right.rows, right.cols, right.dataPtr
    ) ?: error("Native returned null pointer")

    val handle = handlePtr.pointed
    val dataPtr = handle.data ?: error("Handle contained null data pointer")
    val rows = handle.rows
    val cols = handle.cols

    // Automatic cleanup registered by DenseMatrix constructor
    return DenseMatrix(rows, cols, dataPtr, handlePtr)
}


/** Compares two matrices using Eigen::isApprox (tolerance-aware). */
@OptIn(ExperimentalForeignApi::class)
fun DenseMatrix.equalsApprox(other: DenseMatrix, tolerance: Double = 1e-6): Boolean {
    return eq(
        rows, cols, dataPtr,
        other.rows, other.cols, other.dataPtr,
        tolerance
    ) != 0
}
