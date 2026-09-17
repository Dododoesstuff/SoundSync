package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.MusicPlatform

@Entity(tableName = "listening_stats")
data class ListeningStatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackId: String,
    val trackTitle: String,
    val artist: String,
    val platform: MusicPlatform,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSec: Int,
    val genre: String,
    val syncedToCloud: Boolean = true
)

@Entity(tableName = "migration_tasks")
data class MigrationTaskEntity(
    @PrimaryKey val id: String,
    val playlistName: String,
    val sourcePlatform: MusicPlatform,
    val targetPlatform: MusicPlatform,
    val totalTracks: Int,
    val matchedTracks: Int,
    val failedTracks: Int,
    val status: String,
    val timestamp: Long = System.currentTimeMillis(),
    val exportFormats: String = "JSON, CSV, M3U"
)

@Entity(tableName = "social_rooms")
data class SocialRoomEntity(
    @PrimaryKey val roomCode: String,
    val title: String,
    val hostName: String,
    val hostAvatarUrl: String,
    val currentTrackId: String,
    val currentTrackTitle: String,
    val currentArtist: String,
    val currentCoverUrl: String,
    val listenersCount: Int,
    val platformMode: MusicPlatform,
    val isLive: Boolean = true,
    val tags: String = "Chill, Lo-Fi, Electronic"
)
