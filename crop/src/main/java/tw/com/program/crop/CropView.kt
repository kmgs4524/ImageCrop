package tw.com.program.crop

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.LayoutInflater
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
    private val cropImageView: CropImageView by lazy { container.findViewById<CropImageView>(R.id.image) }
    private val maskView: MaskView by lazy { container.findViewById<MaskView>(R.id.mask) }

    init {
        maskView.onRadiusReady = {
            setRadius(it)
        }
        cropImageView.onImageReady = {
            setBitmap(it)
        }
        cropImageView.onDeltaScaleChange = {
            setDeltaScale(it)
        }
        cropImageView.onMatrixChange = {
            setMatrix(it)
        }
    }

    private fun setRadius(radius: Float) {
        cropImageView.radius = radius.toInt()
    }

    private fun setBitmap(bitmap: Bitmap) {
        maskView.cropBitmap = bitmap
    }

    private fun setDeltaScale(deltaScale: Float) {
        maskView.deltaScale = deltaScale
    }

    private fun setMatrix(matrix: Matrix) {
        maskView.bitmapMatrix = matrix
    }

    // TODO: 2020/11/25 實作外部設置遮罩顏色
    fun setMaskColor(color: Int) {}

    // TODO: 2020/11/25 實作外部設置裁切框形狀
    fun setCropShape(@CropShape shape: Int) {}

    fun snapshot(): Bitmap {
        return maskView.snapshot()!!
    }
}