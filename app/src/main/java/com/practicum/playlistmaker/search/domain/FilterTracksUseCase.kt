package com.practicum.playlistmaker.search.domain

import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.contract.FilterTracksUseCaseContract
import javax.inject.Inject

/**
 * UseCase для фильтрации списка треков по поисковому запросу.
 * Выполняет поиск по названию трека и имени исполнителя (без учёта регистра).
 */
class FilterTracksUseCase @Inject constructor() : FilterTracksUseCaseContract {

    /**
     * Фильтрует список треков по поисковому запросу.
     * Поиск выполняется по полям:
     * - trackName (название трека);
     * - artistName (имя исполнителя).
     * Сравнение производится без учёта регистра.
     *
     * @param tracks список треков для фильтрации
     * @param query поисковый запрос
     * @return отфильтрованный список треков, соответствующих запросу;
     *         пустой список, если запрос пуст или совпадений нет
     */
    override operator fun invoke(tracks: List<Track>, query: String): List<Track> {
        if (query.isEmpty()) return emptyList()
        val lowerQuery = query.lowercase()
        return tracks.filter { track ->
            track.trackName.lowercase().contains(lowerQuery) ||
                    track.artistName.lowercase().contains(lowerQuery)
        }
    }
}
