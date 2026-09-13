package com.example.audio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

object MediaNotificationHelper {

  const val CHANNEL_ID = "yalps_playback_channel"
  const val NOTIFICATION_ID = 4001

  const val ACTION_PLAY_PAUSE = "com.example.audio.ACTION_PLAY_PAUSE"
  const val ACTION_PREVIOUS = "com.example.audio.ACTION_PREVIOUS"
  const val ACTION_NEXT = "com.example.audio.ACTION_NEXT"
  const val ACTION_DISMISS = "com.example.audio.ACTION_DISMISS"

  private var isChannelCreated = false

  fun createNotificationChannel(context: Context) {
    if (isChannelCreated) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val name = "YALPS Lossless Audio Playback"
      val descriptionText = "Shows active playback controls and track metadata"
      val importance = NotificationManager.IMPORTANCE_LOW
      val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
        description = descriptionText
        setShowBadge(false)
        setSound(null, null)
        enableVibration(false)
      }
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      notificationManager?.createNotificationChannel(channel)
    }
    isChannelCreated = true
  }

  fun updateNotification(context: Context, song: Song, isPlaying: Boolean) {
    if (song.id.isEmpty() || song == Song.EMPTY) {
      clearNotification(context)
      return
    }

    createNotificationChannel(context)

    CoroutineScope(Dispatchers.IO).launch {
      val artworkBitmap = loadCoverBitmap(context, song.imageUrl)

      withContext(Dispatchers.Main) {
        try {
          // Open App Intent
          val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
          }
          val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
          )

          // Action: Previous
          val prevIntent = Intent(context, PlaybackActionReceiver::class.java).apply {
            action = ACTION_PREVIOUS
          }
          val prevPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
          )

          // Action: Play/Pause Toggle
          val playPauseIntent = Intent(context, PlaybackActionReceiver::class.java).apply {
            action = ACTION_PLAY_PAUSE
          }
          val playPausePendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
          )

          // Action: Next
          val nextIntent = Intent(context, PlaybackActionReceiver::class.java).apply {
            action = ACTION_NEXT
          }
          val nextPendingIntent = PendingIntent.getBroadcast(
            context,
            3,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
          )

          // Action: Dismiss (when paused)
          val dismissIntent = Intent(context, PlaybackActionReceiver::class.java).apply {
            action = ACTION_DISMISS
          }
          val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            4,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
          )

          val playPauseIcon = if (isPlaying) {
            android.R.drawable.ic_media_pause
          } else {
            android.R.drawable.ic_media_play
          }
          val playPauseTitle = if (isPlaying) "Pause" else "Play"

          val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(song.title.ifBlank { "Lossless Master Track" })
            .setContentText("${song.composer} • ${song.movieOrAlbum}")
            .setSubText(song.audioSpec)
            .setContentIntent(contentPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setSound(null)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent)
            .addAction(playPauseIcon, playPauseTitle, playPausePendingIntent)
            .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)

          if (artworkBitmap != null) {
            builder.setLargeIcon(artworkBitmap)
          }

          if (isPlaying) {
            // Ongoing while playing
            builder.setOngoing(true)
            builder.setAutoCancel(false)
          } else {
            // Dismissable and clearable by user when paused
            builder.setOngoing(false)
            builder.setAutoCancel(true)
            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Clear", dismissPendingIntent)
            builder.setDeleteIntent(dismissPendingIntent)
          }

          val notificationManager = NotificationManagerCompat.from(context)
          try {
            notificationManager.notify(NOTIFICATION_ID, builder.build())
          } catch (e: SecurityException) {
            // POST_NOTIFICATIONS permission not yet granted by user
          }
        } catch (e: Exception) {
          // Ignore notification dispatch error
        }
      }
    }
  }

  fun clearNotification(context: Context) {
    try {
      val notificationManager = NotificationManagerCompat.from(context)
      notificationManager.cancel(NOTIFICATION_ID)
    } catch (e: Exception) {
      // Ignore
    }
  }

  private fun loadCoverBitmap(context: Context, imageUrl: String): Bitmap? {
    if (imageUrl.isBlank()) return null
    return try {
      when {
        imageUrl.startsWith("/") || imageUrl.startsWith("file://") -> {
          val cleanPath = imageUrl.removePrefix("file://")
          val file = File(cleanPath)
          if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
          } else null
        }
        imageUrl.startsWith("http://") || imageUrl.startsWith("https://") -> {
          val url = URL(imageUrl)
          val conn = url.openConnection()
          conn.connectTimeout = 3000
          conn.readTimeout = 3000
          conn.getInputStream().use { stream ->
            BitmapFactory.decodeStream(stream)
          }
        }
        else -> null
      }
    } catch (e: Exception) {
      null
    }
  }
}
