package tw.com.program.crop

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout

/**
 * 裁切照片元件
 * 可以設置裁切框形狀，遮罩顏色
 *
 */
class CropView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private val container = LayoutInflater.from(context)
        .inflate(
            R.layout.crop_view,
            this,
            true
        )
    private val cropImageView: CropImageView
    private val maskView: MaskView

    init {
        cropImageView = container.findViewById(R.id.crop)
        maskView = container.findViewById(R.id.mask)
        maskView.onRadiusReady = {
            setRadius(it)
        }
    }

    private fun setRadius(radius: Float) {
        cropImageView.radius = radius.toInt()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    // TODO: 2020/11/25 實作外部設置遮罩顏色
    fun setMaskColor(color: Int) {}

    // TODO: 2020/11/25 實作外部設置裁切框形狀
    fun setCropShape(@CropShape shape: Int) {}
}