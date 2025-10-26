#include "dmmbase.h"
#include <Eigen/Dense>
#include <stdexcept>

extern "C"
{

    MatrixHandle *mul(
        int lRows, int lCols, const double *leftPtr,
        int rRows, int rCols, const double *rightPtr)
    {
        try
        {
            using Mat = Eigen::Matrix<double, Eigen::Dynamic, Eigen::Dynamic, Eigen::RowMajor>;
            Eigen::Map<const Mat> left(leftPtr, lRows, lCols);
            Eigen::Map<const Mat> right(rightPtr, rRows, rCols);

            auto *mat = new Mat(left * right);
            auto *handle = new MatrixHandle{mat, mat->data(), lRows, rCols};
            return handle;
        }
        catch (...)
        {
            return nullptr;
        }

        return nullptr; // <-- satisfies compiler
    }

    int eq(
        int lRows, int lCols, const double *leftPtr,
        int rRows, int rCols, const double *rightPtr,
        double errorTolerance)
    {
        using Mat = Eigen::Matrix<double, Eigen::Dynamic, Eigen::Dynamic, Eigen::RowMajor>;
        if (lRows != rRows || lCols != rCols)
            return 0;
        Eigen::Map<const Mat> left(leftPtr, lRows, lCols);
        Eigen::Map<const Mat> right(rightPtr, rRows, rCols);
        return left.isApprox(right, errorTolerance) ? 1 : 0;
    }

    void free_matrix(MatrixHandle *handle)
    {
        if (!handle)
            return;
        using Mat = Eigen::Matrix<double, Eigen::Dynamic, Eigen::Dynamic, Eigen::RowMajor>;
        auto *mat = reinterpret_cast<Mat *>(handle->matrixPtr);
        delete mat;
        delete handle;
    }

} // extern "C"
