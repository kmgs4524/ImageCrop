package tw.com.program.imagecrop.selector

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import tw.com.program.imagecrop.base.BarStyleParameters
import tw.com.program.imagecrop.base.TransparentBarActivity
import tw.com.program.imagecrop.selector.databinding.ActivitySelectorBinding

class SelectorActivity : TransparentBarActivity() {

    private lateinit var binding: ActivitySelectorBinding
    private val viewModel by viewModels<SelectorViewModel> {
        SelectorViewModelFactory(ImageStorage(application), application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        barStyleParameters =
            intent.getParcelableExtra(ImageSelector.BAR_PARAMETERS) ?: BarStyleParameters(
                R.color.blue_charcoal,
                R.drawable.ic_back,
                getString(R.string.crop_bar_title),
                R.color.white,
                R.color.white
            )

        binding = DataBindingUtil.setContentView(this, R.layout.activity_selector)

        setToolbarPadding(binding.toolbar)
        setToolbarStyle(barStyleParameters)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(ContextCompat.getDrawable(applicationContext, barStyleParameters.backDrawable))
        }

        viewModel.images.observe(this, Observer {
            var size = 0
            it.forEach { size += it.thumbnail.allocationByteCount }
            initSelector(it)
        })
        viewModel.sendImageResult.observe(this, EventObserver {
            sendBackResult(it)
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        if (item.itemId == android.R.id.home) finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.recycleThumbnails()
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

    private fun initSelector(images: List<Image>) {
        binding.selector.apply {
            layoutManager = GridLayoutManager(this@SelectorActivity, 3)
            adapter = SelectorAdapter(viewModel, images)
            addItemDecoration(
                GridItemDecoration(
                    context.getColor(R.color.blue_charcoal),
                    resources.getDimensionPixelSize(R.dimen.grid_divider_line_width)
                )
            )
        }
    }

    private fun sendBackResult(image: Image) {
        setResult(RESULT_OK, Intent().putExtra(ImageSelector.IMAGE_URI, image.uri))
        finish()
    }
}