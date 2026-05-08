package com.practicum.playlistmaker.player.data.repository

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
        return dao.isFavorite(trackId)
    }

    override suspend fun addToFavorites(track: Track) {
        dao.insert(mapper.toEntity(track))
    }

    override suspend fun removeFromFavorites(trackId: String) {
        dao.deleteById(trackId)
    }

    override fun getFavoriteTracks(): Flow<List<Track>> {
        return dao.getAllFavoriteTracks()
            .map { entities ->
                entities.map { mapper.toDomain(it) }
            }
    }
}