package com.example.model

data class LyricLine(
  val timestampSeconds: Int,
  val text: String,
  val translation: String = ""
)

data class Song(
  val id: String = "",
  val title: String = "No Song Selected",
  val movieOrAlbum: String = "No Album",
  val year: Int = 2026,
  val composer: String = "",
  val singers: String = "",
  val durationSeconds: Int = 0,
  val audioSpec: String = "FLAC 96kHz / 24-bit", // "FLAC 96kHz / 24-bit", "Dolby Atmos 7.1.4", "192kHz Bit-Perfect"
  val isSpatial: Boolean = true,
  val imageUrl: String = "",
  val lyrics: List<LyricLine> = emptyList(),
  val streamCountText: String = "0",
  val eraCategory: String = "2021-Present", // "2000-2005", "2006-2010", "2011-2015", "2016-2020", "2021-Present"
  val baseFrequencyHz: Float = 440f, // Used by audio engine
  val isFavorite: Boolean = false,
  val audioUrl: String = ""
) {
  companion object {
    val EMPTY = Song(
      id = "",
      title = "No Track Selected",
      movieOrAlbum = "No Album",
      year = 2026,
      composer = "",
      singers = "",
      durationSeconds = 0,
      audioSpec = "Lossless",
      imageUrl = ""
    )
  }
}
