package au.com.codeka.podcreep.app.service

import android.content.Context
import android.media.AudioManager
import android.util.Log
import androidx.media.AudioAttributesCompat
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat

/**
 * This class looks after audio focus. It requests it when we want to play, it monitors when we lose
 * it and so on.
 */
class AudioFocusManager(context: Context, private val mediaManager: MediaManager) {
  companion object {
    private const val TAG = "AudioFocusManager"
  }

  private var lastFocusRequest: AudioFocusRequestCompat? = null
  private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
  private var resumeOnFocusGain = false

  /**
   * Requests audio focus, returns true if we got it.
   */
  fun request(): Boolean {
    if (lastFocusRequest != null) {
      // We already have focus, nothing to do.
      return true
    }

    val focusRequest = AudioFocusRequestCompat.Builder(AudioManagerCompat.AUDIOFOCUS_GAIN)
        .setAudioAttributes(AudioAttributesCompat.Builder()
            .setUsage(AudioAttributesCompat.USAGE_MEDIA)
            .setContentType(AudioAttributesCompat.CONTENT_TYPE_MUSIC)
            .build())
        .setWillPauseWhenDucked(true)
        .setOnAudioFocusChangeListener(focusListener)
        .build()
    val status = AudioManagerCompat.requestAudioFocus(audioManager, focusRequest)
    if (status == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
      lastFocusRequest = focusRequest
      return true
    }
    return false
  }

  /**
   * Abandons the audio focus that we previously-requested.
   */
  fun abandon() {
    val focusRequest = lastFocusRequest ?: return
    AudioManagerCompat.abandonAudioFocusRequest(audioManager, focusRequest)
    lastFocusRequest = null
  }

  private val focusListener = AudioManager.OnAudioFocusChangeListener {
    when(it) {
      AudioManager.AUDIOFOCUS_GAIN -> {
        // (re-)gained focus, if we're supposed to resume, then resume
        if (resumeOnFocusGain) {
          resumeOnFocusGain = false
          mediaManager.play()
        }
      }
      AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
        // we've temporarily lost focus (e.g. navigation instruction), so we'll want to resume
        // playing when we regain it.
        resumeOnFocusGain = true
        mediaManager.pause()
      }
      AudioManager.AUDIOFOCUS_LOSS -> {
        // we've lost focus, don't try to resume on re-gain
        mediaManager.pause()
      }
      else -> {
        Log.i(TAG, "Unexpected/unknown audio focus change: $it")
      }
    }
  }
}