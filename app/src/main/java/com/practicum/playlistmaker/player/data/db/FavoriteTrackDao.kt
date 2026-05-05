package com.practicum.playlistmaker.player.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteTrackDao {

    @Query("SELECT COUNT(*) FROM favorite_tracks WHERE trackId = :trackId")
    suspend fun isTrackFavorite(trackId: String): Int

    @Query("SELECT * FROM favorite_tracks ORDER BY addedAt DESC")
    fun getAllFavoriteTracks(): Flow<List<FavoriteTrackEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(track: FavoriteTrackEntity)

    @Delete
    suspend fun delete(track: FavoriteTrackEntity)

    @Query("SELECT trackId FROM favorite_tracks")
    suspend fun getFavoriteTrackIds(): List<String>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_tracks WHERE trackId = :trackId)")
    suspend fun isFavorite(trackId: String): Boolean

    @Query("DELETE FROM favorite_tracks WHERE trackId = :trackId")
    suspend fun deleteById(trackId: String)

}