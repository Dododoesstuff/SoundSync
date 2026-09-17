package com.example.data.model

enum class MusicPlatform(val displayName: String, val brandColorHex: Long) {
    SPOTIFY("Spotify", 0xFF1DB954),
    YOUTUBE_MUSIC("YouTube Music", 0xFFFF0033),
    UNIFIED("Unified Hub", 0xFF6366F1)
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    AMOLED_BLACK,
    CYBERPUNK_NIGHT,
    HIGH_CONTRAST
}

enum class AccentTheme(val title: String, val hex: Long) {
    SPOTIFY_GREEN("Spotify Emerald", 0xFF1DB954),
    YOUTUBE_RED("YouTube Crimson", 0xFFFF0033),
    ELECTRIC_VIOLET("Electric Violet", 0xFF8B5CF6),
    CYAN_PULSE("Cyan Pulse", 0xFF06B6D4),
    SUNSET_AMBER("Sunset Amber", 0xFFF59E0B),
    ROSE_GOLD("Rose Quartz", 0xFFEC4899),
    AURORA_MINT("Aurora Mint", 0xFF10B981),
    ROYAL_SAPPHIRE("Royal Sapphire", 0xFF3B82F6),
    CORAL_BLAZE("Coral Blaze", 0xFFFF5722)
}

enum class MigrationStatus {
    IDLE,
    ANALYZING,
    MIGRATING,
    COMPLETED,
    FAILED
}

data class AccountConnection(
    val platform: MusicPlatform,
    val isConnected: Boolean,
    val userEmail: String,
    val displayName: String,
    val playlistsSynced: Int,
    val tracksSynced: Int,
    val lastSyncTimestamp: Long
)

data class SyncState(
    val isSyncing: Boolean = false,
    val progress: Float = 1.0f,
    val message: String = "All services synced",
    val lastCloudSync: Long = System.currentTimeMillis(),
    val offlineModeActive: Boolean = false
)
