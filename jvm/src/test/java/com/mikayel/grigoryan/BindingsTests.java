package com.mikayel.grigoryan;

import org.junit.jupiter.api.Test;

public class BindingsTests {
    @Test
    // Multiplication checks for square matrices
    public void testMulSquare() {
        var left = new DenseMatrix(new double[][]{
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}});
        var right = new DenseMatrix(new double[][]{
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}});
        var expected = new DenseMatrix(new double[][]{
                {30, 36, 42},
                {66, 81, 96},
                {102, 126, 150}
        });
        assert left.mul(right).equals(expected);
    }

    @Test
    // Multiplication checks for arbitrary MxN matrices
    public void testMulArbitrary() {
        var left = new DenseMatrix(new double[][]{
                {1, 2, 3, 4},
                {6, 7, 8, 9},
        });
        var right = new DenseMatrix(new double[][]{
                {12, 34},
                {12, 34},
                {12, 34},
                {12, 34},
        });
        var expected = new DenseMatrix(new double[][]{
                {120.0, 340.0},
                {360.0, 1020.0}
        });
        assert left.mul(right).equals(expected);
    }

    @Test
    // Equivalence checks
    public void testEq() {
        var left = new DenseMatrix(new double[][]{
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}});
        var right = new DenseMatrix(new double[][]{
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}});

        assert left.equals(right);
    }

    @Test
    // When the error is too precise, the equivalence must fail
    public void testEqWithTolerance() {
        var left = new DenseMatrix(new double[][]{{1}});
        left.setErrorTolerance(10e-4);
        var right = new DenseMatrix(new double[][]{{1.002}});
        assert !left.equals(right);
    }

    @Test
    // Non-equivalence checks
    public void testNotEq() {
        var left = new DenseMatrix(new double[][]{
                {2, 2, 3},
                {4, 5, 6},
                {7, 8, 9}});
        var right = new DenseMatrix(new double[][]{
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}});

        assert !left.equals(right);
    }
}
