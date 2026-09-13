package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.audio.AudioPlayerManager
import com.example.model.Song
import com.example.ui.theme.YalpsBackground
import com.example.ui.theme.YalpsPrimary
import com.example.ui.theme.YalpsSecondary
import com.example.ui.theme.YalpsSurfaceContainer
import com.example.ui.theme.YalpsSurfaceContainerHigh
import com.example.ui.theme.YalpsSurfaceContainerLowest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailSheet(
  albumName: String,
  albumSongs: List<Song>,
  currentSong: Song,
  isPlaying: Boolean,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val playerManager = AudioPlayerManager.getInstance()
  val sampleSong = albumSongs.firstOrNull() ?: Song.EMPTY

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    containerColor = YalpsSurfaceContainerLowest,
    modifier = modifier
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 32.dp)
    ) {
      // Header: Close button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        LosslessBadge(text = "MASTER ALBUM", isAtmos = albumSongs.any { it.isSpatial })
        IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_album_sheet_btn")) {
          Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Album Hero Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        AsyncImage(
          model = sampleSong.imageUrl,
          contentDescription = albumName,
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .size(110.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, YalpsPrimary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
        )

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = albumName,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Composer: ${sampleSong.composer}",
            color = YalpsPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = "${sampleSong.year} • ${albumSongs.size} Tracks • Lossless FLAC",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Scoped Playback Scope Indicator Banner
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0xFF8B5CF6).copy(alpha = 0.15f))
          .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
          .padding(horizontal = 12.dp, vertical = 8.dp)
      ) {
        Text(
          text = "💿 Album Mode: Playing inside this album plays only its tracks.",
          color = Color(0xFFC084FC),
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Play All / Shuffle Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = {
            playerManager.playAlbum(albumName, albumSongs, startIndex = 0)
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(containerColor = YalpsPrimary, contentColor = Color.Black),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.weight(1f).testTag("play_album_btn")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
            Text("Play Album", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        }

        OutlinedButton(
          onClick = {
            playerManager.playAlbum(albumName, albumSongs.shuffled(), startIndex = 0)
            onDismiss()
          },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
          modifier = Modifier.weight(1f).testTag("shuffle_album_btn")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(imageVector = Icons.Default.Shuffle, contentDescription = null, tint = YalpsSecondary, modifier = Modifier.size(18.dp))
            Text("Shuffle Album", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "TRACKLIST (${albumSongs.size})",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Album Songs Lazy List
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .height(260.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        itemsIndexed(albumSongs) { index, song ->
          val isCurrent = song.id == currentSong.id
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(if (isCurrent) YalpsPrimary.copy(alpha = 0.15f) else YalpsSurfaceContainer)
              .border(
                1.dp,
                if (isCurrent) YalpsPrimary.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.04f),
                RoundedCornerShape(10.dp)
              )
              .clickable {
                // Clicking inside album plays this track and scopes queue to this album only!
                playerManager.playAlbum(albumName, albumSongs, startIndex = index)
                onDismiss()
              }
              .padding(horizontal = 12.dp, vertical = 10.dp)
              .testTag("album_track_$index"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text(
                text = "${index + 1}",
                color = if (isCurrent) YalpsPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(20.dp)
              )

              Column {
                Text(
                  text = song.title,
                  color = if (isCurrent) YalpsPrimary else Color.White,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = song.singers.ifBlank { song.composer },
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 11.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }

            if (isCurrent && isPlaying) {
              AnimatedEqualizer(isPlaying = true, barCount = 3)
            } else {
              val min = song.durationSeconds / 60
              val sec = song.durationSeconds % 60
              Text(
                text = String.format("%d:%02d", min, sec),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
