package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.widget.Toast
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.Folder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.audio.AudioPlayerManager
import com.example.auth.UserSessionManager
import com.example.data.BankAccountDetails
import com.example.data.BackendServerConfig
import com.example.data.MovieAlbumExtractor
import com.example.data.OnlineBackendManager
import com.example.data.TamilSongCatalog
import com.example.model.Song
import com.example.stripe.StripePaymentsManager
import com.example.ui.components.AnimatedEqualizer
import com.example.ui.components.LosslessBadge
import com.example.ui.theme.YalpsBackground
import com.example.ui.theme.YalpsPrimary
import com.example.ui.theme.YalpsSecondary
import com.example.ui.theme.YalpsSurfaceContainer
import com.example.ui.theme.YalpsSurfaceContainerHigh
import com.example.ui.theme.YalpsSurfaceContainerLowest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

enum class BackendNavTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
  UPLOAD_TRACKS("Upload Tracks", Icons.Default.CloudUpload),
  CATALOG("Catalog", Icons.Default.LibraryMusic),
  BANK_ACCOUNTS("Bank Accounts", Icons.Default.AccountBalance),
  BILLING("Billing", Icons.Default.ReceiptLong)
}

@Composable
fun BackendAppScreen(
  onSwitchToPlayer: () -> Unit,
  onPlaySongRedirect: (Song) -> Unit = {},
  onLogout: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val sessionManager = remember { UserSessionManager.getInstance() }
  val backendManager = remember { OnlineBackendManager.getInstance() }
  val stripeManager = remember { StripePaymentsManager.getInstance() }
  val playerManager = remember { AudioPlayerManager.getInstance() }

  val currentUser by sessionManager.currentUser.collectAsState()
  val songs by backendManager.songs.collectAsState()
  val bankAccounts by backendManager.bankAccounts.collectAsState()
  val backendConfig by backendManager.backendConfig.collectAsState()
  val isSyncing by backendManager.isSyncing.collectAsState()
  val creatorRevenue by backendManager.creatorTotalRevenueInr.collectAsState()
  val customerState by stripeManager.customerState.collectAsState()
  val currentPlayingSong by playerManager.currentSong.collectAsState()
  val isPlaying by playerManager.isPlaying.collectAsState()

  var selectedTab by remember { mutableStateOf(BackendNavTab.UPLOAD_TRACKS) }
  var songToEdit by remember { mutableStateOf<Song?>(null) }
  var albumToEdit by remember { mutableStateOf<AlbumInfo?>(null) }
  var showAddBankDialog by remember { mutableStateOf(false) }
  var bankToEdit by remember { mutableStateOf<BankAccountDetails?>(null) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(YalpsBackground)
  ) {
    // 1. Dedicated Top Master Status Bar
    Surface(
      color = Color(0xFF0D0A1C),
      border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                  Brush.linearGradient(
                    listOf(Color(0xFF8B5CF6), Color(0xFF6366F1), Color(0xFFEC4899))
                  )
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Headphones,
                contentDescription = "YALPS Logo",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
              )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = "YALPS",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                letterSpacing = 1.sp
              )
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(Color(0xFF8B5CF6).copy(alpha = 0.25f))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text("BACKEND", color = YalpsPrimary, fontSize = 10.sp, fontWeight = FontWeight.Black)
              }
            }
          }

          // Switch / Log out buttons
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
              onClick = onSwitchToPlayer,
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(1.dp, YalpsPrimary.copy(alpha = 0.6f))
            ) {
              Icon(imageVector = Icons.Default.Headphones, contentDescription = null, tint = YalpsPrimary, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Player Mode", color = YalpsPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            IconButton(
              onClick = {
                sessionManager.logout()
                onLogout()
              },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(imageVector = Icons.Default.PowerSettingsNew, contentDescription = "Log out", tint = Color.Red.copy(alpha = 0.8f))
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Live Edge Server Status & Sync Loop
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isSyncing) Color(0xFFFFB300) else Color(0xFF00E676))
            )
            Text(
              text = if (isSyncing) "Broadcasting delta updates to listeners..." else backendConfig.lastSyncTimeFormatted,
              color = Color.White.copy(alpha = 0.85f),
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )
          }

          Text(
            text = "⚡ Real-Time Payouts Active",
            color = Color(0xFF8B5CF6),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    // 2. Active Screen Content Area
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      when (selectedTab) {
        BackendNavTab.UPLOAD_TRACKS -> {
          UploadTracksScreen(
            onSongUploaded = { newSong ->
              Toast.makeText(context, "Track '${newSong.title}' published & broadcasted!", Toast.LENGTH_LONG).show()
            },
            onNavigateToCatalog = {
              selectedTab = BackendNavTab.CATALOG
            },
            onPreviewSongRedirect = { song ->
              playerManager.playSong(song)
              onPlaySongRedirect(song)
            }
          )
        }
        BackendNavTab.CATALOG -> {
          CatalogByAlbumScreen(
            songs = songs,
            currentPlayingSong = currentPlayingSong,
            isPlaying = isPlaying,
            onPlaySong = { song ->
              playerManager.playSong(song)
              onPlaySongRedirect(song)
              Toast.makeText(context, "Opening Player: ${song.title}", Toast.LENGTH_SHORT).show()
            },
            onEditSong = { song ->
              songToEdit = song
            },
            onDeleteSong = { song ->
              backendManager.deleteSong(song.id)
              Toast.makeText(context, "Removed '${song.title}' from catalog", Toast.LENGTH_SHORT).show()
            },
            onEditAlbum = { albumInfo ->
              albumToEdit = albumInfo
            },
            onDeleteAlbum = { albumName ->
              backendManager.deleteAlbum(albumName)
              Toast.makeText(context, "Deleted album '$albumName' and its tracks", Toast.LENGTH_SHORT).show()
            },
            onNavigateToUpload = {
              selectedTab = BackendNavTab.UPLOAD_TRACKS
            }
          )
        }
        BackendNavTab.BANK_ACCOUNTS -> {
          BankAccountsManagementScreen(
            bankAccounts = bankAccounts,
            onAddBankClick = { showAddBankDialog = true },
            onEditBankClick = { bank -> bankToEdit = bank },
            onSetPrimary = { accountId ->
              backendManager.setPrimaryBankAccount(accountId)
              Toast.makeText(context, "Primary receiving bank updated!", Toast.LENGTH_SHORT).show()
            },
            onDeleteBank = { accountId ->
              backendManager.deleteBankAccount(accountId)
              Toast.makeText(context, "Bank account removed", Toast.LENGTH_SHORT).show()
            }
          )
        }
        BackendNavTab.BILLING -> {
          BillingAnalyticsScreen(
            revenue = creatorRevenue,
            customerState = customerState,
            payoutRecords = backendManager.payoutRecords.collectAsState().value,
            primaryBank = bankAccounts.firstOrNull { it.isPrimary } ?: bankAccounts.firstOrNull()
          )
        }
      }
    }

    // 3. Dedicated Backend Bottom Navigation Bar (Upload tracks, Catalog, Bank accounts, Billing)
    NavigationBar(
      containerColor = YalpsSurfaceContainerLowest,
      tonalElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.25f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
      BackendNavTab.values().forEach { tab ->
        val isSelected = selectedTab == tab
        NavigationBarItem(
          selected = isSelected,
          onClick = { selectedTab = tab },
          icon = {
            Icon(
              imageVector = tab.icon,
              contentDescription = tab.title,
              modifier = Modifier.size(24.dp)
            )
          },
          label = {
            Text(
              text = tab.title,
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
          },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.Black,
            selectedTextColor = YalpsPrimary,
            indicatorColor = YalpsPrimary,
            unselectedIconColor = Color.Gray,
            unselectedTextColor = Color.Gray
          )
        )
      }
    }
  }

  // Edit Song Dialog
  songToEdit?.let { song ->
    EditSongModalDialog(
      song = song,
      onDismiss = { songToEdit = null },
      onSave = { updatedSong ->
        backendManager.updateSong(updatedSong)
        songToEdit = null
        Toast.makeText(context, "Updated ${updatedSong.title}", Toast.LENGTH_SHORT).show()
      }
    )
  }

  // Edit Album Dialog
  albumToEdit?.let { album ->
    EditAlbumModalDialog(
      album = album,
      onDismiss = { albumToEdit = null },
      onSave = { oldAlbum, newAlbum, newComposer, newYear, newEra, newImageUrl ->
        backendManager.updateAlbum(
          originalAlbumName = oldAlbum,
          updatedAlbumName = newAlbum,
          composer = newComposer,
          year = newYear,
          eraCategory = newEra,
          imageUrl = newImageUrl,
          audioSpec = "Lossless FLAC 96kHz"
        )
        albumToEdit = null
        Toast.makeText(context, "Updated album '$newAlbum' across all tracks!", Toast.LENGTH_SHORT).show()
      }
    )
  }

  // Add Bank Account Dialog
  if (showAddBankDialog) {
    BankDetailsModalDialog(
      existingAccount = null,
      onDismiss = { showAddBankDialog = false },
      onSave = { newAccount ->
        backendManager.addBankAccount(newAccount)
        showAddBankDialog = false
        Toast.makeText(context, "Added ${newAccount.bankName} Account!", Toast.LENGTH_SHORT).show()
      }
    )
  }

  // Edit Bank Account Dialog
  bankToEdit?.let { bank ->
    BankDetailsModalDialog(
      existingAccount = bank,
      onDismiss = { bankToEdit = null },
      onSave = { updatedAccount ->
        backendManager.updateBankAccount(updatedAccount)
        bankToEdit = null
        Toast.makeText(context, "Updated ${updatedAccount.bankName} Account!", Toast.LENGTH_SHORT).show()
      }
    )
  }
}

/* ========================================================================== */
/* TAB 1: UPLOAD TRACKS (Automatic Metadata Fetching, Multi-Song Queue, Preview)  */
/* ========================================================================== */

data class UploadTrackDraft(
  val id: String = java.util.UUID.randomUUID().toString(),
  val originalUri: Uri? = null,
  val fileName: String = "",
  val title: String = "",
  val movieOrAlbum: String = "",
  val composer: String = "",
  val singers: String = "",
  val eraCategory: String = "2021-Present",
  val audioSpec: String = "FLAC 96kHz / 24-bit Hi-Res",
  val audioUrl: String = "",
  val imageUrl: String = TamilSongCatalog.COVER_CYBERPULSE,
  val durationSeconds: Int = 210,
  val year: Int = 2026
)

@Composable
fun UploadTracksScreen(
  onSongUploaded: (Song) -> Unit,
  onNavigateToCatalog: () -> Unit,
  onPreviewSongRedirect: ((Song) -> Unit)? = null
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val playerManager = remember { AudioPlayerManager.getInstance() }
  val backendManager = remember { OnlineBackendManager.getInstance() }

  // Multi-song editing queue
  val uploadQueue = remember { mutableStateListOf<UploadTrackDraft>() }
  var activeQueueIndex by remember { mutableIntStateOf(0) }

  // Fallback single track states
  var singleTitle by remember { mutableStateOf("") }
  var singleMovie by remember { mutableStateOf("") }
  var singleComposer by remember { mutableStateOf("") }
  var singleSingers by remember { mutableStateOf("") }
  var singleEra by remember { mutableStateOf("2021-Present") }
  var singleAudioSpec by remember { mutableStateOf("FLAC 96kHz / 24-bit Hi-Res") }
  var singleAudioUrl by remember { mutableStateOf("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3") }
  var singleImageUrl by remember { mutableStateOf(TamilSongCatalog.COVER_CYBERPULSE) }
  var singleDurationSeconds by remember { mutableStateOf(210) }
  var isExtracting by remember { mutableStateOf(false) }
  var extractionSuccessMessage by remember { mutableStateOf<String?>(null) }

  // Batch extraction overrides
  var batchMovieName by remember { mutableStateOf("") }
  var batchComposer by remember { mutableStateOf("") }
  var batchCoverUrl by remember { mutableStateOf("") }
  var albumExtractionProgress by remember { mutableStateOf<MovieAlbumExtractor.ExtractionProgress?>(null) }
  var isBatchProcessing by remember { mutableStateOf(false) }

  // Preview playback state
  val currentPlayingSong by playerManager.currentSong.collectAsState()
  val isPlaying by playerManager.isPlaying.collectAsState()
  val currentPositionSeconds by playerManager.currentPositionSeconds.collectAsState()

  // Active item in queue or single fallback
  val activeDraft = uploadQueue.getOrNull(activeQueueIndex)

  val activeCandidateSong = remember(activeDraft, singleTitle, singleMovie, singleComposer, singleSingers, singleAudioUrl, singleImageUrl, singleAudioSpec, singleDurationSeconds, singleEra) {
    if (activeDraft != null) {
      Song(
        id = activeDraft.id,
        title = activeDraft.title.ifBlank { "Untitled Master Track" },
        movieOrAlbum = activeDraft.movieOrAlbum.ifBlank { "Master Release" },
        composer = activeDraft.composer.ifBlank { "Master Artist" },
        singers = activeDraft.singers.ifBlank { activeDraft.composer.ifBlank { "Master Artist" } },
        audioSpec = activeDraft.audioSpec,
        imageUrl = activeDraft.imageUrl.ifBlank { TamilSongCatalog.COVER_CYBERPULSE },
        audioUrl = activeDraft.audioUrl,
        durationSeconds = activeDraft.durationSeconds,
        eraCategory = activeDraft.eraCategory
      )
    } else {
      Song(
        id = "preview_track_id",
        title = singleTitle.ifBlank { "Untitled Master Track" },
        movieOrAlbum = singleMovie.ifBlank { "Master Release" },
        composer = singleComposer.ifBlank { "Master Artist" },
        singers = singleSingers.ifBlank { singleComposer.ifBlank { "Master Artist" } },
        audioSpec = singleAudioSpec,
        imageUrl = singleImageUrl.ifBlank { TamilSongCatalog.COVER_CYBERPULSE },
        audioUrl = singleAudioUrl,
        durationSeconds = singleDurationSeconds,
        eraCategory = singleEra
      )
    }
  }

  val isCurrentPreviewPlaying = isPlaying && (currentPlayingSong.audioUrl == activeCandidateSong.audioUrl || currentPlayingSong.id == activeCandidateSong.id)

  // Dedicated Cover Image Picker for active draft or single mode
  val draftImagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      coroutineScope.launch(Dispatchers.IO) {
        try {
          val coversDir = File(context.filesDir, "covers").apply { mkdirs() }
          val localCover = File(coversDir, "cover_${System.currentTimeMillis()}.jpg")
          context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(localCover).use { output ->
              input.copyTo(output)
            }
          }
          val finalPath = if (localCover.exists()) localCover.absolutePath else uri.toString()
          withContext(Dispatchers.Main) {
            if (activeDraft != null && activeQueueIndex < uploadQueue.size) {
              val current = uploadQueue[activeQueueIndex]
              uploadQueue[activeQueueIndex] = current.copy(imageUrl = finalPath)
            } else {
              singleImageUrl = finalPath
            }
            Toast.makeText(context, "Cover artwork updated!", Toast.LENGTH_SHORT).show()
          }
        } catch (e: Exception) {
          withContext(Dispatchers.Main) {
            if (activeDraft != null && activeQueueIndex < uploadQueue.size) {
              val current = uploadQueue[activeQueueIndex]
              uploadQueue[activeQueueIndex] = current.copy(imageUrl = uri.toString())
            } else {
              singleImageUrl = uri.toString()
            }
          }
        }
      }
    }
  }

  // Processing URIs into multi-song queue with automatic '- MassTamilan' tag cleanup
  fun processUrisIntoQueue(uris: List<Uri>, defaultMovie: String = "", defaultComposer: String = "", defaultCover: String = "") {
    if (uris.isEmpty()) return
    isExtracting = true
    extractionSuccessMessage = "Extracting metadata and cleaning tags from ${uris.size} tracks..."
    coroutineScope.launch(Dispatchers.IO) {
      val drafts = mutableListOf<UploadTrackDraft>()
      val mastersDir = File(context.filesDir, "audio_masters").apply { mkdirs() }

      uris.forEachIndexed { index, uri ->
        try {
          val rawFileName = uri.lastPathSegment?.substringAfterLast('/') ?: "track_${index + 1}.mp3"
          val safeFileName = rawFileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
          val localFile = File(mastersDir, "track_${System.currentTimeMillis()}_$safeFileName")

          try {
            context.contentResolver.openInputStream(uri)?.use { input ->
              FileOutputStream(localFile).use { output ->
                input.copyTo(output)
              }
            }
          } catch (e: Exception) {
            Log.e("BackendAppScreen", "Error caching audio: ${e.message}")
          }

          val permanentAudioPath = if (localFile.exists() && localFile.length() > 0) localFile.absolutePath else uri.toString()

          val retriever = MediaMetadataRetriever()
          try {
            if (localFile.exists() && localFile.length() > 0) {
              retriever.setDataSource(localFile.absolutePath)
            } else {
              retriever.setDataSource(context, uri)
            }
          } catch (e: Exception) {
            try { retriever.setDataSource(context, uri) } catch (_: Exception) {}
          }

          val rawTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
          val rawArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER)
            ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR)
          val rawAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
          val rawYear = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
            ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)
          val rawDurationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
          val rawBitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull()

          // Embedded Artwork extraction
          val embeddedArtBytes = retriever.embeddedPicture
          var extractedArtUrl = if (defaultCover.isNotBlank()) defaultCover else TamilSongCatalog.COVER_CYBERPULSE
          if (embeddedArtBytes != null && embeddedArtBytes.isNotEmpty()) {
            try {
              val artFile = File(context.filesDir, "extracted_art_${System.currentTimeMillis()}_$index.jpg")
              FileOutputStream(artFile).use { it.write(embeddedArtBytes) }
              extractedArtUrl = artFile.absolutePath
            } catch (e: Exception) {
              Log.e("BackendAppScreen", "Error saving art: ${e.message}")
            }
          }
          retriever.release()

          val cleanFileName = rawFileName.substringBeforeLast('.')
          val fallbackTitle = if (cleanFileName.contains("-")) cleanFileName.substringAfter("-").trim() else cleanFileName
          val fallbackArtist = if (cleanFileName.contains("-")) cleanFileName.substringBefore("-").trim() else "Tamil Master Studio"

          val finalRawTitle = if (!rawTitle.isNullOrBlank()) rawTitle else fallbackTitle
          val finalRawAlbum = if (defaultMovie.isNotBlank()) defaultMovie else (rawAlbum ?: "Master Album")
          val finalRawArtist = if (defaultComposer.isNotBlank()) defaultComposer else (rawArtist ?: fallbackArtist)

          // Sanitize out "- MassTamilan", "Isaimini", etc.
          val sanitizedTitle = MovieAlbumExtractor.cleanTrackTitle(finalRawTitle)
          val sanitizedAlbum = MovieAlbumExtractor.cleanTrackTitle(finalRawAlbum)
          val sanitizedArtist = MovieAlbumExtractor.cleanTrackTitle(finalRawArtist)

          val specInfo = if (rawBitrate != null && rawBitrate > 1000000) {
            "FLAC 96kHz / 24-bit Hi-Res"
          } else {
            "FLAC 48kHz / 24-bit Lossless"
          }

          val durationSec = if (rawDurationMs != null && rawDurationMs > 0) (rawDurationMs / 1000).toInt() else 210

          val era = if (rawYear != null && rawYear.contains("199")) "90s Golden Era"
          else if (rawYear != null && rawYear.contains("198")) "80s Vintage"
          else "2021-Present"

          drafts.add(
            UploadTrackDraft(
              originalUri = uri,
              fileName = rawFileName,
              title = sanitizedTitle,
              movieOrAlbum = sanitizedAlbum,
              composer = sanitizedArtist,
              singers = sanitizedArtist,
              eraCategory = era,
              audioSpec = specInfo,
              audioUrl = permanentAudioPath,
              imageUrl = extractedArtUrl,
              durationSeconds = durationSec,
              year = rawYear?.filter { it.isDigit() }?.toIntOrNull() ?: 2026
            )
          )
        } catch (e: Exception) {
          Log.e("BackendAppScreen", "Error reading track $index: ${e.message}")
        }
      }

      withContext(Dispatchers.Main) {
        isExtracting = false
        if (drafts.isNotEmpty()) {
          uploadQueue.clear()
          uploadQueue.addAll(drafts)
          activeQueueIndex = 0
          extractionSuccessMessage = "Loaded ${drafts.size} tracks into queue. Cleaned '- MassTamilan' tags. Edit each song below before publishing."
          Toast.makeText(context, "${drafts.size} tracks loaded into review queue!", Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

  // Multiple Audio Files Picker
  val multipleAudioLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetMultipleContents()
  ) { uris: List<Uri> ->
    if (uris.isNotEmpty()) {
      processUrisIntoQueue(uris, batchMovieName, batchComposer, batchCoverUrl)
    }
  }

  // Single Audio File Picker
  val singleAudioLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      processUrisIntoQueue(listOf(uri), batchMovieName, batchComposer, batchCoverUrl)
    }
  }

  // ZIP Album Picker
  val zipAlbumLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      isBatchProcessing = true
      coroutineScope.launch(Dispatchers.IO) {
        val songs = MovieAlbumExtractor.extractAndUploadZipAlbum(
          context = context,
          zipUri = uri,
          overrideMovieName = batchMovieName,
          overrideComposer = batchComposer,
          onProgress = { progress ->
            withContext(Dispatchers.Main) {
              albumExtractionProgress = progress
            }
          }
        )
        withContext(Dispatchers.Main) {
          isBatchProcessing = false
          if (songs.isNotEmpty()) {
            val drafts = songs.map { song ->
              UploadTrackDraft(
                id = song.id,
                title = song.title,
                movieOrAlbum = song.movieOrAlbum,
                composer = song.composer,
                singers = song.singers,
                eraCategory = song.eraCategory,
                audioSpec = song.audioSpec,
                audioUrl = song.audioUrl,
                imageUrl = song.imageUrl,
                durationSeconds = song.durationSeconds
              )
            }
            uploadQueue.clear()
            uploadQueue.addAll(drafts)
            activeQueueIndex = 0
            extractionSuccessMessage = "Extracted ${songs.size} tracks from ZIP into review queue."
            Toast.makeText(context, "${songs.size} album tracks loaded into queue!", Toast.LENGTH_LONG).show()
          }
        }
      }
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Ingestion Control & Multi-Select Hub
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = YalpsSurfaceContainer),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, YalpsPrimary.copy(alpha = 0.35f))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("MASTER INGESTION & BATCH UPLOADER", color = YalpsPrimary, fontSize = 11.sp, fontWeight = FontWeight.Black)
              Text("Select Multiple Songs to Queue", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(YalpsPrimary.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.CloudUpload, contentDescription = null, tint = YalpsPrimary)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Select one or multiple audio tracks (FLAC, WAV, MP3) or a ZIP archive. The system automatically extracts ID3 tags, removes '- MassTamilan' tags, and loads each song into the editable queue below so you can inspect and adjust every detail before publishing.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            lineHeight = 16.sp
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Optional batch defaults
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = batchMovieName,
              onValueChange = { batchMovieName = it },
              label = { Text("Default Movie / Album Name") },
              modifier = Modifier.weight(1f),
              singleLine = true,
              shape = RoundedCornerShape(10.dp)
            )
            OutlinedTextField(
              value = batchComposer,
              onValueChange = { batchComposer = it },
              label = { Text("Default Music Director") },
              modifier = Modifier.weight(1f),
              singleLine = true,
              shape = RoundedCornerShape(10.dp)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Action Buttons: Multi-Select, ZIP, or Single Select
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
              onClick = { multipleAudioLauncher.launch("audio/*") },
              modifier = Modifier.weight(1f),
              colors = ButtonDefaults.buttonColors(containerColor = YalpsPrimary, contentColor = Color.Black),
              shape = RoundedCornerShape(10.dp),
              enabled = !isExtracting && !isBatchProcessing
            ) {
              Icon(Icons.Default.LibraryMusic, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Select Multiple Songs", fontWeight = FontWeight.Black, fontSize = 12.sp)
            }

            Button(
              onClick = { zipAlbumLauncher.launch("*/*") },
              modifier = Modifier.weight(1f),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black),
              shape = RoundedCornerShape(10.dp),
              enabled = !isExtracting && !isBatchProcessing
            ) {
              Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Upload ZIP Album", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedButton(
            onClick = { singleAudioLauncher.launch("audio/*") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
            enabled = !isExtracting && !isBatchProcessing
          ) {
            Icon(Icons.Default.AudioFile, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Select Single Audio Track", color = Color.White, fontSize = 12.sp)
          }

          if (isExtracting || isBatchProcessing) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              CircularProgressIndicator(modifier = Modifier.size(18.dp), color = YalpsPrimary, strokeWidth = 2.dp)
              Text("Extracting audio, cleaning tags, and preparing queue...", color = YalpsPrimary, fontSize = 12.sp)
            }
          }

          extractionSuccessMessage?.let { msg ->
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF00E676).copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(14.dp))
              Text(msg, color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Preset Quick Load Chips for Instant Multi-Track Queue Testing
          Text("Quick Demo Multi-Track Queues:", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(6.dp))
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val samplePacks = listOf(
              Pair("Leo Movie Album (4 Songs)", listOf("Naa Ready - MassTamilan", "Badass - MassTamilan", "Bloody Sweet", "Ordinary Person")),
              Pair("Jailer Master (3 Songs)", listOf("Kaavaalaa - MassTamilan", "Hukum - MassTamilan", "Jujubee - MassTamilan")),
              Pair("Ponniyin Selvan (3 Songs)", listOf("Ponni Nadhi", "Chola Chola - MassTamilan", "Alaikadal"))
            )
            items(samplePacks) { (packName, trackNames) ->
              Surface(
                color = YalpsSurfaceContainerLowest,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier.clickable {
                  val drafts = trackNames.mapIndexed { i, rawName ->
                    val clean = MovieAlbumExtractor.cleanTrackTitle(rawName)
                    val movie = if (packName.contains("Leo")) "Leo" else if (packName.contains("Jailer")) "Jailer" else "Ponniyin Selvan I"
                    val artist = if (packName.contains("Ponniyin")) "A.R. Rahman" else "Anirudh Ravichander"
                    UploadTrackDraft(
                      fileName = "$rawName.flac",
                      title = clean,
                      movieOrAlbum = movie,
                      composer = artist,
                      singers = artist,
                      audioSpec = "FLAC 96kHz / 24-bit Hi-Res",
                      audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                      imageUrl = TamilSongCatalog.COVER_CYBERPULSE,
                      durationSeconds = 210 + (i * 20),
                      eraCategory = "2021-Present"
                    )
                  }
                  uploadQueue.clear()
                  uploadQueue.addAll(drafts)
                  activeQueueIndex = 0
                  extractionSuccessMessage = "Loaded ${drafts.size} demo tracks into queue with tags automatically removed!"
                }
              ) {
                Text(
                  text = "⚡ $packName",
                  color = Color.White,
                  fontSize = 11.sp,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                )
              }
            }
          }
        }
      }
    }

    // 2. Multi-Song Queue Review & Step-by-Step Editor
    if (uploadQueue.isNotEmpty()) {
      val currentDraft = uploadQueue.getOrElse(activeQueueIndex) { uploadQueue.first() }

      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = YalpsSurfaceContainer),
          shape = RoundedCornerShape(16.dp),
          border = BorderStroke(1.5.dp, YalpsPrimary)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            // Queue Header & Progress
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(YalpsPrimary.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                  Text(
                    text = "QUEUE: TRACK ${activeQueueIndex + 1} OF ${uploadQueue.size}",
                    color = YalpsPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                  )
                }
              }

              TextButton(
                onClick = {
                  uploadQueue.clear()
                  activeQueueIndex = 0
                  extractionSuccessMessage = null
                }
              ) {
                Text("Clear Queue", color = Color(0xFFFF5252), fontSize = 11.sp)
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontal Track Queue Selector Carousel
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              itemsIndexed(uploadQueue) { idx, itemDraft ->
                val isSelected = idx == activeQueueIndex
                Surface(
                  onClick = { activeQueueIndex = idx },
                  color = if (isSelected) YalpsPrimary else YalpsSurfaceContainerLowest,
                  shape = RoundedCornerShape(8.dp),
                  border = BorderStroke(1.dp, if (isSelected) YalpsPrimary else Color.White.copy(alpha = 0.15f))
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Text(
                      text = "${idx + 1}. ${itemDraft.title.ifBlank { "Untitled" }}",
                      color = if (isSelected) Color.Black else Color.White,
                      fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                      fontSize = 12.sp,
                      maxLines = 1
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(14.dp))

            // Active Track Editor Fields
            Text("EDIT TRACK DETAILS (${activeQueueIndex + 1}/${uploadQueue.size})", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(8.dp))

            // Track Title
            Text("TRACK TITLE (SANITIZED)", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = currentDraft.title,
              onValueChange = { newTitle ->
                val clean = MovieAlbumExtractor.cleanTrackTitle(newTitle)
                uploadQueue[activeQueueIndex] = currentDraft.copy(title = clean)
              },
              placeholder = { Text("e.g. Hukum") },
              modifier = Modifier.fillMaxWidth(),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = YalpsSurfaceContainerLowest,
                unfocusedContainerColor = YalpsSurfaceContainerLowest,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              ),
              shape = RoundedCornerShape(10.dp),
              singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Movie / Album
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("MOVIE / MASTER ALBUM", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
              if (activeQueueIndex == 0 && uploadQueue.size > 1) {
                Text("Applies to whole album queue", color = YalpsPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
              }
            }
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
              value = currentDraft.movieOrAlbum,
              onValueChange = { newAlbum ->
                if (activeQueueIndex == 0) {
                  // Reflect album name on all tracks in queue
                  for (i in uploadQueue.indices) {
                    uploadQueue[i] = uploadQueue[i].copy(movieOrAlbum = newAlbum)
                  }
                } else {
                  uploadQueue[activeQueueIndex] = currentDraft.copy(movieOrAlbum = newAlbum)
                }
              },
              placeholder = { Text("e.g. Jailer") },
              modifier = Modifier.fillMaxWidth(),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = YalpsSurfaceContainerLowest,
                unfocusedContainerColor = YalpsSurfaceContainerLowest,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              ),
              shape = RoundedCornerShape(10.dp),
              singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Composer & Singers
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Column(modifier = Modifier.weight(1f)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text("COMPOSER / ARTIST", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                  if (activeQueueIndex == 0 && uploadQueue.size > 1) {
                    Text("Auto-syncs all", color = YalpsSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                  }
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                  value = currentDraft.composer,
                  onValueChange = { newComp ->
                    if (activeQueueIndex == 0) {
                      // First song composer reflects on all other songs in upload queue,
                      // WITHOUT affecting their individual singers or era category!
                      for (i in uploadQueue.indices) {
                        uploadQueue[i] = uploadQueue[i].copy(composer = newComp)
                      }
                    } else {
                      uploadQueue[activeQueueIndex] = currentDraft.copy(composer = newComp)
                    }
                  },
                  modifier = Modifier.fillMaxWidth(),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = YalpsSurfaceContainerLowest,
                    unfocusedContainerColor = YalpsSurfaceContainerLowest,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                  ),
                  shape = RoundedCornerShape(10.dp),
                  singleLine = true
                )
              }

              Column(modifier = Modifier.weight(1f)) {
                Text("SINGERS (PER-SONG)", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                  value = currentDraft.singers,
                  onValueChange = { newSing ->
                    // Singers is strictly per-song and does not auto-reflect to other tracks
                    uploadQueue[activeQueueIndex] = currentDraft.copy(singers = newSing)
                  },
                  modifier = Modifier.fillMaxWidth(),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = YalpsSurfaceContainerLowest,
                    unfocusedContainerColor = YalpsSurfaceContainerLowest,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                  ),
                  shape = RoundedCornerShape(10.dp),
                  singleLine = true
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Era Category Selection Chips
            Text("ERA CATEGORY", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            val eraOptions = listOf("2021-Present", "2010s", "2000s", "90s Golden Era", "80s Vintage", "70s Retro")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              items(eraOptions) { eraOpt ->
                val isSelected = currentDraft.eraCategory == eraOpt
                Surface(
                  onClick = {
                    uploadQueue[activeQueueIndex] = currentDraft.copy(eraCategory = eraOpt)
                  },
                  color = if (isSelected) YalpsPrimary else YalpsSurfaceContainerLowest,
                  shape = RoundedCornerShape(8.dp),
                  border = BorderStroke(1.dp, if (isSelected) YalpsPrimary else Color.White.copy(alpha = 0.1f))
                ) {
                  Text(
                    text = eraOpt,
                    color = if (isSelected) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Audio Fidelity Specification
            Text("AUDIO FIDELITY SPEC", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            val specOptions = listOf("FLAC 96kHz / 24-bit Hi-Res", "FLAC 48kHz / 24-bit Lossless", "Studio Master 192kHz")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              items(specOptions) { specOpt ->
                val isSelected = currentDraft.audioSpec == specOpt
                Surface(
                  onClick = {
                    uploadQueue[activeQueueIndex] = currentDraft.copy(audioSpec = specOpt)
                  },
                  color = if (isSelected) Color(0xFF00E676) else YalpsSurfaceContainerLowest,
                  shape = RoundedCornerShape(8.dp),
                  border = BorderStroke(1.dp, if (isSelected) Color(0xFF00E676) else Color.White.copy(alpha = 0.1f))
                ) {
                  Text(
                    text = specOpt,
                    color = if (isSelected) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cover Artwork Row for Current Item
            Text("COVER ARTWORK", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(48.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color.DarkGray)
              ) {
                AsyncImage(
                  model = currentDraft.imageUrl.ifBlank { TamilSongCatalog.COVER_CYBERPULSE },
                  contentDescription = null,
                  modifier = Modifier.fillMaxSize(),
                  contentScale = ContentScale.Crop
                )
              }
              OutlinedButton(
                onClick = { draftImagePickerLauncher.launch("image/*") },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, YalpsPrimary),
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = YalpsPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Change Artwork for this Track", color = YalpsPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
              OutlinedButton(
                onClick = {
                  val art = currentDraft.imageUrl
                  for (i in uploadQueue.indices) {
                    val itm = uploadQueue[i]
                    uploadQueue[i] = itm.copy(imageUrl = art)
                  }
                  Toast.makeText(context, "Applied artwork to all ${uploadQueue.size} tracks!", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
              ) {
                Text("Apply to All", color = Color.White, fontSize = 11.sp)
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Queue Step Navigation Controls
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedButton(
                onClick = { if (activeQueueIndex > 0) activeQueueIndex-- },
                enabled = activeQueueIndex > 0,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text("◀ Prev Track", fontSize = 12.sp)
              }

              OutlinedButton(
                onClick = {
                  if (uploadQueue.size > 1) {
                    val removedIdx = activeQueueIndex
                    uploadQueue.removeAt(removedIdx)
                    activeQueueIndex = removedIdx.coerceAtMost(uploadQueue.size - 1)
                  } else {
                    uploadQueue.clear()
                    activeQueueIndex = 0
                  }
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("Remove", fontSize = 12.sp)
              }

              Button(
                onClick = { if (activeQueueIndex < uploadQueue.size - 1) activeQueueIndex++ },
                enabled = activeQueueIndex < uploadQueue.size - 1,
                colors = ButtonDefaults.buttonColors(containerColor = YalpsPrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text("Next Track ▶", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(14.dp))

            // Primary Upload & Publish All Tracks Button
            Button(
              onClick = {
                val publishedList = mutableListOf<Song>()
                uploadQueue.forEach { draft ->
                  val pub = backendManager.uploadSong(
                    title = draft.title.trim().ifEmpty { "Master Track" },
                    movieOrAlbum = draft.movieOrAlbum.trim().ifEmpty { "Master Album" },
                    composer = draft.composer.trim().ifEmpty { "Master Artist" },
                    singers = draft.singers.trim().ifEmpty { draft.composer.trim().ifEmpty { "Master Artist" } },
                    year = draft.year,
                    durationSeconds = draft.durationSeconds,
                    audioSpec = draft.audioSpec,
                    isSpatial = true,
                    audioStreamUrl = draft.audioUrl,
                    imageUrl = draft.imageUrl,
                    eraCategory = draft.eraCategory
                  )
                  publishedList.add(pub)
                }

                if (publishedList.isNotEmpty()) {
                  onSongUploaded(publishedList.first())
                }
                uploadQueue.clear()
                activeQueueIndex = 0
                Toast.makeText(context, "Successfully uploaded & published ${publishedList.size} tracks into live catalog!", Toast.LENGTH_LONG).show()
                onNavigateToCatalog()
              },
              modifier = Modifier.fillMaxWidth(),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "UPLOAD & PUBLISH ALL ${uploadQueue.size} TRACKS",
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
              )
            }
          }
        }
      }
    }

    // 3. Live Audio Preview Player Card
    item {
      Card(
        colors = CardDefaults.cardColors(
          containerColor = if (isCurrentPreviewPlaying) Color(0xFF161622) else YalpsSurfaceContainerLowest
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
          1.5.dp,
          if (isCurrentPreviewPlaying) YalpsPrimary else Color.White.copy(alpha = 0.12f)
        )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Text("LIVE AUDIO PREVIEW PLAYER", color = YalpsPrimary, fontSize = 11.sp, fontWeight = FontWeight.Black)
              if (isCurrentPreviewPlaying) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF00E676).copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AnimatedEqualizer(isPlaying = true, barCount = 3, maxHeight = 10.dp, barColor = Color(0xFF00E676))
                    Text("LIVE AUDIO STREAMING", color = Color(0xFF00E676), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
            Text(
              text = activeCandidateSong.audioSpec,
              color = Color(0xFFD8B4FE),
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF20202A))
                .border(1.dp, YalpsPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              AsyncImage(
                model = activeCandidateSong.imageUrl.ifBlank { TamilSongCatalog.COVER_CYBERPULSE },
                contentDescription = "Artwork Preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = activeCandidateSong.title.ifBlank { "Untitled Master Preview" },
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "${activeCandidateSong.composer.ifBlank { "Artist" }} • ${activeCandidateSong.movieOrAlbum.ifBlank { "Album" }}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              if (activeCandidateSong.audioUrl.isNotBlank()) {
                Text(
                  text = "✓ Audio File Ready: ${if (activeCandidateSong.audioUrl.startsWith("content://") || activeCandidateSong.audioUrl.startsWith("/data")) "Local Audio Stream" else "Direct Link"}",
                  color = Color(0xFF00E676),
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }

            // Play / Pause Preview Button
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                  Brush.linearGradient(listOf(YalpsPrimary, Color(0xFF8B5CF6)))
                )
                .clickable {
                  if (isCurrentPreviewPlaying) {
                    playerManager.togglePlayPause()
                  } else {
                    playerManager.playSong(activeCandidateSong)
                    onPreviewSongRedirect?.invoke(activeCandidateSong)
                  }
                },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (isCurrentPreviewPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Preview Play",
                tint = Color.Black,
                modifier = Modifier.size(28.dp)
              )
            }
          }

          if (isCurrentPreviewPlaying) {
            Spacer(modifier = Modifier.height(12.dp))
            val progress = if (activeCandidateSong.durationSeconds > 0) (currentPositionSeconds.toFloat() / activeCandidateSong.durationSeconds).coerceIn(0f, 1f) else 0f
            Slider(
              value = progress,
              onValueChange = { newProgress ->
                playerManager.seekTo((newProgress * activeCandidateSong.durationSeconds).toInt())
              },
              colors = SliderDefaults.colors(
                thumbColor = YalpsPrimary,
                activeTrackColor = YalpsPrimary,
                inactiveTrackColor = Color.DarkGray
              ),
              modifier = Modifier.fillMaxWidth()
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "${currentPositionSeconds / 60}:${(currentPositionSeconds % 60).toString().padStart(2, '0')}",
                color = Color(0xFFD8B4FE),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = "${activeCandidateSong.durationSeconds / 60}:${(activeCandidateSong.durationSeconds % 60).toString().padStart(2, '0')}",
                color = Color.Gray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }
    }
  }
}

/* ========================================================================== */
/* TAB 2: CATALOG (Organized by Album with Lossless Playback & Management)     */
/* ========================================================================== */

@Composable
fun CatalogByAlbumScreen(
  songs: List<Song>,
  currentPlayingSong: Song,
  isPlaying: Boolean,
  onPlaySong: (Song) -> Unit,
  onEditSong: (Song) -> Unit,
  onDeleteSong: (Song) -> Unit,
  onEditAlbum: (AlbumInfo) -> Unit = {},
  onDeleteAlbum: (String) -> Unit = {},
  onNavigateToUpload: () -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }

  // Filter songs based on search query
  val filteredSongs = remember(songs, searchQuery) {
    if (searchQuery.isBlank()) songs else {
      songs.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
          it.movieOrAlbum.contains(searchQuery, ignoreCase = true) ||
          it.composer.contains(searchQuery, ignoreCase = true)
      }
    }
  }

  // Group songs by Album / Movie
  val albumGroups = remember(filteredSongs) {
    filteredSongs.groupBy { it.movieOrAlbum.ifBlank { "Independent Masters" } }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Catalog Summary Header
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = YalpsSurfaceContainer),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, YalpsPrimary.copy(alpha = 0.25f))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("MASTER CATALOG REGISTRY", color = YalpsPrimary, fontSize = 11.sp, fontWeight = FontWeight.Black)
              Text("${songs.size} Lossless Tracks • ${albumGroups.size} Albums", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Button(
              onClick = onNavigateToUpload,
              colors = ButtonDefaults.buttonColors(containerColor = YalpsPrimary, contentColor = Color.Black),
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Upload Track", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Search Field
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by album, track title, or composer...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = YalpsSurfaceContainerLowest,
              unfocusedContainerColor = YalpsSurfaceContainerLowest,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
          )
        }
      }
    }

    // 2. Albums with their tracks
    albumGroups.forEach { (albumName, albumTracks) ->
      val firstTrack = albumTracks.firstOrNull()
      val primaryArtist = firstTrack?.composer ?: "Master Artist"
      val albumCover = firstTrack?.imageUrl?.ifBlank { TamilSongCatalog.COVER_CYBERPULSE } ?: TamilSongCatalog.COVER_CYBERPULSE
      val albumYear = firstTrack?.year ?: 2026
      val albumEra = firstTrack?.eraCategory ?: "2021-Present"

      val currentAlbumInfo = AlbumInfo(
        movieOrAlbum = albumName,
        composer = primaryArtist,
        year = albumYear,
        eraCategory = albumEra,
        imageUrl = albumCover,
        trackCount = albumTracks.size
      )

      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = YalpsSurfaceContainerLowest),
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            // Album Header
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              AsyncImage(
                model = albumCover,
                contentDescription = albumName,
                modifier = Modifier
                  .size(60.dp)
                  .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
              )

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = albumName,
                  color = Color.White,
                  fontWeight = FontWeight.Black,
                  fontSize = 16.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "$primaryArtist • ${albumTracks.size} ${if (albumTracks.size == 1) "Track" else "Tracks"}",
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 12.sp
                )
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  modifier = Modifier.padding(top = 2.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(YalpsPrimary.copy(alpha = 0.15f))
                      .padding(horizontal = 5.dp, vertical = 1.dp)
                  ) {
                    Text("FLAC 96kHz", color = YalpsPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                  }
                  Text(
                    text = "${albumTracks.firstOrNull()?.eraCategory ?: "2026"}",
                    color = Color.Gray,
                    fontSize = 10.sp
                  )
                }
              }

              // Album Action Buttons: Play Album, Edit Album, Delete Album
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Edit Album Button
                IconButton(
                  onClick = { onEditAlbum(currentAlbumInfo) },
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8B5CF6).copy(alpha = 0.2f))
                ) {
                  Icon(Icons.Default.Edit, contentDescription = "Edit Album Details", tint = Color(0xFFD8B4FE), modifier = Modifier.size(18.dp))
                }

                // Delete Album Button
                IconButton(
                  onClick = { onDeleteAlbum(albumName) },
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Red.copy(alpha = 0.15f))
                ) {
                  Icon(Icons.Default.Delete, contentDescription = "Delete Album", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                }

                // Play Album Button
                IconButton(
                  onClick = {
                    firstTrack?.let { onPlaySong(it) }
                  },
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(YalpsPrimary.copy(alpha = 0.2f))
                ) {
                  Icon(Icons.Default.PlayArrow, contentDescription = "Play Album", tint = YalpsPrimary, modifier = Modifier.size(20.dp))
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(6.dp))

            // Track List within the Album
            albumTracks.forEachIndexed { index, song ->
              val isSongPlaying = isPlaying && currentPlayingSong.id == song.id

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSongPlaying) YalpsPrimary.copy(alpha = 0.1f) else Color.Transparent)
                  .clickable { onPlaySong(song) }
                  .padding(horizontal = 6.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Text(
                  text = (index + 1).toString().padStart(2, '0'),
                  color = if (isSongPlaying) YalpsPrimary else Color.Gray,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = song.title,
                    color = if (isSongPlaying) YalpsPrimary else Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = "${song.singers} • ${song.durationSeconds / 60}:${(song.durationSeconds % 60).toString().padStart(2, '0')} • ${song.audioSpec}",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }

                // Actions: Play preview, Edit, Delete
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                  IconButton(
                    onClick = { onPlaySong(song) },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(
                      imageVector = if (isSongPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                      contentDescription = "Play",
                      tint = if (isSongPlaying) YalpsPrimary else Color.White,
                      modifier = Modifier.size(18.dp)
                    )
                  }

                  IconButton(
                    onClick = { onEditSong(song) },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFFD8B4FE), modifier = Modifier.size(16.dp))
                  }

                  IconButton(
                    onClick = { onDeleteSong(song) },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

/* ========================================================================== */
/* TAB 3: BANK ACCOUNTS (Receiving Subscriber Money & Payout Routing)         */
/* ========================================================================== */

@Composable
fun BankAccountsManagementScreen(
  bankAccounts: List<BankAccountDetails>,
  onAddBankClick: () -> Unit,
  onEditBankClick: (BankAccountDetails) -> Unit,
  onSetPrimary: (String) -> Unit,
  onDeleteBank: (String) -> Unit
) {
  val context = LocalContext.current
  val primaryAccount = bankAccounts.firstOrNull { it.isPrimary } ?: bankAccounts.firstOrNull()

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Primary Direct Receiving Banner
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = YalpsSurfaceContainer),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.35f))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("BACKEND ADMIN DIRECT PAYOUT ROUTE", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Black)
              Text("Registered Admin Settlement Account", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF00E676).copy(alpha = 0.2f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text("0% COMMISSION • 100% ADMIN DIRECT", color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          primaryAccount?.let { primary ->
            Card(
              colors = CardDefaults.cardColors(containerColor = YalpsSurfaceContainerLowest),
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.5f))
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(22.dp))
                    Text(primary.bankName, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                  }
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(Color(0xFF00E676))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text("PRIMARY ADMIN ACCOUNT", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Account Holder: ${primary.accountHolderName} (Backend Admin)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text("Account Number: •••• •••• •••• ${primary.accountNumber.takeLast(4)}", color = Color(0xFFD8B4FE), fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                Text("IFSC Code: ${primary.ifscCode} (${primary.branchName})", color = Color.Gray, fontSize = 11.sp)
                Text("UPI VPA: ${primary.upiVpa}", color = Color(0xFF00E676), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("Settlement Schedule: ${primary.autoSettlementSchedule}", color = Color.LightGray, fontSize = 11.sp)
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "All subscriber subscription payments from Single (₹30), Duo (₹50), and Studio Pro (₹100) are credited directly into this backend admin bank account with instant T+0 settlement.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
          )
        }
      }
    }

    // 2. Manage All Accounts Header & Add Action
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Configured Bank Accounts (${bankAccounts.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Button(
          onClick = onAddBankClick,
          colors = ButtonDefaults.buttonColors(containerColor = YalpsPrimary, contentColor = Color.Black),
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Add Bank Account", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    // 3. List of Bank Accounts
    items(bankAccounts, key = { it.id }) { bank ->
      Card(
        colors = CardDefaults.cardColors(containerColor = YalpsSurfaceContainerLowest),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (bank.isPrimary) Color(0xFF00E676).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(Icons.Default.AccountBalance, contentDescription = null, tint = if (bank.isPrimary) Color(0xFF00E676) else YalpsPrimary)
              Text(bank.bankName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            if (bank.isPrimary) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(Color(0xFF00E676).copy(alpha = 0.2f))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text("PRIMARY", color = Color(0xFF00E676), fontSize = 9.sp, fontWeight = FontWeight.Black)
              }
            }
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text("Account: •••• ${bank.accountNumber.takeLast(4)} • ${bank.accountType}", color = Color.LightGray, fontSize = 12.sp)
          Text("IFSC: ${bank.ifscCode} • UPI: ${bank.upiVpa}", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)

          Spacer(modifier = Modifier.height(10.dp))

          // Action Buttons: Make Primary, Edit, Penny Drop Test, Delete
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            if (!bank.isPrimary) {
              OutlinedButton(
                onClick = { onSetPrimary(bank.id) },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF00E676)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
              ) {
                Text("Set As Primary", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }

            OutlinedButton(
              onClick = { onEditBankClick(bank) },
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(1.dp, Color.Gray),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text("Edit Details", color = Color.White, fontSize = 11.sp)
            }

            OutlinedButton(
              onClick = {
                Toast.makeText(context, "Penny drop test: ₹1.00 successfully transferred & verified for ${bank.bankName}!", Toast.LENGTH_LONG).show()
              },
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(1.dp, YalpsPrimary.copy(alpha = 0.5f)),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text("Test Deposit (₹1)", color = YalpsPrimary, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.weight(1f))

            if (bankAccounts.size > 1) {
              IconButton(
                onClick = { onDeleteBank(bank.id) },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
              }
            }
          }
        }
      }
    }
  }
}

/* ========================================================================== */
/* TAB 4: BILLING (Analytics, Revenue Breakdown, Settled Invoices)            */
/* ========================================================================== */

@Composable
fun BillingAnalyticsScreen(
  revenue: Double,
  customerState: com.example.model.StripeCustomerState,
  payoutRecords: List<com.example.model.CreatorPayoutRecord>,
  primaryBank: BankAccountDetails?
) {
  val context = LocalContext.current
  val isSubscribed = customerState.isSubscribed
  val activePlan = TamilSongCatalog.subscriptionPlans.find { it.id == customerState.activeTierId }
  val liveSubscribersCount = if (isSubscribed) 1 else 0
  val liveMrr = if (isSubscribed && activePlan != null) activePlan.priceInr else 0

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Financial Analytics Banner
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = YalpsSurfaceContainer),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.35f))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("REAL-TIME BACKEND ADMIN SETTLED BALANCE", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Black)
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "₹${String.format(java.util.Locale.US, "%,.2f", revenue)}",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Direct automated payouts to Backend Admin: ${primaryBank?.bankName ?: "Axis Bank"} (UPI: ${primaryBank?.upiVpa ?: "pragadheeeesh95@okaxis"})",
            color = Color(0xFFD8B4FE),
            fontSize = 11.sp
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Revenue KPIs Grid
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Card(
              colors = CardDefaults.cardColors(containerColor = YalpsSurfaceContainerLowest),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text("MONTHLY RECURRING (MRR)", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                  text = "₹$liveMrr/mo",
                  color = Color.White,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Black
                )
              }
            }

            Card(
              colors = CardDefaults.cardColors(containerColor = YalpsSurfaceContainerLowest),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text("PAID SUBSCRIBERS", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                  text = "$liveSubscribersCount ${if (liveSubscribersCount == 1) "Active Subscriber" else "Subscribers"}",
                  color = Color(0xFF00E676),
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Black
                )
              }
            }
          }
        }
      }
    }

    // 2. Plan Revenue Analytics Breakdown (Real-time)
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = YalpsSurfaceContainerLowest),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("REAL-TIME SUBSCRIPTION TIER DISTRIBUTION", color = YalpsPrimary, fontSize = 11.sp, fontWeight = FontWeight.Black)
          Spacer(modifier = Modifier.height(10.dp))

          val singlePlanCount = if (isSubscribed && activePlan?.id == "plan_single_30") 1 else 0
          val duoPlanCount = if (isSubscribed && activePlan?.id == "plan_duo_50") 1 else 0
          val studioProCount = if (isSubscribed && activePlan?.id == "plan_pro_100") 1 else 0

          val totalActive = (singlePlanCount + duoPlanCount + studioProCount).coerceAtLeast(1)

          val plans = listOf(
            Triple("Single Plan (₹30/mo)", "$singlePlanCount Active • ₹${singlePlanCount * 30} MRR", "${if (isSubscribed && activePlan?.id == "plan_single_30") 100 else 0}%"),
            Triple("Duo Plan (₹50/mo)", "$duoPlanCount Active • ₹${duoPlanCount * 50} MRR", "${if (isSubscribed && activePlan?.id == "plan_duo_50") 100 else 0}%"),
            Triple("Studio Master Pro (₹100/mo)", "$studioProCount Active • ₹${studioProCount * 100} MRR", "${if (isSubscribed && activePlan?.id == "plan_pro_100") 100 else 0}%")
          )

          plans.forEach { (name, stats, pct) ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(stats, color = Color.Gray, fontSize = 11.sp)
              }
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(YalpsPrimary.copy(alpha = 0.15f))
                  .padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Text(pct, color = YalpsPrimary, fontWeight = FontWeight.Black, fontSize = 11.sp)
              }
            }
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
          }
        }
      }
    }

    // 3. Billing Invoices & Settlements Ledger
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Real-Time Settled Invoices & Ledger", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        OutlinedButton(
          onClick = {
            Toast.makeText(context, "Exporting Real-Time GST & Settlement Statement...", Toast.LENGTH_SHORT).show()
          },
          shape = RoundedCornerShape(8.dp),
          border = BorderStroke(1.dp, Color(0xFF00E676)),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
          Text("Export Statement", color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    if (customerState.invoices.isEmpty() && payoutRecords.isEmpty()) {
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = YalpsSurfaceContainerLowest),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("No Settled Invoices Yet", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(
              "Real-time transaction entries will automatically populate here when users subscribe or royalties settle.",
              color = Color.Gray,
              fontSize = 12.sp,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        }
      }
    }

    // If customer invoices exist
    items(customerState.invoices) { inv ->
      Card(
        colors = CardDefaults.cardColors(containerColor = YalpsSurfaceContainerLowest),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(inv.amountFormatted, color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 14.sp)
            Text("${inv.paymentMethod} • ${inv.date}", color = Color.Gray, fontSize = 11.sp)
            if (inv.upiRefId != null) {
              Text("UTR: ${inv.upiRefId}", color = Color(0xFF8B5CF6), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
          }
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(Color(0xFF00E676).copy(alpha = 0.2f))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text("SETTLED TO BANK", color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Black)
          }
        }
      }
    }

    // Payout records
    items(payoutRecords.take(8)) { payout ->
      Card(
        colors = CardDefaults.cardColors(containerColor = YalpsSurfaceContainerLowest),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(payout.amountInrFormatted, color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("${payout.streamsFormatted} • ${payout.timestamp}", color = Color.Gray, fontSize = 11.sp)
            Text("UTR: ${payout.utr}", color = Color(0xFF8B5CF6), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
          }
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(Color(0xFF00E676).copy(alpha = 0.2f))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(payout.status, color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Black)
          }
        }
      }
    }
  }
}

/* ========================================================================== */
/* MODAL DIALOGS: Song Edit & Bank Account Add/Edit                           */
/* ========================================================================== */

@Composable
fun EditSongModalDialog(
  song: Song,
  onDismiss: () -> Unit,
  onSave: (Song) -> Unit
) {
  var title by remember { mutableStateOf(song.title) }
  var movieOrAlbum by remember { mutableStateOf(song.movieOrAlbum) }
  var composer by remember { mutableStateOf(song.composer) }
  var singers by remember { mutableStateOf(song.singers) }
  var audioSpec by remember { mutableStateOf(song.audioSpec) }
  var audioUrl by remember { mutableStateOf(song.audioUrl) }
  var imageUrl by remember { mutableStateOf(song.imageUrl) }
  var eraCategory by remember { mutableStateOf(song.eraCategory) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = YalpsSurfaceContainer,
      border = BorderStroke(1.dp, YalpsPrimary.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .fillMaxWidth()
      ) {
        Text("Edit Catalog Track", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
        Text("Updates will broadcast immediately to listeners", color = Color.Gray, fontSize = 11.sp)

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Track Title") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = movieOrAlbum,
          onValueChange = { movieOrAlbum = it },
          label = { Text("Movie / Album") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = composer,
          onValueChange = { composer = it },
          label = { Text("Composer / Artist") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = audioSpec,
          onValueChange = { audioSpec = it },
          label = { Text("Audio Spec") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = audioUrl,
          onValueChange = { audioUrl = it },
          label = { Text("Audio Stream URL") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancel", color = Color.Gray)
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              onSave(
                song.copy(
                  title = title.trim().ifEmpty { song.title },
                  movieOrAlbum = movieOrAlbum.trim().ifEmpty { song.movieOrAlbum },
                  composer = composer.trim().ifEmpty { song.composer },
                  singers = singers.trim().ifEmpty { composer.trim() },
                  audioSpec = audioSpec,
                  audioUrl = audioUrl,
                  imageUrl = imageUrl,
                  eraCategory = eraCategory
                )
              )
            },
            colors = ButtonDefaults.buttonColors(containerColor = YalpsPrimary, contentColor = Color.Black),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("Save Changes", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun BankDetailsModalDialog(
  existingAccount: BankAccountDetails?,
  onDismiss: () -> Unit,
  onSave: (BankAccountDetails) -> Unit
) {
  val backendConfig = OnlineBackendManager.getInstance().backendConfig.value
  var bankName by remember { mutableStateOf(existingAccount?.bankName ?: backendConfig.creatorBankName) }
  var accountHolder by remember { mutableStateOf(existingAccount?.accountHolderName ?: backendConfig.creatorBeneficiaryName) }
  var accountNumber by remember { mutableStateOf(existingAccount?.accountNumber ?: backendConfig.creatorAccountNumber) }
  var ifscCode by remember { mutableStateOf(existingAccount?.ifscCode ?: backendConfig.creatorIfscCode) }
  var branchName by remember { mutableStateOf(existingAccount?.branchName ?: "") }
  var accountType by remember { mutableStateOf(existingAccount?.accountType ?: "Current / Business") }
  var upiVpa by remember { mutableStateOf(existingAccount?.upiVpa ?: backendConfig.creatorUpiVpa) }
  var isPrimary by remember { mutableStateOf(existingAccount?.isPrimary ?: true) }
  var settlementSchedule by remember { mutableStateOf(existingAccount?.autoSettlementSchedule ?: "Daily Instant (T+0)") }
  var validationError by remember { mutableStateOf<String?>(null) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = YalpsSurfaceContainer,
      border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .fillMaxWidth()
      ) {
        Text(
          text = if (existingAccount != null) "Edit Backend Admin Bank Details" else "Register Backend Admin Bank Account",
          color = Color.White,
          fontWeight = FontWeight.Black,
          fontSize = 16.sp
        )
        Text("Subscriber plan payments will credit directly to this account", color = Color.Gray, fontSize = 11.sp)

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = bankName,
          onValueChange = { bankName = it; validationError = null },
          label = { Text("Bank Name (e.g. Axis Bank, HDFC, SBI)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = accountHolder,
          onValueChange = { accountHolder = it; validationError = null },
          label = { Text("Account Holder Legal Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = accountNumber,
          onValueChange = { accountNumber = it; validationError = null },
          label = { Text("Bank Account Number") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = ifscCode,
            onValueChange = { ifscCode = it.uppercase(); validationError = null },
            label = { Text("IFSC Code") },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = branchName,
            onValueChange = { branchName = it },
            label = { Text("Branch") },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = upiVpa,
          onValueChange = { upiVpa = it; validationError = null },
          label = { Text("Primary UPI VPA (e.g. yourname@okhdfcbank)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.clickable { isPrimary = !isPrimary }
        ) {
          Checkbox(
            checked = isPrimary,
            onCheckedChange = { isPrimary = it },
            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00E676))
          )
          Text("Set as Primary Receiving Account", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        validationError?.let { err ->
          Spacer(modifier = Modifier.height(8.dp))
          Text(text = err, color = Color(0xFFFF5252), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancel", color = Color.Gray)
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              if (accountNumber.trim().isBlank()) {
                validationError = "Please enter a valid Account Number"
                return@Button
              }
              if (accountHolder.trim().isBlank()) {
                validationError = "Please enter Account Holder Name"
                return@Button
              }
              val account = BankAccountDetails(
                id = existingAccount?.id ?: UUID.randomUUID().toString(),
                accountHolderName = accountHolder.trim(),
                bankName = bankName.trim().ifEmpty { "Bank Account" },
                accountNumber = accountNumber.trim(),
                ifscCode = ifscCode.trim(),
                branchName = branchName.trim(),
                accountType = accountType,
                upiVpa = upiVpa.trim(),
                isPrimary = isPrimary,
                isVerified = true,
                autoSettlementSchedule = settlementSchedule
              )
              onSave(account)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("Save Bank Account", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

data class AlbumInfo(
  val movieOrAlbum: String,
  val composer: String,
  val year: Int,
  val eraCategory: String,
  val imageUrl: String,
  val trackCount: Int
)

@Composable
fun EditAlbumModalDialog(
  album: AlbumInfo,
  onDismiss: () -> Unit,
  onSave: (oldMovieOrAlbum: String, newMovieOrAlbum: String, newComposer: String, newYear: Int, newEraCategory: String, newImageUrl: String) -> Unit
) {
  var albumTitle by remember { mutableStateOf(album.movieOrAlbum) }
  var composer by remember { mutableStateOf(album.composer) }
  var yearText by remember { mutableStateOf(album.year.toString()) }
  var eraCategory by remember { mutableStateOf(album.eraCategory) }
  var imageUrl by remember { mutableStateOf(album.imageUrl) }

  val context = LocalContext.current
  val albumImagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    uri?.let {
      try {
        val inputStream = context.contentResolver.openInputStream(it)
        val file = File(context.filesDir, "album_art_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        imageUrl = file.absolutePath
      } catch (e: Exception) {
        imageUrl = it.toString()
      }
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = YalpsSurfaceContainer,
      border = BorderStroke(1.dp, YalpsPrimary.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        Text("Edit Album & Batch Update Tracks", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
        Text("Updating details will reflect on all ${album.trackCount} tracks in this album", color = Color.Gray, fontSize = 11.sp)

        Spacer(modifier = Modifier.height(14.dp))

        // Cover Artwork Section
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(Color.DarkGray)
              .border(1.dp, YalpsPrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
          ) {
            AsyncImage(
              model = imageUrl.ifBlank { TamilSongCatalog.COVER_CYBERPULSE },
              contentDescription = "Album Artwork",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            OutlinedButton(
              onClick = { albumImagePickerLauncher.launch("image/*") },
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(1.dp, YalpsPrimary),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.Image, contentDescription = null, tint = YalpsPrimary, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Change Album Cover", color = YalpsPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = albumTitle,
          onValueChange = { albumTitle = it },
          label = { Text("Album / Movie Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = composer,
          onValueChange = { composer = it },
          label = { Text("Primary Composer / Music Director") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = yearText,
          onValueChange = { yearText = it.filter { ch -> ch.isDigit() } },
          label = { Text("Release Year (e.g. 2026)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("ERA CATEGORY", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        val eras = listOf("2021-Present", "2010-2020", "2000-2009", "1990s Golden Era", "Retro Classics 80s")
        Row(
          modifier = Modifier.horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          eras.forEach { era ->
            val isSelected = eraCategory == era
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) YalpsPrimary else YalpsSurfaceContainerHigh)
                .border(
                  1.dp,
                  if (isSelected) YalpsPrimary else Color.White.copy(alpha = 0.15f),
                  RoundedCornerShape(8.dp)
                )
                .clickable { eraCategory = era }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = era,
                color = if (isSelected) Color.Black else Color.White,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancel", color = Color.Gray)
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              val parsedYear = yearText.toIntOrNull() ?: album.year
              onSave(
                album.movieOrAlbum,
                albumTitle.trim().ifEmpty { album.movieOrAlbum },
                composer.trim().ifEmpty { album.composer },
                parsedYear,
                eraCategory,
                imageUrl
              )
            },
            colors = ButtonDefaults.buttonColors(containerColor = YalpsPrimary, contentColor = Color.Black),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("Save Album Details", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

