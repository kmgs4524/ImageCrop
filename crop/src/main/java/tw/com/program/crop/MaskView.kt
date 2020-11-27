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
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat

class MaskView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
    private var oval = RectF()
    private var radius = 0f
    private val path = Path()

    var cropBitmap: Bitmap? = null
    var bitmapMatrix = Matrix()
    var deltaScale = 1f

    private var canvas: Canvas? = null

    /**
     * 監聽遮罩測量完寬高後，設置的裁切框半徑
     */
    var onRadiusReady: ((Float) -> Unit)? = null

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

        val left = (measuredWidth / 2) - radius
        val top = (measuredHeight / 2) - radius
        val right = (measuredWidth / 2) + radius
        val bottom = (measuredHeight / 2) + radius

        canvas.drawColor(ContextCompat.getColor(context, R.color.transparent_grey))

        canvas.save()

        oval.set(left, top, right, bottom)
        path.addCircle(measuredWidth / 2f, measuredHeight / 2f, radius, Path.Direction.CCW)
        canvas.clipPath(path)
        // canvas.drawArc(oval, 0f, 360f, false, paint)
        canvas.drawPath(path, paint)
        canvas.drawLine(measuredWidth / 2f,
            measuredHeight / 2f,
            (measuredWidth / 2f) + radius,
        measuredHeight / 2f, Paint().also
         {
             it.color = Color.BLACK
             it.strokeWidth = 5f
         })
        canvas.drawColor(ContextCompat.getColor(context, R.color.transparent))

        canvas.restore()

        // 圓形圖片
        // canvas.save()
        // val path = Path().apply {
        //     addCircle(measuredWidth / 2f, measuredHeight / 2f, radius, Path.Direction.CCW)
        // }
        // canvas.clipPath(path)
        // canvas.drawBitmap(
        //     bitmap,
        //     measuredWidth.toFloat(),
        //     measuredHeight / 2f - bitmap.height / 2,
        //     Paint()
        // )
        // canvas.restore()
    }

    fun snapshot(): Bitmap? {
        val bitmap = cropBitmap?.copy(Bitmap.Config.ARGB_8888, true) ?: return null
        val outputImage = Bitmap.createBitmap(
            // (radius * RADIUS_MULTIPLIER).toInt(),
            // (radius * RADIUS_MULTIPLIER).toInt(),
            measuredWidth,
            measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        // 放大Bitmap
        // bitmapMatrix.postTranslate(-300f, 0f)
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
        val pathTamp = Path().apply {
            addCircle(radius, radius, radius, Path.Direction.CCW)
        }
        canvas.clipPath(path)

        // 繪製 Bitmap
        // measuredHeight / 2f - radius,
        // canvas.drawBitmap(
        //     result,
        //     bitmapMatrix,
        //     Paint()
        // )
        Log.i("after crop", "x:${measuredWidth / 2f}, y:${measuredHeight / 2f - radius}")
        // sample 1
        canvas.drawBitmap(
            result,
            getMatrixValue(bitmapMatrix, 2),
            getMatrixValue(bitmapMatrix, 5),
            Paint()
        )

        // canvas.drawBitmap(
        //     result,
        //     0f,
        //     0f,
        //     Paint()
        // )

        // val rect = Rect(0, 0, outputImage.width, outputImage.height)
        // val paint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN) }
        // canvas.drawARGB(0, 0, 0, 0)
        // canvas.drawBitmap(
        //     outputImage,
        //     rect,
        //     rect,
        //     paint
        // )

        val output = Bitmap.createBitmap(
            outputImage,
            // getMatrixValue(bitmapMatrix, 2).toInt(),
            // getMatrixValue(bitmapMatrix, 5).toInt(),
            (measuredWidth / 2 - radius).toInt(),
            (measuredHeight / 2 - radius).toInt(),
            (radius * 2).toInt(),
            (radius * 2).toInt()
        )
        return output
    }

    private fun getMatrixValue(
        matrix: Matrix,
        @IntRange(
            from = 0,
            to = 9
        ) valueIndex: Int
    ): Float {
        val matrixValue = FloatArray(9)
        matrix.getValues(matrixValue)
        return matrixValue[valueIndex]
    }

    companion object {
        private const val RADIUS_MULTIPLIER = 2
    }
}