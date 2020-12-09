package tw.com.program.imagecrop.selector

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.provider.MediaStore
import android.provider.MediaStore.Images.Thumbnails.MINI_KIND
import android.util.Size
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import java.io.FileNotFoundException
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class ImageStorage(context: Context) {

    private val contentResolver = context.contentResolver

    /**
     * Use four threads at most to speed up decoding thumbnail Bitmap,
     */
    private val executor = Executors.newFixedThreadPool(MAX_POOL_SIZE)

    @WorkerThread
    fun getThumbnailOfImages(size: Size): List<Image> {
        val internalStorageUri = MediaStore.Images.Media.INTERNAL_CONTENT_URI
        val externalStorageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        return getAllImagesFromStorage(internalStorageUri, size) + getAllImagesFromStorage(externalStorageUri, size)
    }

    /**
    * @param volumeUri this should be MediaStore.Images.Media.INTERNAL_CONTENT_URI or
    * MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    */
    private fun getAllImagesFromStorage(
        volumeUri: Uri,
        size: Size
    ): List<Image> {
        val tasks = mutableListOf<Callable<Image>>()
        val columns = arrayOf(MediaStore.Images.Media._ID)
        val images = mutableListOf<Image>()
        contentResolver.query(volumeUri, columns, null, null, null).use {
            val cursor = it ?: return images
            if (!cursor.moveToFirst()) return images // no image in storage, return empty list
            do {
                val imageId = cursor.getLong(cursor.getColumnIndex(columns[0]))
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId)
                val loadBitmapTask = Callable<Image> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        loadThumbnailAboveQ(imageUri, size)
                    } else {
                        loadThumbnail(imageId, imageUri, size)
                    }
                }
                tasks.add(loadBitmapTask)
            } while (cursor.moveToNext())
            val futures = executor.invokeAll(tasks)
            images.addAll(futures.map { future -> future.get() })
        }
        return images
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun loadThumbnailAboveQ(imageUri: Uri, size: Size): Image {
        val cancelSignal = CancellationSignal().apply { isCanceled }
        val bitmap: Bitmap
        bitmap = try {
            contentResolver.loadThumbnail(imageUri, size, cancelSignal)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            Bitmap.createBitmap(EMPTY_BITMAP_LENGTH, EMPTY_BITMAP_LENGTH, Bitmap.Config.ARGB_8888)
        }
        return Image(bitmap, imageUri)
    }

    private fun loadThumbnail(imageId: Long, imageUri: Uri, size: Size): Image {
        val bitmap: Bitmap
        bitmap = try {
            MediaStore.Images.Thumbnails.getThumbnail(contentResolver, imageId, MINI_KIND, BitmapFactory.Options())
        } catch (e: Exception) {
            e.printStackTrace()
            Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
        }
        return Image(bitmap, imageUri)
    }

    companion object {
        private const val MAX_POOL_SIZE = 4
        private const val EMPTY_BITMAP_LENGTH = 40
    }
}