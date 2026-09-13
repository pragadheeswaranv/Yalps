package com.example.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioPlayerManager
import com.example.auth.UserSessionManager
import com.example.data.OnlineBackendManager
import com.example.model.Song
import com.example.notion.NotionAnalyticsManager
import com.example.stripe.StripePaymentsManager
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.SubscriptionRequiredDialog
import com.example.ui.components.YalpsTopAppBar
import com.example.ui.screens.BackendAppScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NowPlayingScreen
import com.example.ui.screens.PlansScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.theme.YalpsPrimary
import com.example.ui.theme.YalpsSecondary
import com.example.ui.theme.YalpsSurfaceContainerLowest

enum class YalpsTab(val title: String) {
  HOME("Home"),
  LIBRARY("Library"),
  PLANS("Plans"),
  PROFILE("Profile"),
  BACKEND("Backend")
}

@Composable
fun YalpsApp() {
  val context = LocalContext.current
  val playerManager = remember { AudioPlayerManager.getInstance() }
  val notionManager = remember { NotionAnalyticsManager.getInstance() }
  val sessionManager = remember { UserSessionManager.getInstance() }
  val stripeManager = remember { StripePaymentsManager.getInstance() }

  val isLoggedIn by sessionManager.isLoggedIn.collectAsState()
  val currentUser by sessionManager.currentUser.collectAsState()
  val customerState by stripeManager.customerState.collectAsState()
  val backendManager = remember { OnlineBackendManager.getInstance() }
  val catalogSongs by backendManager.songs.collectAsState()

  val isMasterAdmin = currentUser?.isMasterBackendAdmin == true

  var showLoginScreen by remember { mutableStateOf(!isLoggedIn) }
  var showSubscriptionDialog by remember { mutableStateOf(false) }
  var pendingSongToPlay by remember { mutableStateOf<Song?>(null) }
  var isExpandedPlayerVisible by remember { mutableStateOf(false) }
  
  val rootTab = if (isMasterAdmin) YalpsTab.BACKEND else YalpsTab.HOME
  var currentTab by remember { mutableStateOf(rootTab) }
  val tabHistory = remember { mutableStateListOf(rootTab) }

  fun navigateToTab(newTab: YalpsTab) {
    if (currentTab != newTab) {
      if (tabHistory.isEmpty() || tabHistory.last() != newTab) {
        tabHistory.add(newTab)
      }
      currentTab = newTab
    }
  }

  // Auto-switch to Backend App mode when Master Admin logs in
  LaunchedEffect(isMasterAdmin) {
    if (isMasterAdmin && currentTab != YalpsTab.BACKEND) {
      navigateToTab(YalpsTab.BACKEND)
    }
  }

  // Intercept system Back button for strict page-by-page backward navigation
  val canHandleBack = isExpandedPlayerVisible ||
                      showSubscriptionDialog ||
                      (showLoginScreen && isLoggedIn) ||
                      tabHistory.size > 1 ||
                      currentTab != YalpsTab.HOME

  BackHandler(enabled = canHandleBack) {
    when {
      isExpandedPlayerVisible -> {
        isExpandedPlayerVisible = false
      }
      showSubscriptionDialog -> {
        showSubscriptionDialog = false
        pendingSongToPlay = null
      }
      showLoginScreen && isLoggedIn -> {
        showLoginScreen = false
      }
      tabHistory.size > 1 -> {
        tabHistory.removeAt(tabHistory.lastIndex)
        currentTab = tabHistory.lastOrNull() ?: YalpsTab.HOME
      }
      currentTab != YalpsTab.HOME -> {
        tabHistory.clear()
        tabHistory.add(YalpsTab.HOME)
        currentTab = YalpsTab.HOME
      }
    }
  }

  val isSubscribed = currentUser?.isProMember == true || customerState.status == "Active" || isMasterAdmin

  fun requestPlayTrack(song: Song) {
    if (isSubscribed) {
      playerManager.playSong(song)
    } else {
      pendingSongToPlay = song
      showSubscriptionDialog = true
    }
  }

  // Connect player events to Notion telemetry
  LaunchedEffect(playerManager) {
    playerManager.onTrackPlayedListener = { song ->
      notionManager.logTrackPlayed(song)
    }
    playerManager.onTrackSkippedListener = { song ->
      notionManager.logTrackSkipped(song, playerManager.currentPositionSeconds.value)
    }
    playerManager.onFavoriteToggledListener = { song, isFav ->
      notionManager.logFavoriteToggled(song, isFav)
    }
  }

  val currentSong by playerManager.currentSong.collectAsState()
  val isPlaying by playerManager.isPlaying.collectAsState()
  val currentPositionSeconds by playerManager.currentPositionSeconds.collectAsState()
  val isShuffle by playerManager.isShuffle.collectAsState()
  val isRepeat by playerManager.isRepeat.collectAsState()
  val queue by playerManager.queue.collectAsState()
  val favoriteIds by playerManager.favoriteIds.collectAsState()

  val progress = if (currentSong.durationSeconds > 0) {
    currentPositionSeconds.toFloat() / currentSong.durationSeconds
  } else 0f

  Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = MaterialTheme.colorScheme.background,
      topBar = {
        if (currentTab != YalpsTab.BACKEND) {
          YalpsTopAppBar(
            onSearchClick = {
              navigateToTab(YalpsTab.LIBRARY)
            },
            onProfileClick = {
              navigateToTab(YalpsTab.PROFILE)
            },
            onBackendClick = {
              navigateToTab(YalpsTab.BACKEND)
            },
            isAdminBackend = isMasterAdmin,
            userInitial = currentUser?.displayName ?: "P"
          )
        }
      },
      bottomBar = {
        if (currentTab != YalpsTab.BACKEND) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .windowInsetsPadding(WindowInsets.navigationBars)
          ) {
            // Floating Mini Player (docked right above navigation bar)
            MiniPlayerBar(
              currentSong = currentSong,
              isPlaying = isPlaying,
              progress = progress,
              isFavorite = favoriteIds.contains(currentSong.id),
              onPlayPauseClick = {
                playerManager.togglePlayPause()
              },
              onFavoriteClick = { playerManager.toggleFavorite(currentSong.id) },
              onExpandPlayer = {
                isExpandedPlayerVisible = true
              }
            )

            // Bottom Navigation Bar for Listener App
            NavigationBar(
              containerColor = YalpsSurfaceContainerLowest,
              tonalElevation = 8.dp,
              modifier = Modifier
                .fillMaxWidth()
                .border(
                  1.dp,
                  Color.White.copy(alpha = 0.06f),
                  RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .testTag("bottom_nav_bar")
            ) {
              if (isMasterAdmin) {
                NavigationBarItem(
                  selected = currentTab == YalpsTab.BACKEND,
                  onClick = { navigateToTab(YalpsTab.BACKEND) },
                  icon = {
                    Icon(
                      imageVector = Icons.Default.AdminPanelSettings,
                      contentDescription = "Backend",
                      modifier = Modifier.size(24.dp)
                    )
                  },
                  label = {
                    Text(
                      text = "Backend",
                      fontSize = 11.sp,
                      fontWeight = if (currentTab == YalpsTab.BACKEND) FontWeight.Bold else FontWeight.Medium
                    )
                  },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color(0xFF8B5CF6),
                    indicatorColor = Color(0xFF8B5CF6),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                  ),
                  modifier = Modifier.testTag("nav_item_backend")
                )
              }

              NavigationBarItem(
                selected = currentTab == YalpsTab.HOME,
                onClick = { navigateToTab(YalpsTab.HOME) },
                icon = {
                  Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(24.dp)
                  )
                },
                label = {
                  Text(
                    text = "Home",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == YalpsTab.HOME) FontWeight.Bold else FontWeight.Medium
                  )
                },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = Color.Black,
                  selectedTextColor = YalpsPrimary,
                  indicatorColor = YalpsPrimary,
                  unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                  unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_item_home")
              )

              NavigationBarItem(
                selected = currentTab == YalpsTab.LIBRARY,
                onClick = { navigateToTab(YalpsTab.LIBRARY) },
                icon = {
                  Icon(
                    imageVector = Icons.Default.LibraryMusic,
                    contentDescription = "Library",
                    modifier = Modifier.size(24.dp)
                  )
                },
                label = {
                  Text(
                    text = "Library",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == YalpsTab.LIBRARY) FontWeight.Bold else FontWeight.Medium
                  )
                },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = Color.Black,
                  selectedTextColor = YalpsSecondary,
                  indicatorColor = YalpsSecondary,
                  unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                  unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_item_library")
              )

              NavigationBarItem(
                selected = currentTab == YalpsTab.PLANS,
                onClick = { navigateToTab(YalpsTab.PLANS) },
                icon = {
                  Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = "Plans",
                    modifier = Modifier.size(24.dp)
                  )
                },
                label = {
                  Text(
                    text = "Plans",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == YalpsTab.PLANS) FontWeight.Bold else FontWeight.Medium
                  )
                },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = Color.Black,
                  selectedTextColor = Color(0xFF635BFF),
                  indicatorColor = Color(0xFF635BFF),
                  unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                  unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_item_plans")
              )

              NavigationBarItem(
                selected = currentTab == YalpsTab.PROFILE,
                onClick = { navigateToTab(YalpsTab.PROFILE) },
                icon = {
                  Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(24.dp)
                  )
                },
                label = {
                  Text(
                    text = "Profile",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == YalpsTab.PROFILE) FontWeight.Bold else FontWeight.Medium
                  )
                },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = Color.Black,
                  selectedTextColor = YalpsPrimary,
                  indicatorColor = YalpsPrimary,
                  unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                  unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_item_profile")
              )
            }
          }
        }
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        when (currentTab) {
          YalpsTab.BACKEND -> {
            BackendAppScreen(
              onSwitchToPlayer = {
                navigateToTab(YalpsTab.HOME)
              },
              onPlaySongRedirect = { song ->
                requestPlayTrack(song)
                navigateToTab(YalpsTab.HOME)
                isExpandedPlayerVisible = true
              },
              onLogout = {
                sessionManager.logout()
                showLoginScreen = true
              }
            )
          }
          YalpsTab.HOME -> {
            HomeScreen(
              songs = catalogSongs,
              currentSong = currentSong,
              isPlaying = isPlaying,
              onSongClick = { song ->
                requestPlayTrack(song)
              },
              onPlayFeaturedDrop = {
                if (catalogSongs.isNotEmpty()) {
                  requestPlayTrack(catalogSongs.first())
                }
              }
            )
          }
          YalpsTab.LIBRARY -> {
            LibraryScreen(
              allSongs = catalogSongs,
              currentSong = currentSong,
              isPlaying = isPlaying,
              favoriteIds = favoriteIds,
              onSongClick = { song ->
                requestPlayTrack(song)
              },
              onToggleFavorite = { songId ->
                playerManager.toggleFavorite(songId)
              }
            )
          }
          YalpsTab.PLANS -> {
            PlansScreen()
          }
          YalpsTab.PROFILE -> {
            UserProfileScreen(
              onNavigateToPlans = {
                navigateToTab(YalpsTab.PLANS)
              },
              onNavigateToBackend = {
                navigateToTab(YalpsTab.BACKEND)
              },
              onSignOut = {
                sessionManager.logout()
                showLoginScreen = true
              }
            )
          }
        }
      }
    }

    // Full Screen Now Playing Drawer
    AnimatedVisibility(
      visible = isExpandedPlayerVisible,
      enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
      exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
      NowPlayingScreen(
        song = currentSong,
        isPlaying = isPlaying,
        currentPositionSeconds = currentPositionSeconds,
        isFavorite = favoriteIds.contains(currentSong.id),
        isShuffle = isShuffle,
        isRepeat = isRepeat,
        queue = queue,
        onClose = { isExpandedPlayerVisible = false },
        onPlayPauseToggle = { playerManager.togglePlayPause() },
        onSeek = { seconds -> playerManager.seekTo(seconds) },
        onSkipNext = { playerManager.skipNext() },
        onSkipPrevious = { playerManager.skipPrevious() },
        onFavoriteToggle = { playerManager.toggleFavorite(currentSong.id) },
        onShuffleToggle = { playerManager.toggleShuffle() },
        onRepeatToggle = { playerManager.toggleRepeat() },
        onSelectQueueSong = { song -> requestPlayTrack(song) }
      )
    }

    // Full Screen Login Screen (Phone OTP & Gmail Password)
    AnimatedVisibility(
      visible = showLoginScreen,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.background)
      ) {
        LoginScreen(
          onLoginSuccess = {
            showLoginScreen = false
            if (!customerState.isSubscribed) {
              navigateToTab(YalpsTab.PLANS)
              Toast.makeText(context, "Welcome! Please subscribe to a plan to start listening to lossless tracks.", Toast.LENGTH_LONG).show()
            }
          },
          onSkipToGuest = {
            showLoginScreen = false
            if (!customerState.isSubscribed) {
              navigateToTab(YalpsTab.PLANS)
            }
          }
        )
      }
    }

    // Modal Subscription Required Paywall Dialog
    if (showSubscriptionDialog) {
      SubscriptionRequiredDialog(
        targetSong = pendingSongToPlay,
        onDismissRequest = {
          showSubscriptionDialog = false
          pendingSongToPlay = null
        },
        onSubscribed = {
          showSubscriptionDialog = false
          pendingSongToPlay?.let { song ->
            playerManager.playSong(song)
          }
          pendingSongToPlay = null
        },
        onNavigateToPlans = {
          showSubscriptionDialog = false
          pendingSongToPlay = null
          navigateToTab(YalpsTab.PLANS)
        }
      )
    }
  }
}
