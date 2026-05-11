package com.practicum.playlistmaker.search.domain.usecase.history

import com.practicum.playlistmaker.core.contract.GetSearchHistoryUseCaseContract
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.search.domain.repository.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class GetSearchHistoryUseCaseImpl(
    private val historyRepository: HistoryRepository
) : GetSearchHistoryUseCase {

    override fun invoke(): Flow<List<Track>> {
        return historyRepository.getHistory()
            .flowOn(Dispatchers.IO)
    }
}