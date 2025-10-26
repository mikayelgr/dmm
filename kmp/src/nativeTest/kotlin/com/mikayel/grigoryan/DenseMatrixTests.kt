package com.mikayel.grigoryan

import kotlinx.cinterop.*
import kotlin.native.runtime.GC
import kotlin.test.*
import kotlin.native.runtime.NativeRuntimeApi

/**
 * Unit tests for verifying DenseMatrix core behavior:
 * - Multiplication correctness
 * - Approximate equality logic
 * - Automatic memory cleanup
 * - Edge case handling (identity, zero, incompatible dimensions)
 */
@OptIn(ExperimentalForeignApi::class)
class DenseMatrixTests {
    /**
     * Helper to create a DenseMatrix from a DoubleArray.
     * Pins memory so native interop can access it directly.
     */
    private fun createMatrix(rows: Int, cols: Int, values: DoubleArray): DenseMatrix {
        require(values.size == rows * cols)
        val pinned = values.pin()
        return DenseMatrix(rows, cols, pinned.addressOf(0))
    }

    /**
     * Tests multiplication between a 2x3 and a 3x2 matrix.
     */
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
        assertEquals(2, result.rows)
        assertEquals(2, result.cols)
        assertEquals(58.0, result[0, 0], 1e-9)
        assertEquals(64.0, result[0, 1], 1e-9)
        assertEquals(139.0, result[1, 0], 1e-9)
        assertEquals(154.0, result[1, 1], 1e-9)
    }

    /**
     * Tests multiplication by an identity matrix.
     */
    @Test
    fun testMatrixMultiplication_identity() {
        val aData = doubleArrayOf(1.0, 2.0, 3.0, 4.0)
        val idData = doubleArrayOf(1.0, 0.0, 0.0, 1.0)
        val a = createMatrix(2, 2, aData)
        val id = createMatrix(2, 2, idData)

        val result = mul(a, id)
        assertTrue(a.equalsApprox(result, 1e-9), "Multiplying by identity should return same matrix")
    }

    /**
     * Tests that multiplying by a zero matrix yields all zeros.
     */
    @Test
    fun testMatrixMultiplication_zeroMatrix() {
        val aData = doubleArrayOf(1.0, 2.0, 3.0, 4.0)
        val zeroData = doubleArrayOf(0.0, 0.0, 0.0, 0.0)
        val a = createMatrix(2, 2, aData)
        val zero = createMatrix(2, 2, zeroData)

        val result = mul(a, zero)
        for (r in 0 until result.rows)
            for (c in 0 until result.cols)
                assertEquals(0.0, result[r, c], 1e-9, "Result should be zero matrix")
    }

    /**
     * Ensures equalsApprox correctly detects near-equality.
     */
    @Test
    fun testEqualsApprox_sameMatrix() {
        val aData = doubleArrayOf(1.0, 2.0, 3.0, 4.0)
        val bData = doubleArrayOf(1.0000001, 2.0000001, 3.0, 4.0)

        val a = createMatrix(2, 2, aData)
        val b = createMatrix(2, 2, bData)
        val isEqual = a.equalsApprox(b, tolerance = 1e-5)
        assertTrue(isEqual, "Matrices should be approximately equal")
    }

    /**
     * Ensures equalsApprox correctly rejects distinct matrices.
     */
    @Test
    fun testEqualsApprox_differentMatrix() {
        val aData = doubleArrayOf(1.0, 2.0, 3.0, 4.0)
        val bData = doubleArrayOf(1.0, 2.0, 3.0, 5.0)

        val a = createMatrix(2, 2, aData)
        val b = createMatrix(2, 2, bData)
        val isEqual = a.equalsApprox(b, tolerance = 1e-6)
        assertFalse(isEqual, "Matrices should not be approximately equal")
    }

    /**
     * Tests that multiplication with incompatible dimensions throws an exception.
     */
    @Test
    fun testMatrixMultiplication_incompatibleDimensions() {
        val a = createMatrix(2, 3, doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0))
        val b = createMatrix(4, 2, DoubleArray(8) { 1.0 })
        assertFailsWith<MatrixOperationException> {
            mul(a, b)
        }
    }

    /**
     * Tests automatic native resource cleanup (relies on native free_matrix()).
     */
    @OptIn(NativeRuntimeApi::class)
    @Test
    fun testAutoCleanup() {
        val aData = doubleArrayOf(1.0, 0.0, 0.0, 1.0)
        val bData = doubleArrayOf(2.0, 3.0, 4.0, 5.0)
        val a = createMatrix(2, 2, aData)
        val b = createMatrix(2, 2, bData)
        val result = mul(a, b)
        println("Result (for cleaner test):")
        println(result)

        // Triggering garbage collection for 100 times
        for (r in 0 until 100) {
            GC.collect() // trigger GC for manual cleanup
        }

        assertEquals(a.nativeHandlePtr, null)
        assertEquals(b.nativeHandlePtr, null)
    }
}
