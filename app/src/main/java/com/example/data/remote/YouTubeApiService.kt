package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {
    @GET("youtube/v3/search")
    suspend fun searchMusic(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("part") part: String = "snippet",
        @Query("type") type: String = "video",
        @Query("videoCategoryId") videoCategoryId: String = "10",
        @Query("maxResults") maxResults: Int = 20
    ): YouTubeSearchResponse
}

@JsonClass(generateAdapter = true)
data class YouTubeSearchResponse(
    @Json(name = "items") val items: List<YouTubeSearchItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class YouTubeSearchItem(
    @Json(name = "id") val id: YouTubeIdItem,
    @Json(name = "snippet") val snippet: YouTubeSnippetItem
)

@JsonClass(generateAdapter = true)
data class YouTubeIdItem(
    @Json(name = "videoId") val videoId: String? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeSnippetItem(
    @Json(name = "title") val title: String,
    @Json(name = "channelTitle") val channelTitle: String,
    @Json(name = "description") val description: String = "",
    @Json(name = "thumbnails") val thumbnails: YouTubeThumbnailsContainer? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeThumbnailsContainer(
    @Json(name = "high") val high: YouTubeThumbnailItem? = null,
    @Json(name = "medium") val medium: YouTubeThumbnailItem? = null,
    @Json(name = "default") val default: YouTubeThumbnailItem? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeThumbnailItem(
    @Json(name = "url") val url: String = ""
)
