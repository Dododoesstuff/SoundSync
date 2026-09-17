package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.MusicPlatform

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationSec: Int,
    val coverUrl: String,
    val platform: MusicPlatform,
    val externalId: String,
    val streamUrl: String = "",
    val isOfflineDownloaded: Boolean = false,
    val offlineFileSizeMb: Double = 0.0,
    val playCount: Int = 0,
    val lastPlayedTimestamp: Long = 0L,
    val genre: String = "Pop",
    val energyRating: Float = 0.7f,
    val isLiked: Boolean = false,
    val syncedLyrics: String = "",
    val lyrics: String = "♪ Instrumental beat intro\n♪ Vibrant synthesizer melody flowing\n♪ Harmonious vocals and resonant bass\n♪ Outro fade..."
)
