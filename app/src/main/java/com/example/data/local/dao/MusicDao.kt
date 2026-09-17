package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.ListeningStatEntity
import com.example.data.local.entities.MigrationTaskEntity
import com.example.data.local.entities.PlaylistEntity
import com.example.data.local.entities.PlaylistTrackCrossRef
import com.example.data.local.entities.SocialRoomEntity
import com.example.data.local.entities.TrackEntity
import com.example.data.model.MusicPlatform
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {

    // TRACKS
    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun getTracksCount(): Int

    @Query("SELECT * FROM tracks ORDER BY playCount DESC, title ASC")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE isLiked = 1 ORDER BY title ASC")
    fun getLikedTracks(): Flow<List<TrackEntity>>

    @Query("SELECT COUNT(*) FROM tracks WHERE isLiked = 1")
    fun getLikedTracksCount(): Flow<Int>

    @Query("UPDATE tracks SET isLiked = :isLiked WHERE id = :trackId")
    suspend fun setTrackLiked(trackId: String, isLiked: Boolean)

    @Query("SELECT * FROM tracks WHERE isOfflineDownloaded = 1 ORDER BY title ASC")
    fun getOfflineTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE platform = :platform ORDER BY title ASC")
    fun getTracksByPlatform(platform: MusicPlatform): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :trackId LIMIT 1")
    suspend fun getTrackById(trackId: String): TrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Update
    suspend fun updateTrack(track: TrackEntity)

    @Query("UPDATE tracks SET isOfflineDownloaded = :isDownloaded, offlineFileSizeMb = :sizeMb WHERE id = :trackId")
    suspend fun setOfflineStatus(trackId: String, isDownloaded: Boolean, sizeMb: Double)

    @Query("UPDATE tracks SET playCount = playCount + 1, lastPlayedTimestamp = :timestamp WHERE id = :trackId")
    suspend fun incrementPlayCount(trackId: String, timestamp: Long)

    // PLAYLISTS
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE platform = :platform ORDER BY name ASC")
    fun getPlaylistsByPlatform(platform: MusicPlatform): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId LIMIT 1")
    suspend fun getPlaylistById(playlistId: String): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylists(playlists: List<PlaylistEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: String)

    // PLAYLIST TRACK RELATIONS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTrackCrossRefs(crossRefs: List<PlaylistTrackCrossRef>)

    @Query("""
        SELECT t.* FROM tracks t
        INNER JOIN playlist_tracks pt ON t.id = pt.trackId
        WHERE pt.playlistId = :playlistId
        ORDER BY pt.orderIndex ASC
    """)
    fun getTracksForPlaylist(playlistId: String): Flow<List<TrackEntity>>

    @Query("""
        SELECT t.* FROM tracks t
        INNER JOIN playlist_tracks pt ON t.id = pt.trackId
        WHERE pt.playlistId = :playlistId
        ORDER BY pt.orderIndex ASC
    """)
    suspend fun getTracksForPlaylistSync(playlistId: String): List<TrackEntity>

    // LISTENING STATS & HABITS
    @Query("SELECT * FROM listening_stats ORDER BY timestamp DESC LIMIT 200")
    fun getAllListeningStats(): Flow<List<ListeningStatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListeningStat(stat: ListeningStatEntity)

    @Query("SELECT COUNT(*) FROM listening_stats")
    fun getTotalListeningEvents(): Flow<Int>

    // MIGRATION TASKS
    @Query("SELECT * FROM migration_tasks ORDER BY timestamp DESC")
    fun getAllMigrationTasks(): Flow<List<MigrationTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMigrationTask(task: MigrationTaskEntity)

    // SOCIAL ROOMS
    @Query("SELECT * FROM social_rooms ORDER BY listenersCount DESC")
    fun getAllSocialRooms(): Flow<List<SocialRoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSocialRooms(rooms: List<SocialRoomEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSocialRoom(room: SocialRoomEntity)

    @Query("UPDATE social_rooms SET listenersCount = listenersCount + :delta WHERE roomCode = :code")
    suspend fun updateRoomListeners(code: String, delta: Int)
}
