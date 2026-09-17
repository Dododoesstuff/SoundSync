package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.MusicPlatform

@Composable
fun CreateRoomDialog(
    onDismiss: () -> Unit,
    onCreateRoom: (title: String, platform: MusicPlatform, tags: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf(MusicPlatform.UNIFIED) }
    var tags by remember { mutableStateOf("Chill, Lo-Fi, Electronic") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Host Live Jam Session",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Create a collaborative real-time room where friends can listen along and add tracks to the shared queue.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Session Title") },
                    placeholder = { Text("e.g. Weekend Synth Vibes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("room_title_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Target Catalog:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(MusicPlatform.UNIFIED, MusicPlatform.SPOTIFY, MusicPlatform.YOUTUBE_MUSIC).forEach { platform ->
                        FilterChip(
                            selected = selectedPlatform == platform,
                            onClick = { selectedPlatform = platform },
                            label = { Text(platform.displayName) },
                            modifier = Modifier.testTag("room_chip_${platform.name}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Genres / Mood Tags") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("room_tags_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = title.ifBlank { "Live SoundSync Session" }
                    onCreateRoom(finalTitle, selectedPlatform, tags)
                    onDismiss()
                },
                modifier = Modifier.testTag("submit_create_room_btn")
            ) {
                Text("Start Session")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreatePlaylist: (name: String, description: String, platform: MusicPlatform) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf(MusicPlatform.UNIFIED) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Unified Playlist",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Playlist Name") },
                    placeholder = { Text("My Awesome Mix") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("playlist_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Cross-platform collection") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Sync Platform:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(MusicPlatform.UNIFIED, MusicPlatform.SPOTIFY, MusicPlatform.YOUTUBE_MUSIC).forEach { platform ->
                        FilterChip(
                            selected = selectedPlatform == platform,
                            onClick = { selectedPlatform = platform },
                            label = { Text(platform.displayName) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreatePlaylist(name, description, selectedPlatform)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("confirm_create_playlist_btn")
            ) {
                Text("Create Playlist")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
