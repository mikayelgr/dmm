package com.mikayel.grigoryan;

import org.junit.jupiter.api.Test;

public class BindingsTests {
    @Test
    public void testMul() {
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
