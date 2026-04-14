package com.practicum.playlistmaker.search.domain.repository

import com.practicum.playlistmaker.search.domain.model.SearchResponse
import kotlinx.coroutines.flow.Flow

interface ItunesRepository {
    fun search(query: String): Flow<Result<SearchResponse>>
}