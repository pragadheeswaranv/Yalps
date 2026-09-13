package com.example.model

data class DailyMix(
  val id: String,
  val title: String,
  val subtitle: String,
  val trackCount: Int,
  val durationText: String,
  val coverUrl: String,
  val mixNumber: String,
  val accentColorHex: Long
)
