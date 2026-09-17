package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.entities.PlaylistEntity
import com.example.data.model.MusicPlatform
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.components.ExportDialog
import com.example.ui.components.OfflineSyncManagerDialog
import com.example.ui.components.PlatformBadge
import com.example.ui.components.PlaylistCard
import com.example.ui.components.TrackListItem
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.YouTubeRed
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun LibraryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedPlatformFilter by viewModel.selectedPlatformFilter.collectAsStateWithLifecycle()
    val onlyOfflineFilter by viewModel.onlyOfflineFilter.collectAsStateWithLifecycle()
    val filteredTracks by viewModel.filteredTracks.collectAsStateWithLifecycle()
    val filteredPlaylists by viewModel.filteredPlaylists.collectAsStateWithLifecycle()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val allPlaylists by viewModel.allPlaylists.collectAsStateWithLifecycle()
    val downloadProgressMap by viewModel.playlistDownloadProgress.collectAsStateWithLifecycle()
    val offlineTracks by viewModel.offlineTracks.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) } // 0 = Playlists, 1 = Songs
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var playlistToExport by remember { mutableStateOf<PlaylistEntity?>(null) }
    var showOfflineSyncDialog by remember { mutableStateOf(false) }

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onCreatePlaylist = { name, desc, platform ->
                viewModel.createPlaylist(name, desc, platform)
            }
        )
    }

    if (playlistToExport != null) {
        ExportDialog(
            playlist = playlistToExport!!,
            onExportMetadata = { pl, fmt -> viewModel.exportPlaylistMetadata(pl, fmt) },
            onDismiss = { playlistToExport = null }
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

    Box(modifier = modifier.fillMaxSize().testTag("library_screen")) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header / Selected playlist title
            if (selectedPlaylist != null) {
                // Playlist detail top header
                val playlist = selectedPlaylist!!
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.selectPlaylist(null) },
                        modifier = Modifier.testTag("back_to_library_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = playlist.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PlatformBadge(platform = playlist.platform, compact = true)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${playlist.trackCount} tracks • Creator: ${playlist.creatorName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = { playlistToExport = playlist }) {
                        Icon(Icons.Default.Share, contentDescription = "Export")
                    }
                    IconButton(onClick = {
                        val target = if (playlist.platform == MusicPlatform.SPOTIFY) MusicPlatform.YOUTUBE_MUSIC else MusicPlatform.SPOTIFY
                        viewModel.migratePlaylist(playlist, target)
                    }) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Migrate", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                // Main Library Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Music Library",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Unified Spotify & YouTube Music Catalogs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showOfflineSyncDialog = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("open_offline_sync_manager_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Offline Sync Manager",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = { showCreatePlaylistDialog = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .testTag("create_playlist_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New Playlist",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search songs, artists, playlists, genres...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("library_search_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                // Filter Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedPlatformFilter == null && !onlyOfflineFilter,
                        onClick = {
                            viewModel.setPlatformFilter(null)
                            viewModel.setOnlyOfflineFilter(false)
                        },
                        label = { Text("All") },
                        modifier = Modifier.testTag("filter_all")
                    )

                    FilterChip(
                        selected = selectedPlatformFilter == MusicPlatform.SPOTIFY,
                        onClick = {
                            viewModel.setPlatformFilter(if (selectedPlatformFilter == MusicPlatform.SPOTIFY) null else MusicPlatform.SPOTIFY)
                        },
                        label = { Text("Spotify") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SpotifyGreen.copy(alpha = 0.2f),
                            selectedLabelColor = SpotifyGreen
                        ),
                        modifier = Modifier.testTag("filter_spotify")
                    )

                    FilterChip(
                        selected = selectedPlatformFilter == MusicPlatform.YOUTUBE_MUSIC,
                        onClick = {
                            viewModel.setPlatformFilter(if (selectedPlatformFilter == MusicPlatform.YOUTUBE_MUSIC) null else MusicPlatform.YOUTUBE_MUSIC)
                        },
                        label = { Text("YT Music") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = YouTubeRed.copy(alpha = 0.2f),
                            selectedLabelColor = YouTubeRed
                        ),
                        modifier = Modifier.testTag("filter_yt_music")
                    )

                    FilterChip(
                        selected = onlyOfflineFilter,
                        onClick = { viewModel.setOnlyOfflineFilter(!onlyOfflineFilter) },
                        label = { Text("Offline") },
                        modifier = Modifier.testTag("filter_offline")
                    )
                }

                // Sub-tabs: Playlists vs Songs
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("Playlists (${filteredPlaylists.size})", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("All Songs (${filteredTracks.size})", fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            // Body Content
            if (selectedPlaylist != null) {
                // Playlist Tracks View
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(filteredTracks) { track ->
                        TrackListItem(
                            track = track,
                            isCurrentTrack = playbackState.currentTrack?.id == track.id,
                            isPlaying = playbackState.isPlaying && playbackState.currentTrack?.id == track.id,
                            onTrackClick = { viewModel.playTrack(track, filteredTracks) },
                            onToggleOffline = { viewModel.toggleOfflineDownload(track) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                        )
                    }
                }
            } else {
                if (activeTab == 0) {
                    // Playlists Grid/List
                    if (filteredPlaylists.isEmpty()) {
                        EmptyLibraryState(
                            title = "No Playlists Found",
                            subtitle = "Try resetting your filter, fetching from linked accounts, or creating a new playlist.",
                            onSyncClick = { viewModel.refreshLinkedAccountsData() }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredPlaylists) { playlist ->
                                PlaylistCard(
                                    playlist = playlist,
                                    onClick = { viewModel.selectPlaylist(playlist) },
                                    onMigrateClick = {
                                        val target = if (playlist.platform == MusicPlatform.SPOTIFY) MusicPlatform.YOUTUBE_MUSIC else MusicPlatform.SPOTIFY
                                        viewModel.migratePlaylist(playlist, target)
                                    },
                                    onExportClick = { playlistToExport = playlist }
                                )
                            }
                        }
                    }
                } else {
                    // Songs List
                    if (filteredTracks.isEmpty()) {
                        EmptyLibraryState(
                            title = "No Songs Found",
                            subtitle = if (onlyOfflineFilter) "No offline downloaded tracks found. Tap the download icon on any song to save for offline listening." else "Try adjusting your search query, or sync songs from your streaming accounts.",
                            onSyncClick = if (!onlyOfflineFilter) { { viewModel.refreshLinkedAccountsData() } } else null
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 96.dp)
                        ) {
                            items(filteredTracks) { track ->
                                TrackListItem(
                                    track = track,
                                    isCurrentTrack = playbackState.currentTrack?.id == track.id,
                                    isPlaying = playbackState.isPlaying && playbackState.currentTrack?.id == track.id,
                                    onTrackClick = { viewModel.playTrack(track, filteredTracks) },
                                    onToggleOffline = { viewModel.toggleOfflineDownload(track) },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyLibraryState(
    title: String,
    subtitle: String,
    onSyncClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LibraryMusic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (onSyncClick != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onSyncClick,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("library_empty_sync_btn")
            ) {
                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Fetch from Linked Accounts", fontSize = 12.sp)
            }
        }
    }
}
