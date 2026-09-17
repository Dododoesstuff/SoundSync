package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.MusicPlatform

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val coverUrl: String,
    val platform: MusicPlatform,
    val isSyncedAcrossPlatforms: Boolean = false,
    val isCollaborative: Boolean = false,
    val trackCount: Int = 0,
    val totalDurationSec: Int = 0,
    val cloudSyncTimestamp: Long = System.currentTimeMillis(),
    val shareCode: String = "",
    val creatorName: String = "David",
    val likesCount: Int = 0
)

@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["playlistId", "trackId"]
)
data class PlaylistTrackCrossRef(
    val playlistId: String,
    val trackId: String,
    val orderIndex: Int
)
