package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.domain.repository.ItunesRepository
import javax.inject.Inject

class SearchTracksUseCase @Inject constructor (
    private val itunesRepository: ItunesRepository
) : SearchTracksUseCaseContract  {

    override suspend operator fun invoke(query: String): Result<List<Track>> {
        return try {
            val response = itunesRepository.search(query)
            if (response.results.isEmpty()) {
                Result.success(emptyList()) // Явно возвращаем пустой список
            } else {
                Result.success(response.results)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}