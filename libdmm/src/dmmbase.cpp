#include "dmmbase.h"
#include <Eigen/Dense>
#include <stdexcept>

extern "C"
{
    // Define a type alias for a row-major matrix using the Eigen library.
    using Mat = Eigen::Matrix<double, Eigen::Dynamic, Eigen::Dynamic, Eigen::RowMajor>;

    /**
     * A simple implementation of matrix multiplication using the Eigen library. The function
     * expects two row-major matrices as input as well as non-negative row and column counts.
     * Validation is not performed on the input parameters and must be done by the caller.
     */
    MatrixHandle *mul(
        int lRows, int lCols, const double *leftPtr,
        int rRows, int rCols, const double *rightPtr)
    {
        try
        {
            Eigen::Map<const Mat> left(leftPtr, lRows, lCols);
            Eigen::Map<const Mat> right(rightPtr, rRows, rCols);

            auto *computed = new Mat(left * right);
            // Return a handle to the computed matrix so that the caller can manage its lifetime.
            // In this case, we are going to manage it with Kotlin Native.
            auto *handle = new MatrixHandle{computed, computed->data(), lRows, rCols};
            return handle;
        }
        catch (...)
        {
            return nullptr;
        }

        return nullptr; // <-- satisfies compiler
    }

    /**
     * A simple implementation of matrix equality check using the Eigen library. The function
     * expects two row-major matrices as input as well as non-negative row and column counts,
     * and an error tolerance factor.
     *
     * Validation is not performed on the input parameters and must be done by the caller.
     */
    int eq(
        int lRows, int lCols, const double *leftPtr,
        int rRows, int rCols, const double *rightPtr,
        double errorTolerance)
    {
        if (lRows != rRows || lCols != rCols)
            return 0;
        Eigen::Map<const Mat> left(leftPtr, lRows, lCols);
        Eigen::Map<const Mat> right(rightPtr, rRows, rCols);
        return left.isApprox(right, errorTolerance) ? 1 : 0;
    }

    /**
     * Frees the memory allocated for the computed matrix handle created by the Eigen
     * library in the `mul` function.
     */
    void free_matrix(MatrixHandle *handle)
    {
        if (!handle)
        {
            return;
        }

        auto *mat = reinterpret_cast<Mat *>(handle->matrixPtr);
        delete mat;    // Free the Eigen matrix.
        delete handle; // Free the handle itself.
    }

} // extern "C"
