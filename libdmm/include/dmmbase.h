#pragma once
#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

// Handle struct that carries both data pointer and dimensions.
typedef struct MatrixHandle {
    void* matrixPtr;     // actual Eigen::Matrix* pointer
    double* data;        // pointer to raw contiguous data (row-major)
    int rows;
    int cols;
} MatrixHandle;

// Allocates a new matrix = left * right. Must be freed with free_matrix().
MatrixHandle* mul(
    int lRows, int lCols, const double* leftPtr,
    int rRows, int rCols, const double* rightPtr
);

// Compares two matrices element-wise using Eigen::isApprox().
int eq(
    int lRows, int lCols, const double* leftPtr,
    int rRows, int rCols, const double* rightPtr,
    double errorTolerance
);

// Frees a MatrixHandle and its underlying Eigen object.
void free_matrix(MatrixHandle* handle);

#ifdef __cplusplus
}
#endif
