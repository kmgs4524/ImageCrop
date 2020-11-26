package tw.com.program.crop

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap

class MaskView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
    private var oval = RectF()
    private var radius = 0f
    private val path = Path()

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
        radius = measuredWidth / 2.5f
        onRadiusReady?.invoke(radius)
    }

    override fun onDraw(canvas: Canvas) {
        val left = (measuredWidth / 2) - radius
        val top = (measuredHeight / 2) - radius
        val right = (measuredWidth / 2) + radius
        val bottom = (measuredHeight / 2) + radius

        canvas.drawColor(ContextCompat.getColor(context, R.color.transparent_grey))

        canvas.save()

        oval.set(left, top, right, bottom)
        path.addCircle(measuredWidth / 2f, measuredHeight / 2f, radius, Path.Direction.CCW)
        canvas.clipPath(path)
        canvas.drawArc(oval, 0f, 360f, false, paint)
        canvas.drawColor(ContextCompat.getColor(context, R.color.transparent))

        canvas.restore()
    }
}