package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.audio.AudioPlayerManager
import com.example.model.Song
import com.example.ui.theme.YalpsPrimary
import com.example.ui.theme.YalpsSecondary
import com.example.ui.theme.YalpsSurfaceContainer
import com.example.ui.theme.YalpsSurfaceContainerHigh
import com.example.ui.theme.YalpsSurfaceContainerLowest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeableSongRow(
  song: Song,
  isCurrent: Boolean,
  isPlaying: Boolean,
  isFavorite: Boolean,
  onClick: () -> Unit,
  onToggleFavorite: () -> Unit,
  modifier: Modifier = Modifier,
  onAddedToQueue: (() -> Unit)? = null
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val offsetX = remember { Animatable(0f) }
  val swipeThresholdPx = 180f

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
  ) {
    // 1. Background Action Area (Revealed during left-to-right swipe)
    Box(
      modifier = Modifier
        .matchParentSize()
        .clip(RoundedCornerShape(14.dp))
        .background(Color(0xFF00C853).copy(alpha = 0.85f))
        .padding(start = 16.dp),
      contentAlignment = Alignment.CenterStart
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.QueueMusic,
          contentDescription = "Add to Queue",
          tint = Color.Black,
          modifier = Modifier.size(24.dp)
        )
        Text(
          text = "Add to Queue",
          color = Color.Black,
          fontSize = 12.sp,
          fontWeight = FontWeight.Black
        )
      }
    }

    // 2. Foreground Song Card (Drags along user's finger from left to right)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
        .clip(RoundedCornerShape(14.dp))
        .background(if (isCurrent) YalpsSurfaceContainerHigh else YalpsSurfaceContainerLowest)
        .border(
          1.dp,
          if (isCurrent) YalpsPrimary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.05f),
          RoundedCornerShape(14.dp)
        )
        .pointerInput(song.id) {
          detectHorizontalDragGestures(
            onDragEnd = {
              coroutineScope.launch {
                if (offsetX.value >= swipeThresholdPx) {
                  // Add to Queue action triggered
                  AudioPlayerManager.getInstance().addToQueue(song)
                  Toast.makeText(context, "Added \"${song.title}\" to Queue", Toast.LENGTH_SHORT).show()
                  onAddedToQueue?.invoke()
                }
                // Smoothly snap back to center
                offsetX.animateTo(
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
                offsetX.animateTo(0f)
              }
            },
            onHorizontalDrag = { change, dragAmount ->
              change.consume()
              coroutineScope.launch {
                // Drag towards right (positive values only, dragging along finger)
                val newOffset = (offsetX.value + dragAmount).coerceIn(0f, 300f)
                offsetX.snapTo(newOffset)
              }
            }
          )
        }
        .clickable { onClick() }
        .padding(10.dp)
        .testTag("swipeable_song_${song.id}")
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Album Art Thumbnail
        Box(
          modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(YalpsSurfaceContainer)
        ) {
          AsyncImage(
            model = song.imageUrl,
            contentDescription = song.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )

          if (isCurrent && isPlaying) {
            Box(
              modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(2.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(3.dp)
            ) {
              AnimatedEqualizer(isPlaying = true, barCount = 3, maxHeight = 10.dp)
            }
          }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Song Title, Movie & Composer
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = song.title,
            color = if (isCurrent) YalpsPrimary else Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = "${song.movieOrAlbum} (${song.year}) • ${song.composer}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.height(2.dp))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            LosslessBadge(text = song.audioSpec.take(12), isAtmos = song.isSpatial)
          }
        }

        // Favorite Toggle Icon
        IconButton(
          onClick = onToggleFavorite,
          modifier = Modifier
            .size(36.dp)
            .testTag("fav_btn_${song.id}")
        ) {
          Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Toggle Favorite",
            tint = if (isFavorite) Color(0xFFEC4899) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}
