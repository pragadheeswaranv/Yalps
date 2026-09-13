package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.audio.AudioPlayerManager
import com.example.auth.UserSessionManager
import com.example.data.OnlineBackendManager
import com.example.stripe.StripePaymentsManager
import com.example.ui.YalpsApp
import com.example.ui.theme.YalpsTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    OnlineBackendManager.getInstance().initialize(this)
    UserSessionManager.getInstance().initialize(this)
    StripePaymentsManager.getInstance().initialize(this)
    AudioPlayerManager.getInstance().initialize(this)
    enableEdgeToEdge()
    setContent {
      YalpsTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          YalpsApp()
        }
      }
    }
  }
}


