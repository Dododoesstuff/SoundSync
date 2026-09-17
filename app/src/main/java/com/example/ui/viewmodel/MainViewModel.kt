package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.ListeningStatEntity
import com.example.data.local.entities.MigrationTaskEntity
import com.example.data.local.entities.PlaylistEntity
import com.example.data.local.entities.SocialRoomEntity
import com.example.data.local.entities.TrackEntity
import com.example.data.model.AccentTheme
import com.example.data.model.AccountConnection
import com.example.data.model.MusicPlatform
import com.example.data.model.SearchCategory
import com.example.data.model.SearchResultTrack
import com.example.data.model.SyncState
import com.example.data.model.ThemeMode
import com.example.data.repository.AudioPlayerEngine
import com.example.data.repository.MusicRepository
import com.example.data.repository.PlayerPlaybackState
import com.example.data.repository.RepeatMode
import com.example.data.remote.SyncedLyricLine
import com.example.data.remote.SyncedLyricsEngine
import com.example.ui.components.VisualizerPalette
import com.example.ui.components.VisualizerStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen(val label: String) {
    DASHBOARD("Dashboard"),
    SEARCH("Search"),
    LIBRARY("Library"),
    MIGRATION("Migrate & Export"),
    ANALYTICS("Listening Stats"),
    SOCIAL("Jam Rooms"),
    SETTINGS("Settings")
}

data class AnalyticsSummary(
    val totalMinutes: Int = 0,
    val spotifyPercentage: Int = 50,
    val youtubePercentage: Int = 50,
    val topArtists: List<Pair<String, Int>> = emptyList(),
    val topGenres: List<Pair<String, Int>> = emptyList(),
    val moodScoreEnergy: Float = 0.82f,
    val moodScoreDanceability: Float = 0.76f,
    val weeklyListeningTrend: List<Int> = listOf(42, 65, 80, 55, 95, 120, 110)
)

data class UiNotification(
    val message: String,
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = MusicRepository(database.musicDao(), application)

    val playerEngine = AudioPlayerEngine(
        context = application,
        onTrackPlayed = { track, durationSec ->
            viewModelScope.launch {
                repository.recordPlayEvent(track, durationSec)
            }
        }
    )

    val playbackState: StateFlow<PlayerPlaybackState> = playerEngine.playbackState

    // Navigation & Current Screen
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _isPlayerExpanded = MutableStateFlow(false)
    val isPlayerExpanded: StateFlow<Boolean> = _isPlayerExpanded.asStateFlow()

    // Themes & Personalization
    private val _themeMode = MutableStateFlow(ThemeMode.DARK)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _accentTheme = MutableStateFlow(AccentTheme.SPOTIFY_GREEN)
    val accentTheme: StateFlow<AccentTheme> = _accentTheme.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(true)
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    // Visualizer Customization Settings
    private val _visualizerStyle = MutableStateFlow(VisualizerStyle.SPECTRUM_BARS)
    val visualizerStyle: StateFlow<VisualizerStyle> = _visualizerStyle.asStateFlow()

    private val _visualizerPalette = MutableStateFlow(VisualizerPalette.CYBER_NEON)
    val visualizerPalette: StateFlow<VisualizerPalette> = _visualizerPalette.asStateFlow()

    private val _visualizerSensitivity = MutableStateFlow(1.0f)
    val visualizerSensitivity: StateFlow<Float> = _visualizerSensitivity.asStateFlow()

    // Real-Time Synced Lyrics
    private val _currentSyncedLyrics = MutableStateFlow<List<SyncedLyricLine>>(emptyList())
    val currentSyncedLyrics: StateFlow<List<SyncedLyricLine>> = _currentSyncedLyrics.asStateFlow()

    // Sleep Timer
    private val _sleepTimerMinutes = MutableStateFlow<Int?>(null)
    val sleepTimerMinutes: StateFlow<Int?> = _sleepTimerMinutes.asStateFlow()
    private var sleepTimerJob: kotlinx.coroutines.Job? = null

    // Notifications & Snackbars
    private val _notification = MutableStateFlow<UiNotification?>(null)
    val notification: StateFlow<UiNotification?> = _notification.asStateFlow()

    // Cross-Platform Search Engine State
    private val _searchEngineQuery = MutableStateFlow("")
    val searchEngineQuery: StateFlow<String> = _searchEngineQuery.asStateFlow()

    private val _searchEngineFilter = MutableStateFlow<MusicPlatform?>(null)
    val searchEngineFilter: StateFlow<MusicPlatform?> = _searchEngineFilter.asStateFlow()

    private val _searchCategoryFilter = MutableStateFlow(SearchCategory.ALL)
    val searchCategoryFilter: StateFlow<SearchCategory> = _searchCategoryFilter.asStateFlow()

    private val _searchEngineResults = MutableStateFlow<List<SearchResultTrack>>(emptyList())
    val searchEngineResults: StateFlow<List<SearchResultTrack>> = _searchEngineResults.asStateFlow()

    private val _searchSuggestions = MutableStateFlow<List<String>>(emptyList())
    val searchSuggestions: StateFlow<List<String>> = _searchSuggestions.asStateFlow()

    private val _isSearchLoading = MutableStateFlow(false)
    val isSearchLoading: StateFlow<Boolean> = _isSearchLoading.asStateFlow()

    val recentSearches: StateFlow<List<String>> = repository.recentSearches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Refresh state for Pull-To-Refresh / SwipeRefresh
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Offline Sync Manager State
    private val _playlistDownloadProgress = MutableStateFlow<Map<String, PlaylistDownloadProgress>>(emptyMap())
    val playlistDownloadProgress: StateFlow<Map<String, PlaylistDownloadProgress>> = _playlistDownloadProgress.asStateFlow()

    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedPlatformFilter = MutableStateFlow<MusicPlatform?>(null)
    val selectedPlatformFilter: StateFlow<MusicPlatform?> = _selectedPlatformFilter.asStateFlow()

    private val _onlyOfflineFilter = MutableStateFlow(false)
    val onlyOfflineFilter: StateFlow<Boolean> = _onlyOfflineFilter.asStateFlow()

    // Active Selected Playlist for Detail View
    private val _selectedPlaylist = MutableStateFlow<PlaylistEntity?>(null)
    val selectedPlaylist: StateFlow<PlaylistEntity?> = _selectedPlaylist.asStateFlow()

    // Repository Flows
    val allTracks: StateFlow<List<TrackEntity>> = repository.allTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val likedTracks: StateFlow<List<TrackEntity>> = repository.likedTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val likedTracksCount: StateFlow<Int> = repository.likedTracksCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val offlineTracks: StateFlow<List<TrackEntity>> = repository.offlineTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlaylists: StateFlow<List<PlaylistEntity>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val migrationTasks: StateFlow<List<MigrationTaskEntity>> = repository.migrationTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val socialRooms: StateFlow<List<SocialRoomEntity>> = repository.socialRooms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val syncState: StateFlow<SyncState> = repository.syncState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncState())

    val spotifyAccount: StateFlow<AccountConnection> = repository.spotifyAccount
    val youtubeAccount: StateFlow<AccountConnection> = repository.youtubeAccount

    // Filtered Tracks
    val filteredTracks: StateFlow<List<TrackEntity>> = combine(
        allTracks,
        _searchQuery,
        _selectedPlatformFilter,
        _onlyOfflineFilter
    ) { tracks, query, platform, onlyOffline ->
        tracks.filter { track ->
            val matchesQuery = query.isBlank() ||
                    track.title.contains(query, ignoreCase = true) ||
                    track.artist.contains(query, ignoreCase = true) ||
                    track.album.contains(query, ignoreCase = true) ||
                    track.genre.contains(query, ignoreCase = true)
            val matchesPlatform = platform == null || track.platform == platform
            val matchesOffline = !onlyOffline || track.isOfflineDownloaded
            matchesQuery && matchesPlatform && matchesOffline
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Playlists
    val filteredPlaylists: StateFlow<List<PlaylistEntity>> = combine(
        allPlaylists,
        _searchQuery,
        _selectedPlatformFilter
    ) { playlists, query, platform ->
        playlists.filter { playlist ->
            val matchesQuery = query.isBlank() ||
                    playlist.name.contains(query, ignoreCase = true) ||
                    playlist.description.contains(query, ignoreCase = true)
            val matchesPlatform = platform == null || playlist.platform == platform || playlist.platform == MusicPlatform.UNIFIED
            matchesQuery && matchesPlatform
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Real-time Analytics Summary
    val analyticsSummary: StateFlow<AnalyticsSummary> = repository.allListeningStats.combine(allTracks) { stats, tracks ->
        if (stats.isEmpty()) {
            AnalyticsSummary(
                totalMinutes = 0,
                spotifyPercentage = 0,
                youtubePercentage = 0,
                topArtists = emptyList(),
                topGenres = emptyList(),
                moodScoreEnergy = 0f,
                moodScoreDanceability = 0f,
                weeklyListeningTrend = listOf(0, 0, 0, 0, 0, 0, 0)
            )
        } else {
            val totalSec = stats.sumOf { it.durationSec }
            val spotifyCount = stats.count { it.platform == MusicPlatform.SPOTIFY }
            val ytCount = stats.count { it.platform == MusicPlatform.YOUTUBE_MUSIC }
            val totalEvents = spotifyCount + ytCount
            val spRatio = if (totalEvents > 0) (spotifyCount * 100) / totalEvents else 0
            val ytRatio = if (totalEvents > 0) 100 - spRatio else 0

            val artistCounts = stats.groupingBy { it.artist }.eachCount().toList().sortedByDescending { it.second }.take(5)
            val genreCounts = stats.groupingBy { it.genre }.eachCount().toList().sortedByDescending { it.second }.take(4)

            AnalyticsSummary(
                totalMinutes = totalSec / 60,
                spotifyPercentage = spRatio,
                youtubePercentage = ytRatio,
                topArtists = artistCounts,
                topGenres = genreCounts,
                moodScoreEnergy = 0.84f,
                moodScoreDanceability = 0.77f,
                weeklyListeningTrend = listOf(0, 0, 0, 0, 0, 0, totalSec / 60)
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsSummary())

    init {
        viewModelScope.launch {
            playbackState.collectLatest { state ->
                val track = state.currentTrack
                if (track != null) {
                    val lyrics = SyncedLyricsEngine.getSyncedLyrics(track)
                    _currentSyncedLyrics.value = lyrics
                } else {
                    _currentSyncedLyrics.value = emptyList()
                }
            }
        }
    }

    // ACTIONS
    fun toggleLikeTrack(track: TrackEntity) {
        viewModelScope.launch {
            repository.toggleTrackLiked(track.id)
            val isNowLiked = !track.isLiked
            val msg = if (isNowLiked) "Added '${track.title}' to Liked Songs" else "Removed '${track.title}' from Liked Songs"
            showNotification(msg)
        }
    }

    fun setVisualizerStyle(style: VisualizerStyle) {
        _visualizerStyle.value = style
        showNotification("Visualizer style set to ${style.displayName}")
    }

    fun setVisualizerPalette(palette: VisualizerPalette) {
        _visualizerPalette.value = palette
        showNotification("Visualizer palette set to ${palette.displayName}")
    }

    fun setVisualizerSensitivity(sensitivity: Float) {
        _visualizerSensitivity.value = sensitivity
    }

    fun setSleepTimer(minutes: Int?) {
        _sleepTimerMinutes.value = minutes
        sleepTimerJob?.cancel()
        if (minutes != null) {
            showNotification("Sleep timer active: audio will pause in $minutes minutes")
            sleepTimerJob = viewModelScope.launch {
                kotlinx.coroutines.delay(minutes * 60 * 1000L)
                playerEngine.pause()
                _sleepTimerMinutes.value = null
                showNotification("Sleep timer completed: audio paused")
            }
        } else {
            showNotification("Sleep timer disabled")
        }
    }

    fun syncLikedSongsAndPlaylists(
        syncSpotify: Boolean,
        syncYoutube: Boolean,
        syncPlaylists: Boolean,
        autoMerge: Boolean
    ) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val result = repository.syncLikedSongsAndPlaylists(syncSpotify, syncYoutube, syncPlaylists, autoMerge)
                showNotification(result.message)
            } catch (e: Exception) {
                showNotification("Sync error: ${e.localizedMessage}", isError = true)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun mergePlaylists(playlistA: PlaylistEntity, playlistB: PlaylistEntity, newName: String) {
        viewModelScope.launch {
            val superPl = repository.mergePlaylists(playlistA, playlistB, newName)
            showNotification("SuperMix '${superPl.name}' created with ${superPl.trackCount} unified tracks!")
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setPlayerExpanded(expanded: Boolean) {
        _isPlayerExpanded.value = expanded
    }

    fun selectPlaylist(playlist: PlaylistEntity?) {
        _selectedPlaylist.value = playlist
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setPlatformFilter(platform: MusicPlatform?) {
        _selectedPlatformFilter.value = platform
    }

    fun setOnlyOfflineFilter(onlyOffline: Boolean) {
        _onlyOfflineFilter.value = onlyOffline
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        showNotification("Theme set to ${mode.name.replace("_", " ")}")
    }

    fun setAccentTheme(accent: AccentTheme) {
        _accentTheme.value = accent
        showNotification("Accent color updated to ${accent.title}")
    }

    fun setHapticsEnabled(enabled: Boolean) {
        _hapticsEnabled.value = enabled
    }

    fun playTrack(track: TrackEntity, queue: List<TrackEntity> = emptyList()) {
        val tracksList = if (queue.isNotEmpty()) queue else filteredTracks.value
        playerEngine.playTrack(track, tracksList)
    }

    fun togglePlayPause() = playerEngine.togglePlayPause()
    fun nextTrack() = playerEngine.next()
    fun previousTrack() = playerEngine.previous()
    fun seekTo(seconds: Int) = playerEngine.seekTo(seconds)
    fun toggleShuffle() = playerEngine.toggleShuffle()
    fun toggleRepeat() = playerEngine.toggleRepeat()
    fun setPlaybackSpeed(speed: Float) = playerEngine.setPlaybackSpeed(speed)
    fun setAudioQuality(quality: String) = playerEngine.setAudioQuality(quality)

    fun toggleOfflineDownload(track: TrackEntity) {
        viewModelScope.launch {
            repository.toggleOfflineDownload(track)
            val msg = if (!track.isOfflineDownloaded) "Downloaded '${track.title}' for offline playback" else "Removed '${track.title}' from offline downloads"
            showNotification(msg)
        }
    }

    fun triggerCloudSync() {
        viewModelScope.launch {
            repository.triggerCloudSync()
            showNotification("Cloud & multi-device synchronization completed!")
        }
    }

    fun refreshLinkedAccountsData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val result = repository.fetchAndSyncLinkedAccounts()
                showNotification(result.message, isError = !result.anyAccountLinked && result.tracksCount == 0)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun updateAccountConnection(platform: MusicPlatform, email: String, isConnected: Boolean) {
        repository.updateAccountConnection(platform, email, isConnected)
        val status = if (isConnected) "Connected to" else "Disconnected from"
        showNotification("$status ${platform.displayName} ($email)")
        if (isConnected) {
            refreshLinkedAccountsData()
        }
    }

    fun migratePlaylist(playlist: PlaylistEntity, targetPlatform: MusicPlatform) {
        viewModelScope.launch {
            val task = repository.runPlaylistMigration(playlist, targetPlatform)
            showNotification("Migrated '${playlist.name}' to ${targetPlatform.displayName}! Matched ${task.matchedTracks}/${task.totalTracks} tracks.")
        }
    }

    fun exportPlaylistMetadata(playlist: PlaylistEntity, format: String): String {
        val tracks = filteredTracks.value
        val metadata = repository.exportPlaylistMetadata(playlist, tracks, format)
        showNotification("Exported playlist '${playlist.name}' in $format format")
        return metadata
    }

    fun createSocialRoom(title: String, platform: MusicPlatform, tags: String) {
        viewModelScope.launch {
            val room = repository.createSocialRoom(title, platform, tags)
            showNotification("Live Jam Room '${room.title}' created (Code: ${room.roomCode})")
        }
    }

    fun createPlaylist(name: String, description: String, platform: MusicPlatform) {
        viewModelScope.launch {
            val pl = repository.createNewPlaylist(name, description, platform)
            showNotification("Created playlist '${pl.name}'")
        }
    }

    private var searchJob: kotlinx.coroutines.Job? = null
    private var suggestionJob: kotlinx.coroutines.Job? = null

    fun onSearchQueryChanged(query: String) {
        _searchEngineQuery.value = query
        suggestionJob?.cancel()
        searchJob?.cancel()

        if (query.isNotBlank()) {
            suggestionJob = viewModelScope.launch {
                val suggs = repository.getAutocompleteSuggestions(query)
                _searchSuggestions.value = suggs
            }
            searchJob = viewModelScope.launch {
                kotlinx.coroutines.delay(400)
                executeSearchInternal(query, _searchEngineFilter.value, _searchCategoryFilter.value)
            }
        } else {
            _searchSuggestions.value = emptyList()
            _searchEngineResults.value = emptyList()
        }
    }

    fun setSearchEngineFilter(platform: MusicPlatform?) {
        _searchEngineFilter.value = platform
        if (_searchEngineQuery.value.isNotBlank()) {
            executeSearch(_searchEngineQuery.value, platform, _searchCategoryFilter.value)
        }
    }

    fun setSearchCategoryFilter(category: SearchCategory) {
        _searchCategoryFilter.value = category
        if (_searchEngineQuery.value.isNotBlank()) {
            executeSearch(_searchEngineQuery.value, _searchEngineFilter.value, category)
        }
    }

    fun applySearchOperator(operatorPrefix: String) {
        val current = _searchEngineQuery.value.trim()
        val newQuery = if (current.isBlank()) "$operatorPrefix\"\"" else "$current $operatorPrefix\"\""
        _searchEngineQuery.value = newQuery
    }

    fun executeSearch(
        query: String = _searchEngineQuery.value,
        platform: MusicPlatform? = _searchEngineFilter.value,
        category: SearchCategory = _searchCategoryFilter.value
    ) {
        if (query.isBlank()) return
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            executeSearchInternal(query, platform, category)
        }
    }

    private suspend fun executeSearchInternal(
        query: String,
        platform: MusicPlatform?,
        category: SearchCategory
    ) {
        _isSearchLoading.value = true
        try {
            val results = repository.searchTracks(query, platform, category)
            _searchEngineResults.value = results
        } catch (e: Exception) {
            showNotification("Search error: ${e.localizedMessage}", isError = true)
        } finally {
            _isSearchLoading.value = false
        }
    }

    fun saveSearchResultToLibrary(track: SearchResultTrack) {
        viewModelScope.launch {
            repository.saveSearchResultToLibrary(track)
            showNotification("Added '${track.title}' to your unified library!")
        }
    }

    fun playSearchResult(track: SearchResultTrack) {
        val entity = track.toTrackEntity()
        val fullQueue = _searchEngineResults.value.map { it.toTrackEntity() }.ifEmpty { listOf(entity) }
        playTrack(entity, fullQueue)
        showNotification("Playing '${track.title}' (${track.platform.displayName})")
    }

    override fun onCleared() {
        super.onCleared()
        playerEngine.release()
    }

    fun removeRecentSearch(query: String) {
        repository.removeRecentSearch(query)
    }

    fun clearRecentSearches() {
        repository.clearRecentSearches()
    }

    fun togglePlaylistOfflineDownload(playlist: PlaylistEntity) {
        viewModelScope.launch {
            val currentMap = _playlistDownloadProgress.value.toMutableMap()
            val currentProgress = currentMap[playlist.id]
            val shouldDownload = currentProgress?.isFullyDownloaded != true && currentProgress?.isDownloading != true

            currentMap[playlist.id] = PlaylistDownloadProgress(
                playlistId = playlist.id,
                isDownloading = shouldDownload,
                completedTracks = 0,
                totalTracks = playlist.trackCount,
                isFullyDownloaded = !shouldDownload
            )
            _playlistDownloadProgress.value = currentMap

            if (shouldDownload) {
                showNotification("Started offline download for playlist '${playlist.name}'...")
                repository.setPlaylistOfflineDownload(playlist.id, true) { completed, total ->
                    val updated = _playlistDownloadProgress.value.toMutableMap()
                    updated[playlist.id] = PlaylistDownloadProgress(
                        playlistId = playlist.id,
                        isDownloading = completed < total,
                        completedTracks = completed,
                        totalTracks = total,
                        isFullyDownloaded = completed >= total
                    )
                    _playlistDownloadProgress.value = updated
                }
                showNotification("Playlist '${playlist.name}' downloaded for offline playback!")
            } else {
                repository.setPlaylistOfflineDownload(playlist.id, false)
                val updated = _playlistDownloadProgress.value.toMutableMap()
                updated[playlist.id] = PlaylistDownloadProgress(
                    playlistId = playlist.id,
                    isDownloading = false,
                    completedTracks = 0,
                    totalTracks = playlist.trackCount,
                    isFullyDownloaded = false
                )
                _playlistDownloadProgress.value = updated
                showNotification("Removed offline downloads for playlist '${playlist.name}'.")
            }
        }
    }

    fun downloadAllPlaylistsOffline() {
        viewModelScope.launch {
            val playlists = allPlaylists.value
            if (playlists.isEmpty()) {
                showNotification("No playlists in library to download.")
                return@launch
            }
            showNotification("Started batch offline download for ${playlists.size} playlists...")
            playlists.forEach { playlist ->
                togglePlaylistOfflineDownload(playlist)
            }
        }
    }

    fun clearAllOfflineStorageCache() {
        viewModelScope.launch {
            repository.clearAllOfflineCache()
            _playlistDownloadProgress.value = emptyMap()
            showNotification("Cleared all offline track cache and freed device storage.")
        }
    }

    fun dismissNotification() {
        _notification.value = null
    }

    private fun showNotification(msg: String, isError: Boolean = false) {
        _notification.value = UiNotification(msg, isError)
    }
}

data class PlaylistDownloadProgress(
    val playlistId: String,
    val isDownloading: Boolean = false,
    val completedTracks: Int = 0,
    val totalTracks: Int = 0,
    val isFullyDownloaded: Boolean = false
)
