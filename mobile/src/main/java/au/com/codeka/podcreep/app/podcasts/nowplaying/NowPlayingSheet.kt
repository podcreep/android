package au.com.codeka.podcreep.app.podcasts.nowplaying

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import au.com.codeka.podcreep.app.service.MediaServiceClient
import au.com.codeka.podcreep.databinding.NowPlayingSheetBinding

interface Callbacks {
  fun onPlayPauseClick()
}

class NowPlayingSheet(context: Context, attributeSet: AttributeSet)
  : FrameLayout(context, attributeSet), Callbacks {

  private val binding: NowPlayingSheetBinding
  private var currPlaybackState: PlaybackStateCompat? = null
  private var currMetadata: MediaMetadataCompat? = null

  init {
    val inflater = LayoutInflater.from(context)
    binding = NowPlayingSheetBinding.inflate(inflater, this, true)
    binding.callbacks = this
    binding.executePendingBindings()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    MediaServiceClient.i.addCallback(mediaCallback)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()

    MediaServiceClient.i.removeCallback(mediaCallback)
  }

  override fun onPlayPauseClick() {
    if (currPlaybackState?.state == PlaybackStateCompat.STATE_PLAYING) {
      MediaServiceClient.i.pause()
    } else {
      MediaServiceClient.i.play()
    }
  }

  private val mediaCallback = object : MediaControllerCompat.Callback() {
    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
      currMetadata = metadata

      if (metadata != null) {
        binding.metadata = metadata
        binding.albumArtUri = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)
        binding.executePendingBindings()
      }
    }

    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
      currPlaybackState = state
    }
  }
}
