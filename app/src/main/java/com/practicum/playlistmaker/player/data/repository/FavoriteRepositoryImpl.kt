package com.practicum.playlistmaker.player.data.repository

import android.util.Log
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.player.data.db.FavoriteTrackDao
import com.practicum.playlistmaker.player.data.mapper.TrackDbMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteRepositoryImpl(
    private val dao: FavoriteTrackDao,
    private val mapper: TrackDbMapper
) : FavoriteRepository {

    override suspend fun isTrackFavorite(trackId: String): Boolean {
        return dao.isTrackFavorite(trackId) > 0
    }

    override suspend fun addToFavorites(track: Track) {
        try {
            dao.insert(mapper.toEntity(track))
        } catch (e: Exception) {
            throw RuntimeException("Ошибка добавления в избранное: ${e.message}", e)
        }
    }

    override suspend fun removeFromFavorites(trackId: String) {
        try {
            dao.deleteById(trackId)
        } catch (e: Exception) {
            throw RuntimeException("Ошибка удаления из избранного: ${e.message}", e)
        }
    }

    override fun getFavoriteTracks(): Flow<List<Track>> {
        return dao.getAllFavoriteTracks()
            .map { entities ->
                entities.map { mapper.toDomain(it) }
            }
    }

}
