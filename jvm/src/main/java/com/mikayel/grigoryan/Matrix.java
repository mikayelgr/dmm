package com.mikayel.grigoryan;

/**
 * Represents a mathematical matrix of fixed dimensions with data stored in a
 * flat (1D) array for efficient memory access and compatibility with native
 * code via JNI. This design avoids the complexity of nested Java arrays while
 * allowing direct mapping to contiguous memory structures (e.g., Eigen matrices
 * in C++).
 *
 * <p>The matrix is row-major, meaning that elements are stored row by row in
 * the underlying {@code double[]} array. The element at position (row, col)
 * is located at index {@code row * cols + col}.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Matrix m = new Matrix(2, 3);
 * m.setData(0, 0, 1.0);
 * m.setData(0, 1, 2.0);
 * m.setData(1, 2, 3.0);
 * }</pre>
 *
 * <p>This class performs bounds checking on all index operations and throws
 * an {@link IllegalArgumentException} if an invalid index is accessed.</p>
 */
public class Matrix {
    /**
     * The number of rows a given matrix is going to have.
     */
    private final int rows;
    /**
     * The number of columns a given matrix is going to have.
     */
    private final int cols;
    /**
     * The data kept in a flat format for easy access and representation via JNI.
     */
    private final double[] data;

    /**
     * Constructs a new {@code Matrix} with the specified number of rows and columns.
     * <p>
     * This constructor allocates a flat {@code double[]} array of size
     * {@code rows * cols} to store the matrix data in row-major order. The
     * dimensions are validated to ensure they are non-negative before allocation.
     * </p>
     *
     * @param rows the number of rows in the matrix
     * @param cols the number of columns in the matrix
     * @throws IllegalArgumentException if either {@code rows} or {@code cols} is negative
     */
    public Matrix(int rows, int cols) throws IllegalArgumentException {
        // Making sure that the rows and columns are non-negative
        throwOnNegativeRowColError(rows, cols);

        this.rows = rows;
        this.cols = cols;

        // This way, we avoid the complexity of passing 2D array data to the JNI, which
        // in turn helps us with indexing from the C++ side.
        this.data = new double[rows * cols];
    }

    /**
     * Constructs a new {@code Matrix} from a two-dimensional {@code double[][]} array.
     * <p>
     * The input array is interpreted in row-major order, where each inner array
     * represents a row of the matrix. All rows must have the same number of columns.
     * Internally, the data is flattened into a single contiguous {@code double[]}
     * for efficient memory access and JNI compatibility.
     * </p>
     *
     * @param data the 2D array representing the matrix values
     * @throws IllegalArgumentException if {@code data} is {@code null}, empty,
     *                                  or if the rows have inconsistent lengths
     */
    public Matrix(double[][] data) throws IllegalArgumentException {
        this(data.length, data[0].length);
        int cRow = 0; // current row
        for (var row : data) {
            setRow(cRow, row);
            cRow++;
        }
    }

    /**
     * Returns the underlying flat data array that stores the elements of this matrix.
     * <p>
     * The returned array is in <strong>row-major</strong> order, meaning that
     * consecutive elements of the same row are stored next to each other in memory.
     * Modifying this array directly will affect the matrix contents.
     * </p>
     *
     * @return the internal {@code double[]} representing the matrix data
     */
    public double[] getData() {
        return this.data;
    }

    /**
     * Retrieves the element at the specified row and column in the matrix.
     *
     * @param row the zero-based row index
     * @param col the zero-based column index
     * @return the value stored at position ({@code row}, {@code col})
     * @throws IndexOutOfBoundsException if the specified indices are outside the matrix dimensions
     */
    public double get(int row, int col) {
        return data[row * cols + col];
    }

    /**
     * Sets the element at the specified row and column in the matrix to the given value.
     *
     * @param row   the zero-based row index
     * @param col   the zero-based column index
     * @param value the value to assign to the element at position ({@code row}, {@code col})
     * @throws IndexOutOfBoundsException if the specified indices are outside the matrix dimensions
     */
    public void set(int row, int col, double value) {
        data[row * cols + col] = value;
    }

    /**
     * This is an internal-use method only, used for wrapping the computed matrix
     * returned by Eigen from C++ in a Java-friendly API. This method assumes that
     * the matrix is fully valid and contains valid data.
     *
     * @param rows Number of rows (from C++)
     * @param cols Number of columns (from C++)
     * @param data Actual double[][] (from C++)
     */
    private Matrix(int rows, int cols, double[] data) {
        this.rows = rows;
        this.cols = cols;
        this.data = data;
    }

    /**
     * Validates that the specified row and column indices are non-negative.
     * <p>
     * This method ensures that both {@code row} and {@code col} are zero or greater.
     * It does not perform any upper-bound checks; it is intended to be used as an
     * early guard against invalid negative indices before additional validation.
     * </p>
     *
     * @param row the row index to validate (must be {@code >= 0})
     * @param col the column index to validate (must be {@code >= 0})
     * @throws IllegalArgumentException if {@code row} or {@code col} is negative
     */
    private void throwOnNegativeRowColError(int row, int col) throws IllegalArgumentException {
        if (row < 0 || col < 0) {
            throw new IllegalArgumentException("Cannot have negative number of rows or columns");
        }
    }

    /**
     * Validates that the provided row has the correct number of columns
     * expected by this matrix.
     * <p>
     * This method ensures that the length of the given {@code row} array
     * matches the predefined number of columns in the matrix. It is typically
     * used when constructing or modifying a matrix from external data to
     * guarantee structural consistency.
     * </p>
     *
     * @param row the row array to validate
     * @throws IllegalArgumentException if the length of {@code row} does not
     *                                  equal {@code this.cols}
     */
    private void throwOnMismatchingNumberOfColsError(double[] row) {
        if (row.length != this.cols)
            throw new IllegalArgumentException("Number of elements in the columns isn't equal to the predefined number of columns %d!=%d".formatted(data.length, this.cols));
    }

    /**
     * A safe wrapper function implemented on top of {@link com.mikayel.grigoryan.Bindings} with Java-native
     * exceptions and error-handling.
     *
     * @param left  The left-hand-side matrix
     * @param right The right-hand-side matrix
     * @return The computed matrix
     * @throws IllegalArgumentException If any of the `left` or `right` matrices are null
     */
    public static Matrix mul(Matrix left, Matrix right)
            throws IllegalArgumentException {
        if (left == null) throw new IllegalArgumentException("left == null");
        if (right == null) throw new IllegalArgumentException("right == null");
        if (left.cols != right.rows)
            throw new IllegalArgumentException("Number of left columns does not match number of right rows");

        // Computing the final result using the bindings
        double[] computed = Bindings.mul(
                left.rows, left.cols, left.data,
                right.rows, right.cols, right.data
        );

        return new Matrix(left.cols, right.cols, computed);
    }

    /**
     * Multiplies this matrix by the specified {@code right} matrix and returns the result.
     * <p>
     * This is a convenience instance method that delegates to the static
     * {@link Matrix#mul(Matrix, Matrix)} method, which performs the actual
     * multiplication.
     * </p>
     *
     * @param right the matrix to multiply with this matrix (the right-hand operand)
     * @return a new {@code Matrix} representing the product {@code this * right}
     * @throws IllegalArgumentException if the matrices have incompatible dimensions
     *                                  (i.e., {@code this.cols != right.rows})
     */
    public Matrix mul(Matrix right) throws IllegalArgumentException {
        return Matrix.mul(this, right);
    }

    /**
     * Assigns a complete row of values to the specified row index within this matrix.
     * <p>
     * This method validates that the given {@code data} array has the correct
     * number of elements (equal to {@code this.cols}) and that the row index
     * is non-negative. The data is then efficiently copied into the underlying
     * flat storage array in row-major order.
     * </p>
     *
     * @param row  the zero-based row index at which to set the data
     * @param data the array of values representing a single row
     * @throws IllegalArgumentException if {@code row} is negative or
     *                                  if {@code data.length != this.cols}
     */
    private void setRow(int row, double[] data) throws IllegalArgumentException {
        throwOnNegativeRowColError(row, this.cols);
        throwOnMismatchingNumberOfColsError(data);
        System.arraycopy(data, 0, this.data, row * this.cols, this.cols);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.cols; i++) {
            for (int j = 0; j < this.rows; j++) sb.append("%f\t".formatted(this.data[i * this.rows + j]));
            sb.append("\n");
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object m) {
        if (m instanceof Matrix r) {
            return Bindings.eq(this.rows, this.cols, this.data, r.rows, r.cols, r.data);
        }

        return false;
    }
}
