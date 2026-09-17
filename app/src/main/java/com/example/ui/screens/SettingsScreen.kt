package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AccentTheme
import com.example.data.model.MusicPlatform
import com.example.data.model.ThemeMode
import com.example.ui.components.AccountLoginDialog
import com.example.ui.components.EqualizerDialog
import com.example.ui.components.OfflineSyncManagerDialog
import com.example.ui.components.PlaylistClashDialog
import com.example.ui.components.SyncLikedSongsDialog
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.YouTubeRed
import com.example.ui.viewmodel.MainViewModel
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val spotifyAccount by viewModel.spotifyAccount.collectAsStateWithLifecycle()
    val youtubeAccount by viewModel.youtubeAccount.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val accentTheme by viewModel.accentTheme.collectAsStateWithLifecycle()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsStateWithLifecycle()
    val offlineTracks by viewModel.offlineTracks.collectAsStateWithLifecycle()
    val likedCount by viewModel.likedTracksCount.collectAsStateWithLifecycle()
    val allPlaylists by viewModel.allPlaylists.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val sleepTimerMinutes by viewModel.sleepTimerMinutes.collectAsStateWithLifecycle()
    val downloadProgressMap by viewModel.playlistDownloadProgress.collectAsStateWithLifecycle()

    var loginPlatformToManage by remember { mutableStateOf<MusicPlatform?>(null) }
    var showSyncLikedDialog by remember { mutableStateOf(false) }
    var showClashDialog by remember { mutableStateOf(false) }
    var showEqDialog by remember { mutableStateOf(false) }
    var showOfflineSyncDialog by remember { mutableStateOf(false) }

    if (loginPlatformToManage != null) {
        val platform = loginPlatformToManage!!
        val isSp = platform == MusicPlatform.SPOTIFY
        AccountLoginDialog(
            platform = platform,
            currentEmail = if (isSp) spotifyAccount.userEmail else youtubeAccount.userEmail,
            isConnected = if (isSp) spotifyAccount.isConnected else youtubeAccount.isConnected,
            onDismiss = { loginPlatformToManage = null },
            onConfirmLogin = { email, isConnected ->
                viewModel.updateAccountConnection(platform, email, isConnected)
            }
        )
    }

    if (showSyncLikedDialog) {
        SyncLikedSongsDialog(
            spotifyAccount = spotifyAccount,
            youtubeAccount = youtubeAccount,
            isSyncing = isRefreshing,
            onStartSync = { syncSp, syncYt, syncPl, autoMerge ->
                viewModel.syncLikedSongsAndPlaylists(syncSp, syncYt, syncPl, autoMerge)
                showSyncLikedDialog = false
            },
            onDismiss = { showSyncLikedDialog = false }
        )
    }

    if (showClashDialog) {
        PlaylistClashDialog(
            playlists = allPlaylists,
            onMergePlaylists = { plA, plB, name ->
                viewModel.mergePlaylists(plA, plB, name)
                showClashDialog = false
            },
            onDismiss = { showClashDialog = false }
        )
    }

    if (showEqDialog) {
        EqualizerDialog(
            currentSleepTimerMinutes = sleepTimerMinutes,
            onSetSleepTimer = { viewModel.setSleepTimer(it) },
            onDismiss = { showEqDialog = false }
        )
    }

    if (showOfflineSyncDialog) {
        OfflineSyncManagerDialog(
            playlists = allPlaylists,
            downloadProgressMap = downloadProgressMap,
            offlineTracks = offlineTracks,
            onTogglePlaylistDownload = { viewModel.togglePlaylistOfflineDownload(it) },
            onDownloadAllPlaylists = { viewModel.downloadAllPlaylistsOffline() },
            onClearOfflineCache = { viewModel.clearAllOfflineStorageCache() },
            onDismiss = { showOfflineSyncDialog = false }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Android OS Style Large Title
        item {
            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "System preferences, accounts, theme & audio controls",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // --- SECTION 1: ACCOUNTS & SYNC ---
        item {
            SettingsCategoryHeader(title = "ACCOUNTS & SYNC")
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column {
                    // Spotify Account Row
                    SettingsItemRow(
                        icon = Icons.Default.AccountCircle,
                        iconTint = SpotifyGreen,
                        title = "Spotify Account",
                        subtitle = if (spotifyAccount.isConnected) spotifyAccount.userEmail else "Not connected",
                        trailing = {
                            Button(
                                onClick = { loginPlatformToManage = MusicPlatform.SPOTIFY },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (spotifyAccount.isConnected) MaterialTheme.colorScheme.errorContainer else SpotifyGreen,
                                    contentColor = if (spotifyAccount.isConnected) MaterialTheme.colorScheme.onErrorContainer else Color.White
                                ),
                                modifier = Modifier.testTag("spotify_account_manage_btn")
                            ) {
                                Text(if (spotifyAccount.isConnected) "Manage" else "Sign In", fontSize = 12.sp)
                            }
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // YouTube Music Account Row
                    SettingsItemRow(
                        icon = Icons.Default.AccountCircle,
                        iconTint = YouTubeRed,
                        title = "YouTube Music Account",
                        subtitle = if (youtubeAccount.isConnected) youtubeAccount.userEmail else "Not connected",
                        trailing = {
                            Button(
                                onClick = { loginPlatformToManage = MusicPlatform.YOUTUBE_MUSIC },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (youtubeAccount.isConnected) MaterialTheme.colorScheme.errorContainer else YouTubeRed,
                                    contentColor = if (youtubeAccount.isConnected) MaterialTheme.colorScheme.onErrorContainer else Color.White
                                ),
                                modifier = Modifier.testTag("youtube_account_manage_btn")
                            ) {
                                Text(if (youtubeAccount.isConnected) "Manage" else "Sign In", fontSize = 12.sp)
                            }
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // Direct Cloud Sync Row
                    SettingsItemRow(
                        icon = Icons.Default.CloudSync,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Direct Cross-Platform Sync",
                        subtitle = "$likedCount liked songs unified • Sync playlists & liked music",
                        onClick = { showSyncLikedDialog = true },
                        trailing = {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Open", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // Playlist Clash & Merger Row
                    SettingsItemRow(
                        icon = Icons.Default.CompareArrows,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        title = "Playlist Clash Merger",
                        subtitle = "Detect and combine duplicate playlists into SuperMix",
                        onClick = { showClashDialog = true },
                        trailing = {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Open", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    )
                }
            }
        }

        // --- SECTION 2: DISPLAY & THEME ---
        item {
            SettingsCategoryHeader(title = "DISPLAY & APPEARANCE")
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Theme Appearance Mode", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Select default OS surface canvas mode", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            Triple(ThemeMode.DARK, "Dark Slate Studio", "Deep charcoal surface with soft contrast"),
                            Triple(ThemeMode.AMOLED_BLACK, "AMOLED Deep Black", "Pure #000000 black, maximum battery conservation"),
                            Triple(ThemeMode.CYBERPUNK_NIGHT, "Cyberpunk Neon Night", "High-glow cyan and electric violet neon canvas"),
                            Triple(ThemeMode.HIGH_CONTRAST, "High-Contrast Accessibility", "Ultra-high luminance WCAG AAA contrast"),
                            Triple(ThemeMode.LIGHT, "Light Mode", "Crisp high-luminance light surface")
                        ).forEach { (mode, title, desc) ->
                            val isSelected = themeMode == mode
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                                    )
                                    .clickable { viewModel.setThemeMode(mode) }
                                    .padding(12.dp)
                                    .testTag("theme_mode_${mode.name}"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text("Accent Color Scheme", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AccentTheme.values().take(5).forEach { accent ->
                            val isSelected = accentTheme == accent
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(accent.hex))
                                    .clickable { viewModel.setAccentTheme(accent) }
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .testTag("accent_color_${accent.name}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AccentTheme.values().drop(5).forEach { accent ->
                            val isSelected = accentTheme == accent
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(accent.hex))
                                    .clickable { viewModel.setAccentTheme(accent) }
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .testTag("accent_color_${accent.name}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 3: SOUND & HAPTICS ---
        item {
            SettingsCategoryHeader(title = "SOUND & HAPTICS")
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column {
                    // Equalizer & Sleep Timer Row
                    SettingsItemRow(
                        icon = Icons.Default.Equalizer,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "SoundStage Equalizer & Sleep Timer",
                        subtitle = if ((sleepTimerMinutes ?: 0) > 0) "Sleep timer set for $sleepTimerMinutes min • Tap to configure" else "Acoustic presets, Bass Boost & Sleep Timer",
                        onClick = { showEqDialog = true },
                        trailing = {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Open", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // Touch Haptics Row
                    SettingsItemRow(
                        icon = Icons.Default.Vibration,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        title = "Responsive Touch Haptics",
                        subtitle = "Tactile pulses on playback controls and volume sliders",
                        trailing = {
                            Switch(
                                checked = hapticsEnabled,
                                onCheckedChange = { viewModel.setHapticsEnabled(it) },
                                modifier = Modifier.testTag("haptics_switch")
                            )
                        }
                    )
                }
            }
        }

        // --- SECTION 4: STORAGE & DOWNLOADS ---
        item {
            SettingsCategoryHeader(title = "STORAGE & OFFLINE CACHE")
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column {
                    val totalMb = offlineTracks.sumOf { it.offlineFileSizeMb }
                    SettingsItemRow(
                        icon = Icons.Default.SdStorage,
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        title = "Offline Sync Manager",
                        subtitle = "${offlineTracks.size} downloaded tracks (${String.format(Locale.getDefault(), "%.1f", totalMb)} MB) • Manage playlist downloads",
                        onClick = { showOfflineSyncDialog = true },
                        trailing = {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Open", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.1.sp
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}
