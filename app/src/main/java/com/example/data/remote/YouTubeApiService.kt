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
    @field:Json(name = "items") val items: List<YouTubeSearchItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class YouTubeSearchItem(
    @field:Json(name = "id") val id: YouTubeIdItem,
    @field:Json(name = "snippet") val snippet: YouTubeSnippetItem
)

@JsonClass(generateAdapter = true)
data class YouTubeIdItem(
    @field:Json(name = "videoId") val videoId: String? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeSnippetItem(
    @field:Json(name = "title") val title: String,
    @field:Json(name = "channelTitle") val channelTitle: String,
    @field:Json(name = "description") val description: String = "",
    @field:Json(name = "thumbnails") val thumbnails: YouTubeThumbnailsContainer? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeThumbnailsContainer(
    @field:Json(name = "high") val high: YouTubeThumbnailItem? = null,
    @field:Json(name = "medium") val medium: YouTubeThumbnailItem? = null,
    @field:Json(name = "default") val default: YouTubeThumbnailItem? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeThumbnailItem(
    @field:Json(name = "url") val url: String = ""
)
