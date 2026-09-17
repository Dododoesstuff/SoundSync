package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileDownloadDone
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.repository.RepeatMode
import com.example.ui.components.CustomizableVisualizer
import com.example.ui.components.EqualizerDialog
import com.example.ui.components.PlatformBadge
import com.example.ui.components.SyncedLyricsView
import com.example.ui.components.VisualizerPalette
import com.example.ui.components.VisualizerStyle
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.YouTubeRed
import com.example.ui.viewmodel.MainViewModel

@Composable
fun PlayerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val track = playbackState.currentTrack ?: return
    val syncedLyrics by viewModel.currentSyncedLyrics.collectAsStateWithLifecycle()
    val visualizerStyle by viewModel.visualizerStyle.collectAsStateWithLifecycle()
    val visualizerPalette by viewModel.visualizerPalette.collectAsStateWithLifecycle()
    val visualizerSensitivity by viewModel.visualizerSensitivity.collectAsStateWithLifecycle()
    val sleepTimerMinutes by viewModel.sleepTimerMinutes.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableIntStateOf(0) } // 0 = Player & Visualizer, 1 = Real-Time Synced Lyrics, 2 = Queue & SoundStage EQ
    var showEqDialog by remember { mutableStateOf(false) }
    var showVisualizerCustomizer by remember { mutableStateOf(false) }

    val currentMin = playbackState.currentPositionSec / 60
    val currentSec = (playbackState.currentPositionSec % 60).toString().padStart(2, '0')
    val durMin = playbackState.durationSec / 60
    val durSec = (playbackState.durationSec % 60).toString().padStart(2, '0')

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("full_player_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.setPlayerExpanded(false) },
                    modifier = Modifier.testTag("player_minimize_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Minimize Player",
                        modifier = Modifier.size(30.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ORIGINAL LOSSLESS MASTER",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    PlatformBadge(platform = track.platform, compact = true)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showEqDialog = true },
                        modifier = Modifier.testTag("player_eq_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = "Audio SoundStage EQ",
                            tint = if (sleepTimerMinutes != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleOfflineDownload(track) },
                        modifier = Modifier.testTag("player_offline_btn")
                    ) {
                        Icon(
                            imageVector = if (track.isOfflineDownloaded) Icons.Default.FileDownloadDone else Icons.Default.Download,
                            contentDescription = "Download Offline",
                            tint = if (track.isOfflineDownloaded) SpotifyGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Sub tabs selector (Player / Synced Lyrics / Queue & EQ)
            TabRow(
                selectedTabIndex = activeSubTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Tab(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    text = { Text("Player", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    text = { Text("Live Lyrics", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeSubTab == 2,
                    onClick = { activeSubTab = 2 },
                    text = { Text("Queue (${playbackState.queue.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (activeSubTab) {
                0 -> {
                    // MAIN PLAYER VIEW WITH CUSTOMIZABLE VISUALIZER
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Album Art with Glow
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .aspectRatio(1f)
                                .shadow(18.dp, RoundedCornerShape(24.dp))
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            AsyncImage(
                                model = track.coverUrl,
                                contentDescription = "Track Artwork",
                                modifier = Modifier.matchParentSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Title, Artist & Like Heart Icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${track.artist} — ${track.album}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Like Button
                            IconButton(
                                onClick = { viewModel.toggleLikeTrack(track) },
                                modifier = Modifier.testTag("player_like_heart_btn")
                            ) {
                                Icon(
                                    imageVector = if (track.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Like Song",
                                    tint = if (track.isLiked) YouTubeRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Customizable Visualizer Bar & Switcher
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(visualizerStyle.displayName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    AssistChip(
                                        onClick = { showVisualizerCustomizer = !showVisualizerCustomizer },
                                        label = { Text(if (showVisualizerCustomizer) "Close Styles" else "Customize", fontSize = 10.sp) },
                                        leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(12.dp)) },
                                        modifier = Modifier.height(26.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Render the active customizable visualizer
                                CustomizableVisualizer(
                                    waveformLevels = playbackState.waveformLevels,
                                    isPlaying = playbackState.isPlaying,
                                    style = visualizerStyle,
                                    palette = visualizerPalette,
                                    sensitivity = visualizerSensitivity,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(75.dp)
                                )

                                // Collapsible Visualizer Customizer Controls
                                AnimatedVisibility(visible = showVisualizerCustomizer) {
                                    Column(modifier = Modifier.padding(top = 10.dp)) {
                                        Text("Visualizer Mode:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            VisualizerStyle.values().take(3).forEach { s ->
                                                FilterChip(
                                                    selected = visualizerStyle == s,
                                                    onClick = { viewModel.setVisualizerStyle(s) },
                                                    label = { Text(s.displayName.take(12), fontSize = 9.sp) },
                                                    modifier = Modifier.height(28.dp)
                                                )
                                            }
                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            VisualizerStyle.values().drop(3).forEach { s ->
                                                FilterChip(
                                                    selected = visualizerStyle == s,
                                                    onClick = { viewModel.setVisualizerStyle(s) },
                                                    label = { Text(s.displayName.take(12), fontSize = 9.sp) },
                                                    modifier = Modifier.height(28.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Color Palette:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            VisualizerPalette.values().forEach { p ->
                                                FilterChip(
                                                    selected = visualizerPalette == p,
                                                    onClick = { viewModel.setVisualizerPalette(p) },
                                                    label = { Text(p.displayName.take(10), fontSize = 9.sp) },
                                                    modifier = Modifier.height(28.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Seekbar Slider
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Slider(
                                value = playbackState.currentPositionSec.toFloat(),
                                onValueChange = { viewModel.seekTo(it.toInt()) },
                                valueRange = 0f..playbackState.durationSec.toFloat().coerceAtLeast(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.testTag("player_seekbar")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$currentMin:$currentSec",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$durMin:$durSec",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Main Controls Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Shuffle
                            IconButton(
                                onClick = { viewModel.toggleShuffle() },
                                modifier = Modifier.testTag("player_shuffle_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = "Shuffle",
                                    tint = if (playbackState.isShuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }

                            // Previous
                            IconButton(
                                onClick = { viewModel.previousTrack() },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("player_prev_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Previous",
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            // Big Play / Pause Button
                            IconButton(
                                onClick = { viewModel.togglePlayPause() },
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .testTag("player_main_play_btn")
                            ) {
                                Icon(
                                    imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(38.dp)
                                )
                            }

                            // Next
                            IconButton(
                                onClick = { viewModel.nextTrack() },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("player_next_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Next",
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            // Repeat
                            IconButton(
                                onClick = { viewModel.toggleRepeat() },
                                modifier = Modifier.testTag("player_repeat_btn")
                            ) {
                                Icon(
                                    imageVector = if (playbackState.repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                    contentDescription = "Repeat",
                                    tint = if (playbackState.repeatMode != RepeatMode.OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                1 -> {
                    // REAL-TIME SYNCHRONIZED LYRICS ENGINE VIEW
                    SyncedLyricsView(
                        lyrics = syncedLyrics,
                        currentPositionMs = (playbackState.currentPositionSec * 1000L),
                        isPlaying = playbackState.isPlaying,
                        onSeekToMs = { ms ->
                            viewModel.seekTo((ms / 1000).toInt())
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                2 -> {
                    // QUEUE & AUDIO ENGINE SETTINGS
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // SoundStage Master Button
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showEqDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Equalizer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Open 5-Band SoundStage EQ & Sleep Timer", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Customize frequency bands, bass boost & 3D spatial stage", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Playback Speed Controls
                        Text(
                            text = "Playback Speed: ${playbackState.playbackSpeed}x",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(0.8f, 1.0f, 1.25f, 1.5f).forEach { speed ->
                                FilterChip(
                                    selected = playbackState.playbackSpeed == speed,
                                    onClick = { viewModel.setPlaybackSpeed(speed) },
                                    label = { Text("${speed}x") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Audio Stream Quality
                        Text(
                            text = "Audio Stream Engine:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("320 kbps (High)", "Lossless FLAC", "160 kbps (Normal)").forEach { q ->
                                FilterChip(
                                    selected = playbackState.audioQuality == q,
                                    onClick = { viewModel.setAudioQuality(q) },
                                    label = { Text(q) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Up Next Queue
                        Text(
                            text = "Up Next (${playbackState.queue.size} tracks):",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        playbackState.queue.forEach { qTrack ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { viewModel.playTrack(qTrack, playbackState.queue) },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (qTrack.id == track.id) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = qTrack.coverUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(6.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(qTrack.title, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text(qTrack.artist, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    PlatformBadge(platform = qTrack.platform, compact = true)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEqDialog) {
        EqualizerDialog(
            currentSleepTimerMinutes = sleepTimerMinutes,
            onSetSleepTimer = { viewModel.setSleepTimer(it) },
            onDismiss = { showEqDialog = false }
        )
    }
}
