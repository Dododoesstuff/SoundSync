package com.example.data.model

import com.example.data.local.entities.TrackEntity

enum class SearchCategory(val displayName: String) {
    ALL("All"),
    TRACKS("Songs"),
    ARTISTS("Artists"),
    ALBUMS("Albums")
}

data class SearchResultTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationSec: Int,
    val coverUrl: String,
    val platform: MusicPlatform,
    val externalId: String,
    val externalUrl: String = "",
    val previewUrl: String? = null,
    val isrc: String? = null,
    val popularity: Int? = null,
    val isExplicit: Boolean = false,
    val genre: String = "Pop",
    val hasSpotifyMatch: Boolean = false,
    val hasYouTubeMatch: Boolean = false,
    val spotifyUri: String? = null,
    val youtubeVideoId: String? = null,
    val cleanTitle: String = title,
    val cleanArtist: String = artist,
    val category: SearchCategory = SearchCategory.TRACKS,
    val isTopResult: Boolean = false,
    val matchConfidence: Float = 0.95f
) {
    fun toTrackEntity(): TrackEntity {
        return TrackEntity(
            id = id,
            title = cleanTitle,
            artist = cleanArtist,
            album = album,
            durationSec = durationSec,
            coverUrl = coverUrl,
            platform = platform,
            externalId = externalId,
            streamUrl = previewUrl ?: externalUrl,
            genre = genre,
            energyRating = 0.8f
        )
    }

    val isUnifiedMatch: Boolean
        get() = platform == MusicPlatform.UNIFIED || (hasSpotifyMatch && hasYouTubeMatch)
}
