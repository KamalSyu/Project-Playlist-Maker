package com.practicum.playlistmaker.search.domain.repository

import com.practicum.playlistmaker.search.domain.model.SearchResponse

interface ItunesRepository {
    suspend fun search(query: String): SearchResponse
}