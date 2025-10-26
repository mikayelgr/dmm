package com.mikayel.grigoryan

import kotlinx.cinterop.*
import kotlin.test.*
import kotlin.native.runtime.GC
import kotlin.native.runtime.NativeRuntimeApi

@OptIn(ExperimentalForeignApi::class)
class DenseMatrixTests {
    private fun createMatrix(rows: Int, cols: Int, values: DoubleArray): DenseMatrix {
        require(values.size == rows * cols)
        val pinned = values.pin()
        return DenseMatrix(rows, cols, pinned.addressOf(0))
    }

    @Test
    fun testMatrixMultiplication_basic() {
        // Matrix A (2x3)
        val aData = doubleArrayOf(
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0
        )
        // Matrix B (3x2)
        val bData = doubleArrayOf(
            7.0, 8.0,
            9.0, 10.0,
            11.0, 12.0
        )

        val left = createMatrix(2, 3, aData)
        val right = createMatrix(3, 2, bData)

        val result = mul(left, right)

        // Expected result (2x2)
        // [[58, 64],
        //  [139, 154]]
        assertEquals(2, result.rows)
        assertEquals(2, result.cols)

        assertEquals(58.0, result[0, 0], 1e-9)
        assertEquals(64.0, result[0, 1], 1e-9)
        assertEquals(139.0, result[1, 0], 1e-9)
        assertEquals(154.0, result[1, 1], 1e-9)

        println("✅ Multiplication result:")
        println(result)
    }

    @Test
    fun testEqualsApprox_sameMatrix() {
        val aData = doubleArrayOf(1.0, 2.0, 3.0, 4.0)
        val bData = doubleArrayOf(1.0000001, 2.0000001, 3.0, 4.0)

        val a = createMatrix(2, 2, aData)
        val b = createMatrix(2, 2, bData)

        val isEqual = a.equalsApprox(b, tolerance = 1e-5)
        assertTrue(isEqual, "Matrices should be approximately equal")
    }

    @Test
    fun testEqualsApprox_differentMatrix() {
        val aData = doubleArrayOf(1.0, 2.0, 3.0, 4.0)
        val bData = doubleArrayOf(1.0, 2.0, 3.0, 5.0)

        val a = createMatrix(2, 2, aData)
        val b = createMatrix(2, 2, bData)

        val isEqual = a.equalsApprox(b, tolerance = 1e-6)
        assertFalse(isEqual, "Matrices should not be approximately equal")
    }

    @OptIn(NativeRuntimeApi::class)
    @Test
    fun testAutoCleanup() {
        // You can verify automatic freeing by logging inside free_matrix() in C++
        val aData = doubleArrayOf(1.0, 0.0, 0.0, 1.0)
        val bData = doubleArrayOf(2.0, 3.0, 4.0, 5.0)

        val a = createMatrix(2, 2, aData)
        val b = createMatrix(2, 2, bData)

        val result = mul(a, b)
        println("Result (for cleaner test):")
        println(result)
    }
}
