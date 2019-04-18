package au.com.codeka.podcreep.app.service

import android.app.*
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.app.NotificationCompat.MediaStyle
import au.com.codeka.podcreep.MainActivity
import au.com.codeka.podcreep.R
import au.com.codeka.podcreep.model.store.Episode
import au.com.codeka.podcreep.model.store.Podcast

/**
 * NotificationManager manages our various notifications. It keeps it updated, makes sure it's displaying the correct
 * state, handles button presses and so on.
 */
class NotificationManager(
    private val service: Service,
    private val notificationId: Int,
    channelName: String,
    channelDesc: String /* TODO: make this a resource */) {

  private val _builder: NotificationCompat.Builder

  init {
    val channelId =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          createNotificationChannel(channelName, channelDesc)
        } else {
          // If earlier version channel ID is not used
          ""
        }

    _builder = NotificationCompat.Builder(service, channelId)
  }

  val builder: NotificationCompat.Builder
    get() = _builder

  fun startForeground() {
    service.startForeground(notificationId, builder.build())
  }

  fun stopService() {
    service.stopSelf()
  }

  fun refresh(title: String) {
    _builder.apply {
      setContentTitle(title)
      setSmallIcon(R.drawable.ic_refresh_black_24dp)
      color = ContextCompat.getColor(service, R.color.colorPrimaryDark)
    }
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

      // Add a pause button.
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
      setStyle(MediaStyle()
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