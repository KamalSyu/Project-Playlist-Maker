package com.practicum.playlistmaker.search.domain.usecase

import com.practicum.playlistmaker.core.contract.GetSearchHistoryUseCaseContract
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.search.domain.repository.HistoryRepository

/**
 * UseCase для получения истории поиска.
 * Извлекает сохранённые треки из локального хранилища.
 *
 * @param historyRepository репозиторий для работы с историей поиска
 */
class GetSearchHistoryUseCase (
    private val historyRepository: HistoryRepository
) : GetSearchHistoryUseCaseContract {

    /**
     * Получает историю поиска из локального хранилища.
     * Возвращает список треков в порядке добавления (последний — первый).
     *
     * @return список треков из истории поиска;
     *         пустой список, если история пуста или произошла ошибка
     */
    override suspend operator fun invoke(): List<Track> {
        return historyRepository.getHistory()
    }
}