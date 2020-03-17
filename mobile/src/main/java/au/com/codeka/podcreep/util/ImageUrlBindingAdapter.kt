package au.com.codeka.podcreep.util

import android.net.Uri
import androidx.databinding.BindingAdapter
import android.util.Log
import android.widget.ImageView
import au.com.codeka.podcreep.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.now_playing_sheet.view.*

@BindingAdapter("app:imageUrl")
fun ImageView.loadImage(imageUrl: String?) {
  if (imageUrl != null) {
    val existingImageUrl = getTag(R.id.image_url_tag)
    if (existingImageUrl != null && existingImageUrl == imageUrl) {
      // Already loaded, nothing to do.
      return
    }

    Picasso.get().load(imageUrl).into(this)
    setTag(R.id.image_url_tag, imageUrl)
  }
}

@BindingAdapter("app:imageUrl")
fun ImageView.loadImage(imageUrl: Uri?) {
  if (imageUrl != null) {
    val existingImageUrl = getTag(R.id.image_url_tag)
    if (existingImageUrl != null && existingImageUrl == imageUrl.toString()) {
      // Already loaded, nothing to do.
      return
    }

    Picasso.get().load(imageUrl).into(this)
    setTag(R.id.image_url_tag, imageUrl.toString())
  }
}
