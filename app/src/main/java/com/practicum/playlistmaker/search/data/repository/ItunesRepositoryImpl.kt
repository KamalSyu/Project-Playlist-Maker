package com.practicum.playlistmaker.search.data.repository

import com.practicum.playlistmaker.search.domain.model.SearchResponse
import com.practicum.playlistmaker.search.domain.repository.ItunesRepository
import com.practicum.playlistmaker.search.data.dto.SearchResponseDTO
import com.practicum.playlistmaker.search.data.mapper.SearchResponseMapper
import com.practicum.playlistmaker.search.data.network.ItunesApi

/**
 * Реализация репозитория для взаимодействия с iTunes API.
 * Выполняет поиск треков и преобразует ответ API в доменную модель.
 *
 * @param api клиент для запросов к iTunes API
 * @param searchResponseMapper маппер для преобразования DTO в доменную модель
 */
class ItunesRepositoryImpl(
    private val api: ItunesApi,
    private val searchResponseMapper: SearchResponseMapper
) : ItunesRepository {

    /**
     * Выполняет поиск треков по запросу через iTunes API.
     * - отправляет запрос к API;
     * - преобразует полученный DTO-объект в доменную модель SearchResponse;
     * - возвращает результат для дальнейшего использования в бизнес‑логике.
     *
     * @param query поисковый запрос (название трека, исполнителя и т. д.)
     * @return объект SearchResponse с результатами поиска
     * @throws Exception если запрос к API завершился ошибкой
     */
    override suspend fun search(query: String): SearchResponse {
        val response: SearchResponseDTO = api.searchTracks(query)
        return searchResponseMapper.toDomain(response)
    }
}
