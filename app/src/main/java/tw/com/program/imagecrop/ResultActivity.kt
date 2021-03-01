package tw.com.program.imagecrop

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import tw.com.program.imagecrop.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_result)

        val imageUri = intent.getParcelableExtra<Uri>(IMAGE_URI)
        binding.resultImage.setImageURI(imageUri)
    }

    companion object {
        private const val IMAGE_URI = "IMAGE_URI"

        fun newIntent(context: Context, imageUri: Uri): Intent {
            return Intent().setClass(context, ResultActivity::class.java)
                .putExtra("IMAGE_URI", imageUri)
        }
    }
}