package tw.com.program.crop

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat

class MaskView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    /**
     * 監聽遮罩測量完寬高後，設置的裁切框半徑
     */
    var onRadiusReady: ((Float) -> Unit)? = null
    var cropBitmap: Bitmap? = null
    var bitmapMatrix = Matrix()

    /**
     * 當前縮放倍率，1 = 沒有縮放， < 1 = 縮小，> 1 = 放大
     */
    var deltaScale = 1f

    /**
     * 圖片翻轉角度，用於裁切 Bitmap 時找出正確的左上角起始點
     */
    var rotateDegree = 0

    private val paint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) // Clear mode 會將繪製區塊摟空，可以透視到下層的 View
    }
    private var oval = RectF()
    private var radius = 0f
    private val cropHollowCircle: Path by lazy {
        Path().apply {
            addCircle(
                measuredWidth / 2f,
                measuredHeight / 2f,
                radius,
                Path.Direction.CCW
            )
        }
    }
    private var canvas: Canvas? = null
    private val circleCropBorder: Path by lazy {
        Path().apply {
            addCircle(
                measuredWidth / 2f,
                measuredHeight / 2f,
                radius,
                Path.Direction.CCW
            )
        }
    }
    private val circleCropBorderPaint: Paint by lazy {
        Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = resources.getDimension(R.dimen.crop_border_width)
            color = Color.WHITE
        }
    }

    init {
        // 一定要設置軟體加速，否則無法透出下層
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        radius = measuredWidth / 4f
        onRadiusReady?.invoke(radius)
    }

    override fun onDraw(canvas: Canvas) {
        this.canvas = canvas

        canvas.drawColor(ContextCompat.getColor(context, R.color.transparent_grey))
        canvas.save()

        // 繪製中間簍空的圓形裁切框
        val left = (measuredWidth / 2) - radius
        val top = (measuredHeight / 2) - radius
        val right = (measuredWidth / 2) + radius
        val bottom = (measuredHeight / 2) + radius
        oval.set(left, top, right, bottom)
        canvas.clipPath(cropHollowCircle)
        canvas.drawPath(cropHollowCircle, paint)
        canvas.drawPath(circleCropBorder, circleCropBorderPaint)
        canvas.drawColor(ContextCompat.getColor(context, R.color.transparent))

        canvas.restore()
    }

    @WorkerThread
    fun snapshot(): Bitmap? {
        val bitmap = cropBitmap?.copy(Bitmap.Config.ARGB_8888, true) ?: return null
        // 建立空白 Bitmap，用於被繪製裁切後的 Bitmap
        val outputImage = Bitmap.createBitmap(
            measuredWidth,
            measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        // 縮放改變的是 ropImageView 的 Matrix，從 CropImageView 傳來的 Bitmap 還是原始大小
        // 故須將 Bitmap 縮放到當前大小
        val result = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            bitmapMatrix,
            true
        )
        // 裁剪畫布
        val canvas = Canvas(outputImage)
        canvas.clipPath(cropHollowCircle)
        // 繪製時會從 Bitmap 左上角開始繪製，當圖片旋轉後代表左上角起始點的 transX, transY 會跟著改變，
        // 需以各旋轉角度計算出正確的繪製起始點
        val marginCalculator = MarginCalculator(bitmapMatrix)
        val drawableLeft = marginCalculator.calculateLeft(rotateDegree, bitmap.width, bitmap.height)
        val drawableTop = marginCalculator.calculateTop(rotateDegree, bitmap.width, bitmap.height)
        // 將裁切後的 Bitmap 繪製到空白 Bitmap
        canvas.drawBitmap(
            result,
            drawableLeft,
            drawableTop,
            Paint()
        )
        // 將繪製完成的 Bitmap 裁切成剛好符合裁切框的尺寸
        return Bitmap.createBitmap(
            outputImage,
            (measuredWidth / 2 - radius).toInt(),
            (measuredHeight / 2 - radius).toInt(),
            (radius * 2).toInt(),
            (radius * 2).toInt()
        )
    }
}