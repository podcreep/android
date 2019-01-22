package au.com.codeka.podcreep.util

import android.net.Uri
import androidx.databinding.BindingAdapter
import android.util.Log
import android.widget.ImageView
import com.squareup.picasso.Picasso

@BindingAdapter("app:imageUrl")
fun ImageView.loadImage(imageUrl: String?) {
  if (imageUrl != null) {
    Picasso.get().load(imageUrl).into(this)
  }
}

@BindingAdapter("app:imageUrl")
fun ImageView.loadImage(imageUrl: Uri?) {
  if (imageUrl != null) {
    Picasso.get().load(imageUrl).into(this)
  }
}
