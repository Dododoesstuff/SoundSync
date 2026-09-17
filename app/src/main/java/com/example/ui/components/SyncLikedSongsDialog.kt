package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AccountConnection
import com.example.data.model.MusicPlatform
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.YouTubeRed

@Composable
fun SyncLikedSongsDialog(
    spotifyAccount: AccountConnection,
    youtubeAccount: AccountConnection,
    isSyncing: Boolean,
    onStartSync: (syncSpotifyLiked: Boolean, syncYtLiked: Boolean, syncPlaylists: Boolean, autoMerge: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var syncSpotifyLiked by remember { mutableStateOf(true) }
    var syncYtLiked by remember { mutableStateOf(true) }
    var syncPlaylists by remember { mutableStateOf(true) }
    var autoMergeDuplicates by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = { if (!isSyncing) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sync_liked_songs_dialog")
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Direct Platform Sync",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Import liked songs & playlists directly",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Source Cards
                Text(
                    text = "Select Data Sources:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Spotify Source
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (syncSpotifyLiked) SpotifyGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { syncSpotifyLiked = !syncSpotifyLiked }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = syncSpotifyLiked,
                            onCheckedChange = { syncSpotifyLiked = it },
                            colors = CheckboxDefaults.colors(checkedColor = SpotifyGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Spotify Liked Songs & Library", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = if (spotifyAccount.isConnected) "Connected (${spotifyAccount.userEmail})" else "Offline catalog mode",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // YouTube Music Source
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (syncYtLiked) YouTubeRed.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { syncYtLiked = !syncYtLiked }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = syncYtLiked,
                            onCheckedChange = { syncYtLiked = it },
                            colors = CheckboxDefaults.colors(checkedColor = YouTubeRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("YouTube Music Liked & Thumbs Up", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = if (youtubeAccount.isConnected) "Connected (${youtubeAccount.userEmail})" else "Offline catalog mode",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Additional Sync Options
                Text(
                    text = "Sync Options:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = syncPlaylists,
                        onCheckedChange = { syncPlaylists = it }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import user playlists & mix collections", fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = autoMergeDuplicates,
                        onCheckedChange = { autoMergeDuplicates = it }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Auto-merge duplicates into SoundSync Liked Hub", fontSize = 12.sp)
                }

                if (isSyncing) {
                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Synchronizing tracks, resolving streams & metadata...",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSyncing,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            onStartSync(syncSpotifyLiked, syncYtLiked, syncPlaylists, autoMergeDuplicates)
                        },
                        enabled = !isSyncing && (syncSpotifyLiked || syncYtLiked || syncPlaylists),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("confirm_sync_liked_btn")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(if (isSyncing) "Syncing..." else "Start Sync")
                    }
                }
            }
        }
    }
}
