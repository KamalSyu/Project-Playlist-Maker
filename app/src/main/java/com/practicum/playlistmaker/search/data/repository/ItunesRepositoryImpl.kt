package com.practicum.playlistmaker.search.data.repository

import com.practicum.playlistmaker.player.data.db.FavoriteTrackDao
import com.practicum.playlistmaker.search.domain.model.SearchResponse
import com.practicum.playlistmaker.search.domain.repository.ItunesRepository
import com.practicum.playlistmaker.search.data.dto.SearchResponseDTO
import com.practicum.playlistmaker.search.data.mapper.SearchResponseMapper
import com.practicum.playlistmaker.search.data.network.ItunesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ItunesRepositoryImpl(
    private val api: ItunesApi,
    private val searchResponseMapper: SearchResponseMapper,
    private val favoriteTrackDao: FavoriteTrackDao  // ДОБАВИТЬ
) : ItunesRepository {

    override fun search(query: String): Flow<Result<SearchResponse>> = flow {
        try {
            val responseDto: SearchResponseDTO = api.searchTracks(query)
            val domainResponse: SearchResponse = searchResponseMapper.toDomain(responseDto)

            val favoriteIds = favoriteTrackDao.getFavoriteTrackIds()

            domainResponse.results?.forEach { track ->
                track.isFavorite = track.trackId in favoriteIds
            }

            emit(Result.success(domainResponse))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}

