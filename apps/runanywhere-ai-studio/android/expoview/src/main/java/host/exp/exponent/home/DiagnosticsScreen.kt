package host.exp.exponent.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import host.exp.expoview.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
  bottomBar: @Composable () -> Unit = { },
  accountHeader: @Composable () -> Unit = { }
) {
  Scaffold(
    topBar = { SettingsTopBar(accountHeader = accountHeader) },
    bottomBar = bottomBar
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Header
      DiagnosticsHeader()

      // Audio Diagnostics
      AudioDiagnosticsCard()

      // Location Diagnostics
      LocationDiagnosticsCard()

      // Geofencing Diagnostics
      GeofencingDiagnosticsCard()

      // Privacy Note
      PrivacyNote()
    }
  }
}

@Composable
private fun DiagnosticsHeader() {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    shape = RoundedCornerShape(12.dp)
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Text(
        text = "Permission Diagnostics",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "Test and verify permission behaviors for your apps. These diagnostics help you understand how Android handles audio, location, and geofencing in different scenarios.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun AudioDiagnosticsCard() {
  var isExpanded by remember { mutableStateOf(false) }
  var isPlaying by remember { mutableStateOf(false) }
  var audioMode by remember { mutableStateOf("Normal") }
  val context = LocalContext.current

  DiagnosticsCard(
    icon = R.drawable.terminal_icon,
    iconColor = RAColors.PrimaryAccent,
    title = "Audio",
    description = "Test audio playback in foreground and background. Verify audio focus behavior and speaker routing.",
    isExpanded = isExpanded,
    onExpandClick = { isExpanded = !isExpanded }
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Audio Mode Selection
      Text(
        text = "Audio Mode",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface
      )
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("Normal", "Silent", "Vibrate").forEach { mode ->
          FilterChip(
            selected = audioMode == mode,
            onClick = { audioMode = mode },
            label = { Text(mode) }
          )
        }
      }

      // Play Test Button
      Button(
        onClick = {
          isPlaying = !isPlaying
          // In a real implementation, this would play audio
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary
        )
      ) {
        Text(if (isPlaying) "Stop Audio" else "Play Test Sound")
      }

      // Status
      Text(
        text = "Status: ${if (isPlaying) "Playing" else "Stopped"}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun LocationDiagnosticsCard() {
  var isExpanded by remember { mutableStateOf(false) }
  var hasPermission by remember { mutableStateOf(false) }
  var currentLocation by remember { mutableStateOf<String?>(null) }
  var isTracking by remember { mutableStateOf(false) }
  val context = LocalContext.current

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    hasPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
  }

  LaunchedEffect(Unit) {
    hasPermission = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
  }

  DiagnosticsCard(
    icon = R.drawable.pin,
    iconColor = RAColors.PrimaryBlue,
    title = "Background Location",
    description = "Test location tracking when your app is foregrounded, backgrounded, or closed. Verify location permissions and accuracy settings.",
    isExpanded = isExpanded,
    onExpandClick = { isExpanded = !isExpanded }
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Permission Status
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(if (hasPermission) RAColors.PrimaryGreen else RAColors.PrimaryRed)
        )
        Text(
          text = if (hasPermission) "Location permission granted" else "Location permission not granted",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      if (!hasPermission) {
        Button(
          onClick = {
            permissionLauncher.launch(
              arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
              )
            )
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
          )
        ) {
          Text("Request Permission")
        }
      } else {
        Button(
          onClick = {
            isTracking = !isTracking
            if (isTracking) {
              // Start location tracking
              currentLocation = "Fetching location..."
            } else {
              currentLocation = null
            }
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
          )
        ) {
          Text(if (isTracking) "Stop Tracking" else "Start Tracking")
        }

        currentLocation?.let { location ->
          Text(
            text = location,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Privacy Note
      Text(
        text = "Location data stays on your device.",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun GeofencingDiagnosticsCard() {
  var isExpanded by remember { mutableStateOf(false) }
  var latitude by remember { mutableStateOf("37.7749") }
  var longitude by remember { mutableStateOf("-122.4194") }
  var radius by remember { mutableStateOf("100") }
  var isMonitoring by remember { mutableStateOf(false) }

  DiagnosticsCard(
    icon = R.drawable.pin,
    iconColor = RAColors.PrimaryGreen,
    title = "Geofencing",
    description = "Test region monitoring by defining geographic areas with latitude, longitude, and radius. Verify enter/exit triggers.",
    isExpanded = isExpanded,
    onExpandClick = { isExpanded = !isExpanded }
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Coordinate Input
      OutlinedTextField(
        value = latitude,
        onValueChange = { latitude = it },
        label = { Text("Latitude") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
      )

      OutlinedTextField(
        value = longitude,
        onValueChange = { longitude = it },
        label = { Text("Longitude") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
      )

      OutlinedTextField(
        value = radius,
        onValueChange = { radius = it },
        label = { Text("Radius (meters)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
      )

      Button(
        onClick = { isMonitoring = !isMonitoring },
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary
        )
      ) {
        Text(if (isMonitoring) "Stop Monitoring" else "Start Monitoring")
      }

      if (isMonitoring) {
        Card(
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
          )
        ) {
          Text(
            text = "Monitoring region at ($latitude, $longitude) with radius ${radius}m",
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
private fun DiagnosticsCard(
  icon: Int,
  iconColor: androidx.compose.ui.graphics.Color,
  title: String,
  description: String,
  isExpanded: Boolean,
  onExpandClick: () -> Unit,
  expandedContent: @Composable () -> Unit = {}
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onExpandClick() },
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    shape = RoundedCornerShape(12.dp)
  ) {
    Column {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
      ) {
        // Icon
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(iconColor.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            painter = painterResource(icon),
            contentDescription = title,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
          )
        }

        // Content
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = title,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
              painter = painterResource(
                if (isExpanded) R.drawable.chevron_up else R.drawable.chevron_down
              ),
              contentDescription = if (isExpanded) "Collapse" else "Expand",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp)
            )
          }

          Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      if (isExpanded) {
        HorizontalDivider()
        expandedContent()
      }
    }
  }
}

@Composable
private fun PrivacyNote() {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 16.dp),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      painter = painterResource(R.drawable.lock_icon),
      contentDescription = "Privacy",
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.size(20.dp)
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = "All diagnostic data stays on your device",
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}
