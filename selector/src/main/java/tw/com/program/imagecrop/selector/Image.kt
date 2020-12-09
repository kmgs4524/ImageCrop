package tw.com.program.imagecrop.selector

import android.graphics.Bitmap
import android.net.Uri

data class Image(
    val thumbnail: Bitmap,

    /**
     * Used for returning completed Image，this is content URI. For example:
     * content://media/external/images/media/6
     */
    val uri: Uri
)