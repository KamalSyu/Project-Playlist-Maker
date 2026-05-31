package com.practicum.playlistmaker.player.data.repository

import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.player.data.db.FavoriteTrackEntity
import com.practicum.playlistmaker.player.data.db.FavoriteTracksDao
import com.practicum.playlistmaker.creator.domain.TrackFactory
import com.practicum.playlistmaker.player.domain.repository.FavoriteTracksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteTracksRepositoryImpl(
    private val dao: FavoriteTracksDao,
    private val trackFactory: TrackFactory
) : FavoriteTracksRepository {
    override suspend fun addTrackToFavorites(track: Track) {
        val entity = FavoriteTrackEntity(
            trackId = track.trackId,
            trackName = track.trackName,
            artistName = track.artistName,
            artworkUrl100 = track.artworkUrl100,
            releaseDate = track.releaseDate,
            collectionName = track.collectionName,
            primaryGenreName = track.primaryGenreName,
            country = track.country,
            trackTimeMillis = track.trackTimeMillis,
            previewUrl = track.previewUrl,
            addedDate = track.addedDate
        )
        dao.addToFavorites(entity)
    }
    override suspend fun removeTrackFromFavorites(trackId: String) {
        dao.removeFromFavorites(FavoriteTrackEntity(
            trackId = trackId,
            trackName = "",
            artistName = "",
            artworkUrl100 = null,
            releaseDate = null,
            collectionName = null,
            primaryGenreName = null,
            country = null,
            trackTimeMillis = null,
            previewUrl = null,
            addedDate = System.currentTimeMillis()
        ))
    }
    override fun getFavoriteTracks(): Flow<List<Track>> {
        return dao.getAllFavoriteTracks().map { entities ->
            entities.map { entity ->
                trackFactory.createTrack(
                    trackName = entity.trackName,
                    artistName = entity.artistName,
                    trackTimeMillis = entity.trackTimeMillis,
                    artworkUrl100 = entity.artworkUrl100,
                    releaseDate = entity.releaseDate,
                    collectionName = entity.collectionName,
                    primaryGenreName = entity.primaryGenreName,
                    country = entity.country,
                    previewUrl = entity.previewUrl,
                    addedDate = entity.addedDate
                )
            }
        }
    }
    override suspend fun isTrackFavorite(trackId: String): Boolean {
        return dao.getFavoriteTrackIds().contains(trackId)
    }
}
