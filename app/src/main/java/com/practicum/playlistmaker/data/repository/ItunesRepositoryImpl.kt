package com.practicum.playlistmaker.data.repository

import com.practicum.playlistmaker.data.dto.SearchResponseDTO
import com.practicum.playlistmaker.data.mapper.toDomain
import com.practicum.playlistmaker.data.network.ItunesApi
import com.practicum.playlistmaker.domain.model.SearchResponse
import com.practicum.playlistmaker.domain.repository.ItunesRepository
import javax.inject.Inject

class ItunesRepositoryImpl @Inject constructor(
    private val api: ItunesApi
) : ItunesRepository {


    override suspend fun search(query: String): SearchResponse {
        // 1. Вызов API (Retrofit/Ktor)
        val response: SearchResponseDTO = api.searchTracks(query)

        // 2. Преобразование DTO → доменная модель
        return response.toDomain()
    }
}