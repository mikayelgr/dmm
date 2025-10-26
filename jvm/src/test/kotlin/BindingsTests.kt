import com.mikayel.grigoryan.DenseMatrix
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class BindingsTestsKt {
    @Test
    fun testMulInvalidDimensionsShouldThrow() {
        // Since Kotlin's test library provides this nice function, it made more
        // sense to write it using Kotlin
        assertThrows<IllegalArgumentException> {
            val left = DenseMatrix(
                arrayOf(
                    doubleArrayOf(1.0, 2.0, 3.0),
                    doubleArrayOf(1.0, 2.0, 3.0),
                    doubleArrayOf(1.0, 2.0, 3.0),
                )
            )
            val right = DenseMatrix(arrayOf())
            left.mul(right)
        }

        assertThrows<IllegalArgumentException> {
            val left = DenseMatrix(
                arrayOf(
                    doubleArrayOf(1.0, 2.0, 3.0),
                    doubleArrayOf(4.0, 5.0, 6.0)
                )
            )

            val right = DenseMatrix(
                arrayOf(
                    doubleArrayOf(1.0, 2.0, 3.0, 4.0),
                    doubleArrayOf(5.0, 6.0, 7.0, 8.0),
                    doubleArrayOf(9.0, 10.0, 11.0, 12.0),
                    doubleArrayOf(13.0, 14.0, 15.0, 16.0)
                )
            )

            left.mul(right)
        }
    }
}