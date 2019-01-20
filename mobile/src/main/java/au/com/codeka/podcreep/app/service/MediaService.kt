package au.com.codeka.podcreep.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import au.com.codeka.podcreep.R
import au.com.codeka.podcreep.model.Episode
import au.com.codeka.podcreep.model.Podcast
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.picasso.Picasso

import java.util.ArrayList

/**
 * This is the main media service for Pod Creep. It handles playback and also
 */
class MediaService : MediaBrowserServiceCompat() {

  private lateinit var session: MediaSessionCompat

  override fun onCreate() {
    super.onCreate()

    session = MediaSessionCompat(this, "MediaService")
    sessionToken = session.sessionToken
    session.setCallback(MediaSessionCallback())
    session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val id = super.onStartCommand(intent, flags, startId)

    if (intent != null) {
      val podcastStr = intent.extras["podcast"] as String
      val episodeStr = intent.extras["episode"] as String

      Log.i("DEANH", "Starting media service: podcast=$podcastStr, ep=$episodeStr")

      val moshi = Moshi.Builder()
          .add(KotlinJsonAdapterFactory())
          .build()
      val podcast = moshi.adapter<Podcast>(Podcast::class.java).fromJson(podcastStr)!!
      val episode = moshi.adapter<Episode>(Episode::class.java).fromJson(episodeStr)!!

      // Get the session's metadata
      val controller = session.controller

      val channelId =
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("media_service", "Playback Service")
          } else {
            // If earlier version channel ID is not used
            ""
          }

      val builder = NotificationCompat.Builder(this@MediaService, channelId).apply {
        // Add the metadata for the currently playing track
        setContentTitle(podcast.title)
        setContentText(episode.title)
        setSubText(episode.description)
        // TODO: load the icon
        // Picasso.get().load(podcast.imageUrl).
        // setLargeIcon(podcast.imageUrl)

        // Enable launching the player by clicking the notification
        setContentIntent(controller.sessionActivity)

        // Stop the service when the notification is swiped away
        setDeleteIntent(
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                this@MediaService,
                PlaybackStateCompat.ACTION_STOP
            )
        )

        // Make the transport controls visible on the lockscreen
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // Add an app icon and set its accent color
        // Be careful about the color
        setSmallIcon(R.drawable.ic_notification)
        color = ContextCompat.getColor(this@MediaService, R.color.colorPrimaryDark)

        // Add a pause button
        addAction(
            NotificationCompat.Action(
                R.drawable.ic_pause_black_24dp,
                getString(R.string.pause),
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this@MediaService,
                    PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            )
        )

        // Take advantage of MediaStyle features
        setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(session.sessionToken)
            .setShowActionsInCompactView(0)

            // Add a cancel button
            .setShowCancelButton(true)
            .setCancelButtonIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this@MediaService,
                    PlaybackStateCompat.ACTION_STOP
                )
            )
        )
      }

      // Display the notification and place the service in the foreground
      startForeground(1234, builder.build())

      // And start streaming (TODO: obviously we should do better than this!)
      val uri = Uri.parse(episode.mediaUrl)
      val mediaPlayer: MediaPlayer? = MediaPlayer().apply {
        setAudioStreamType(AudioManager.STREAM_MUSIC)
        setDataSource(applicationContext, uri)
        prepare()
        start()
      }
    }

    return id
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun createNotificationChannel(channelId: String, channelName: String): String{
    val chan = NotificationChannel(channelId,
        channelName, NotificationManager.IMPORTANCE_NONE)
    chan.lightColor = Color.BLUE
    chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
    val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    service.createNotificationChannel(chan)
    return channelId
  }

  override fun onDestroy() {
    session.release()
  }

  override fun onGetRoot(clientPackageName: String,
                         clientUid: Int,
                         rootHints: Bundle?): MediaBrowserServiceCompat.BrowserRoot? {
    return MediaBrowserServiceCompat.BrowserRoot("root", null)
  }

  override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaItem>>) {
    result.sendResult(ArrayList())
  }

  private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
    override fun onPlay() {}

    override fun onSkipToQueueItem(queueId: Long) {}

    override fun onSeekTo(position: Long) {}

    override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {}

    override fun onPause() {}

    override fun onStop() {
      stopSelf()
    }

    override fun onSkipToNext() {}

    override fun onSkipToPrevious() {}

    override fun onCustomAction(action: String?, extras: Bundle?) {}

    override fun onPlayFromSearch(query: String?, extras: Bundle?) {}
  }
}
