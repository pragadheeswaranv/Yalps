package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.audio.AudioPlayerManager
import com.example.data.TamilSongCatalog
import com.example.model.Song
import com.example.ui.components.AnimatedEqualizer
import com.example.ui.components.LosslessBadge
import com.example.ui.theme.YalpsBackground
import com.example.ui.theme.YalpsPrimary
import com.example.ui.theme.YalpsSecondary
import com.example.ui.theme.YalpsSurfaceContainer
import com.example.ui.theme.YalpsSurfaceContainerHigh
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.example.ui.theme.YalpsSurfaceContainerLowest
import com.example.ui.theme.YalpsTertiary
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
  song: Song,
  isPlaying: Boolean,
  currentPositionSeconds: Int,
  isFavorite: Boolean,
  isShuffle: Boolean,
  isRepeat: Boolean,
  queue: List<Song>,
  onClose: () -> Unit,
  onPlayPauseToggle: () -> Unit,
  onSeek: (Int) -> Unit,
  onSkipNext: () -> Unit,
  onSkipPrevious: () -> Unit,
  onFavoriteToggle: () -> Unit,
  onShuffleToggle: () -> Unit,
  onRepeatToggle: () -> Unit,
  onSelectQueueSong: (Song) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val coverOffsetX = remember(song.id) { Animatable(0f) }
  var showLyricsSheet by remember { mutableStateOf(false) }
  var showQueueSheet by remember { mutableStateOf(false) }
  var showDeviceSheet by remember { mutableStateOf(false) }
  var showOptionsSheet by remember { mutableStateOf(false) }
  var showArtistSheet by remember { mutableStateOf(false) }

  val duration = song.durationSeconds.coerceAtLeast(1)
  val progress = (currentPositionSeconds.toFloat() / duration).coerceIn(0f, 1f)

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(YalpsBackground)
  ) {
    // Ambient Background Blur Glow from Album Art
    AsyncImage(
      model = song.imageUrl,
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .fillMaxSize()
        .blur(60.dp)
        .background(Color.Black.copy(alpha = 0.7f))
    )

    // Dark gradient overlay to preserve contrast
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            listOf(
              Color(0xCC0D0D15),
              Color(0xEE13131B),
              Color(0xFF0D0D15)
            )
          )
        )
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // 1. Top Header Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onClose,
          modifier = Modifier.testTag("close_now_playing_btn")
        ) {
          Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Minimize Player",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
          )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "NOW PLAYING",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = { showOptionsSheet = true },
            modifier = Modifier.testTag("player_options_top_btn")
          ) {
            Icon(
              imageVector = Icons.Default.MoreVert,
              contentDescription = "Player Options",
              tint = Color.White,
              modifier = Modifier.size(24.dp)
            )
          }

          IconButton(
            onClick = { showDeviceSheet = true },
            modifier = Modifier.testTag("device_picker_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Cast,
              contentDescription = "Cast & Audio Routing",
              tint = YalpsTertiary,
              modifier = Modifier.size(24.dp)
            )
          }
        }
      }

      // 2. Center Stage Album Artwork with Touch Swipe Gesture
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Box(
          modifier = Modifier
            .size(260.dp),
          contentAlignment = Alignment.Center
        ) {
          // Pulsing Glow Halo
          Box(
            modifier = Modifier
              .size(255.dp)
              .clip(RoundedCornerShape(26.dp))
              .background(
                Brush.radialGradient(
                  listOf(
                    YalpsPrimary.copy(alpha = if (isPlaying) 0.35f else 0.12f),
                    Color(0xFF8B5CF6).copy(alpha = if (isPlaying) 0.2f else 0.05f),
                    Color.Transparent
                  )
                )
              )
          )

          // Album Artwork Card with Swipe-to-Change Track
          Box(
            modifier = Modifier
              .offset { IntOffset(coverOffsetX.value.roundToInt(), 0) }
              .size(240.dp)
              .clip(RoundedCornerShape(22.dp))
              .background(YalpsSurfaceContainer)
              .border(
                1.5.dp,
                if (isPlaying) YalpsPrimary.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.15f),
                RoundedCornerShape(22.dp)
              )
              .pointerInput(song.id) {
                detectHorizontalDragGestures(
                  onDragEnd = {
                    coroutineScope.launch {
                      val currentVal = coverOffsetX.value
                      if (currentVal >= 110f) {
                        // Swiped Left to Right -> Previous Song
                        onSkipPrevious()
                      } else if (currentVal <= -110f) {
                        // Swiped Right to Left -> Next Song
                        onSkipNext()
                      }
                      coverOffsetX.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                          dampingRatio = Spring.DampingRatioMediumBouncy,
                          stiffness = Spring.StiffnessLow
                        )
                      )
                    }
                  },
                  onDragCancel = {
                    coroutineScope.launch {
                      coverOffsetX.animateTo(0f)
                    }
                  },
                  onHorizontalDrag = { change, dragAmount ->
                    change.consume()
                    coroutineScope.launch {
                      val newOffset = (coverOffsetX.value + dragAmount).coerceIn(-240f, 240f)
                      coverOffsetX.snapTo(newOffset)
                    }
                  }
                )
              }
              .testTag("now_playing_album_cover"),
            contentAlignment = Alignment.Center
          ) {
            AsyncImage(
              model = song.imageUrl,
              contentDescription = song.title,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )

            // Dynamic visual badge while swiping
            if (coverOffsetX.value > 40f) {
              Box(
                modifier = Modifier
                  .align(Alignment.CenterStart)
                  .padding(12.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color.Black.copy(alpha = 0.8f))
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = null, tint = YalpsPrimary, modifier = Modifier.size(16.dp))
                  Text("PREV", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
              }
            } else if (coverOffsetX.value < -40f) {
              Box(
                modifier = Modifier
                  .align(Alignment.CenterEnd)
                  .padding(12.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color.Black.copy(alpha = 0.8f))
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  Text("NEXT", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                  Icon(imageVector = Icons.Default.SkipNext, contentDescription = null, tint = YalpsPrimary, modifier = Modifier.size(16.dp))
                }
              }
            }
          }
        }
      }

      // 3. Track Details & Favorite Heart
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = song.title,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.height(3.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "${song.composer} • ${song.movieOrAlbum} (${song.year})",
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = "Verified Master",
              tint = YalpsPrimary,
              modifier = Modifier.size(16.dp)
            )
          }
          Text(
            text = "Vocals: ${song.singers}",
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        IconButton(
          onClick = onFavoriteToggle,
          modifier = Modifier.testTag("now_playing_fav_btn")
        ) {
          Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Toggle Favorite",
            tint = if (isFavorite) Color(0xFFEC4899) else Color.White,
            modifier = Modifier.size(28.dp)
          )
        }
      }

      // 4. Progress Scrubber with Sleek Thin Timeline and Centered Round Dot Thumb
      Column(modifier = Modifier.fillMaxWidth()) {
        // Scrubber slider with clean line and vertically centered circular dot
        Slider(
          value = progress.coerceIn(0f, 1f),
          onValueChange = { frac ->
            onSeek((frac * duration).toInt())
          },
          thumb = {
            Box(
              modifier = Modifier
                .size(16.dp),
              contentAlignment = Alignment.Center
            ) {
              Box(
                modifier = Modifier
                  .size(12.dp)
                  .clip(CircleShape)
                  .background(YalpsPrimary)
                  .border(1.5.dp, Color.White, CircleShape)
              )
            }
          },
          track = { sliderState ->
            SliderDefaults.Track(
              sliderState = sliderState,
              modifier = Modifier.height(4.dp),
              colors = SliderDefaults.colors(
                activeTrackColor = YalpsPrimary,
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
              ),
              thumbTrackGapSize = 0.dp,
              trackInsideCornerSize = 0.dp
            )
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("audio_scrubber_slider")
        )

        // Time labels
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          val curM = currentPositionSeconds / 60
          val curS = currentPositionSeconds % 60
          val durM = duration / 60
          val durS = duration % 60

          Text(
            text = "%02d:%02d".format(curM, curS),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "%02d:%02d".format(durM, durS),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      // 5. Main Transport Controls
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onShuffleToggle,
          modifier = Modifier.testTag("shuffle_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Shuffle,
            contentDescription = "Shuffle",
            tint = if (isShuffle) YalpsSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
          )
        }

        IconButton(
          onClick = onSkipPrevious,
          modifier = Modifier.size(48.dp).testTag("prev_song_btn")
        ) {
          Icon(
            imageVector = Icons.Default.SkipPrevious,
            contentDescription = "Previous Song",
            tint = Color.White,
            modifier = Modifier.size(36.dp)
          )
        }

        // Hero Play / Pause Glow Button
        Box(
          modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(
              Brush.linearGradient(
                listOf(YalpsPrimary, Color(0xFF8B5CF6))
              )
            )
            .border(2.dp, YalpsPrimary.copy(alpha = 0.5f), CircleShape)
            .clickable { onPlayPauseToggle() }
            .testTag("hero_play_pause_btn"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = Color.Black,
            modifier = Modifier.size(38.dp)
          )
        }

        IconButton(
          onClick = onSkipNext,
          modifier = Modifier.size(48.dp).testTag("next_song_btn")
        ) {
          Icon(
            imageVector = Icons.Default.SkipNext,
            contentDescription = "Next Song",
            tint = Color.White,
            modifier = Modifier.size(36.dp)
          )
        }

        IconButton(
          onClick = onRepeatToggle,
          modifier = Modifier.testTag("repeat_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Repeat,
            contentDescription = "Repeat",
            tint = if (isRepeat) YalpsSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
          )
        }
      }

      // 6. Bottom Utility Controls (Audio Route, Lyrics, Queue)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(YalpsSurfaceContainerLowest.copy(alpha = 0.8f))
          .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
          .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Real-time Connected User Device Indicator
        val deviceModel = remember {
          val manufacturer = android.os.Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
          "$manufacturer ${android.os.Build.MODEL}"
        }
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier
            .clickable { showDeviceSheet = true }
            .testTag("device_indicator_pill")
        ) {
          Icon(
            imageVector = Icons.Default.PhoneAndroid,
            contentDescription = "Playback Device",
            tint = YalpsTertiary,
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = deviceModel,
            color = YalpsTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Lyrics Toggle
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(YalpsPrimary.copy(alpha = 0.15f))
              .clickable { showLyricsSheet = true }
              .padding(horizontal = 8.dp, vertical = 4.dp)
              .testTag("lyrics_btn")
          ) {
            Text(
              text = "LYRICS",
              color = YalpsPrimary,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }

          // Queue Button
          IconButton(
            onClick = { showQueueSheet = true },
            modifier = Modifier.size(32.dp).testTag("queue_btn")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.QueueMusic,
              contentDescription = "Queue",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }

    // Modal Sheet 1: Synchronized Lyrics
    if (showLyricsSheet) {
      ModalBottomSheet(
        onDismissRequest = { showLyricsSheet = false },
        sheetState = rememberModalBottomSheetState(),
        containerColor = YalpsSurfaceContainerHigh
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "SYNCHRONIZED LYRICS",
              color = YalpsPrimary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "LIVE SYNC",
              color = YalpsSecondary,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          if (song.lyrics.isEmpty()) {
            Text(
              text = "Instrumental track. Lyrics syncing in progress...",
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 14.sp
            )
          } else {
            LazyColumn(
              modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
            ) {
              itemsIndexed(song.lyrics) { index, line ->
                val isActive = (currentPositionSeconds >= line.timestampSeconds) &&
                  (index == song.lyrics.size - 1 || currentPositionSeconds < song.lyrics[index + 1].timestampSeconds)

                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onSeek(line.timestampSeconds) }
                ) {
                  Text(
                    text = line.text,
                    color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontSize = if (isActive) 18.sp else 15.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                  )
                  if (line.translation.isNotEmpty()) {
                    Text(
                      text = line.translation,
                      color = if (isActive) YalpsSecondary else Color.Transparent,
                      fontSize = 12.sp
                    )
                  }
                }
              }
            }
          }
          Spacer(modifier = Modifier.height(20.dp))
        }
      }
    }

    // Modal Sheet 2: Queue & Up Next
    if (showQueueSheet) {
      ModalBottomSheet(
        onDismissRequest = { showQueueSheet = false },
        sheetState = rememberModalBottomSheetState(),
        containerColor = YalpsSurfaceContainerHigh
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
          Text(
            text = "PLAYING QUEUE (${queue.size} TRACKS)",
            color = YalpsSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.height(12.dp))

          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .height(320.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            itemsIndexed(queue) { _, item ->
              val isItemCurrent = item.id == song.id
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (isItemCurrent) YalpsPrimary.copy(alpha = 0.15f) else Color.Transparent)
                  .clickable {
                    onSelectQueueSong(item)
                    showQueueSheet = false
                  }
                  .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                      .size(40.dp)
                      .clip(RoundedCornerShape(6.dp))
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = item.title,
                      color = if (isItemCurrent) YalpsPrimary else Color.White,
                      fontSize = 14.sp,
                      fontWeight = if (isItemCurrent) FontWeight.Bold else FontWeight.Normal,
                      maxLines = 1
                    )
                    Text(
                      text = "${item.composer} • ${item.movieOrAlbum}",
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      fontSize = 11.sp,
                      maxLines = 1
                    )
                  }
                }
                if (isItemCurrent) {
                  AnimatedEqualizer(isPlaying = isPlaying, barCount = 3)
                }
              }
            }
          }
        }
      }
    }

    // Modal Sheet 3: Connected Playback Device (Real-Time from Device)
    if (showDeviceSheet) {
      data class AudioOutputDevice(
        val name: String,
        val subtitle: String,
        val isBluetooth: Boolean,
        val isSpeaker: Boolean,
        val isActive: Boolean
      )

      val audioManager = remember { context.getSystemService(android.content.Context.AUDIO_SERVICE) as? android.media.AudioManager }
      val realAudioDevices = remember(showDeviceSheet) {
        val list = mutableListOf<AudioOutputDevice>()
        var foundSpeaker = false
        var foundBluetooth = false

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && audioManager != null) {
          try {
            val outputs = audioManager.getDevices(android.media.AudioManager.GET_DEVICES_OUTPUTS)
            outputs.forEach { dev ->
              when (dev.type) {
                android.media.AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> {
                  foundSpeaker = true
                  list.add(
                    AudioOutputDevice(
                      name = "Built-in Device Speaker",
                      subtitle = "Internal Stereo Speakers (${android.os.Build.MODEL})",
                      isBluetooth = false,
                      isSpeaker = true,
                      isActive = true
                    )
                  )
                }
                android.media.AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                android.media.AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> {
                  foundBluetooth = true
                  val btName = dev.productName.toString().ifBlank { "Bluetooth Audio Device" }
                  list.add(
                    AudioOutputDevice(
                      name = btName,
                      subtitle = "Connected via Bluetooth A2DP / Lossless LDAC",
                      isBluetooth = true,
                      isSpeaker = false,
                      isActive = true
                    )
                  )
                }
                android.media.AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                android.media.AudioDeviceInfo.TYPE_WIRED_HEADSET -> {
                  list.add(
                    AudioOutputDevice(
                      name = "Wired 3.5mm Headset",
                      subtitle = "Direct Analog Output",
                      isBluetooth = false,
                      isSpeaker = false,
                      isActive = true
                    )
                  )
                }
                android.media.AudioDeviceInfo.TYPE_USB_DEVICE,
                android.media.AudioDeviceInfo.TYPE_USB_HEADSET -> {
                  val usbName = dev.productName.toString().ifBlank { "USB DAC / Audio Interface" }
                  list.add(
                    AudioOutputDevice(
                      name = usbName,
                      subtitle = "High-Res USB Output",
                      isBluetooth = false,
                      isSpeaker = false,
                      isActive = true
                    )
                  )
                }
                else -> {
                  val otherName = dev.productName.toString().ifBlank { "System Audio Output" }
                  list.add(
                    AudioOutputDevice(
                      name = otherName,
                      subtitle = "Audio Endpoint",
                      isBluetooth = false,
                      isSpeaker = false,
                      isActive = true
                    )
                  )
                }
              }
            }
          } catch (e: Exception) {
            // Ignore
          }
        }

        // Always ensure Built-in Speaker is explicitly listed
        if (!foundSpeaker) {
          list.add(
            0,
            AudioOutputDevice(
              name = "Built-in Device Speaker",
              subtitle = "Internal Stereo Speakers (${android.os.Build.MODEL})",
              isBluetooth = false,
              isSpeaker = true,
              isActive = true
            )
          )
        }

        // Check if paired/connected Bluetooth devices are queryable via BluetoothAdapter
        try {
          val btAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
          if (btAdapter != null && btAdapter.isEnabled) {
            val bonded = btAdapter.bondedDevices
            bonded?.forEach { bDev ->
              val bName = bDev.name ?: "Bluetooth Device"
              // Add if not already listed
              if (list.none { it.name.equals(bName, ignoreCase = true) }) {
                list.add(
                  AudioOutputDevice(
                    name = bName,
                    subtitle = "Paired Bluetooth Device",
                    isBluetooth = true,
                    isSpeaker = false,
                    isActive = false
                  )
                )
              }
            }
          }
        } catch (e: SecurityException) {
          // Normal permission guard
        } catch (e: Exception) {
          // Ignore
        }

        list
      }

      ModalBottomSheet(
        onDismissRequest = { showDeviceSheet = false },
        sheetState = rememberModalBottomSheetState(),
        containerColor = YalpsSurfaceContainerHigh
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
          Text(
            text = "CONNECTED AUDIO DEVICES",
            color = YalpsTertiary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.height(14.dp))

          // Real User Device Card
          Card(
            colors = CardDefaults.cardColors(containerColor = YalpsSurfaceContainerLowest),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, YalpsTertiary.copy(alpha = 0.4f))
          ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(YalpsTertiary.copy(alpha = 0.2f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = YalpsTertiary,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "${android.os.Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} ${android.os.Build.MODEL}",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "Android ${android.os.Build.VERSION.RELEASE} • Active Session",
                    color = YalpsTertiary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                  )
                }
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = "Active",
                  tint = YalpsTertiary,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = "SPEAKERS & BLUETOOTH OUTPUTS",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.height(8.dp))

          realAudioDevices.forEach { devItem ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(YalpsSurfaceContainerLowest.copy(alpha = 0.6f))
                .padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
              ) {
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                      if (devItem.isBluetooth) Color(0xFF00E5FF).copy(alpha = 0.15f)
                      else YalpsPrimary.copy(alpha = 0.15f)
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Speaker,
                    contentDescription = null,
                    tint = if (devItem.isBluetooth) Color(0xFF00E5FF) else YalpsPrimary,
                    modifier = Modifier.size(18.dp)
                  )
                }
                Column {
                  Text(
                    text = devItem.name,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = devItem.subtitle,
                    color = Color.Gray,
                    fontSize = 10.sp
                  )
                }
              }
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(
                    if (devItem.isActive) Color(0xFF00E676).copy(alpha = 0.2f)
                    else Color.White.copy(alpha = 0.08f)
                  )
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = if (devItem.isActive) "ACTIVE" else "PAIRED",
                  color = if (devItem.isActive) Color(0xFF00E676) else Color.Gray,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }

    // Modal Sheet 4: Player Options (Share, Add to queue, Go to queue, Go to Artist)
    if (showOptionsSheet) {
      ModalBottomSheet(
        onDismissRequest = { showOptionsSheet = false },
        sheetState = rememberModalBottomSheetState(),
        containerColor = YalpsSurfaceContainerHigh
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Track Header
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 10.dp)
          ) {
            AsyncImage(
              model = song.imageUrl,
              contentDescription = song.title,
              modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp)),
              contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = song.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "${song.composer} • ${song.movieOrAlbum}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          // 1. Share link
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .clickable {
                showOptionsSheet = false
                val sendIntent = Intent().apply {
                  action = Intent.ACTION_SEND
                  putExtra(
                    Intent.EXTRA_TEXT,
                    "Listen to '${song.title}' by ${song.composer} on YALPS: https://yalps.audio/track/${song.id}"
                  )
                  type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Share ${song.title}")
                context.startActivity(shareIntent)
              }
              .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Share link",
              tint = YalpsPrimary,
              modifier = Modifier.size(24.dp)
            )
            Text(
              text = "Share link",
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          // 2. Add to queue
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .clickable {
                AudioPlayerManager.getInstance().addToQueue(song)
                Toast.makeText(context, "Added '${song.title}' to queue", Toast.LENGTH_SHORT).show()
                showOptionsSheet = false
              }
              .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.QueueMusic,
              contentDescription = "Add to queue",
              tint = YalpsSecondary,
              modifier = Modifier.size(24.dp)
            )
            Text(
              text = "Add to queue",
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          // 3. Go to queue
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .clickable {
                showOptionsSheet = false
                showQueueSheet = true
              }
              .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.List,
              contentDescription = "Go to queue",
              tint = YalpsTertiary,
              modifier = Modifier.size(24.dp)
            )
            Text(
              text = "Go to queue",
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          // 4. Go to Artist
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .clickable {
                showOptionsSheet = false
                showArtistSheet = true
              }
              .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "Go to Artist",
              tint = YalpsPrimary,
              modifier = Modifier.size(24.dp)
            )
            Text(
              text = "Go to Artist",
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          Spacer(modifier = Modifier.height(28.dp))
        }
      }
    }

    // Modal Sheet 5: Artist Details & Songs
    if (showArtistSheet) {
      val artistSongs = remember(song.composer) {
        val filtered = TamilSongCatalog.allSongs.filter {
          it.composer.contains(song.composer, ignoreCase = true) ||
            song.composer.contains(it.composer, ignoreCase = true)
        }
        if (filtered.isNotEmpty()) filtered else TamilSongCatalog.allSongs.take(6)
      }

      ModalBottomSheet(
        onDismissRequest = { showArtistSheet = false },
        sheetState = rememberModalBottomSheetState(),
        containerColor = YalpsSurfaceContainerHigh
      ) {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Artist Header
          item {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(16.dp),
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(64.dp)
                  .clip(CircleShape)
                  .background(
                    Brush.linearGradient(
                      listOf(Color(0xFF8B5CF6), YalpsPrimary, YalpsSecondary)
                    )
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = null,
                  tint = Color.Black,
                  modifier = Modifier.size(34.dp)
                )
              }

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = song.composer,
                  color = Color.White,
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Black
                )
                Text(
                  text = "Composer & Music Director",
                  color = YalpsSecondary,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium
                )
                Text(
                  text = "${artistSongs.size} songs available on YALPS",
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 11.sp
                )
              }
            }
          }

          item {
            Text(
              text = "POPULAR SONGS BY ${song.composer.uppercase()}",
              color = YalpsPrimary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              letterSpacing = 1.sp
            )
          }

          items(artistSongs.size) { idx ->
            val track = artistSongs[idx]
            val isCurrentTrack = track.id == song.id
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (isCurrentTrack) YalpsPrimary.copy(alpha = 0.15f) else YalpsSurfaceContainerLowest)
                .clickable {
                  onSelectQueueSong(track)
                  showArtistSheet = false
                }
                .padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
              ) {
                AsyncImage(
                  model = track.imageUrl,
                  contentDescription = track.title,
                  modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp)),
                  contentScale = ContentScale.Crop
                )
                Column {
                  Text(
                    text = track.title,
                    color = if (isCurrentTrack) YalpsPrimary else Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = "${track.movieOrAlbum} (${track.year})",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = track.singers,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }

              Icon(
                imageVector = if (isCurrentTrack && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Play song",
                tint = if (isCurrentTrack) YalpsPrimary else Color.White,
                modifier = Modifier.size(24.dp)
              )
            }
          }

          item {
            Spacer(modifier = Modifier.height(28.dp))
          }
        }
      }
    }
  }
}
