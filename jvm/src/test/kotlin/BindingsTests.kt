import com.mikayel.grigoryan.DenseMatrix
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class BindingsTestsKt {
    @Test
    fun testInvalidMul() {
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
    }
}