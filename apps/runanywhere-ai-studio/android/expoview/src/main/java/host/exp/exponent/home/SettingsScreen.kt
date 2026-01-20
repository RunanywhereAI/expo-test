package host.exp.exponent.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import expo.modules.devmenu.DevMenuPreferences
import host.exp.exponent.generated.ExponentBuildConstants
import host.exp.exponent.graphql.fragment.CurrentUserActorData
import host.exp.exponent.services.ThemeSetting
import host.exp.expoview.R
import kotlinx.coroutines.launch

private fun getMajorVersion(version: String): String {
  return version.split(".").firstOrNull() ?: version
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  viewModel: HomeAppViewModel,
  bottomBar: @Composable () -> Unit = { },
  accountHeader: @Composable () -> Unit = { }
) {
  val selectedTheme by viewModel.selectedTheme.collectAsStateWithLifecycle()
  val selectedAccount by viewModel.selectedAccount.collectAsStateWithLifecycle()

  Scaffold(
    topBar = { RANavigationHeader(accountHeader = accountHeader) },
    bottomBar = bottomBar
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(paddingValues)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
      // Theme Section
      ThemeSection(
        selectedTheme = selectedTheme,
        onThemeSelected = { viewModel.selectedTheme.value = it }
      )

      // Developer Menu Section
      DeveloperMenuSection(viewModel.devMenuPreferencesAdapter)

      // App Info Section
      AppInfoSection(
        clientVersion = viewModel.expoVersion ?: "1.0.0",
        supportedSdk = getMajorVersion(ExponentBuildConstants.TEMPORARY_SDK_VERSION)
      )

      // Account Section (if signed in)
      if (selectedAccount != null) {
        AccountSection(
          account = selectedAccount,
          onSignOut = { viewModel.logout() }
        )
      }

      // Legal Section
      LegalSection()

      // Delete Account Section (if signed in)
      if (selectedAccount != null) {
        DeleteAccountSection()
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
fun ThemeSection(
  selectedTheme: ThemeSetting,
  onThemeSelected: (ThemeSetting) -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text(
      text = "Theme",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
      color = MaterialTheme.colorScheme.onSurface
    )

    Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column {
        RAThemeOptionRow(
          icon = R.drawable.theme_auto,
          title = "Automatic",
          isSelected = selectedTheme == ThemeSetting.Automatic,
          onClick = { onThemeSelected(ThemeSetting.Automatic) }
        )
        HorizontalDivider()
        RAThemeOptionRow(
          icon = R.drawable.theme_light,
          title = "Light",
          isSelected = selectedTheme == ThemeSetting.Light,
          onClick = { onThemeSelected(ThemeSetting.Light) }
        )
        HorizontalDivider()
        RAThemeOptionRow(
          icon = R.drawable.theme_dark,
          title = "Dark",
          isSelected = selectedTheme == ThemeSetting.Dark,
          onClick = { onThemeSelected(ThemeSetting.Dark) }
        )
      }
    }

    Text(
      text = "Automatic uses your device's system appearance setting.",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
fun RAThemeOptionRow(
  icon: Int,
  title: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(16.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      painter = painterResource(icon),
      contentDescription = title,
      tint = RAColors.PrimaryAccent,
      modifier = Modifier.size(20.dp)
    )

    Text(
      text = title,
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.weight(1f)
    )

    if (isSelected) {
      Icon(
        painter = painterResource(R.drawable.check),
        contentDescription = "Selected",
        tint = RAColors.PrimaryAccent,
        modifier = Modifier.size(20.dp)
      )
    }
  }
}

@Composable
fun AppInfoSection(
  clientVersion: String,
  supportedSdk: String
) {
  val context = LocalContext.current

  fun copyToClipboard(label: String, text: String) {
    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
  }

  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text(
      text = "App Info",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
      color = MaterialTheme.colorScheme.onSurface
    )

    Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column {
        RAInfoRow(
          label = "Client Version",
          value = clientVersion,
          onClick = { copyToClipboard("Client Version", clientVersion) }
        )
        HorizontalDivider()
        RAInfoRow(
          label = "Supported SDK",
          value = supportedSdk,
          onClick = { copyToClipboard("Supported SDK", supportedSdk) }
        )
        HorizontalDivider()
        RAInfoRow(
          label = "Powered by",
          value = "RunAnywhere + Expo",
          onClick = { copyToClipboard("Powered by", "RunAnywhere + Expo") }
        )
      }
    }

    // Copy Build Info Button
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .clickable {
          val buildInfo = "RunAnywhere AI Studio\nVersion: $clientVersion\nSDK: $supportedSdk"
          copyToClipboard("Build Info", buildInfo)
        },
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          painter = painterResource(R.drawable.copy_icon),
          contentDescription = "Copy",
          tint = RAColors.PrimaryAccent,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Copy Build Info",
          style = MaterialTheme.typography.bodyLarge,
          color = RAColors.PrimaryAccent
        )
      }
    }
  }
}

@Composable
fun RAInfoRow(
  label: String,
  value: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(16.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
fun AccountSection(
  account: CurrentUserActorData.Account?,
  onSignOut: () -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text(
      text = "Account",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
      color = MaterialTheme.colorScheme.onSurface
    )

    Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // User info
        Row(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Avatar placeholder
          Box(
            modifier = Modifier
              .size(48.dp)
              .background(
                RAColors.PrimaryAccent.copy(alpha = 0.1f),
                RoundedCornerShape(24.dp)
              ),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = account?.name?.take(1)?.uppercase() ?: "U",
              style = MaterialTheme.typography.titleLarge,
              color = RAColors.PrimaryAccent
            )
          }

          Column {
            Text(
              text = account?.name ?: "User",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Signed in",
              style = MaterialTheme.typography.bodySmall,
              color = RAColors.PrimaryGreen
            )
          }
        }

        // Sign Out Button
        Button(
          onClick = onSignOut,
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(
            containerColor = RAColors.PrimaryAccent.copy(alpha = 0.1f),
            contentColor = RAColors.PrimaryAccent
          ),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text("Sign Out")
        }
      }
    }
  }
}

@Composable
fun LegalSection() {
  val uriHandler = LocalUriHandler.current

  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text(
      text = "Legal",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
      color = MaterialTheme.colorScheme.onSurface
    )

    Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column {
        // Privacy Policy
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { uriHandler.openUri("https://runanywhere.dev/privacy") }
            .padding(16.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            painter = painterResource(R.drawable.privacy_icon),
            contentDescription = "Privacy",
            tint = RAColors.PrimaryAccent,
            modifier = Modifier.size(20.dp)
          )
          Text(
            text = "Privacy Policy",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
          )
          Icon(
            painter = painterResource(R.drawable.external_link),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
          )
        }

        HorizontalDivider()

        // Terms of Service
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { uriHandler.openUri("https://runanywhere.dev/terms") }
            .padding(16.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            painter = painterResource(R.drawable.document_icon),
            contentDescription = "Terms",
            tint = RAColors.PrimaryAccent,
            modifier = Modifier.size(20.dp)
          )
          Text(
            text = "Terms of Service",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
          )
          Icon(
            painter = painterResource(R.drawable.external_link),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
          )
        }

        HorizontalDivider()

        // Visit Website
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { uriHandler.openUri("https://runanywhere.dev") }
            .padding(16.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            painter = painterResource(R.drawable.globe_icon),
            contentDescription = "Website",
            tint = RAColors.PrimaryAccent,
            modifier = Modifier.size(20.dp)
          )
          Text(
            text = "Visit Website",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
          )
          Icon(
            painter = painterResource(R.drawable.external_link),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}

@Composable
fun DeveloperMenuSection(
  devMenuPreference: DevMenuPreferences
) {
  var launchAtStart by remember { mutableStateOf(devMenuPreference.showsAtLaunch) }
  var enableShake by remember { mutableStateOf(devMenuPreference.motionGestureEnabled) }
  var enableThreeFingerLongPress by remember { mutableStateOf(devMenuPreference.touchGestureEnabled) }
  var showFab by remember { mutableStateOf(devMenuPreference.showFab) }

  DisposableEffect(true) {
    val onNewPreferences = {
      launchAtStart = devMenuPreference.showsAtLaunch
      enableShake = devMenuPreference.motionGestureEnabled
      enableThreeFingerLongPress = devMenuPreference.touchGestureEnabled
      showFab = devMenuPreference.showFab
    }

    devMenuPreference.addOnChangeListener(onNewPreferences)
    onDispose {
      devMenuPreference.removeOnChangeListener(onNewPreferences)
    }
  }

  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text(
      text = "Developer Menu Gestures",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
      color = MaterialTheme.colorScheme.onSurface
    )

    Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column {
        RASwitchRow(
          icon = R.drawable.launch_at_start,
          title = "Show at launch",
          checked = launchAtStart,
          onCheckedChange = { newValue ->
            launchAtStart = newValue
            devMenuPreference.showsAtLaunch = newValue
          }
        )

        HorizontalDivider()

        RASwitchRow(
          icon = R.drawable.shake,
          title = "Shake device",
          checked = enableShake,
          onCheckedChange = { newValue ->
            enableShake = newValue
            devMenuPreference.motionGestureEnabled = newValue
          }
        )

        HorizontalDivider()

        RASwitchRow(
          icon = R.drawable.three_finger_long_press,
          title = "Three-finger long press",
          checked = enableThreeFingerLongPress,
          onCheckedChange = { newValue ->
            enableThreeFingerLongPress = newValue
            devMenuPreference.touchGestureEnabled = newValue
          }
        )

        HorizontalDivider()

        RASwitchRow(
          icon = R.drawable.fab,
          title = "Action button",
          checked = showFab,
          onCheckedChange = { newValue ->
            showFab = newValue
            devMenuPreference.showFab = newValue
          }
        )
      }
    }

    Text(
      text = "Selected gestures toggle the developer menu inside an experience. The menu lets you reload, return home, and access developer tools.",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
fun RASwitchRow(
  icon: Int,
  title: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onCheckedChange(!checked) }
      .padding(horizontal = 16.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      painter = painterResource(icon),
      contentDescription = title,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.size(20.dp)
    )

    Text(
      text = title,
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.weight(1f)
    )

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = MaterialTheme.colorScheme.primary,
        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
        checkedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
        uncheckedBorderColor = androidx.compose.ui.graphics.Color.Transparent
      )
    )
  }
}

@Composable
fun DeleteAccountSection() {
  var deletionError by remember { mutableStateOf<String?>(null) }
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  val handleDeleteAccount: () -> Unit = {
    deletionError = null

    coroutineScope.launch {
      try {
        val redirectBase = "runanywhere://after-delete"
        val encodedRedirect = Uri.encode(redirectBase)
        val authSessionURL = "https://expo.dev/settings/delete-user-expo-go?post_delete_redirect_uri=$encodedRedirect"

        val intent = Intent(Intent.ACTION_VIEW, authSessionURL.toUri()).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
      } catch (e: Exception) {
        deletionError = e.message ?: "An unknown error occurred"
      }
    }
  }

  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(12.dp)
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text(
        text = "Delete Account",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface
      )

      Text(
        text = "This action is irreversible. It will delete your personal account, projects, and activity.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      deletionError?.let { error ->
        Text(
          text = error,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error
        )
      }

      Button(
        onClick = handleDeleteAccount,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
          containerColor = RAColors.PrimaryRed.copy(alpha = 0.1f),
          contentColor = RAColors.PrimaryRed
        ),
        shape = RoundedCornerShape(8.dp)
      ) {
        Text("Delete Account")
      }
    }
  }
}
