package au.com.codeka.podcreep.util

import android.databinding.BindingAdapter
import android.util.Log
import android.widget.ImageView
import com.squareup.picasso.Picasso

@BindingAdapter("app:imageUrl")
fun ImageView.loadImage(imageUrl: String) {
  Picasso.get().load(imageUrl).into(this)
}
