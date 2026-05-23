package com.practicum.playlistmaker.mediateka.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface PlaylistsDao {
    @Insert
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrackToPlaylist(track: PlaylistTrackEntity)

    @Query("SELECT COUNT(*) FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun isTrackInPlaylist(playlistId: String, trackId: String): Int

    @Query("UPDATE playlists SET trackCount = trackCount + 1 WHERE id = :playlistId")
    suspend fun incrementTrackCount(playlistId: Long)

}