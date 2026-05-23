package com.practicum.playlistmaker.mediateka.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PlaylistsDao {
    @Insert
    suspend fun insert(playlist: PlaylistEntity): Long // Возвращает ID вставленной записи

    @Update
    suspend fun update(playlist: PlaylistEntity)

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): LiveData<List<PlaylistEntity>>

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun delete(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrackToPlaylist(track: PlaylistTrackEntity)
}