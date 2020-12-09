package tw.com.program.crop

import android.graphics.Matrix

/**
 * 當圖片旋轉後代表左上角起始點的 transX, transY 會跟著改變，
 * 需以各旋轉角度計算出圖片正確的四個邊界
 *  degree = 0      degree = 90
 * (0,0)                   (100, 0)
 * x------           |---------x
 * |     |           |         |
 * |     |           |---------|
 * |     |
 * -------
 */
class MarginCalculator(
    private val currentImageMatrix: Matrix
) {

    fun calculateLeft(
        rotateDegree: Int,
        imageWidth: Int,
        imageHeight: Int
    ): Float {
        return when (rotateDegree) {
            0, 270, 360 -> currentImageMatrix.getTranslateX()
            90 -> { currentImageMatrix.getTranslateX() - imageHeight * currentImageMatrix.getScale() }
            180 -> { currentImageMatrix.getTranslateX() - imageWidth * currentImageMatrix.getScale() }
            else -> 0f
        }
    }

    fun calculateTop(
        rotateDegree: Int,
        imageWidth: Int,
        imageHeight: Int
    ): Float {
        return when (rotateDegree) {
            0, 90, 360 -> currentImageMatrix.getTranslateY()
            180 -> { currentImageMatrix.getTranslateY() - imageHeight * currentImageMatrix.getScale() }
            270 -> { currentImageMatrix.getTranslateY() - imageWidth * currentImageMatrix.getScale() }
            else -> 0f
        }
    }

    fun calculateRight(
        rotateDegree: Int,
        imageWidth: Int,
        imageHeight: Int
    ): Float {
        return when (rotateDegree) {
            0, 360 -> currentImageMatrix.getTranslateX() + imageWidth * currentImageMatrix.getScale()
            90, 180 -> currentImageMatrix.getTranslateX()
            270 -> currentImageMatrix.getTranslateX() + imageHeight * currentImageMatrix.getScale()
            else -> 0f
        }
    }

    fun calculateBottom(
        rotateDegree: Int,
        imageWidth: Int,
        imageHeight: Int
    ): Float {
        return when (rotateDegree) {
            0, 360 -> currentImageMatrix.getTranslateY() + imageHeight * currentImageMatrix.getScale()
            90 -> currentImageMatrix.getTranslateY() + imageWidth * currentImageMatrix.getScale()
            180, 270 -> currentImageMatrix.getTranslateY()
            else -> 0f
        }
    }
}