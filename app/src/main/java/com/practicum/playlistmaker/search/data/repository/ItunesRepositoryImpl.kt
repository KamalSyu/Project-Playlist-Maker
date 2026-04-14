package com.practicum.playlistmaker.search.data.repository

import com.practicum.playlistmaker.search.domain.model.SearchResponse
import com.practicum.playlistmaker.search.domain.repository.ItunesRepository
import com.practicum.playlistmaker.search.data.dto.SearchResponseDTO
import com.practicum.playlistmaker.search.data.mapper.SearchResponseMapper
import com.practicum.playlistmaker.search.data.network.ItunesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

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
     * Выполняет поиск треков по запросу через iTunes API и возвращает поток данных.
     * Поток излучает один элемент: результат запроса (успех или ошибка).
     *
     * @param query поисковый запрос
     * @return Flow<Result<SearchResponse>> — поток с результатом
     */
    override fun search(query: String): Flow<Result<SearchResponse>> = flow {
        try {
            // 1. Выполняем сетевой запрос
            val responseDto: SearchResponseDTO = api.searchTracks(query)

            // 2. Преобразуем DTO в доменную модель
            val domainResponse: SearchResponse = searchResponseMapper.toDomain(responseDto)

            // 3. Отправляем успешный результат в поток
            emit(Result.success(domainResponse))

        } catch (e: Exception) {
            // 4. В случае ошибки — отправляем ошибку в поток
            emit(Result.failure(e))
        }
    }
        .flowOn(Dispatchers.IO) // Выполняем в фоновом потоке
}

