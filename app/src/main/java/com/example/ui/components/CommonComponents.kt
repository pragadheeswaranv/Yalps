package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.YalpsPrimary
import com.example.ui.theme.YalpsSecondary
import com.example.ui.theme.YalpsSurfaceContainer
import com.example.ui.theme.YalpsSurfaceContainerHigh
import com.example.ui.theme.YalpsTertiary

@Composable
fun LosslessBadge(
  text: String,
  modifier: Modifier = Modifier,
  isAtmos: Boolean = false
) {
  val accentColor = if (isAtmos) YalpsTertiary else YalpsSecondary
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(4.dp))
      .background(accentColor.copy(alpha = 0.15f))
      .border(0.8.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
      .padding(horizontal = 6.dp, vertical = 2.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
      Box(
        modifier = Modifier
          .size(5.dp)
          .clip(CircleShape)
          .background(accentColor)
      )
      Text(
        text = text,
        color = accentColor,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 0.5.sp
      )
    }
  }
}

@Composable
fun AnimatedEqualizer(
  isPlaying: Boolean,
  modifier: Modifier = Modifier,
  barCount: Int = 4,
  maxHeight: Dp = 18.dp,
  barColor: Color = YalpsSecondary
) {
  val transition = rememberInfiniteTransition(label = "eq_transition")
  val anim1 by transition.animateFloat(
    initialValue = 0.2f,
    targetValue = 0.95f,
    animationSpec = infiniteRepeatable(
      animation = tween(420, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "eq1"
  )
  val anim2 by transition.animateFloat(
    initialValue = 0.7f,
    targetValue = 0.3f,
    animationSpec = infiniteRepeatable(
      animation = tween(310, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "eq2"
  )
  val anim3 by transition.animateFloat(
    initialValue = 0.35f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(530, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "eq3"
  )
  val anim4 by transition.animateFloat(
    initialValue = 0.85f,
    targetValue = 0.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(380, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "eq4"
  )

  val heights = listOf(anim1, anim2, anim3, anim4)

  Row(
    modifier = modifier.height(maxHeight),
    horizontalArrangement = Arrangement.spacedBy(2.dp),
    verticalAlignment = Alignment.Bottom
  ) {
    for (i in 0 until barCount) {
      val h = if (isPlaying) heights[i % heights.size] else 0.25f
      Box(
        modifier = Modifier
          .width(2.5.dp)
          .height(maxHeight * h)
          .clip(RoundedCornerShape(1.dp))
          .background(barColor)
      )
    }
  }
}

@Composable
fun YalpsTopAppBar(
  onSearchClick: () -> Unit = {},
  onProfileClick: () -> Unit = {},
  onBackendClick: () -> Unit = {},
  isAdminBackend: Boolean = false,
  userInitial: String = "P",
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.background)
      .statusBarsPadding()
      .padding(horizontal = 16.dp, vertical = 10.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Brand Logo and Pro badge
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Monogram logo
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(
            Brush.linearGradient(
              listOf(Color(0xFF8B5CF6), Color(0xFF6366F1), Color(0xFFEC4899))
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Headphones,
          contentDescription = "Yalps Logo",
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "YALPS",
          color = Color.White,
          fontWeight = FontWeight.Black,
          fontSize = 18.sp,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isAdminBackend) Color(0xFF8B5CF6).copy(alpha = 0.2f) else YalpsPrimary.copy(alpha = 0.2f))
            .border(0.8.dp, if (isAdminBackend) Color(0xFF8B5CF6) else YalpsPrimary, RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 1.dp)
        ) {
          Text(
            text = if (isAdminBackend) "BACKEND" else "PRO",
            color = if (isAdminBackend) Color(0xFFD8B4FE) else YalpsPrimary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }

    // Top action buttons
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      if (isAdminBackend) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF8B5CF6).copy(alpha = 0.18f))
            .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .clickable { onBackendClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("👑", fontSize = 12.sp)
            Text("Backend App", color = Color(0xFFD8B4FE), fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      IconButton(
        onClick = onSearchClick,
        modifier = Modifier
          .size(38.dp)
          .testTag("top_search_btn")
      ) {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = "Search Songs",
          tint = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.size(22.dp)
        )
      }

      // Profile avatar button (authenticated state / login)
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(
            Brush.linearGradient(listOf(Color(0xFF8B5CF6), YalpsPrimary))
          )
          .border(1.dp, YalpsPrimary.copy(alpha = 0.6f), CircleShape)
          .clickable { onProfileClick() }
          .testTag("top_profile_btn"),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = userInitial.take(1).uppercase(),
          color = Color.Black,
          fontSize = 13.sp,
          fontWeight = FontWeight.Black
        )
      }
    }
  }
}
