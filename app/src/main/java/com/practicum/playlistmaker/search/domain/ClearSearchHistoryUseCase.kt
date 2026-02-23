package com.practicum.playlistmaker.search.domain

import com.practicum.playlistmaker.search.domain.repository.HistoryRepository
import com.practicum.playlistmaker.core.contract.ClearSearchHistoryUseCaseContract
import javax.inject.Inject

/**
 * UseCase для очистки истории поиска.
 * Удаляет все сохранённые треки из истории.
 *
 * @param historyRepository репозиторий для работы с историей поиска
 */
class ClearSearchHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) : ClearSearchHistoryUseCaseContract {

    /**
     * Очищает всю историю поиска.
     * Удаляет все записи из локального хранилища истории.
     */
    override suspend operator fun invoke() {
        historyRepository.clearHistory()
    }
}
