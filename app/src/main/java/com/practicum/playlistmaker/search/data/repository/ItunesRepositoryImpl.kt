package com.practicum.playlistmaker.search.data.repository

import com.practicum.playlistmaker.search.domain.model.SearchResponse
import com.practicum.playlistmaker.search.domain.repository.ItunesRepository
import com.practicum.playlistmaker.search.data.dto.SearchResponseDTO
import com.practicum.playlistmaker.search.data.mapper.SearchResponseMapper
import com.practicum.playlistmaker.search.data.network.ItunesApi
import javax.inject.Inject

class ItunesRepositoryImpl @Inject constructor(
    private val api: ItunesApi,
    private val searchResponseMapper: SearchResponseMapper
) : ItunesRepository {

    override suspend fun search(query: String): SearchResponse {
        val response: SearchResponseDTO = api.searchTracks(query)
        return searchResponseMapper.toDomain(response)
    }
}