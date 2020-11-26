package tw.com.program.crop

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import kotlin.math.sqrt

class CropImageView(context: Context?, attrs: AttributeSet?) : ViewGroup(context, attrs) {

    private val imageView: ImageView = ImageView(context).apply {
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.cat))
        val drawable = ContextCompat.getDrawable(getContext(), R.drawable.cat)
        Log.d(TAG, "drawable width: ${drawable!!.intrinsicWidth} height: ${drawable.intrinsicHeight}")
        scaleType = ImageView.ScaleType.MATRIX
    }

    private val currentImageMatrix = Matrix()
    private val minScale = 0.5
    private val maxScale = 5

    private val scaleListener = ScaleListener(currentImageMatrix, imageView)
    private val scaleGestureDetector = ScaleGestureDetector(context, scaleListener)

    private val currentScale = 0
    private var deltaScale = 1f

    private var drawableWidth = 0
    private var drawableHeight = 0

    private var imageOriginLeft = 0
    private var imageOriginTop = 0

    var radius = 0
        set(value) = initScaleImage(value)

    init {
        // imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.cat))
        addView(imageView)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        imageView.measure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        // val imageOriginLeft = (measuredWidth / 2) - (imageView.drawable.intrinsicWidth / 2)
        // val imageOriginTop = (measuredHeight / 2) - (imageView.drawable.intrinsicHeight / 2)
        imageView.layout(left, top, right, bottom)

        // 圖片置中
        // currentImageMatrix.setTranslate(imageOriginLeft.toFloat(), imageOriginTop.toFloat())

        // TODO: 2020/11/25 圖片放大到至少比裁切框大
        drawableWidth = imageView.drawable.intrinsicWidth
        drawableHeight = imageView.drawable.intrinsicHeight
        while (drawableWidth * deltaScale < radius * 2 || drawableHeight * deltaScale < radius * 2) {
            deltaScale += 0.2f
        }
        // if (drawableWidth * deltaScale < radius * 2 || drawableHeight * deltaScale < radius * 2) {
        //     deltaScale = deltaScale *
        //     currentImageMatrix.postScale()
        // }

        currentImageMatrix.postScale(deltaScale, deltaScale)
        imageView.imageMatrix = currentImageMatrix
        Log.d(TAG, "imageView width: ${imageView.measuredWidth} height: ${imageView.measuredHeight}")
        // Log.d(TAG, "CropImageView left: ${imageView.measuredWidth} height: ${imageView.measuredHeight}")
        Log.d(TAG, "CropImageView width: $measuredWidth height: $measuredHeight")
    }

    private fun initScaleImage(radius: Int) {
        while (drawableWidth * deltaScale < radius * 2 || drawableHeight * deltaScale < radius * 2) {
            deltaScale += 0.2f
        }
        val imageOriginLeft = (measuredWidth / 2) - (imageView.drawable.intrinsicWidth * deltaScale / 2)
        val imageOriginTop = (measuredHeight / 2) - (imageView.drawable.intrinsicHeight * deltaScale / 2)
        currentImageMatrix.setTranslate(imageOriginLeft.toFloat(), imageOriginTop.toFloat())
        currentImageMatrix.preScale(deltaScale, deltaScale)
        imageView.imageMatrix = currentImageMatrix
    }

    fun setMaxScale(max: Int) {}

    fun setMinScale(min: Int) {}

    fun getCurrentScale(): Float {
        return getMatrixScale(currentImageMatrix)
    }

    private fun getMatrixScale(matrix: Matrix): Float {
        return sqrt(
            Math.pow(getMatrixValue(matrix, Matrix.MSCALE_X).toDouble(), 2.0) +
                Math.pow(getMatrixValue(matrix, Matrix.MSKEW_Y).toDouble(), 2.0
            )
        ).toFloat()
    }

    private val matrixValue = FloatArray(MATRIX_VALUES_COUNT.toInt())

    private fun getMatrixValue(
        matrix: Matrix,
        @IntRange(
            from = 0,
            to = MATRIX_VALUES_COUNT
        ) valueIndex: Int
    ): Float {
        matrix.getValues(matrixValue)
        return matrixValue[valueIndex]
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return scaleGestureDetector.onTouchEvent(event)
    }

    private inner class ScaleListener(
        private val currentImageMatrix: Matrix,
        private val imageView: ImageView
    ) : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            Log.d(TAG, "currentSpan: ${detector.currentSpan} previousSpan: ${detector.previousSpan} scaleFactor: ${detector.scaleFactor}")

            val factor = detector.scaleFactor
            if (factor > 1 && getCurrentScale() * factor <= maxScale) {
                // postScale(getCurrentScale() * factor, detector.focusX, detector.focusY)
                postScale(factor, detector.focusX, detector.focusY)
            } else if (factor < 1.0 && getCurrentScale() * factor >= minScale) {
                Log.d(TAG, "focusX, ${detector.focusX} focusY: ${detector.focusY}")
                // postScale(getCurrentScale() * factor, detector.focusX, detector.focusY)
                postScale(factor, detector.focusX, detector.focusY)
            }
            return true
        }

        private fun postScale(factor: Float, centerX: Float, centerY: Float) {
            currentImageMatrix.postScale(factor, factor, centerX, centerY)
            imageView.imageMatrix = currentImageMatrix
        }
    }

    companion object {
        private const val MAX_SCALE_MULTIPLIER = 10
        private const val MATRIX_VALUES_COUNT = 9L
        private const val TAG = "CropImageView"
    }
}