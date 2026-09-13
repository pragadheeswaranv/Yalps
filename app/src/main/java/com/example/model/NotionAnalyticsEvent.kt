package com.example.model

data class NotionAnalyticsEvent(
  val id: String,
  val eventType: String, // "track_played", "track_skipped", "favorite_toggled", "plan_upgraded", "search_executed"
  val songTitle: String,
  val artistOrComposer: String,
  val timestamp: String,
  val durationSeconds: Int = 0,
  val audioFidelity: String = "96kHz FLAC",
  val isSyncedToNotion: Boolean = true,
  val notionPageId: String = "notion-page-8821"
)

data class NotionIntegrationConfig(
  val databaseId: String = "notion-db-user-behavior-telemetry-2026",
  val isConnected: Boolean = true,
  val workspaceName: String = "Yalps Lossless Intelligence",
  val lastSyncTime: String = "Just now",
  val totalEventsLogged: Int = 18420
)
