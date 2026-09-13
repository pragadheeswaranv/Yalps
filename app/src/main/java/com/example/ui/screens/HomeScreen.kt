package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.TamilSongCatalog
import com.example.model.DailyMix
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
import com.example.ui.theme.YalpsTertiary
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.runtime.collectAsState

import androidx.compose.material.icons.filled.SearchOff
import com.example.audio.AudioPlayerManager
import com.example.ui.components.AlbumDetailSheet
import com.example.ui.components.SwipeableSongRow

@Composable
fun HomeScreen(
  songs: List<Song>,
  currentSong: Song,
  isPlaying: Boolean,
  onSongClick: (Song) -> Unit,
  onPlayFeaturedDrop: () -> Unit,
  modifier: Modifier = Modifier
) {
  val stripeManager = remember { StripePaymentsManager.getInstance() }
  val playerManager = remember { AudioPlayerManager.getInstance() }
  val favoriteIds by playerManager.favoriteIds.collectAsState()
  val customerState by stripeManager.customerState.collectAsState()

  var selectedFilter by remember { mutableStateOf("All") }
  var selectedAlbumForSheet by remember { mutableStateOf<Pair<String, List<Song>>?>(null) }

  val filterChips = listOf(
    "All",
    "Top Hits",
    "A.R. Rahman",
    "Anirudh",
    "Yuvan",
    "Harris Jayaraj",
    "G.V. Prakash",
    "Melody",
    "Trending"
  )

  val filteredSongs = remember(selectedFilter, songs) {
    when (selectedFilter) {
      "All" -> songs
      "Top Hits" -> songs
      "A.R. Rahman" -> songs.filter { it.composer.contains("Rahman", ignoreCase = true) }
      "Anirudh" -> songs.filter { it.composer.contains("Anirudh", ignoreCase = true) }
      "Yuvan" -> songs.filter { it.composer.contains("Yuvan", ignoreCase = true) }
      "Harris Jayaraj" -> songs.filter { it.composer.contains("Harris", ignoreCase = true) }
      "G.V. Prakash" -> songs.filter { it.composer.contains("Prakash", ignoreCase = true) }
      "Melody" -> songs.filter { it.durationSeconds > 240 }
      "Trending" -> songs.take(10)
      else -> songs.filter {
        it.composer.contains(selectedFilter, ignoreCase = true) ||
        it.movieOrAlbum.contains(selectedFilter, ignoreCase = true) ||
        it.eraCategory.contains(selectedFilter, ignoreCase = true)
      }
    }
  }

  val categoryAlbums = remember(filteredSongs, selectedFilter) {
    if (selectedFilter == "All") emptyList()
    else filteredSongs.groupBy { it.movieOrAlbum }.values.map { it.first() }
  }

  val jumpBackInList = remember(songs) { songs.take(6) }
  val trendingList = remember(songs) { songs.drop(2).take(5) }
  val featuredSong = songs.firstOrNull() ?: Song.EMPTY

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(YalpsBackground)
      .testTag("home_screen_feed"),
    contentPadding = PaddingValues(bottom = 120.dp)
  ) {
    // 1. Filter Chips Horizontal Carousel (Displayed under the logo)
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        filterChips.forEach { filter ->
          val isSelected = filter == selectedFilter
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(
                if (isSelected) YalpsPrimary else YalpsSurfaceContainerHigh
              )
              .border(
                1.dp,
                if (isSelected) YalpsPrimary else Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(20.dp)
              )
              .clickable { selectedFilter = filter }
              .padding(horizontal = 14.dp, vertical = 7.dp)
              .testTag("filter_chip_$filter")
          ) {
            Text(
              text = filter,
              color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
          }
        }
      }
    }

    // 2. Specific Category Destination Mode (When a category is selected under the logo)
    if (selectedFilter != "All") {
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
          // Navigation Back Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Surface(
              onClick = { selectedFilter = "All" },
              color = YalpsSurfaceContainerHigh,
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Back to All",
                  tint = Color.White,
                  modifier = Modifier.size(16.dp)
                )
                Text("All Categories", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(YalpsPrimary.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "${filteredSongs.size} TRACKS",
                color = YalpsPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Category Destination Hero Card
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(
                Brush.linearGradient(
                  listOf(Color(0xFF1E1035), Color(0xFF0F172A), Color(0xFF180D2B))
                )
              )
              .border(1.dp, YalpsPrimary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
              .padding(18.dp)
          ) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                LosslessBadge(text = "CATEGORY DESTINATION", isAtmos = true)
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = selectedFilter,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
              )
              Text(
                text = "Explore lossless master discography, studio releases, and soundtrack albums for $selectedFilter.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
              )
            }
          }
        }
      }

      // If Category has No Songs / Albums -> Display Dedicated "Not Found" Page
      if (filteredSongs.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 32.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(YalpsSurfaceContainerLowest)
              .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(60.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFFF5252).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.SearchOff,
                  contentDescription = "Not Found",
                  tint = Color(0xFFFF5252),
                  modifier = Modifier.size(32.dp)
                )
              }
              Text(
                text = "No Tracks or Albums Found",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "We couldn't find any master tracks or albums under \"$selectedFilter\" in the current catalog.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
              Spacer(modifier = Modifier.height(6.dp))
              Button(
                onClick = { selectedFilter = "All" },
                colors = ButtonDefaults.buttonColors(containerColor = YalpsPrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("Browse All Music", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }
            }
          }
        }
      } else {
        // Albums in this category
        if (categoryAlbums.isNotEmpty()) {
          item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
              Text(
                text = "ALBUMS (${categoryAlbums.size})",
                color = YalpsPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
              )
              Spacer(modifier = Modifier.height(10.dp))
              LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(categoryAlbums) { album ->
                  val albumSongsList = songs.filter { it.movieOrAlbum.equals(album.movieOrAlbum, ignoreCase = true) }
                  Surface(
                    onClick = {
                      selectedAlbumForSheet = Pair(album.movieOrAlbum, albumSongsList.ifEmpty { listOf(album) })
                    },
                    color = YalpsSurfaceContainerLowest,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.width(145.dp)
                  ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                      AsyncImage(
                        model = album.imageUrl,
                        contentDescription = album.movieOrAlbum,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                          .fillMaxWidth()
                          .height(115.dp)
                          .clip(RoundedCornerShape(10.dp))
                      )
                      Spacer(modifier = Modifier.height(8.dp))
                      Text(
                        text = album.movieOrAlbum,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                      Text(
                        text = "${album.year} • ${album.composer}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                    }
                  }
                }
              }
            }
          }
        }

        // Songs in this category with Swipe-to-Queue
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "SONGS & TRACKS (${filteredSongs.size})",
              color = Color.White,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "👉 Drag right to Queue",
              color = YalpsSecondary,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        items(filteredSongs) { song ->
          val isCurrent = song.id == currentSong.id
          val isFav = favoriteIds.contains(song.id)
          Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            SwipeableSongRow(
              song = song,
              isCurrent = isCurrent,
              isPlaying = isPlaying,
              isFavorite = isFav,
              onClick = { onSongClick(song) },
              onToggleFavorite = { playerManager.toggleFavorite(song.id) }
            )
          }
        }
      }
    } else {
      // 3. General Home Feed Mode (All Music)
      if (songs.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(YalpsSurfaceContainerLowest)
              .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = null,
                tint = YalpsPrimary,
                modifier = Modifier.size(48.dp)
              )
              Text(
                text = "No Tracks Available",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Lossless Tamil tracks and albums will appear here automatically.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }
      } else {
        // Unsubscribed Prompt Banner (if user has not subscribed yet)
        if (!customerState.isSubscribed) {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                  Brush.horizontalGradient(
                    listOf(
                      Color(0xFF8B5CF6).copy(alpha = 0.2f),
                      Color(0xFFEC4899).copy(alpha = 0.15f)
                    )
                  )
                )
                .border(1.dp, YalpsPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .clickable {
                  if (songs.isNotEmpty()) {
                    onSongClick(songs.first())
                  }
                }
                .padding(14.dp)
                .testTag("home_subscribe_prompt_banner")
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(YalpsPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.Lock,
                      contentDescription = null,
                      tint = YalpsPrimary,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                  Column {
                    Text(
                      text = "UNLOCK LOSSLESS STREAMING",
                      color = YalpsPrimary,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace,
                      letterSpacing = 1.sp
                    )
                    Text(
                      text = "Subscribe to any plan to play master tracks",
                      color = Color.White,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.SemiBold
                    )
                  }
                }

                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(YalpsPrimary)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                  Text(
                    text = "₹30/mo",
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                  )
                }
              }
            }
          }
        }

        // Hero Banner Featured Master Drop
        if (featuredSong.id.isNotEmpty()) {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(YalpsSurfaceContainerLowest)
                .border(1.dp, YalpsPrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                .testTag("featured_drop_card")
            ) {
              AsyncImage(
                model = featuredSong.imageUrl,
                contentDescription = featuredSong.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(260.dp)
              )

              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(260.dp)
                  .background(
                    Brush.verticalGradient(
                      listOf(
                        Color.Transparent,
                        Color(0xAA13131B),
                        Color(0xFF0D0D15)
                      )
                    )
                  )
              )

              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .align(Alignment.BottomStart)
                  .padding(16.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  LosslessBadge(text = "FEATURED MASTER", isAtmos = featuredSong.isSpatial)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                  text = featuredSong.title,
                  color = Color.White,
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Black,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "${featuredSong.singers.ifBlank { featuredSong.composer }} • ${featuredSong.movieOrAlbum}",
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 12.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Button(
                    onClick = { onSongClick(featuredSong) },
                    colors = ButtonDefaults.buttonColors(
                      containerColor = YalpsPrimary,
                      contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("listen_now_hero_btn")
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                      )
                      Text(
                        text = "Listen Now",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                      )
                    }
                  }

                  Box(
                    modifier = Modifier
                      .size(38.dp)
                      .clip(CircleShape)
                      .background(YalpsSurfaceContainerHigh.copy(alpha = 0.8f))
                      .clickable { playerManager.addToQueue(featuredSong) },
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                      contentDescription = "Queue",
                      tint = Color.White,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
              }
            }
          }
        }

        // Jump Back In Horizontal Carousel
        item {
          Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Jump Back In",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "SEE ALL",
                color = YalpsPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
              contentPadding = PaddingValues(horizontal = 16.dp),
              horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              items(jumpBackInList) { song ->
                val isCurrent = song.id == currentSong.id
                Column(
                  modifier = Modifier
                    .width(130.dp)
                    .clickable { onSongClick(song) }
                    .testTag("jump_back_song_${song.id}")
                ) {
                  Box(
                    modifier = Modifier
                      .size(130.dp)
                      .clip(RoundedCornerShape(14.dp))
                      .background(YalpsSurfaceContainer)
                      .border(
                        1.dp,
                        if (isCurrent) YalpsPrimary else Color.White.copy(alpha = 0.08f),
                        RoundedCornerShape(14.dp)
                      )
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
                          .padding(6.dp)
                          .clip(RoundedCornerShape(4.dp))
                          .background(Color.Black.copy(alpha = 0.7f))
                          .padding(4.dp)
                      ) {
                        AnimatedEqualizer(isPlaying = true, barCount = 3)
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(6.dp))

                  Text(
                    text = song.title,
                    color = if (isCurrent) YalpsPrimary else Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = song.composer,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            }
          }
        }

        // Trending on Yalps (Swipeable Song Rows)
        item {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 20.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Trending on Yalps",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "👉 Drag right to Queue",
                color = YalpsSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            trendingList.forEach { song ->
              val isCurrent = song.id == currentSong.id
              val isFav = favoriteIds.contains(song.id)
              Box(modifier = Modifier.padding(vertical = 4.dp)) {
                SwipeableSongRow(
                  song = song,
                  isCurrent = isCurrent,
                  isPlaying = isPlaying,
                  isFavorite = isFav,
                  onClick = { onSongClick(song) },
                  onToggleFavorite = { playerManager.toggleFavorite(song.id) }
                )
              }
            }
          }
        }
      }
    }

    // 5. Daily Mixes For You (2x2 Bento Grid)
    if (TamilSongCatalog.dailyMixes.isNotEmpty() && songs.isNotEmpty()) {
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
          Text(
            text = "Daily Mixes For You",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Curated acoustic algorithms based on Notion behavior tracking",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
          )

          Spacer(modifier = Modifier.height(12.dp))

          val mixes = TamilSongCatalog.dailyMixes
          val pairs = mixes.take(4).chunked(2)
          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            pairs.forEachIndexed { rowIdx, rowMixes ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                rowMixes.forEachIndexed { colIdx, mix ->
                  val targetSongIdx = (rowIdx * 2 + colIdx) % songs.size
                  val targetSong = songs[targetSongIdx]
                  DailyMixCard(
                    mix = mix,
                    onClick = { onSongClick(targetSong) },
                    modifier = Modifier.weight(1f)
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  // Album Detail Bottom Sheet
  selectedAlbumForSheet?.let { (albumName, albumSongs) ->
    AlbumDetailSheet(
      albumName = albumName,
      albumSongs = albumSongs,
      currentSong = currentSong,
      isPlaying = isPlaying,
      onDismiss = { selectedAlbumForSheet = null }
    )
  }
}

@Composable
fun DailyMixCard(
  mix: DailyMix,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .background(YalpsSurfaceContainerLowest)
      .border(1.dp, Color(mix.accentColorHex).copy(alpha = 0.35f), RoundedCornerShape(14.dp))
      .clickable { onClick() }
      .testTag("daily_mix_${mix.id}")
  ) {
    Column {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(110.dp)
      ) {
        AsyncImage(
          model = mix.coverUrl,
          contentDescription = mix.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
        Box(
          modifier = Modifier
            .padding(6.dp)
            .align(Alignment.TopStart)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
          Text(
            text = mix.mixNumber,
            color = Color(mix.accentColorHex),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      Column(modifier = Modifier.padding(10.dp)) {
        Text(
          text = mix.title,
          color = Color.White,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = mix.subtitle,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 10.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = mix.durationText,
          color = YalpsPrimary,
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    }
  }
}
