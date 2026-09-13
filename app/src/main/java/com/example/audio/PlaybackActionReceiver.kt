package com.example.audio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PlaybackActionReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    val playerManager = AudioPlayerManager.getInstance()
    when (intent?.action) {
      MediaNotificationHelper.ACTION_PLAY_PAUSE -> {
        playerManager.togglePlayPause()
      }
      MediaNotificationHelper.ACTION_PREVIOUS -> {
        playerManager.skipPrevious()
      }
      MediaNotificationHelper.ACTION_NEXT -> {
        playerManager.skipNext()
      }
      MediaNotificationHelper.ACTION_DISMISS -> {
        if (context != null) {
          MediaNotificationHelper.clearNotification(context)
        }
      }
    }
  }
}
