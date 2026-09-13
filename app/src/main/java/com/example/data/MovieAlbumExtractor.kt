package com.example.data

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.example.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object MovieAlbumExtractor {

  data class ExtractionProgress(
    val status: String,
    val currentTrackIndex: Int,
    val totalTracks: Int,
    val currentTrackTitle: String,
    val isComplete: Boolean = false,
    val extractedSongs: List<Song> = emptyList(),
    val errorMessage: String? = null
  )

  private val AUDIO_EXTENSIONS = setOf("mp3", "wav", "flac", "m4a", "aac", "ogg", "opus")
  private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp")

  /**
   * Cleans track and album names by removing MassTamilan, Isaimini, bitrates, and numbering artifacts.
   */
  fun cleanTrackTitle(raw: String): String {
    if (raw.isBlank()) return "Master Audio Track"
    var s = raw
    // Strip MassTamilan / Isaimini / 320kbps / 128kbps etc.
    s = s.replace(Regex("(?i)\\s*[-_–—]?\\s*\\[?\\s*MassTamilan(?:\\.[a-z0-9]+)?\\s*\\]?", RegexOption.IGNORE_CASE), "")
    s = s.replace(Regex("(?i)\\s*[-_–—]?\\s*\\[?\\s*Isaimini(?:\\.[a-z0-9]+)?\\s*\\]?", RegexOption.IGNORE_CASE), "")
    s = s.replace(Regex("(?i)\\s*[-_–—]?\\s*\\[?\\s*Tamilwire(?:\\.[a-z0-9]+)?\\s*\\]?", RegexOption.IGNORE_CASE), "")
    s = s.replace(Regex("(?i)\\s*[-_–—]?\\s*\\[?\\s*TamilRockers(?:\\.[a-z0-9]+)?\\s*\\]?", RegexOption.IGNORE_CASE), "")
    s = s.replace(Regex("(?i)\\s*[-_–—]?\\s*\\[?\\s*(?:128|320)\\s*kbps\\s*\\]?", RegexOption.IGNORE_CASE), "")
    s = s.replace(Regex("(?i)\\s*[-_–—]?\\s*\\[?\\s*(?:flac|lossless|mp3|m4a|aac)\\s*\\]?", RegexOption.IGNORE_CASE), "")
    // Remove leading track numbers ("01 - ", "1. ", "01.")
    s = s.replace(Regex("^\\d+[\\s._-]+"), "")
    s = s.replace(Regex("[_]+"), " ")
    s = s.trim().trim('-', '–', '—', '_', '[', ']', '(', ')')
    return s.ifBlank { "Master Audio Track" }
  }

  /**
   * Extracts an entire movie album from a ZIP archive, extracting all songs track by track
   * and ingesting them into the live OnlineBackendManager catalog.
   */
  suspend fun extractAndUploadZipAlbum(
    context: Context,
    zipUri: Uri,
    overrideMovieName: String = "",
    overrideComposer: String = "",
    onProgress: suspend (ExtractionProgress) -> Unit
  ): List<Song> = withContext(Dispatchers.IO) {
    val tempUnzipDir = File(context.cacheDir, "unzipped_album_${System.currentTimeMillis()}").apply { mkdirs() }
    val mastersDir = File(context.filesDir, "audio_masters").apply { mkdirs() }
    val coversDir = File(context.filesDir, "covers").apply { mkdirs() }

    val extractedAudioFiles = mutableListOf<File>()
    var albumCoverFile: File? = null

    try {
      onProgress(
        ExtractionProgress(
          status = "Unpacking Movie Album Archive...",
          currentTrackIndex = 0,
          totalTracks = 0,
          currentTrackTitle = "Extracting files from archive"
        )
      )

      // 1. Unzip archive
      context.contentResolver.openInputStream(zipUri)?.use { rawStream ->
        ZipInputStream(BufferedInputStream(rawStream)).use { zipIn ->
          var entry: ZipEntry? = zipIn.nextEntry
          while (entry != null) {
            val entryName = entry.name
            // Ignore hidden files and directories
            if (!entry.isDirectory && !entryName.startsWith("__MACOSX") && !entryName.startsWith(".")) {
              val cleanName = entryName.substringAfterLast('/')
              val ext = cleanName.substringAfterLast('.', "").lowercase()
              val outFile = File(tempUnzipDir, cleanName)

              FileOutputStream(outFile).use { fos ->
                zipIn.copyTo(fos)
              }

              if (AUDIO_EXTENSIONS.contains(ext) && outFile.length() > 1024) {
                extractedAudioFiles.add(outFile)
              } else if (IMAGE_EXTENSIONS.contains(ext) && albumCoverFile == null) {
                val coverDest = File(coversDir, "album_cover_${System.currentTimeMillis()}_$cleanName")
                outFile.copyTo(coverDest, overwrite = true)
                albumCoverFile = coverDest
              }
            }
            zipIn.closeEntry()
            entry = zipIn.nextEntry
          }
        }
      }

      // Sort audio files naturally by filename (01 - Song, 02 - Song, etc.)
      extractedAudioFiles.sortBy { it.name.lowercase() }

      if (extractedAudioFiles.isEmpty()) {
        onProgress(
          ExtractionProgress(
            status = "No audio tracks found in ZIP archive",
            currentTrackIndex = 0,
            totalTracks = 0,
            currentTrackTitle = "",
            isComplete = true,
            errorMessage = "No supported audio files (.mp3, .flac, .wav, .m4a) were found inside the ZIP file."
          )
        )
        return@withContext emptyList()
      }

      // 2. Extract metadata & ingest each track track by track
      val totalTracks = extractedAudioFiles.size
      val uploadedSongs = mutableListOf<Song>()
      val backendManager = OnlineBackendManager.getInstance()

      for ((index, file) in extractedAudioFiles.withIndex()) {
        val trackNum = index + 1
        // Copy audio file permanently to audio_masters
        val permanentAudioFile = File(mastersDir, "album_track_${System.currentTimeMillis()}_${file.name}")
        file.copyTo(permanentAudioFile, overwrite = true)

        val retriever = MediaMetadataRetriever()
        var rawTitle: String? = null
        var rawArtist: String? = null
        var rawAlbum: String? = null
        var rawYear: String? = null
        var rawDurationMs: Long? = null
        var rawBitrate: Int? = null

        try {
          retriever.setDataSource(permanentAudioFile.absolutePath)
          rawTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
          rawArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER)
            ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR)
          rawAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
          rawYear = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
            ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)
          rawDurationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
          rawBitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull()

          // If no separate album cover was found in zip, check embedded artwork
          if (albumCoverFile == null) {
            val embeddedArt = retriever.embeddedPicture
            if (embeddedArt != null && embeddedArt.isNotEmpty()) {
              val artFile = File(coversDir, "cover_${System.currentTimeMillis()}_track_$trackNum.jpg")
              FileOutputStream(artFile).use { it.write(embeddedArt) }
              albumCoverFile = artFile
            }
          }
        } catch (e: Exception) {
          Log.w("MovieAlbumExtractor", "Retriever error on ${file.name}: ${e.message}")
        } finally {
          try {
            retriever.release()
          } catch (e: Exception) {}
        }

        // Clean track title: Strip MassTamilan, bitrates, and leading numbers
        val rawCandidate = rawTitle?.trim()?.ifBlank { null } ?: file.nameWithoutExtension
        val finalTitle = cleanTrackTitle(rawCandidate)
        val finalMovie = cleanTrackTitle(overrideMovieName.trim().ifBlank {
          rawAlbum?.trim()?.ifBlank { null } ?: zipUri.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.') ?: "Master Movie Album"
        })
        val finalComposer = cleanTrackTitle(overrideComposer.trim().ifBlank {
          rawArtist?.trim()?.ifBlank { null } ?: "Tamil Music Director"
        })
        val durationSec = if (rawDurationMs != null && rawDurationMs > 0) (rawDurationMs / 1000).toInt() else (180 + (index * 20))
        val spec = if (rawBitrate != null && rawBitrate > 1000000) "FLAC 96kHz / 24-bit Hi-Res" else "FLAC 48kHz / 24-bit Lossless"
        val coverUrl = albumCoverFile?.absolutePath ?: TamilSongCatalog.COVER_CYBERPULSE
        val era = if (rawYear != null && rawYear.contains("199")) "90s Golden Era" else if (rawYear != null && rawYear.contains("198")) "80s Vintage" else "2021-Present"

        onProgress(
          ExtractionProgress(
            status = "Extracting & Ingesting Track $trackNum of $totalTracks...",
            currentTrackIndex = trackNum,
            totalTracks = totalTracks,
            currentTrackTitle = "$finalTitle ($finalMovie)"
          )
        )

        // Upload single track to live catalog
        val uploadedSong = backendManager.uploadSong(
          title = finalTitle,
          movieOrAlbum = finalMovie,
          composer = finalComposer,
          singers = finalComposer,
          year = rawYear?.toIntOrNull() ?: 2026,
          durationSeconds = durationSec,
          audioSpec = spec,
          isSpatial = true,
          audioStreamUrl = permanentAudioFile.absolutePath,
          imageUrl = coverUrl,
          eraCategory = era
        )
        uploadedSongs.add(uploadedSong)
      }

      // Cleanup temp unzip folder
      try {
        tempUnzipDir.deleteRecursively()
      } catch (e: Exception) {}

      onProgress(
        ExtractionProgress(
          status = "Album Extraction Complete! Successfully uploaded $totalTracks tracks.",
          currentTrackIndex = totalTracks,
          totalTracks = totalTracks,
          currentTrackTitle = "All $totalTracks tracks live in catalog",
          isComplete = true,
          extractedSongs = uploadedSongs
        )
      )

      return@withContext uploadedSongs
    } catch (e: Exception) {
      Log.e("MovieAlbumExtractor", "Error extracting zip: ${e.message}", e)
      onProgress(
        ExtractionProgress(
          status = "Extraction failed: ${e.message}",
          currentTrackIndex = 0,
          totalTracks = 0,
          currentTrackTitle = "",
          isComplete = true,
          errorMessage = e.message ?: "Failed to unpack album archive"
        )
      )
      return@withContext emptyList()
    }
  }

  /**
   * Ingests multiple audio tracks selected simultaneously, extracting metadata for each track.
   */
  suspend fun extractAndUploadMultipleFiles(
    context: Context,
    uris: List<Uri>,
    movieName: String,
    composerName: String,
    albumCoverUri: Uri? = null,
    onProgress: suspend (ExtractionProgress) -> Unit
  ): List<Song> = withContext(Dispatchers.IO) {
    val mastersDir = File(context.filesDir, "audio_masters").apply { mkdirs() }
    val coversDir = File(context.filesDir, "covers").apply { mkdirs() }
    val backendManager = OnlineBackendManager.getInstance()

    var savedCoverFile: File? = null
    if (albumCoverUri != null) {
      try {
        val coverDest = File(coversDir, "album_cover_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(albumCoverUri)?.use { input ->
          FileOutputStream(coverDest).use { output ->
            input.copyTo(output)
          }
        }
        if (coverDest.exists() && coverDest.length() > 0) {
          savedCoverFile = coverDest
        }
      } catch (e: Exception) {
        Log.w("MovieAlbumExtractor", "Cover copy error: ${e.message}")
      }
    }

    val totalTracks = uris.size
    val uploadedSongs = mutableListOf<Song>()

    for ((index, uri) in uris.withIndex()) {
      val trackNum = index + 1
      val rawFileName = uri.lastPathSegment?.substringAfterLast('/') ?: "track_$trackNum"
      val cleanFileName = rawFileName.substringBeforeLast('.')
        .replace(Regex("^\\d+[\\s._-]+"), "")
        .replace(Regex("[_]+"), " ")
        .trim()

      val localFile = File(mastersDir, "album_track_${System.currentTimeMillis()}_$rawFileName")
      try {
        context.contentResolver.openInputStream(uri)?.use { input ->
          FileOutputStream(localFile).use { output ->
            input.copyTo(output)
          }
        }
      } catch (e: Exception) {
        Log.e("MovieAlbumExtractor", "Error copying uri: ${e.message}")
      }

      val permanentPath = if (localFile.exists() && localFile.length() > 0) localFile.absolutePath else uri.toString()

      val retriever = MediaMetadataRetriever()
      var rawTitle: String? = null
      var rawArtist: String? = null
      var rawAlbum: String? = null
      var rawDurationMs: Long? = null
      var rawBitrate: Int? = null

      try {
        if (localFile.exists() && localFile.length() > 0) {
          retriever.setDataSource(localFile.absolutePath)
        } else {
          retriever.setDataSource(context, uri)
        }
        rawTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        rawArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
          ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER)
        rawAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
        rawDurationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
        rawBitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull()

        if (savedCoverFile == null) {
          val embeddedArt = retriever.embeddedPicture
          if (embeddedArt != null && embeddedArt.isNotEmpty()) {
            val artFile = File(coversDir, "cover_${System.currentTimeMillis()}_track_$trackNum.jpg")
            FileOutputStream(artFile).use { it.write(embeddedArt) }
            savedCoverFile = artFile
          }
        }
      } catch (e: Exception) {
        Log.w("MovieAlbumExtractor", "Retriever error: ${e.message}")
      } finally {
        try {
          retriever.release()
        } catch (e: Exception) {}
      }

      val rawCandidate = rawTitle?.trim()?.ifBlank { null } ?: rawFileName.substringBeforeLast('.')
      val finalTitle = cleanTrackTitle(rawCandidate)
      val finalMovie = cleanTrackTitle(movieName.trim().ifBlank { rawAlbum?.trim()?.ifBlank { null } ?: "Master Album" })
      val finalComposer = cleanTrackTitle(composerName.trim().ifBlank { rawArtist?.trim()?.ifBlank { null } ?: "Music Director" })
      val durationSec = if (rawDurationMs != null && rawDurationMs > 0) (rawDurationMs / 1000).toInt() else (180 + (index * 20))
      val spec = if (rawBitrate != null && rawBitrate > 1000000) "FLAC 96kHz / 24-bit Hi-Res" else "FLAC 48kHz / 24-bit Lossless"
      val coverUrl = savedCoverFile?.absolutePath ?: TamilSongCatalog.COVER_CYBERPULSE

      onProgress(
        ExtractionProgress(
          status = "Extracting & Ingesting Track $trackNum of $totalTracks...",
          currentTrackIndex = trackNum,
          totalTracks = totalTracks,
          currentTrackTitle = "$finalTitle ($finalMovie)"
        )
      )

      val song = backendManager.uploadSong(
        title = finalTitle,
        movieOrAlbum = finalMovie,
        composer = finalComposer,
        singers = finalComposer,
        year = 2026,
        durationSeconds = durationSec,
        audioSpec = spec,
        isSpatial = true,
        audioStreamUrl = permanentPath,
        imageUrl = coverUrl,
        eraCategory = "2021-Present"
      )
      uploadedSongs.add(song)
    }

    onProgress(
      ExtractionProgress(
        status = "Extraction Complete! Successfully ingested $totalTracks tracks.",
        currentTrackIndex = totalTracks,
        totalTracks = totalTracks,
        currentTrackTitle = "All $totalTracks tracks live in catalog",
        isComplete = true,
        extractedSongs = uploadedSongs
      )
    )

    return@withContext uploadedSongs
  }
}
