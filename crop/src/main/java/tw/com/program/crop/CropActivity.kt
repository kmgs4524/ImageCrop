package tw.com.program.crop

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import tw.com.program.crop.ImageCrop.Companion.BAR_PARAMETERS
import tw.com.program.crop.ImageCrop.Companion.IMAGE_URI
import tw.com.program.crop.databinding.ActivityCropBinding
import tw.com.program.imagecrop.base.BarStyleParameters
import tw.com.program.imagecrop.base.TransparentBarActivity

class CropActivity : TransparentBarActivity() {

    private lateinit var binding: ActivityCropBinding
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        barStyleParameters =
            intent.getParcelableExtra(BAR_PARAMETERS) ?: BarStyleParameters(
                R.color.blue_charcoal,
                R.drawable.ic_baseline_arrow_back_ios_24,
                getString(R.string.crop_bar_title),
                R.color.white,
                R.color.white
            )
        imageUri =
            intent.getParcelableExtra(IMAGE_URI) ?: return

        binding = DataBindingUtil.setContentView(this, R.layout.activity_crop)

        binding.cropImage.setImage(imageUri)

        setToolbarPadding(binding.toolbar)
        setToolbarStyle(barStyleParameters)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(ContextCompat.getDrawable(applicationContext, barStyleParameters.backDrawable))
        }
    }

    private fun setToolbarStyle(parameters: BarStyleParameters) {
        binding.toolbar.apply {
            setBackgroundColor(applicationContext.getColor(parameters.barBackgroundColor))
            findViewById<TextView>(R.id.custom_title).apply {
                text = parameters.titleText
                setTextColor(applicationContext.getColor(parameters.titleTextColor))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.crop, menu)
        // 改變 menu item 的文字顏色
        val item = menu?.findItem(R.id.save)
        val title = menu?.findItem(R.id.save)?.title
        val spannableString = SpannableString(title)
        val foregroundColorSpan = ForegroundColorSpan(
            applicationContext.getColor(barStyleParameters.saveTextColor)
        )
        spannableString.setSpan(
            foregroundColorSpan,
            0,
            spannableString.length,
            0
        )
        item?.title = spannableString
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.save -> {
                binding.cropImage.cropAndSaveFile(object : BitmapCropCallback {
                    override fun onBitmapCropped(imageUri: Uri) {
                        val intent = Intent().putExtra(IMAGE_URI, imageUri)
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                })
            }
        }
        return true
    }

    companion object {
        fun newIntent(
            context: Context,
            imageUri: Uri,
            barStyleParameters: BarStyleParameters?
        ): Intent =
            Intent(context, CropActivity::class.java)
                .putExtra(BAR_PARAMETERS, barStyleParameters)
                .putExtra(IMAGE_URI, imageUri)
    }
}