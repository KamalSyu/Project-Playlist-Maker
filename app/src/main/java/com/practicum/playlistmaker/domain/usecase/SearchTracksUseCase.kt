package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.domain.repository.ItunesRepository
import javax.inject.Inject

class SearchTracksUseCase @Inject constructor (private val itunesRepository: ItunesRepository) {

    suspend operator fun invoke(query: String): Result<List<Track>> {
        return try {
            val response = itunesRepository.search(query)
            Result.success(response.results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}