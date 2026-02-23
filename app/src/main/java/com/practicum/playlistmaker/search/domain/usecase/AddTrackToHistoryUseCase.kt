package com.practicum.playlistmaker.search.domain.usecase

import com.practicum.playlistmaker.core.contract.AddTrackToHistoryUseCaseContract
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.search.domain.repository.HistoryRepository
import javax.inject.Inject

/**
 * UseCase для добавления трека в историю поиска.
 * Делегирует сохранение трека в репозиторий истории.
 *
 * @param historyRepository репозиторий для работы с историей поиска
 */
class AddTrackToHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) : AddTrackToHistoryUseCaseContract {

    /**
     * Добавляет трек в историю поиска.
     * Вызывает метод репозитория для сохранения трека с учётом ограничений:
     * - удаление дубликатов;
     * - ограничение размера истории.
     *
     * @param track трек, который нужно добавить в историю
     */
    override suspend operator fun invoke(track: Track) {
        historyRepository.addTrack(track)
    }
}