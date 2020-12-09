package tw.com.program.imagecrop.selector

import android.app.Activity
import android.content.Intent
import android.net.Uri
import tw.com.program.imagecrop.base.BarStyleParameters

class ImageSelector {

    private var barStyleParameters: BarStyleParameters? = null

    fun setBarStyleParameters(parameters: BarStyleParameters): ImageSelector {
        barStyleParameters = parameters
        return this
    }

    fun select(activity: Activity) {
        val intent = Intent(activity, SelectorActivity::class.java)
            .putExtra(BAR_PARAMETERS, barStyleParameters)
        activity.startActivityForResult(intent, REQUEST_SELECT_IMAGE)
    }

    companion object {
        const val REQUEST_SELECT_IMAGE = 1001
        const val IMAGE_URI = "IMAGE_URI"
        const val BAR_PARAMETERS = "BAR_STYLE_PARAMETERS"

        /**
         * App needs READ_EXTERNAL_PERMISSION before use image selector or it won't show any image
         */
        fun create(): ImageSelector =
            ImageSelector()

        fun getResult(intent: Intent): Uri {
            return intent.getParcelableExtra(IMAGE_URI)!!
        }
    }
}