package au.com.codeka.podcreep.app.podcasts.nowplaying

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import au.com.codeka.podcreep.R
import au.com.codeka.podcreep.app.service.MediaServiceClient

class NowPlayingSheet(context: Context, attributeSet: AttributeSet)
  : RelativeLayout(context, attributeSet) {

  init {
    View.inflate(context, R.layout.now_playing_sheet, this)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    MediaServiceClient.i.addCallback(callback)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()

    MediaServiceClient.i.removeCallback(callback)
  }

  private var callback = object : MediaControllerCompat.Callback() {
    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {

    }

    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {

    }
  }
}
