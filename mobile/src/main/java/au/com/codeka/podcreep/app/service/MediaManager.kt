package au.com.codeka.podcreep.app.service

import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import au.com.codeka.podcreep.model.Episode
import au.com.codeka.podcreep.model.Podcast

/**
 * MediaManager manages the actual playback of the media.
 */
class MediaManager(private val service: MediaService) {
  fun play(podcast: Podcast, episode: Episode) {
    // TODO: obviously we should do better than this!
    val uri = Uri.parse(episode.mediaUrl)
    val mediaPlayer: MediaPlayer? = MediaPlayer().apply {
      setAudioStreamType(AudioManager.STREAM_MUSIC)
      setDataSource(service, uri)
      prepare()
      start()
    }

  }
}
