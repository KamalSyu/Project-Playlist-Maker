package com.practicum.playlistmaker.search.domain.usecase.search

import com.practicum.playlistmaker.core.contract.SearchTracksUseCaseContract
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.search.domain.repository.ItunesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class SearchTracksUseCaseImpl(
    private val itunesRepository: ItunesRepository
) : SearchTracksUseCase {

    override fun invoke(query: String): Flow<Result<List<Track>>> =
        itunesRepository.search(query)
            .map { result ->
                result.map { searchResponse ->
                    searchResponse.results
                }
            }
            .flowOn(Dispatchers.IO)
}