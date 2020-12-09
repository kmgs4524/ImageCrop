package tw.com.program.crop

import android.net.Uri

interface BitmapCropCallback {
    fun onBitmapCropped(imageUri: Uri)
}