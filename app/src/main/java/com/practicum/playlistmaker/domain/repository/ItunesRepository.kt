package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.model.SearchResponse

interface ItunesRepository {
    suspend fun search(query: String): SearchResponse
}
