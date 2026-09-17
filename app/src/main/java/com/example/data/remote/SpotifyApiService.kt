package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface SpotifyApiService {
    @FormUrlEncoded
    @POST("api/token")
    suspend fun getAccessToken(
        @Header("Authorization") basicAuthHeader: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): SpotifyTokenResponse

    @GET("v1/search")
    suspend fun search(
        @Header("Authorization") bearerToken: String,
        @Query("q") query: String,
        @Query("type") type: String = "track,artist,album",
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("market") market: String = "US"
    ): SpotifySearchResponse
}

@JsonClass(generateAdapter = true)
data class SpotifyTokenResponse(
    @field:Json(name = "access_token") val accessToken: String,
    @field:Json(name = "token_type") val tokenType: String,
    @field:Json(name = "expires_in") val expiresIn: Int
)

@JsonClass(generateAdapter = true)
data class SpotifySearchResponse(
    @field:Json(name = "tracks") val tracks: SpotifyTracksPaging? = null
)

@JsonClass(generateAdapter = true)
data class SpotifyTracksPaging(
    @field:Json(name = "items") val items: List<SpotifyTrackItem> = emptyList(),
    @field:Json(name = "total") val total: Int = 0
)

@JsonClass(generateAdapter = true)
data class SpotifyTrackItem(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "artists") val artists: List<SpotifyArtistItem> = emptyList(),
    @field:Json(name = "album") val album: SpotifyAlbumItem? = null,
    @field:Json(name = "duration_ms") val durationMs: Int = 0,
    @field:Json(name = "explicit") val explicit: Boolean = false,
    @field:Json(name = "popularity") val popularity: Int = 0,
    @field:Json(name = "preview_url") val previewUrl: String? = null,
    @field:Json(name = "external_urls") val externalUrls: Map<String, String>? = null,
    @field:Json(name = "external_ids") val externalIds: Map<String, String>? = null,
    @field:Json(name = "uri") val uri: String = ""
)

@JsonClass(generateAdapter = true)
data class SpotifyArtistItem(
    @field:Json(name = "id") val id: String = "",
    @field:Json(name = "name") val name: String = ""
)

@JsonClass(generateAdapter = true)
data class SpotifyAlbumItem(
    @field:Json(name = "id") val id: String = "",
    @field:Json(name = "name") val name: String = "",
    @field:Json(name = "images") val images: List<SpotifyImageItem> = emptyList(),
    @field:Json(name = "release_date") val releaseDate: String? = null
)

@JsonClass(generateAdapter = true)
data class SpotifyImageItem(
    @field:Json(name = "url") val url: String = "",
    @field:Json(name = "height") val height: Int? = null,
    @field:Json(name = "width") val width: Int? = null
)
