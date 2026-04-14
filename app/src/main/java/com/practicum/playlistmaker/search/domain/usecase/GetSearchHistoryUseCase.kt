package com.practicum.playlistmaker.search.domain.usecase

import com.practicum.playlistmaker.core.contract.GetSearchHistoryUseCaseContract
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.search.domain.repository.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlin.collections.map

/**
 * UseCase для получения истории поиска.
 * Извлекает сохранённые треки из локального хранилища.
 *
 * @param historyRepository репозиторий для работы с историей поиска
 */
class GetSearchHistoryUseCase(
    private val historyRepository: HistoryRepository
) : GetSearchHistoryUseCaseContract {

    override fun invoke(): Flow<List<Track>> {
        return historyRepository.getHistory()
            .flowOn(Dispatchers.IO)
    }
}
