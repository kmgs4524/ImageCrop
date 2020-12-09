package tw.com.program.imagecrop.selector

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tw.com.program.imagecrop.selector.databinding.ItemImageBinding

class SelectorAdapter(
    private val viewModel: SelectorViewModel,
    private val images: List<Image>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageHolder(binding)
    }

    override fun getItemCount(): Int = images.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ImageHolder).binding.apply {
            image = images[position]
            viewModel = this@SelectorAdapter.viewModel
        }
    }

    class ImageHolder(val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root)
}