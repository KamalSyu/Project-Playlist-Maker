package com.practicum.playlistmaker.player.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FavoriteTracksDao {

    @Insert
    suspend fun addToFavorites(track: FavoriteTrackEntity)

    @Delete
    suspend fun removeFromFavorites(track: FavoriteTrackEntity)

    @Query("SELECT * FROM favorite_tracks ORDER BY addedDate DESC")
    fun getAllFavoriteTracks(): kotlinx.coroutines.flow.Flow<List<FavoriteTrackEntity>>

    @Query("SELECT trackId FROM favorite_tracks")
    suspend fun getFavoriteTrackIds(): List<String>
}