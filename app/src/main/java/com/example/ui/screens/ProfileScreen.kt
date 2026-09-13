package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.UserSessionManager
import com.example.ui.theme.YalpsPrimary
import com.example.ui.theme.YalpsSecondary
import com.example.ui.theme.YalpsSurfaceContainer
import com.example.ui.theme.YalpsSurfaceContainerHigh
import com.example.ui.theme.YalpsSurfaceContainerLowest
import com.example.ui.theme.YalpsTertiary

data class ConnectedAudioDevice(
  val id: String,
  val name: String,
  val typeLabel: String,
  val category: String, // "Phone Output", "PC & Network"
  val icon: ImageVector,
  val isCurrent: Boolean = false
)

@Composable
fun UserProfileScreen(
  onNavigateToPlans: () -> Unit = {},
  onNavigateToBackend: () -> Unit = {},
  onSignOut: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val sessionManager = remember { UserSessionManager.getInstance() }
  val currentUser by sessionManager.currentUser.collectAsState()
  val isBackendMaster = currentUser?.isMasterBackendAdmin == true

  // State settings
  var showSignOutDialog by remember { mutableStateOf(false) }

  // Privacy settings
  var isPrivateSession by remember { mutableStateOf(false) }
  var isShareActivity by remember { mutableStateOf(true) }
  var isPersonalizedRecommendations by remember { mutableStateOf(true) }
  var cacheClearedText by remember { mutableStateOf("1.2 GB cached") }

  // Notifications settings
  var notifyNewReleases by remember { mutableStateOf(true) }
  var notifyConcerts by remember { mutableStateOf(true) }
  var notifyWeeklyDigest by remember { mutableStateOf(false) }
  var notifyQualityAlerts by remember { mutableStateOf(true) }

  // Audio Hardware Outputs detected from the device in real time
  val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
  var hardwareRefreshTrigger by remember { mutableStateOf(0) }
  val detectedAudioOutputs = remember(hardwareRefreshTrigger) {
    val outputsList = mutableListOf<ConnectedAudioDevice>()
    val devModel = "${Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} ${Build.MODEL}"

    // Primary current device session
    outputsList.add(
      ConnectedAudioDevice(
        id = "primary_device",
        name = devModel,
        typeLabel = "Logged in as ${currentUser?.displayName ?: "User"} • Active Session",
        category = "Logged In Device",
        icon = Icons.Default.PhoneAndroid,
        isCurrent = true
      )
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && audioManager != null) {
      val audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
      audioDevices.forEach { dev ->
        when (dev.type) {
          AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> {
            outputsList.add(
              ConnectedAudioDevice(
                id = "out_speaker",
                name = "Internal Stereo Speakers",
                typeLabel = "Hardware Built-in Transducers",
                category = "Audio Hardware Output",
                icon = Icons.Default.Speaker,
                isCurrent = true
              )
            )
          }
          AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
          AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> {
            outputsList.add(
              ConnectedAudioDevice(
                id = "out_bt_${dev.id}",
                name = dev.productName.toString().ifBlank { "Bluetooth Audio Device" },
                typeLabel = "Connected Wireless Audio (A2DP)",
                category = "Audio Hardware Output",
                icon = Icons.Default.Headphones,
                isCurrent = true
              )
            )
          }
          AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
          AudioDeviceInfo.TYPE_WIRED_HEADSET -> {
            outputsList.add(
              ConnectedAudioDevice(
                id = "out_wired",
                name = "Wired 3.5mm Headset / Headphones",
                typeLabel = "Analog Direct Output Jack",
                category = "Audio Hardware Output",
                icon = Icons.Default.Headphones,
                isCurrent = true
              )
            )
          }
          AudioDeviceInfo.TYPE_USB_DEVICE,
          AudioDeviceInfo.TYPE_USB_HEADSET -> {
            outputsList.add(
              ConnectedAudioDevice(
                id = "out_usb_${dev.id}",
                name = dev.productName.toString().ifBlank { "USB DAC / Audio Interface" },
                typeLabel = "High-Res Digital Audio Output",
                category = "Audio Hardware Output",
                icon = Icons.Default.Usb,
                isCurrent = true
              )
            )
          }
        }
      }
    }

    if (outputsList.size == 1) {
      outputsList.add(
        ConnectedAudioDevice(
          id = "out_speaker_default",
          name = "Internal Stereo Speakers",
          typeLabel = "Hardware Built-in Transducers",
          category = "Audio Hardware Output",
          icon = Icons.Default.Speaker,
          isCurrent = true
        )
      )
    }

    outputsList.distinctBy { it.id }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .statusBarsPadding()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Screen Title Header
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Profile & Settings",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
          )
          Text(
            text = "Account, audio routing & preferences",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
          )
        }
      }
    }

    // 2. User Details Profile Card
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .background(
            Brush.verticalGradient(
              listOf(YalpsSurfaceContainerHigh, YalpsSurfaceContainerLowest)
            )
          )
          .border(1.dp, YalpsPrimary.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
          .padding(18.dp)
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            // Avatar
            Box(
              modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                  Brush.linearGradient(
                    listOf(Color(0xFF8B5CF6), YalpsPrimary, YalpsSecondary)
                  )
                )
                .border(2.dp, Color.White, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = (currentUser?.displayName?.take(1) ?: "P").uppercase(),
                color = Color.Black,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = currentUser?.displayName ?: "Pragadheesh",
                  color = Color.White,
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isBackendMaster) Color(0xFF8B5CF6).copy(alpha = 0.25f) else YalpsPrimary.copy(alpha = 0.2f))
                    .border(0.8.dp, if (isBackendMaster) Color(0xFF8B5CF6) else YalpsPrimary, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = if (isBackendMaster) "👑 MASTER HOST" else "PRO MEMBER",
                    color = if (isBackendMaster) Color(0xFFD8B4FE) else YalpsPrimary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }

              val idText = currentUser?.identifier.orEmpty().ifBlank { "pragadheeeesh95@gmail.com" }
              Text(
                text = idText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
              )

              Text(
                text = if (isBackendMaster) "Full Administrative & Cloud API Access" else "Signed in via ${currentUser?.authMethod?.name ?: "Gmail"}",
                color = if (isBackendMaster) Color(0xFF00E676) else YalpsSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }

          // Sign Out / Switch User Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            OutlinedButton(
              onClick = { showSignOutDialog = true },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("profile_sign_out_btn")
            ) {
              Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = null,
                tint = YalpsPrimary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Switch / Sign Out", color = YalpsPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // 3. User Plans & Subscription
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(YalpsSurfaceContainerLowest)
          .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
              imageVector = Icons.Default.CreditCard,
              contentDescription = null,
              tint = YalpsPrimary,
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = "MEMBERSHIP & PLANS",
              color = Color.White,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              letterSpacing = 1.sp
            )
          }
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(YalpsSecondary.copy(alpha = 0.2f))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "ACTIVE",
              color = YalpsSecondary,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "YALPS HiFi Pro Plan",
              color = Color.White,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "₹100/month • Renews on 10 Oct 2026",
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 12.sp
            )
            Text(
              text = "8 concurrent mobile & PC streams • Ad-free offline mode",
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
              fontSize = 11.sp
            )
          }
        }

        Button(
          onClick = onNavigateToPlans,
          colors = ButtonDefaults.buttonColors(containerColor = YalpsPrimary),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth().testTag("manage_plan_btn")
        ) {
          Text("Manage or Change Plan", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
      }
    }

    // 4. Connected Device & Audio Hardware Outputs (Real-time from Device)
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(YalpsSurfaceContainerLowest)
          .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
              imageVector = Icons.Default.Devices,
              contentDescription = null,
              tint = YalpsTertiary,
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = "CONNECTED DEVICE",
              color = Color.White,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              letterSpacing = 1.sp
            )
          }
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(YalpsTertiary.copy(alpha = 0.15f))
              .clickable {
                hardwareRefreshTrigger++
                Toast.makeText(context, "Hardware audio state scanned", Toast.LENGTH_SHORT).show()
              }
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "SCAN REAL-TIME",
              color = YalpsTertiary,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        // Active Account Device Header
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(YalpsSurfaceContainerHigh)
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
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
            Column {
              Text(
                text = "${Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} ${Build.MODEL}",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT}) • Direct Output",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
              )
            }
          }
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Active",
            tint = YalpsTertiary,
            modifier = Modifier.size(20.dp)
          )
        }

        Text(
          text = "Real-Time Audio Hardware on This Device",
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )

        // Real-Time Hardware Device List
        detectedAudioOutputs.forEach { dev ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(YalpsSurfaceContainer)
              .border(
                1.dp,
                if (dev.isCurrent) YalpsTertiary.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(10.dp)
              )
              .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (dev.id == "primary_device") YalpsTertiary else YalpsSurfaceContainerHigh),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = dev.icon,
                  contentDescription = null,
                  tint = if (dev.id == "primary_device") Color.Black else Color.White,
                  modifier = Modifier.size(20.dp)
                )
              }

              Column {
                Text(
                  text = dev.name,
                  color = Color.White,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = dev.typeLabel,
                  color = if (dev.id == "primary_device") YalpsTertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 11.sp
                )
              }
            }

            Text(
              text = if (dev.id == "primary_device") "ONLINE" else "DETECTED",
              color = if (dev.id == "primary_device") YalpsSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }

    // 5. Privacy & Security Settings
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(YalpsSurfaceContainerLowest)
          .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = YalpsSecondary,
            modifier = Modifier.size(20.dp)
          )
          Text(
            text = "PRIVACY & DATA",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
          )
        }

        // Private Session
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Private Listening Session", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("Listen incognito without updating recommendations or history", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
          }
          Switch(
            checked = isPrivateSession,
            onCheckedChange = { isPrivateSession = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.Black,
              checkedTrackColor = YalpsSecondary
            )
          )
        }

        // Share listening activity
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Share Listening Activity", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("Allow friends to see currently playing tracks", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
          }
          Switch(
            checked = isShareActivity,
            onCheckedChange = { isShareActivity = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.Black,
              checkedTrackColor = YalpsSecondary
            )
          )
        }

        // Cache cleanup
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(YalpsSurfaceContainerHigh)
            .clickable {
              cacheClearedText = "0 KB cached"
              Toast.makeText(context, "Streaming cache purged successfully", Toast.LENGTH_SHORT).show()
            }
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Clear Offline Audio Cache", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(cacheClearedText, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
          }
          Icon(
            imageVector = Icons.Default.DeleteOutline,
            contentDescription = "Clear Cache",
            tint = YalpsPrimary,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }

    // 6. Notification Preferences
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(YalpsSurfaceContainerLowest)
          .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            tint = YalpsPrimary,
            modifier = Modifier.size(20.dp)
          )
          Text(
            text = "NOTIFICATIONS",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("New Music Radar", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("Alerts when your followed artists release new songs", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
          }
          Switch(
            checked = notifyNewReleases,
            onCheckedChange = { notifyNewReleases = it },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = YalpsPrimary)
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Concerts & Events", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("Notices for live shows and festival tickets nearby", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
          }
          Switch(
            checked = notifyConcerts,
            onCheckedChange = { notifyConcerts = it },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = YalpsPrimary)
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Audio Quality Warnings", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("Notify if cellular data limit is approached", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
          }
          Switch(
            checked = notifyQualityAlerts,
            onCheckedChange = { notifyQualityAlerts = it },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = YalpsPrimary)
          )
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(100.dp))
    }
  }

  // Sign out confirmation dialog
  if (showSignOutDialog) {
    AlertDialog(
      onDismissRequest = { showSignOutDialog = false },
      title = {
        Text("Sign Out of YALPS", color = Color.White, fontWeight = FontWeight.Bold)
      },
      text = {
        Text(
          "Are you sure you want to sign out? You can sign back in anytime via your phone number or Gmail.",
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showSignOutDialog = false
            sessionManager.logout()
            onSignOut()
          },
          colors = ButtonDefaults.buttonColors(containerColor = YalpsPrimary)
        ) {
          Text("Sign Out", color = Color.Black, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showSignOutDialog = false }) {
          Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      },
      containerColor = YalpsSurfaceContainerLowest
    )
  }
}

@Composable
fun ProfileScreen(
  onNavigateToPlans: () -> Unit = {},
  onSignOut: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  UserProfileScreen(
    onNavigateToPlans = onNavigateToPlans,
    onSignOut = onSignOut,
    modifier = modifier
  )
}

