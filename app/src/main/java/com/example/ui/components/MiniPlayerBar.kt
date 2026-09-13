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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Song
import com.example.ui.theme.YalpsPrimary
import com.example.ui.theme.YalpsSecondary
import com.example.ui.theme.YalpsSurfaceContainerHigh
import com.example.ui.theme.YalpsSurfaceContainerLowest

@Composable
fun MiniPlayerBar(
  currentSong: Song,
  isPlaying: Boolean,
  progress: Float,
  isFavorite: Boolean,
  onPlayPauseClick: () -> Unit,
  onFavoriteClick: () -> Unit,
  onExpandPlayer: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 10.dp, vertical = 6.dp)
      .clip(RoundedCornerShape(16.dp))
      .background(
        Brush.verticalGradient(
          listOf(
            YalpsSurfaceContainerHigh.copy(alpha = 0.95f),
            YalpsSurfaceContainerLowest.copy(alpha = 0.98f)
          )
        )
      )
      .border(1.dp, YalpsPrimary.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
      .clickable { onExpandPlayer() }
      .testTag("mini_player_bar")
  ) {
    Column {
      // Progress line at very top
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(2.dp)
          .background(Color.White.copy(alpha = 0.1f))
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth(progress.coerceIn(0f, 1f))
            .height(2.dp)
            .background(
              Brush.horizontalGradient(
                listOf(YalpsPrimary, YalpsSecondary)
              )
            )
        )
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Thumbnail & Title & EQ
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          // Album thumbnail
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0xFF20202A))
          ) {
            AsyncImage(
              model = currentSong.imageUrl,
              contentDescription = currentSong.title,
              contentScale = ContentScale.Crop,
              modifier = Modifier.size(46.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                text = currentSong.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
              )
              if (isPlaying) {
                AnimatedEqualizer(isPlaying = true, barCount = 3, maxHeight = 12.dp, barColor = YalpsSecondary)
              }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(
                text = "${currentSong.composer} • ${currentSong.movieOrAlbum}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
              )
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(3.dp))
                  .background(YalpsSecondary.copy(alpha = 0.15f))
                  .padding(horizontal = 4.dp, vertical = 1.dp)
              ) {
                Text(
                  text = "HI-RES",
                  color = YalpsSecondary,
                  fontSize = 8.sp,
                  fontWeight = FontWeight.Black
                )
              }
            }
          }
        }

        // Action controls (Favorite, Play/Pause)
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          IconButton(
            onClick = onFavoriteClick,
            modifier = Modifier
              .size(36.dp)
              .testTag("mini_player_fav_btn")
          ) {
            Icon(
              imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
              contentDescription = "Favorite Song",
              tint = if (isFavorite) Color(0xFFEC4899) else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp)
            )
          }

          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(
                Brush.linearGradient(
                  listOf(YalpsPrimary, Color(0xFF8B5CF6))
                )
              )
              .clickable { onPlayPauseClick() }
              .testTag("mini_player_play_pause_btn"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = if (isPlaying) "Pause" else "Play",
              tint = Color.Black,
              modifier = Modifier.size(24.dp)
            )
          }
        }
      }
    }
  }
}
