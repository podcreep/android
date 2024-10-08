package com.podcreep.mobile.service

import android.Manifest
import android.app.*
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import com.podcreep.mobile.R
import com.podcreep.mobile.data.local.Episode
import com.podcreep.mobile.data.local.Podcast
import com.podcreep.mobile.ui.MainActivity
import com.podcreep.mobile.util.L
import javax.inject.Inject

/**
 * NotificationManager manages our various notifications. It keeps it updated, makes sure it's
 * displaying the correct state, handles button presses and so on.
 */
class NotificationManager @Inject constructor(
    private val service: Service,
    private val mediaServiceClient: MediaServiceClient) {
  companion object {
    private val L: L = L("NotificationManager")

    private const val NOTIFICATION_ID: Int = 3479624
    private const val CHANNEL_NAME: String = "podcreep"
    private const val CHANNEL_DESC: String = "podcreep" /* TODO: make this a resource */
  }

  private val builder = NotificationCompat.Builder(
    service, createNotificationChannel(CHANNEL_NAME, CHANNEL_DESC))

  private var podcast: Podcast? = null
  private var episode: Episode? = null
  private var sessionToken: MediaSessionCompat.Token? = null
  private var playbackState: PlaybackStateCompat? = null

  fun startForeground() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      service.startForeground(
        NOTIFICATION_ID, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
    } else {
      service.startForeground(NOTIFICATION_ID, builder.build())
    }

    mediaServiceClient.addCallback(mediaServiceCallback)
  }

  fun stopService() {
    mediaServiceClient.removeCallback(mediaServiceCallback)

    service.stopSelf()
  }

  fun refresh(podcast: Podcast, episode: Episode, sessionToken: MediaSessionCompat.Token) {
    this.podcast = podcast
    this.episode = episode
    this.sessionToken = sessionToken
    refresh()
  }

  fun refresh() {
    val sessionToken = this.sessionToken ?: return

    builder.apply {
      // Enable launching the player by clicking the notification. Just launch the main activity
      // for now. TODO: find something better?
      val intent = Intent(service, MainActivity::class.java)
      setContentIntent(PendingIntent.getActivity(service, 0, intent, PendingIntent.FLAG_IMMUTABLE))

      // Stop the service when the notification is swiped away
      setDeleteIntent(
          MediaButtonReceiver.buildMediaButtonPendingIntent(
              service,
              PlaybackStateCompat.ACTION_STOP
          )
      )

      // Make the transport controls visible on the lockscreen.
      setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

      // Add an app icon and set its accent color
      // Be careful about the color
      setSmallIcon(R.drawable.ic_notification)
      color = ContextCompat.getColor(service, R.color.colorPrimaryDark)

      // Clear all the actions, we'll add our actions back.
      clearActions()

      // Add a play/pause button.
      val isPlaying = playbackState?.state == PlaybackStateCompat.STATE_PLAYING
      if (isPlaying) {
        addAction(
          NotificationCompat.Action(
            R.drawable.ic_rewind_10_24dp,
            service.getString(R.string.skip_back_10),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
              service, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
          )
        )
      }
      addAction(
          NotificationCompat.Action(
              if (isPlaying) {
                R.drawable.ic_pause_black_24dp
              } else {
                R.drawable.ic_play_arrow_black_24dp
              },
              service.getString(if (isPlaying) R.string.pause else R.string.play),
              MediaButtonReceiver.buildMediaButtonPendingIntent(
                service, PlaybackStateCompat.ACTION_PLAY_PAUSE)
          )
      )
      if (isPlaying) {
        addAction(
          NotificationCompat.Action(
            R.drawable.ic_forward_30_24dp,
            service.getString(R.string.skip_forward_30),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
              service, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
          )
        )
      }
      val playPauseActionIndex = if (isPlaying) 1 else 0

        // TODO:
//      val state = playbackState
//      if (state != null) {
//        setProgress(100, 50, false)
//      }

      // Take advantage of MediaStyle features
      setStyle(MediaStyle()
          .setMediaSession(sessionToken)
          .setShowActionsInCompactView(playPauseActionIndex)

          // Add a cancel button
          .setShowCancelButton(true)
          .setCancelButtonIntent(
              MediaButtonReceiver.buildMediaButtonPendingIntent(
                  service,
                  PlaybackStateCompat.ACTION_STOP
              )
          )
      )
    }

    if (ActivityCompat.checkSelfPermission(
        service,
        Manifest.permission.POST_NOTIFICATIONS
      ) != PackageManager.PERMISSION_GRANTED
    ) {
      // TODO: Consider calling
      //    ActivityCompat#requestPermissions
      // here to request the missing permissions, and then overriding
      //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
      //                                          int[] grantResults)
      // to handle the case where the user grants the permission. See the documentation
      // for ActivityCompat#requestPermissions for more details.
      return
    }
    NotificationManagerCompat.from(service).notify(NOTIFICATION_ID, builder.build())
  }

  private fun createNotificationChannel(channelId: String, channelName: String): String{
    val chan = NotificationChannel(channelId,
        channelName, NotificationManager.IMPORTANCE_NONE)
    chan.lightColor = Color.BLUE
    chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
    val service = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    service.createNotificationChannel(chan)
    return channelId
  }

  private val mediaServiceCallback = object : MediaServiceClient.Callbacks() {
    override fun onMetadataChanged(metadata: MediaMetadataCompat) {
      // TODO??
    }

    override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
      playbackState = state
      refresh()
    }
  }
}