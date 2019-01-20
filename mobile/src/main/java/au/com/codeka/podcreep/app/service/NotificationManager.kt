package au.com.codeka.podcreep.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import au.com.codeka.podcreep.MainActivity
import au.com.codeka.podcreep.R
import au.com.codeka.podcreep.model.Episode
import au.com.codeka.podcreep.model.Podcast

/**
 * NotificationManager manages our playback notification. It keeps it updated, makes sure it's
 * displaying the correct state, handles button presses and so on.
 */
class NotificationManager(private val service: MediaService) {

  companion object {
    val NOTIFICATION_ID = 1234
  }

  private val _builder: NotificationCompat.Builder

  init {
    val channelId =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          createNotificationChannel("media_service", "Playback Service")
        } else {
          // If earlier version channel ID is not used
          ""
        }

    _builder = NotificationCompat.Builder(service, channelId)
  }

  val builder: NotificationCompat.Builder
    get() = _builder

  fun startForeground() {
    service.startForeground(NOTIFICATION_ID, builder.build())
  }

  fun refresh(podcast: Podcast, episode: Episode, sessionToken: MediaSessionCompat.Token) {
    _builder.apply {
      // Add the metadata for the currently playing episode.
      setContentTitle(podcast.title)
      setContentText(episode.title)
      setSubText(episode.description)
      // TODO: load the icon
      // Picasso.get().load(podcast.imageUrl).
      // setLargeIcon(podcast.imageUrl)

      // Enable launching the player by clicking the notification. Just launch the main activity
      // for now. TODO: find something better?
      val intent = Intent(service, MainActivity::class.java)
      setContentIntent(PendingIntent.getActivity(service, 0, intent, 0))

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

      // Add a pause button
      addAction(
          NotificationCompat.Action(
              R.drawable.ic_pause_black_24dp,
              service.getString(R.string.pause),
              MediaButtonReceiver.buildMediaButtonPendingIntent(
                  service,
                  PlaybackStateCompat.ACTION_PLAY_PAUSE
              )
          )
      )

      // Take advantage of MediaStyle features
      setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
          .setMediaSession(sessionToken)
          .setShowActionsInCompactView(0)

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

  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun createNotificationChannel(channelId: String, channelName: String): String{
    val chan = NotificationChannel(channelId,
        channelName, NotificationManager.IMPORTANCE_NONE)
    chan.lightColor = Color.BLUE
    chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
    val service = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    service.createNotificationChannel(chan)
    return channelId
  }
}