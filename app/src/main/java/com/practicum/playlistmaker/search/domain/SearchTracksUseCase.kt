package com.practicum.playlistmaker.search.domain

import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.search.domain.repository.ItunesRepository
import com.practicum.playlistmaker.core.contract.SearchTracksUseCaseContract
import javax.inject.Inject

class SearchTracksUseCase @Inject constructor (
    private val itunesRepository: ItunesRepository
) : SearchTracksUseCaseContract {

    override suspend operator fun invoke(query: String): Result<List<Track>> {
        return try {
            val response = itunesRepository.search(query)
            if (response.results.isEmpty()) {
                Result.success(emptyList())
            } else {
                Result.success(response.results)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}