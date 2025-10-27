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
     * Memory is automatically managed by the DenseMatrix class.
     */
    private fun createMatrix(rows: Int, cols: Int, values: DoubleArray): DenseMatrix {
        return DenseMatrix.fromArray(rows, cols, values)
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

    /**
     * Tests that creating a matrix with invalid array size throws MatrixOperationException.
     */
    @Test
    fun testFromArray_invalidArraySize() {
        val values = doubleArrayOf(1.0, 2.0, 3.0) // 3 elements
        val exception = assertFailsWith<MatrixOperationException> {
            DenseMatrix.fromArray(2, 2, values) // expects 4 elements
        }
        assertTrue(exception.message?.contains("Array size mismatch") == true)
    }

    /**
     * Tests that creating a matrix with non-positive dimensions throws MatrixOperationException.
     */
    @Test
    fun testFromArray_invalidDimensions() {
        val values = doubleArrayOf(1.0, 2.0, 3.0, 4.0)

        // Test zero rows
        assertFailsWith<MatrixOperationException> {
            DenseMatrix.fromArray(0, 4, values)
        }

        // Test negative rows
        assertFailsWith<MatrixOperationException> {
            DenseMatrix.fromArray(-1, 4, values)
        }

        // Test zero cols
        assertFailsWith<MatrixOperationException> {
            DenseMatrix.fromArray(4, 0, values)
        }

        // Test negative cols
        assertFailsWith<MatrixOperationException> {
            DenseMatrix.fromArray(4, -1, values)
        }
    }

    /**
     * Tests the zeros factory method creates a matrix filled with zeros.
     */
    @Test
    fun testZeros() {
        val zero = DenseMatrix.zeros(3, 2)
        assertEquals(3, zero.rows)
        assertEquals(2, zero.cols)
        for (r in 0 until zero.rows) {
            for (c in 0 until zero.cols) {
                assertEquals(0.0, zero[r, c], 1e-9)
            }
        }
    }

    /**
     * Tests the ones factory method creates a matrix filled with ones.
     */
    @Test
    fun testOnes() {
        val ones = DenseMatrix.ones(2, 3)
        assertEquals(2, ones.rows)
        assertEquals(3, ones.cols)
        for (r in 0 until ones.rows) {
            for (c in 0 until ones.cols) {
                assertEquals(1.0, ones[r, c], 1e-9)
            }
        }
    }

    /**
     * Tests the identity factory method creates an identity matrix.
     */
    @Test
    fun testIdentity() {
        val id = DenseMatrix.identity(3)
        assertEquals(3, id.rows)
        assertEquals(3, id.cols)
        for (r in 0 until id.rows) {
            for (c in 0 until id.cols) {
                val expected = if (r == c) 1.0 else 0.0
                assertEquals(expected, id[r, c], 1e-9, "Identity matrix should have 1s on diagonal and 0s elsewhere")
            }
        }
    }

    /**
     * Tests that accessing out of bounds indices throws MatrixOperationException.
     */
    @Test
    fun testGet_outOfBounds() {
        val matrix = DenseMatrix.fromArray(2, 2, doubleArrayOf(1.0, 2.0, 3.0, 4.0))

        // Test negative row
        assertFailsWith<MatrixOperationException> {
            matrix[-1, 0]
        }

        // Test negative col
        assertFailsWith<MatrixOperationException> {
            matrix[0, -1]
        }

        // Test row out of bounds
        assertFailsWith<MatrixOperationException> {
            matrix[2, 0]
        }

        // Test col out of bounds
        assertFailsWith<MatrixOperationException> {
            matrix[0, 2]
        }
    }

    /**
     * Tests that setting out of bounds indices throws MatrixOperationException.
     */
    @Test
    fun testSet_outOfBounds() {
        val matrix = DenseMatrix.fromArray(2, 2, doubleArrayOf(1.0, 2.0, 3.0, 4.0))

        // Test negative row
        assertFailsWith<MatrixOperationException> {
            matrix[-1, 0] = 99.0
        }

        // Test row out of bounds
        assertFailsWith<MatrixOperationException> {
            matrix[2, 0] = 99.0
        }
    }

    /**
     * Tests that the matrix can be modified after creation.
     */
    @Test
    fun testSet_modifyMatrix() {
        val matrix = DenseMatrix.fromArray(2, 2, doubleArrayOf(1.0, 2.0, 3.0, 4.0))
        matrix[0, 0] = 99.0
        assertEquals(99.0, matrix[0, 0], 1e-9)
        assertEquals(2.0, matrix[0, 1], 1e-9) // other elements unchanged
    }

    /**
     * Tests that close() properly releases resources.
     */
    @Test
    fun testClose_releasesResources() {
        val matrix = DenseMatrix.fromArray(2, 2, doubleArrayOf(1.0, 2.0, 3.0, 4.0))
        matrix.close() // Should not throw
    }

    /**
     * Tests using the matrix with use {} block for automatic cleanup.
     */
    @Test
    fun testUseBlock() {
        val result = DenseMatrix.fromArray(2, 2, doubleArrayOf(1.0, 2.0, 3.0, 4.0)).use { matrix ->
            matrix[0, 0] + matrix[1, 1]
        }
        assertEquals(5.0, result, 1e-9)
    }
}
