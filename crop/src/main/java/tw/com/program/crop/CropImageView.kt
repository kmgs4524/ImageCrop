package tw.com.program.crop

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.drawToBitmap
import kotlin.math.sqrt

class CropImageView(context: Context?, attrs: AttributeSet?) : ViewGroup(context, attrs) {

    private val imageView: ImageView = ImageView(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
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
    private val gestureListener = GestureListener(currentImageMatrix, imageView)
    private val gestureDetector = GestureDetector(context, gestureListener)

    private var deltaScale = 1f

    private var drawableWidth = 0
    private var drawableHeight = 0

    var radius = 0
        set(value) = initScaleImage(value)

    var onImageReady: ((Bitmap) -> Unit)? = null
    var onDeltaScaleChange: ((Float) -> Unit)? = null
    var onMatrixChange: ((Matrix) -> Unit)? = null

    init {
        addView(imageView)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        imageView.measure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        imageView.layout(left, top, right, bottom)

        // TODO: 2020/11/25 圖片放大到至少比裁切框大
        drawableWidth = imageView.drawable.intrinsicWidth
        drawableHeight = imageView.drawable.intrinsicHeight
        while (drawableWidth * deltaScale < radius * 2 || drawableHeight * deltaScale < radius * 2) {
            deltaScale += 0.2f
        }

        currentImageMatrix.postScale(deltaScale, deltaScale)
        imageView.imageMatrix = currentImageMatrix
        Log.d(TAG, "imageView width: ${imageView.measuredWidth} height: ${imageView.measuredHeight}")
        // Log.d(TAG, "CropImageView left: ${imageView.measuredWidth} height: ${imageView.measuredHeight}")
        Log.d(TAG, "CropImageView width: $measuredWidth height: $measuredHeight")

        onImageReady?.invoke(imageView.drawable.toBitmap())
    }

    private fun initScaleImage(radius: Int) {
        while (drawableWidth * deltaScale < radius * 2 || drawableHeight * deltaScale < radius * 2) {
            deltaScale += 0.2f
        }
        // 圖片置中
        val imageOriginLeft = (measuredWidth / 2) - (imageView.drawable.intrinsicWidth * deltaScale / 2)
        val imageOriginTop = (measuredHeight / 2) - (imageView.drawable.intrinsicHeight * deltaScale / 2)
        currentImageMatrix.setTranslate(imageOriginLeft.toFloat(), imageOriginTop.toFloat())
        // 圖片放大到至少比裁切一樣大
        currentImageMatrix.preScale(deltaScale, deltaScale)
        imageView.imageMatrix = currentImageMatrix

        onDeltaScaleChange?.invoke(deltaScale)
        onMatrixChange?.invoke(currentImageMatrix)
    }

    fun rotateImage(degree: Int) {
        currentImageMatrix.postRotate(
            degree.toFloat(),
            imageView.measuredWidth / 2f,
            imageView.measuredHeight / 2f
        )
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

    private var previousX = 0f
    private var previousY = 0f

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Log.d(TAG, "onTouchEvent event action ${event!!.action}")
        // if (scaleGestureDetector.onTouchEvent(event)) return true

        scaleGestureDetector.onTouchEvent(event)

        gestureDetector.onTouchEvent(event)

        return true
    }

    private inner class ScaleListener(
        private val currentImageMatrix: Matrix,
        private val imageView: ImageView
    ) : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            Log.d(TAG, "currentSpan: ${detector.currentSpan} previousSpan: ${detector.previousSpan} scaleFactor: ${detector.scaleFactor}")

            val factor = detector.scaleFactor
            if (factor > 1 && getCurrentScale() * factor <= maxScale) {
                postScale(factor, detector.focusX, detector.focusY)
            } else if (factor < 1.0 && getCurrentScale() * factor >= minScale) {
                Log.d(TAG, "focusX, ${detector.focusX} focusY: ${detector.focusY}")
                postScale(factor, detector.focusX, detector.focusY)
            }
            return true
        }

        private fun postScale(factor: Float, centerX: Float, centerY: Float) {
            currentImageMatrix.postScale(factor, factor, centerX, centerY)
            imageView.imageMatrix = currentImageMatrix
        }
    }

    private inner class GestureListener(
        private val currentImageMatrix: Matrix,
        private val imageView: ImageView
    ) : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            currentImageMatrix.postTranslate(-distanceX, -distanceY)
            imageView.imageMatrix = currentImageMatrix
            return true
        }
    }

    companion object {
        private const val MATRIX_VALUES_COUNT = 9L
        private const val TAG = "CropImageView"
    }
}