package tw.com.program.crop

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap

class CropImageView(context: Context?, attrs: AttributeSet?) : ViewGroup(context, attrs) {

    var onImageReady: ((Bitmap) -> Unit)? = null
    var onDeltaScaleChange: ((Float) -> Unit)? = null
    var onMatrixChange: ((Matrix) -> Unit)? = null
    var onRotateDegreeChange: ((Int) -> Unit)? = null

    var radius = 0
        set(value) {
            field = value
            scaleInitImage(value)
        }
    var imageUri: Uri = Uri.EMPTY
        set(value) {
            field = value
            imageView.setImageURI(imageUri)
        }

    private val imageView: ImageView = ImageView(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        scaleType = ImageView.ScaleType.MATRIX
    }

    private val currentImageMatrix = Matrix()
    // 初始縮放倍數，用在最一開始放大圖片，如果圖片比裁切框小的話
    private var originDeltaScale = 1f
        set(value) {
            field = value
            maxScale = originDeltaScale * 5
            minScale = originDeltaScale * 0.1f
        }
    private var maxScale = 5f
    private var minScale = originDeltaScale * 0.1f

    private val scaleListener = ScaleListener(currentImageMatrix, imageView)
    private val scaleGestureDetector = ScaleGestureDetector(context, scaleListener)
    private val gestureListener = GestureListener(currentImageMatrix, imageView)
    private val gestureDetector = GestureDetector(context, gestureListener)

    private var drawableWidth = 0
    private var drawableHeight = 0
    private var rotateDegree = 0
    private val marginCalculator: MarginCalculator by lazy { MarginCalculator(currentImageMatrix) }

    // 裁切框與圖片的邊界，用於滑動縮放時防止裁切框超出圖片範圍
    private var cropLeftMargin = 0
    private var cropTopMargin = 0
    private var cropRightMargin = 0
    private var cropBottomMargin = 0
    private var drawableLeft = 0f
    private var drawableTop = 0f
    private var drawableRight = 0f
    private var drawableBottom = 0f

    init {
        addView(imageView)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        imageView.measure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        imageView.layout(left, top, right, bottom)

        drawableWidth = imageView.drawable.intrinsicWidth
        drawableHeight = imageView.drawable.intrinsicHeight

        onImageReady?.invoke(imageView.drawable.toBitmap())
    }

    private fun scaleInitImage(radius: Int) {
        while (drawableWidth * originDeltaScale < radius * 2 || drawableHeight * originDeltaScale < radius * 2) {
            originDeltaScale += 0.2f
        }
        // 圖片置中
        val imageOriginLeft = (measuredWidth / 2) - (imageView.drawable.intrinsicWidth * originDeltaScale / 2)
        val imageOriginTop = (measuredHeight / 2) - (imageView.drawable.intrinsicHeight * originDeltaScale / 2)
        currentImageMatrix.setTranslate(imageOriginLeft, imageOriginTop)
        // 圖片放大到至少跟裁切框一樣大
        currentImageMatrix.preScale(originDeltaScale, originDeltaScale)
        imageView.imageMatrix = currentImageMatrix

        // 初始化 margin
        cropLeftMargin = measuredWidth / 2 - radius
        cropTopMargin = measuredHeight / 2 - radius
        cropRightMargin = measuredWidth / 2 + radius
        cropBottomMargin = measuredHeight / 2 + radius
        drawableRight = currentImageMatrix.getTranslateX() + drawableWidth * getCurrentScale()
        drawableBottom = currentImageMatrix.getTranslateY() + drawableHeight * getCurrentScale()

        onDeltaScaleChange?.invoke(originDeltaScale)
        onMatrixChange?.invoke(currentImageMatrix)
    }

    fun rotateImage(degree: Int) {
        currentImageMatrix.postRotate(
            degree.toFloat(),
            imageView.measuredWidth / 2f,
            imageView.measuredHeight / 2f
        )
        imageView.imageMatrix = currentImageMatrix
        rotateDegree = (rotateDegree + degree) % 360
        onRotateDegreeChange?.invoke(rotateDegree)
    }

    fun getCurrentScale(): Float = currentImageMatrix.getScale()

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        scaleGestureDetector.onTouchEvent(event)

        gestureDetector.onTouchEvent(event)

        return true
    }

    /**
     * 防止沒阻擋到快速滑動，如果圖片位移後超過裁切框，將圖片定位到裁切框邊界
     */
    private fun translateIfFast() {
        drawableRight = marginCalculator.calculateRight(rotateDegree, drawableWidth, drawableHeight)
        drawableBottom = marginCalculator.calculateBottom(rotateDegree, drawableWidth, drawableHeight)
        drawableLeft = marginCalculator.calculateLeft(rotateDegree, drawableWidth, drawableHeight)
        drawableTop = marginCalculator.calculateTop(rotateDegree, drawableWidth, drawableHeight)

        when {
            drawableLeft > cropLeftMargin -> {
                // 裁切框左邊界超過圖片
                currentImageMatrix.postTranslate(-(drawableLeft - cropLeftMargin), 0f)
                imageView.imageMatrix = currentImageMatrix
            }
            drawableRight < cropRightMargin -> {
                // 裁切框右邊界超過圖片
                currentImageMatrix.postTranslate(cropRightMargin - drawableRight, 0f)
                imageView.imageMatrix = currentImageMatrix
            }
            drawableTop > cropTopMargin -> {
                // 裁切框上邊界超過圖片
                currentImageMatrix.postTranslate(0f, -(drawableTop - cropTopMargin))
                imageView.imageMatrix = currentImageMatrix
            }
            drawableBottom < cropBottomMargin -> {
                // 裁切框下邊界超過圖片
                currentImageMatrix.postTranslate(0f, cropBottomMargin - drawableBottom)
                imageView.imageMatrix = currentImageMatrix
            }
        }
    }

    private inner class ScaleListener(
        private val currentImageMatrix: Matrix,
        private val imageView: ImageView
    ) : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            // 縮放係數，值為當前兩指間的距離為上一次兩指間距離的幾倍
            // 若兩指緩慢放大，值約在 1 < factor < 1.5，縮小：0.5 < factor < 1
            val factor = detector.scaleFactor

            when {
                factor > 1 && getCurrentScale() * factor <= maxScale -> {
                    // factor > 1：放大手勢，(目前縮放倍率 x 縮放係數)不超過限制時放大圖片
                    postScale(factor, detector.focusX, detector.focusY)
                }
                factor < 1.0 && getCurrentScale() * factor >= minScale -> {
                    // factor < 1：縮小手勢，(目前縮放倍率 x 縮放係數)不超過限制時縮小圖片
                    // 若手勢縮小時，判斷圖片四個邊界是否比裁切框邊界大，是則允許縮小圖片
                    drawableLeft = marginCalculator.calculateLeft(rotateDegree, drawableWidth, drawableHeight)
                    drawableTop = marginCalculator.calculateTop(rotateDegree, drawableWidth, drawableHeight)
                    drawableRight = marginCalculator.calculateRight(rotateDegree, drawableWidth, drawableHeight)
                    drawableBottom = marginCalculator.calculateBottom(rotateDegree, drawableWidth, drawableHeight)

                    if (drawableLeft < cropLeftMargin - MARGIN_AVAILABLE_SPACE &&
                        drawableTop < cropTopMargin - MARGIN_AVAILABLE_SPACE &&
                        drawableRight > cropRightMargin + MARGIN_AVAILABLE_SPACE &&
                        drawableBottom > cropBottomMargin + MARGIN_AVAILABLE_SPACE) {
                        postScale(factor, detector.focusX, detector.focusY)
                    }
                    translateIfFast()
                }
            }

            onDeltaScaleChange?.invoke(getCurrentScale())

            // 返回 true，下次 onScale 得到的縮放係數才是當前 onScale 的倍數
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
            // 若手勢正在滑動時，判斷圖片四個邊界是否比裁切框邊界大，是則允許移動圖片
            drawableLeft = marginCalculator.calculateLeft(rotateDegree, drawableWidth, drawableHeight)
            drawableTop = marginCalculator.calculateTop(rotateDegree, drawableWidth, drawableHeight)
            drawableRight = marginCalculator.calculateRight(rotateDegree, drawableWidth, drawableHeight)
            drawableBottom = marginCalculator.calculateBottom(rotateDegree, drawableWidth, drawableHeight)
            val scrollToRight = distanceX < 0
            val scrollToLeft = distanceX > 0
            val scrollToBottom = distanceY < 0
            val scrollToTop = distanceY > 0
            // 如果圖片左邊界碰到裁切框且手勢正往右滑，則停止滑動
            // 如果圖片又邊界碰到裁切框且手勢正往左滑，則停止滑動
            val isHorizontalNotUpToBound = ((drawableLeft < cropLeftMargin - MARGIN_AVAILABLE_SPACE) && scrollToRight) ||
                ((drawableRight > cropRightMargin + MARGIN_AVAILABLE_SPACE) && scrollToLeft)
            // 如果圖片上邊界碰到裁切框且手勢正往下滑，則停止滑動
            // 如果圖片下邊界碰到裁切框且手勢正往上滑，則停止滑動
            val isVerticalUpNotToBound = (drawableTop < cropTopMargin - MARGIN_AVAILABLE_SPACE) && scrollToBottom ||
                (drawableBottom > cropBottomMargin + MARGIN_AVAILABLE_SPACE && scrollToTop)
            if (isHorizontalNotUpToBound && isVerticalUpNotToBound) {
                when {
                    distanceX > SCROLL_LEFT_OR_TOP_OFFSET_LIMIT -> {
                        currentImageMatrix.postTranslate(-10f, -distanceY)
                    }
                    distanceY > SCROLL_LEFT_OR_TOP_OFFSET_LIMIT -> {
                        currentImageMatrix.postTranslate(-distanceX, -10f)
                    }
                    distanceX < SCROLL_RIGHT_OR_BOTTOM_OFFSET_LIMIT -> {
                        currentImageMatrix.postTranslate(10f, -distanceY)
                    }
                    distanceY < SCROLL_RIGHT_OR_BOTTOM_OFFSET_LIMIT -> {
                        currentImageMatrix.postTranslate(-distanceX, 10f)
                    }
                    else -> {
                        currentImageMatrix.postTranslate(-distanceX, -distanceY)
                    }
                }
                currentImageMatrix.postTranslate(-distanceX, -distanceY)
                imageView.imageMatrix = currentImageMatrix

                translateIfFast()
            }

            return true
        }
    }

    companion object {
        private const val TAG = "CropImageView"

        private const val MARGIN_AVAILABLE_SPACE = 10
        private const val SCROLL_LEFT_OR_TOP_OFFSET_LIMIT = 30
        private const val SCROLL_RIGHT_OR_BOTTOM_OFFSET_LIMIT = -30
    }
}