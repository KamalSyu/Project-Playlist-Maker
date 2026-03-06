package com.practicum.playlistmaker.search.domain.usecase

import com.practicum.playlistmaker.core.contract.SearchTracksUseCaseContract
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.search.domain.repository.ItunesRepository

/**
 * UseCase для поиска треков через iTunes API.
 * Обрабатывает запрос к API и преобразует результат в удобный формат.
 *
 * @param itunesRepository репозиторий для взаимодействия с iTunes API
 */
class SearchTracksUseCase (
    private val itunesRepository: ItunesRepository
) : SearchTracksUseCaseContract {

    /**
     * Выполняет поиск треков по запросу через iTunes API.
     * Обрабатывает возможные ошибки сети и API.
     *
     * @param query поисковый запрос
     * @return Result с:
     *   - списком найденных треков (успех);
     *   - пустой список, если результатов нет;
     *   - ошибкой, если запрос завершился неудачно
     */
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