package au.com.codeka.podcreep.util

import android.databinding.BindingAdapter
import android.util.Log
import android.widget.ImageView
import com.squareup.picasso.Picasso

@BindingAdapter("app:imageUrl")
fun ImageView.loadImage(imageUrl: String) {
  Log.i("DEANH", "binding... $imageUrl")
  Picasso.get().load(imageUrl).into(this)
}
