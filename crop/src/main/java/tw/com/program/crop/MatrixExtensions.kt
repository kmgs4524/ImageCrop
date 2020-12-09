package tw.com.program.crop

import android.graphics.Matrix
import androidx.annotation.IntRange
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 縮放倍率
 */
fun Matrix.getScale(): Float {
    return sqrt(
        this.getValueFromField(Matrix.MSCALE_X).toDouble().pow(2.0) +
            this.getValueFromField(Matrix.MSKEW_Y).toDouble().pow(2.0)
    ).toFloat()
}

/**
 * x 軸位移，從左上開始算起
 */
fun Matrix.getTranslateX(): Float = this.getValueFromField(Matrix.MTRANS_X)

/**
 * y 軸位移，從左上開始算起
 */
fun Matrix.getTranslateY(): Float = this.getValueFromField(Matrix.MTRANS_Y)

fun Matrix.getValueFromField(
    @IntRange(from = 0, to = MATRIX_VALUES_SIZE) valueIndex: Int
): Float {
    val matrixValue = FloatArray(MATRIX_VALUES_SIZE.toInt())
    this.getValues(matrixValue)
    return matrixValue[valueIndex]
}

private const val MATRIX_VALUES_SIZE = 9L