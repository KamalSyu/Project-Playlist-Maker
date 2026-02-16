package com.practicum.playlistmaker.search.domain

import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.search.domain.repository.HistoryRepository
import com.practicum.playlistmaker.GetSearchHistoryUseCaseContract
import javax.inject.Inject

class GetSearchHistoryUseCase @Inject constructor (
    private val historyRepository: HistoryRepository
) : GetSearchHistoryUseCaseContract {

    override suspend operator fun invoke(): List<Track> {
        return historyRepository.getHistory()
    }
}