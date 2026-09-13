package com.example.model

data class CreatorMasterSubmission(
  val id: String,
  val title: String,
  val artist: String,
  val isrc: String,
  val audioSpec: String,
  val audioFormatCategory: String,
  val lufs: String,
  val peak: String,
  val status: String, // "Live & Streaming", "QC Ingest Check", "Stem Fix Needed"
  val streamCountText: String,
  val accruedInrText: String,
  val coverUrl: String
)

data class CreatorPayoutRecord(
  val batchId: String,
  val utr: String,
  val recipient: String,
  val vpaOrNeft: String,
  val streamsFormatted: String,
  val fidelityMultiplier: String,
  val amountInrFormatted: String,
  val status: String,
  val timestamp: String
)
