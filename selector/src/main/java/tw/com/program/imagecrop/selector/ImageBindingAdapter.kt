package tw.com.program.imagecrop.selector

import android.widget.ImageView
import androidx.databinding.BindingAdapter

@BindingAdapter("image")
fun setImage(imageView: ImageView, image: Image) {
    imageView.setImageBitmap(image.thumbnail)
}