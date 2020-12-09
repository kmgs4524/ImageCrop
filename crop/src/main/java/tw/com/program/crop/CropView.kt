package tw.com.program.crop

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.net.toUri
import java.io.File

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
    private val rotate: ImageView by lazy { container.findViewById<AppCompatImageView>(R.id.rotate) }

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
        cropImageView.onRotateDegreeChange = {
            setRotateDegree(it)
        }
        cropImageView.onMatrixChange = {
            setMatrix(it)
        }
        rotate.setOnClickListener {
            cropImageView.rotateImage(90)
        }
    }

    private fun setRotateDegree(degree: Int) {
        maskView.rotateDegree = degree
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

    fun setImage(imageUri: Uri) {
        cropImageView.imageUri = imageUri
    }

    fun cropAndSaveFile(callback: BitmapCropCallback) {
        val handlerThread = HandlerThread(CROP_BITMAP).apply { start() }
        val cropHandler by lazy { Handler(handlerThread.looper) }
        cropHandler.post {
            val croppedBitmap = maskView.snapshot()
            val file = File(context.cacheDir, BITMAP_FILE_NAME)
            val compressSuccessful = croppedBitmap?.compress(Bitmap.CompressFormat.JPEG, 80, file.outputStream()) ?: false
            if (compressSuccessful) {
                handler.sendMessage(Message().apply {
                    handler.post {
                        callback.onBitmapCropped(file.toUri())
                    }
                })
            }
        }
    }

    companion object {
        private const val TAG = "CropView"
        private const val CROP_BITMAP = "CROP_BITMAP"
        private const val BITMAP_FILE_NAME = "cropped_bitmap"
    }
}