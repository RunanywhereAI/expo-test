package host.exp.exponent.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import host.exp.exponent.graphql.Home_AccountAppsQuery
import host.exp.exponent.graphql.Home_AccountSnacksQuery
import host.exp.exponent.services.HistoryItem
import host.exp.expoview.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  viewModel: HomeAppViewModel,
  navigateToProjects: () -> Unit,
  navigateToSnacks: () -> Unit,
  navigateToProjectDetails: (appId: String) -> Unit,
  navigateToFeedback: () -> Unit,
  onLoginClick: () -> Unit,
  accountHeader: @Composable () -> Unit = { },
  bottomBar: @Composable () -> Unit = { }
) {
  val recents by viewModel.recents.collectAsStateWithLifecycle()
  val snacks by viewModel.snacks.dataFlow.collectAsStateWithLifecycle()
  val account by viewModel.account.dataFlow.collectAsStateWithLifecycle()
  val isRefreshing by viewModel.account.loadingFlow.collectAsStateWithLifecycle()
  val apps by viewModel.apps.dataFlow.collectAsStateWithLifecycle()
  val developmentServers by viewModel.developmentServers.collectAsStateWithLifecycle()

  val context = LocalContext.current
  val uriHandler = LocalUriHandler.current

  var showHelpDialog by remember { mutableStateOf(false) }

  if (showHelpDialog) {
    AlertDialog(
      onDismissRequest = { showHelpDialog = false },
      title = { Text("Troubleshooting") },
      text = { Text("Make sure you are signed in to the same account on your computer and this app. Also verify that your computer is connected to the internet, and ideally to the same Wi-Fi network as your mobile device.") },
      confirmButton = {
        TextButton(onClick = { showHelpDialog = false }) {
          Text("OK", color = MaterialTheme.colorScheme.primary)
        }
      }
    )
  }

  Scaffold(
    topBar = { RANavigationHeader(accountHeader = accountHeader) },
    bottomBar = bottomBar
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .padding(paddingValues)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
      // Dev Servers Section (Primary functionality - URL entry)
      RADevServersSection(
        developmentServers = developmentServers,
        isSignedIn = account != null,
        onLoginClick = onLoginClick,
        onHelpClick = { showHelpDialog = true },
        onScanQR = { /* QR scanner placeholder */ }
      )

      // Recently Opened Section
      if (recents.isNotEmpty()) {
        RARecentlyOpenedSection(
          recents = recents,
          onClearClick = { viewModel.clearRecents() }
        )
      }

      // Projects Section (if signed in)
      if (apps.isNotEmpty()) {
        RAProjectsSection(
          apps = apps,
          onProjectClick = { appId -> navigateToProjectDetails(appId) },
          onViewAllClick = navigateToProjects
        )
      }

      // Snacks Section (if signed in)
      if (snacks.isNotEmpty()) {
        RASnacksSection(
          snacks = snacks,
          onViewAllClick = navigateToSnacks
        )
      }

      // User Review Section
      UserReviewSection(
        viewModel = viewModel,
        navigateToFeedback = navigateToFeedback
      )

      // Upgrade Warning
      UpgradeWarning()

      // Footer
      RAFooterSection()
    }
  }
}

// MARK: - Navigation Header
@Composable
fun RANavigationHeader(
  accountHeader: @Composable () -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .statusBarsPadding(),
    color = MaterialTheme.colorScheme.surface
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Logo and app name
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // RunAnywhere logo
        Image(
          painter = painterResource(R.drawable.runanywhere_logo),
          contentDescription = "RunAnywhere AI Studio",
          modifier = Modifier.size(32.dp)
        )
        Text(
          text = "RunAnywhere AI Studio",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      // Account header action
      accountHeader()
    }
  }
}

// MARK: - Dev Servers Section (Primary Functionality)
@Composable
fun RADevServersSection(
  developmentServers: List<DevSession>,
  isSignedIn: Boolean,
  onLoginClick: () -> Unit,
  onHelpClick: () -> Unit,
  onScanQR: () -> Unit
) {
  var manualURL by remember { mutableStateOf("") }
  val uriHandler = LocalUriHandler.current

  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    // Section Header
    RASectionHeader(
      title = "ENTER URL OR SCAN QR CODE",
      action = { SmallActionButton(label = "HELP", onClick = onHelpClick) }
    )

    // URL Entry Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = manualURL,
            onValueChange = { manualURL = it },
            placeholder = { Text("exp://192.168.x.x:8081") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Uri,
              imeAction = ImeAction.Go
            ),
            keyboardActions = KeyboardActions(
              onGo = {
                if (manualURL.isNotEmpty()) {
                  val url = if (!manualURL.startsWith("exp://") && !manualURL.startsWith("http://") && !manualURL.startsWith("https://")) {
                    "exp://$manualURL"
                  } else manualURL
                  uriHandler.openUri(url)
                  manualURL = ""
                }
              }
            ),
            shape = RoundedCornerShape(8.dp)
          )

          IconButton(
            onClick = {
              if (manualURL.isNotEmpty()) {
                val url = if (!manualURL.startsWith("exp://") && !manualURL.startsWith("http://") && !manualURL.startsWith("https://")) {
                  "exp://$manualURL"
                } else manualURL
                uriHandler.openUri(url)
                manualURL = ""
              }
            },
            enabled = manualURL.isNotEmpty()
          ) {
            Icon(
              painter = painterResource(R.drawable.arrow_forward),
              contentDescription = "Open URL",
              tint = if (manualURL.isNotEmpty()) RAColors.PrimaryAccent else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(32.dp)
            )
          }
        }

        Text(
          text = "Enter the URL shown when you run 'npx expo start'",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Scan QR Button
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .clickable { onScanQR() },
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          painter = painterResource(R.drawable.qr_code),
          contentDescription = "Scan QR",
          tint = RAColors.PrimaryAccent,
          modifier = Modifier.size(24.dp)
        )
        Text(
          text = "Scan QR Code",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
          painter = painterResource(R.drawable.chevron_right),
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp)
        )
      }
    }

    // Local Development Servers
    if (developmentServers.isNotEmpty()) {
      Text(
        text = "LOCAL DEVELOPMENT SERVERS",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp)
      )

      developmentServers.forEach { session ->
        DevSessionRow(session = session)
      }
    } else if (!isSignedIn) {
      LocalServerTutorial(
        isSignedIn = false,
        modifier = Modifier.padding(top = 8.dp),
        onLoginClick = onLoginClick
      )
    }
  }
}

// MARK: - Recently Opened Section
@Composable
fun RARecentlyOpenedSection(
  recents: List<HistoryItem>,
  onClearClick: () -> Unit
) {
  val uriHandler = LocalUriHandler.current

  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      RASectionHeader(title = "RECENTLY OPENED")
      SmallActionButton(label = "CLEAR", onClick = onClearClick)
    }

    recents.take(5).forEach { historyItem ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { uriHandler.openUri(historyItem.manifestUrl) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // App icon
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(4.dp))
              .background(RAColors.PrimaryAccent.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              painter = painterResource(R.drawable.project_default_icon),
              contentDescription = null,
              tint = RAColors.PrimaryAccent,
              modifier = Modifier.size(24.dp)
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = historyItem.name,
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = historyItem.manifestUrl,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1
            )
          }

          Icon(
            painter = painterResource(R.drawable.chevron_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

// MARK: - Projects Section
@Composable
fun RAProjectsSection(
  apps: List<Home_AccountAppsQuery.App>,
  onProjectClick: (String) -> Unit,
  onViewAllClick: () -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    RASectionHeader(title = "PROJECTS")

    apps.take(3).forEach { app ->
      AppRow(app = app, onClick = { onProjectClick(app.commonAppData.id) })
    }

    if (apps.size > 3) {
      TextButton(
        onClick = onViewAllClick,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "View all projects (${apps.size})",
          color = RAColors.PrimaryAccent
        )
      }
    }
  }
}

// MARK: - Snacks Section
@Composable
fun RASnacksSection(
  snacks: List<Home_AccountSnacksQuery.Snack>,
  onViewAllClick: () -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    RASectionHeader(title = "SNACKS")

    snacks.take(3).forEach { snack ->
      SnackRow(snack = snack)
    }

    if (snacks.size > 3) {
      TextButton(
        onClick = onViewAllClick,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "View all snacks (${snacks.size})",
          color = RAColors.PrimaryAccent
        )
      }
    }
  }
}

// MARK: - Footer
@Composable
fun RAFooterSection() {
  val uriHandler = LocalUriHandler.current

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Text(
      text = "Powered by RunAnywhere SDK",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
      TextButton(onClick = { uriHandler.openUri("https://runanywhere.dev") }) {
        Text("Website", color = RAColors.PrimaryAccent, style = MaterialTheme.typography.bodySmall)
      }
      TextButton(onClick = { uriHandler.openUri("https://docs.runanywhere.dev") }) {
        Text("Docs", color = RAColors.PrimaryAccent, style = MaterialTheme.typography.bodySmall)
      }
    }
  }
}

// MARK: - Section Header
@Composable
fun RASectionHeader(
  title: String,
  action: (@Composable () -> Unit)? = null
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    action?.invoke()
  }
}
