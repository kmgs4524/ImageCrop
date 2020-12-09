package tw.com.program.imagecrop.selector

import android.app.Application
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SelectorViewModel(
    private val imageStorage: ImageStorage,
    application: Application
) : AndroidViewModel(application) {

    val images: LiveData<List<Image>>
        get() = _images
    private val _images = MutableLiveData<List<Image>>()

    val sendImageResult: LiveData<Event<Image>>
        get() = _sendImageResult
    private val _sendImageResult = MutableLiveData<Event<Image>>()

    init {
        loadImagesFromPhone()
    }

    private fun loadImagesFromPhone() {
        val backgroundThread = HandlerThread("GET_BITMAP_THREAD").apply { start() }
        val bitmapsHandler = Handler(backgroundThread.looper)
        bitmapsHandler.post {
            val images = imageStorage.getThumbnailOfImages(Size(BITMAP_LENGTH, BITMAP_LENGTH))
            _images.postValue(images)
        }
    }

    fun onImageClicked(image: Image) {
        _sendImageResult.value = Event(image)
    }

    fun recycleThumbnails() {
        val images = _images.value
        // 一次載入所有縮圖 Bitmap 可能讓記憶體使用量暴增幾十mb，
        // 故 SelectorActivity 被 destroy 時立即告訴 GC 回收 Bitmap
        images?.forEach {
            it.thumbnail.recycle()
            System.gc()
        }
    }

    companion object {
        private const val BITMAP_LENGTH = 100
    }
}