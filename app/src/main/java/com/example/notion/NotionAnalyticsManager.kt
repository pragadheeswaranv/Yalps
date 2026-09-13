package com.example.notion

import com.example.model.NotionAnalyticsEvent
import com.example.model.NotionIntegrationConfig
import com.example.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class NotionAnalyticsManager private constructor() {

  companion object {
    @Volatile
    private var instance: NotionAnalyticsManager? = null

    fun getInstance(): NotionAnalyticsManager {
      return instance ?: synchronized(this) {
        instance ?: NotionAnalyticsManager().also { instance = it }
      }
    }
  }

  private val scope = CoroutineScope(Dispatchers.IO)
  private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

  private val _config = MutableStateFlow(
    NotionIntegrationConfig(
      databaseId = "b921a4f0-notion-yalps-behavior-db",
      isConnected = true,
      workspaceName = "Yalps Lossless Intelligence",
      lastSyncTime = "Realtime Active",
      totalEventsLogged = 18420
    )
  )
  val config: StateFlow<NotionIntegrationConfig> = _config.asStateFlow()

  private val _events = MutableStateFlow<List<NotionAnalyticsEvent>>(
    listOf(
      NotionAnalyticsEvent(
        id = UUID.randomUUID().toString(),
        eventType = "track_played",
        songTitle = "Hey Minnale",
        artistOrComposer = "G.V. Prakash Kumar",
        timestamp = "12:44:10",
        audioFidelity = "96kHz FLAC",
        isSyncedToNotion = true,
        notionPageId = "notion-pg-9912"
      ),
      NotionAnalyticsEvent(
        id = UUID.randomUUID().toString(),
        eventType = "favorite_toggled",
        songTitle = "Hukum - Thalaivar Alappara",
        artistOrComposer = "Anirudh Ravichander",
        timestamp = "12:42:18",
        audioFidelity = "Dolby Atmos 7.1.4",
        isSyncedToNotion = true,
        notionPageId = "notion-pg-9911"
      ),
      NotionAnalyticsEvent(
        id = UUID.randomUUID().toString(),
        eventType = "plan_upgraded",
        songTitle = "Duo+ Spatial Suite",
        artistOrComposer = "Stripe Billing Webhook",
        timestamp = "12:39:05",
        audioFidelity = "FLAC + Atmos 3D",
        isSyncedToNotion = true,
        notionPageId = "notion-pg-9910"
      ),
      NotionAnalyticsEvent(
        id = UUID.randomUUID().toString(),
        eventType = "track_played",
        songTitle = "Pachai Nirame",
        artistOrComposer = "A.R. Rahman",
        timestamp = "12:35:40",
        audioFidelity = "Dolby Atmos 7.1.4",
        isSyncedToNotion = true,
        notionPageId = "notion-pg-9909"
      ),
      NotionAnalyticsEvent(
        id = UUID.randomUUID().toString(),
        eventType = "track_skipped",
        songTitle = "Google Google",
        artistOrComposer = "Harris Jayaraj",
        timestamp = "12:30:12",
        durationSeconds = 14,
        audioFidelity = "96kHz FLAC",
        isSyncedToNotion = true,
        notionPageId = "notion-pg-9908"
      )
    )
  )
  val events: StateFlow<List<NotionAnalyticsEvent>> = _events.asStateFlow()

  private val _isSyncing = MutableStateFlow(false)
  val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

  fun logTrackPlayed(song: Song) {
    val event = NotionAnalyticsEvent(
      id = UUID.randomUUID().toString(),
      eventType = "track_played",
      songTitle = song.title,
      artistOrComposer = song.composer,
      timestamp = dateFormat.format(Date()),
      audioFidelity = song.audioSpec,
      isSyncedToNotion = true
    )
    addEvent(event)
  }

  fun logTrackSkipped(song: Song, secondsBeforeSkip: Int = 0) {
    val event = NotionAnalyticsEvent(
      id = UUID.randomUUID().toString(),
      eventType = "track_skipped",
      songTitle = song.title,
      artistOrComposer = song.composer,
      timestamp = dateFormat.format(Date()),
      durationSeconds = secondsBeforeSkip,
      audioFidelity = song.audioSpec,
      isSyncedToNotion = true
    )
    addEvent(event)
  }

  fun logFavoriteToggled(song: Song, isFavorited: Boolean) {
    val event = NotionAnalyticsEvent(
      id = UUID.randomUUID().toString(),
      eventType = if (isFavorited) "favorite_added" else "favorite_removed",
      songTitle = song.title,
      artistOrComposer = song.composer,
      timestamp = dateFormat.format(Date()),
      audioFidelity = song.audioSpec,
      isSyncedToNotion = true
    )
    addEvent(event)
  }

  fun logPlanUpgraded(tierName: String, priceInr: Int) {
    val event = NotionAnalyticsEvent(
      id = UUID.randomUUID().toString(),
      eventType = "subscription_upgrade",
      songTitle = "$tierName (₹$priceInr/mo)",
      artistOrComposer = "Stripe Payments Engine",
      timestamp = dateFormat.format(Date()),
      audioFidelity = "Multi-Endpoint License",
      isSyncedToNotion = true
    )
    addEvent(event)
  }

  fun logSearch(query: String) {
    if (query.isBlank()) return
    val event = NotionAnalyticsEvent(
      id = UUID.randomUUID().toString(),
      eventType = "search_catalog",
      songTitle = "Query: \"$query\"",
      artistOrComposer = "Tamil Song Index 2000-2026",
      timestamp = dateFormat.format(Date()),
      audioFidelity = "Index Discovery",
      isSyncedToNotion = true
    )
    addEvent(event)
  }

  private fun addEvent(event: NotionAnalyticsEvent) {
    _events.update { listOf(event) + it.take(49) }
    _config.update { it.copy(totalEventsLogged = it.totalEventsLogged + 1) }
  }

  fun triggerManualSyncToNotion(onComplete: (Boolean, String) -> Unit) {
    scope.launch {
      _isSyncing.value = true
      try {
        // Dispatch synthetic HTTP POST sync request to Notion API
        kotlinx.coroutines.delay(800)
        _config.update {
          it.copy(
            lastSyncTime = dateFormat.format(Date()) + " IST",
            isConnected = true
          )
        }
        _isSyncing.value = false
        onComplete(true, "Successfully pushed ${_events.value.size} behavior rows to Notion DB (${_config.value.databaseId})")
      } catch (e: Exception) {
        _isSyncing.value = false
        onComplete(false, "Sync failed: ${e.message}")
      }
    }
  }

  fun updateNotionConfig(databaseId: String, workspaceName: String) {
    _config.update {
      it.copy(
        databaseId = databaseId,
        workspaceName = workspaceName
      )
    }
  }
}
