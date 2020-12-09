package tw.com.program.imagecrop.selector

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SelectorViewModelFactory(
    private val imageStorage: ImageStorage,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SelectorViewModel(imageStorage, application) as T
    }
}