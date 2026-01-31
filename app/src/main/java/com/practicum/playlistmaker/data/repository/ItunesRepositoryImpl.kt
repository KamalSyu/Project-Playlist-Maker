package com.practicum.playlistmaker.data.repository

import com.practicum.playlistmaker.data.dto.SearchResponseDTO
import com.practicum.playlistmaker.data.mapper.DtoMapper
import com.practicum.playlistmaker.data.network.ItunesApi
import com.practicum.playlistmaker.domain.model.SearchResponse
import com.practicum.playlistmaker.domain.repository.ItunesRepository
import javax.inject.Inject

class ItunesRepositoryImpl @Inject constructor(
    private val api: ItunesApi,
    private val dtoMapper: DtoMapper  // Внедряем маппер
) : ItunesRepository {

    override suspend fun search(query: String): SearchResponse {
        val response: SearchResponseDTO = api.searchTracks(query)
        return dtoMapper.toDomain(response)  // Вызов через dtoMapper!
    }
}
