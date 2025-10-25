package com.mikayel.grigoryan

fun mul(left: Array<Array<Double>>, right: Array<Array<Double>>): Array<Array<Double>> {
    val lDims = getMatrixDimensions(left)
    val rDims = getMatrixDimensions(right)
    validateMatrices(left, lDims, right, rDims)

    val row1 = lDims[0]
    val col1 = lDims[1]
    val col2 = rDims[1]
    val product = Array(row1) { Array(col2) { 0.0 } }

    for (i in 0 until row1) {
        for (j in 0 until col2) {
            for (k in 0 until col1) {
                product[i][j] += left[i][k] * right[k][j]
            }
        }
    }

    return product
}

private fun validateMatrices(
    left: Array<Array<Double>>, lDims: Array<Int>,
    right: Array<Array<Double>>, rDims: Array<Int>,
) {
    assert(validateMatrixCols(lDims, left)) { "Left matrix is inconsistent" }
    assert(validateMatrixCols(rDims, right)) { "Right matrix is inconsistent" }
    assert(lDims[1] == rDims[0]) { "Number of columns of the left matrix != to number of rows on right matrix" }
}

private fun getMatrixDimensions(matrix: Array<Array<Double>>): Array<Int> {
    val nRows = matrix.size
    // Assuming the matrix is set to its first column's size
    val nCols = matrix.getOrNull(nRows)?.size ?: 0
    return arrayOf(nRows, nCols)
}

private fun validateMatrixCols(dims: Array<Int>, matrix: Array<Array<Double>>): Boolean {
    for (i in 0 until dims[0]) {
        if (matrix[i].size != dims[1]) {
            return false
        }
    }

    return true
}