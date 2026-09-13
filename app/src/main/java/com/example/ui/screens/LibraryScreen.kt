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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Song
import com.example.notion.NotionAnalyticsManager
import com.example.ui.components.AlbumDetailSheet
import com.example.ui.components.AnimatedEqualizer
import com.example.ui.components.LosslessBadge
import com.example.ui.components.SwipeableSongRow
import com.example.ui.theme.YalpsBackground
import com.example.ui.theme.YalpsPrimary
import com.example.ui.theme.YalpsSecondary
import com.example.ui.theme.YalpsSurfaceContainer
import com.example.ui.theme.YalpsSurfaceContainerHigh
import com.example.ui.theme.YalpsSurfaceContainerLowest
import androidx.compose.foundation.lazy.LazyRow

@Composable
fun LibraryScreen(
  allSongs: List<Song>,
  currentSong: Song,
  isPlaying: Boolean,
  favoriteIds: Set<String>,
  onSongClick: (Song) -> Unit,
  onToggleFavorite: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedEra by remember { mutableStateOf("All Eras") }
  var selectedAlbumForSheet by remember { mutableStateOf<Pair<String, List<Song>>?>(null) }

  val eras = listOf(
    "All Eras",
    "2021-Present",
    "2016-2020",
    "2011-2015",
    "2006-2010",
    "2000-2005",
    "Liked Songs ♥"
  )

  val filteredSongs = remember(searchQuery, selectedEra, allSongs, favoriteIds) {
    var result = allSongs

    if (selectedEra == "Liked Songs ♥") {
      result = result.filter { favoriteIds.contains(it.id) }
    } else if (selectedEra != "All Eras") {
      result = result.filter { it.eraCategory == selectedEra }
    }

    if (searchQuery.isNotBlank()) {
      val q = searchQuery.trim().lowercase()
      result = result.filter { song ->
        val titleLower = song.title.lowercase()
        val albumLower = song.movieOrAlbum.lowercase()
        val composerLower = song.composer.lowercase()
        val singersLower = song.singers.lowercase()

        // Prefix match or word-initial match for strict relevance
        titleLower.startsWith(q) ||
        albumLower.startsWith(q) ||
        composerLower.startsWith(q) ||
        singersLower.startsWith(q) ||
        titleLower.split(" ", "-", "_").any { it.startsWith(q) } ||
        albumLower.split(" ", "-", "_").any { it.startsWith(q) } ||
        composerLower.split(" ", "-", "_").any { it.startsWith(q) } ||
        singersLower.split(" ", ",", "&").any { it.trim().startsWith(q) } ||
        (q.length >= 2 && (titleLower.contains(q) || albumLower.contains(q) || composerLower.contains(q)))
      }
    }
    result
  }

  // Matching Albums grouped by movieOrAlbum
  val matchingAlbums = remember(filteredSongs, searchQuery) {
    if (searchQuery.isBlank()) emptyList()
    else {
      filteredSongs.groupBy { it.movieOrAlbum }.values.map { it.first() }
    }
  }

  // Quick search discovery tags when no query is entered
  val suggestedSearches = listOf("A.R. Rahman", "Anirudh", "Harris Jayaraj", "Yuvan Shankar Raja", "Ilaiyaraaja", "Santhosh Narayanan", "Ponniyin Selvan", "Leo", "Vikram", "Jailer")

  val isSearching = searchQuery.isNotBlank()
  val isSpecificEra = selectedEra != "All Eras"

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(YalpsBackground)
  ) {
    // Top Section: Title & Header
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Library",
          color = Color.White,
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Search TextField
      OutlinedTextField(
        value = searchQuery,
        onValueChange = {
          searchQuery = it
          NotionAnalyticsManager.getInstance().logSearch(it)
        },
        placeholder = {
          Text(
            text = "Search song, album, artist, or composer...",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = YalpsPrimary,
            modifier = Modifier.size(20.dp)
          )
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Clear",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = YalpsSurfaceContainerLowest,
          unfocusedContainerColor = YalpsSurfaceContainerLowest,
          focusedBorderColor = YalpsPrimary,
          unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("catalog_search_input")
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Era Filter Pills
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        eras.forEach { era ->
          val isSelected = era == selectedEra
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(
                if (isSelected) YalpsSecondary else YalpsSurfaceContainerHigh
              )
              .border(
                1.dp,
                if (isSelected) YalpsSecondary else Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(16.dp)
              )
              .clickable { selectedEra = era }
              .padding(horizontal = 12.dp, vertical = 6.dp)
              .testTag("era_tab_$era")
          ) {
            Text(
              text = era,
              color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
          }
        }
      }
    }

    // Main Content Area
    if (!isSearching && !isSpecificEra) {
      // REQUIREMENT: No song should be displayed under the search bar in the library page by default
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(YalpsSurfaceContainerLowest)
              .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
              .padding(20.dp)
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = null,
                  tint = YalpsPrimary,
                  modifier = Modifier.size(20.dp)
                )
                Text(
                  text = "SEARCH LOSSLESS CATALOG",
                  color = YalpsPrimary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  letterSpacing = 1.sp
                )
              }

              Text(
                text = "Find Any Song, Album, or Composer",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
              )

              Text(
                text = "Type in the search bar above to instantly find tracks by initial letter, song title, movie album, or music director.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 18.sp
              )
            }
          }
        }

        item {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              text = "POPULAR SEARCH SUGGESTIONS",
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              suggestedSearches.forEach { suggestion ->
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(YalpsSurfaceContainer)
                    .border(1.dp, YalpsPrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .clickable { searchQuery = suggestion }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Search,
                      contentDescription = null,
                      tint = YalpsPrimary,
                      modifier = Modifier.size(14.dp)
                    )
                    Text(
                      text = suggestion,
                      color = Color.White,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.SemiBold
                    )
                  }
                }
              }
            }
          }
        }
      }
    } else {
      // Song List LazyColumn (Displayed when user is searching or filtered by specific era/liked songs)
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 6.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // 1. Matching Albums Carousel (if search is active and albums match)
        if (matchingAlbums.isNotEmpty()) {
          item {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
              Text(
                text = "MATCHING ALBUMS",
                color = YalpsPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
              )
              Spacer(modifier = Modifier.height(8.dp))
              LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(matchingAlbums) { albumSong ->
                  val albumSongsList = allSongs.filter { it.movieOrAlbum.equals(albumSong.movieOrAlbum, ignoreCase = true) }
                  Surface(
                    onClick = {
                      selectedAlbumForSheet = Pair(albumSong.movieOrAlbum, albumSongsList.ifEmpty { listOf(albumSong) })
                    },
                    color = YalpsSurfaceContainerLowest,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.width(140.dp)
                  ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                      AsyncImage(
                        model = albumSong.imageUrl,
                        contentDescription = albumSong.movieOrAlbum,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                          .fillMaxWidth()
                          .height(100.dp)
                          .clip(RoundedCornerShape(8.dp))
                      )
                      Spacer(modifier = Modifier.height(6.dp))
                      Text(
                        text = albumSong.movieOrAlbum,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                      Text(
                        text = albumSong.composer,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                    }
                  }
                }
              }
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "MATCHING TRACKS (${filteredSongs.size})",
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 11.sp,
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
          }
        } else if (filteredSongs.isNotEmpty()) {
          item {
            Row(
              modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = if (searchQuery.isNotBlank()) "SEARCH RESULTS (${filteredSongs.size})" else "$selectedEra (${filteredSongs.size})",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
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
        }

        if (filteredSongs.isEmpty()) {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Text(
                  text = if (searchQuery.isNotBlank()) "No songs found for \"$searchQuery\"" else "No songs in $selectedEra",
                  color = Color.White,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = if (searchQuery.isNotBlank()) "Check your spelling or try searching by title, composer, or singer." else "Tracks matching this category will appear here.",
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 12.sp,
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
              }
            }
          }
        }

        items(filteredSongs) { song ->
          val isCurrent = song.id == currentSong.id
          val isFav = favoriteIds.contains(song.id)

          SwipeableSongRow(
            song = song,
            isCurrent = isCurrent,
            isPlaying = isPlaying,
            isFavorite = isFav,
            onClick = { onSongClick(song) },
            onToggleFavorite = { onToggleFavorite(song.id) }
          )
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
}

