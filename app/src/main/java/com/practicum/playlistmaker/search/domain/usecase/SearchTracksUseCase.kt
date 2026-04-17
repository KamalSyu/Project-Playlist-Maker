package com.practicum.playlistmaker.search.domain.usecase

import com.practicum.playlistmaker.core.contract.SearchTracksUseCaseContract
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.search.domain.repository.ItunesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * UseCase для поиска треков через iTunes API.
 * Обрабатывает запрос к API и преобразует результат в удобный формат.
 *
 * @param itunesRepository репозиторий для взаимодействия с iTunes API
 */
class SearchTracksUseCase(
    private val itunesRepository: ItunesRepository
) : SearchTracksUseCaseContract {

    override fun invoke(query: String): Flow<Result<List<Track>>> =
        itunesRepository.search(query)
            .map { result ->
                result.map { searchResponse ->
                    searchResponse.results
                }
            }
            .flowOn(Dispatchers.IO)
}

