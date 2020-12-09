package tw.com.program.crop

import android.app.Activity
import android.content.Intent
import android.net.Uri
import tw.com.program.imagecrop.base.BarStyleParameters

class ImageCrop {

    private lateinit var imageUri: Uri

    private var barStyleParameters: BarStyleParameters? = null

    /**
     * 設置裁切頁的 Toolbar 樣式，包含背景色、標題文字等
     */
    fun setBarStyle(parameters: BarStyleParameters): ImageCrop =
        this.apply { barStyleParameters = parameters }

    /**
     * 讀取要裁切圖片的 URI
     */
    fun load(imageUri: Uri): ImageCrop =
        this.apply { this.imageUri = imageUri }

    /**
     * 啟動裁切 Activity，裁切後的圖片會存在 cache 目錄，在 onActivityResult 返回 Uri
     */
    fun crop(activity: Activity) {
        activity.startActivityForResult(
            CropActivity.newIntent(activity, imageUri, barStyleParameters),
            REQUEST_CROP
        )
    }

    companion object {
        const val REQUEST_CROP = 1000

        const val IMAGE_URI = "IMAGE_URI"

        const val BAR_PARAMETERS = "BAR_PARAMETERS"

        private lateinit var crop: ImageCrop

        fun create(): ImageCrop {
            crop = ImageCrop()
            return crop
        }

        fun getResult(intent: Intent): Uri {
            return intent.getParcelableExtra(IMAGE_URI)!!
        }
    }
}