package tw.com.program.imagecrop

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import kotlinx.android.synthetic.main.activity_main.*
import tw.com.program.crop.ImageCrop
import tw.com.program.imagecrop.base.BarStyleParameters
import tw.com.program.imagecrop.selector.ImageSelector
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var resultUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            crop()
        }
        select.setOnClickListener {
            ImageSelector.create()
                .setBarStyleParameters(
                    BarStyleParameters(
                        tw.com.program.imagecrop.selector.R.color.blue_charcoal,
                        tw.com.program.imagecrop.selector.R.drawable.ic_back,
                        application.getString(tw.com.program.imagecrop.selector.R.string.crop_bar_title),
                        tw.com.program.imagecrop.selector.R.color.white,
                        tw.com.program.imagecrop.selector.R.color.white
                    )
                )
                .select(this)
        }
    }

    private fun crop() {
        val fileUri = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "cat.jpeg")
            .toUri()
        ImageCrop.create()
            // .setBarStyle(BarStyleParameters(
            //     R.color.teal_200,
            //     R.drawable.ic_baseline_arrow_back_ios_24,
            //     "Hello world",
            //     R.color.white,
            //     R.color.white
            // ))
            .load(resultUri)
            .crop(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when {
            requestCode == ImageCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK -> {
                resultUri = ImageCrop.getResult(data!!)
                result.setImageURI(resultUri)
                Log.d(TAG, "onActivityResult: REQUEST_CROP resultUri: $resultUri")
            }
            requestCode == ImageSelector.REQUEST_SELECT_IMAGE && resultCode == Activity.RESULT_OK -> {
                resultUri = ImageSelector.getResult(data!!)
                result.setImageURI(resultUri)
                Log.d(TAG, "onActivityResult: REQUEST_SELECT_IMAGE resultUri: $resultUri")
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}