package com.practicum.playlistmaker.search.ui.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.contract.*
import com.practicum.playlistmaker.core.usecase.UseCaseCreator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для управления поиском треков и историей поиска.
 * Отвечает за:
 * - выполнение поиска по запросу;
 * - загрузку и очистку истории поиска;
 * - фильтрацию локальных треков;
 * - передачу выбранного трека в аудиоплеер;
 * - обработку ошибок поиска.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val useCaseCreator: UseCaseCreator
) : ViewModel() {

    // Use cases для бизнес‑логики
    private val searchTracksUseCase = useCaseCreator.createSearchTracksUseCase()
    private val addTrackToHistoryUseCase = useCaseCreator.createAddTrackToHistoryUseCase()
    private val getSearchHistoryUseCase = useCaseCreator.createGetSearchHistoryUseCase()
    private val clearSearchHistoryUseCase = useCaseCreator.createClearSearchHistoryUseCase()
    private val filterTracksUseCase = useCaseCreator.createFilterTracksUseCase()
    private val delayedTrackActionUseCase = useCaseCreator.createDelayedTrackActionUseCase()

    /** Use case для форматирования длительности трека */
    val formatTrackDurationUseCase: FormatTrackDurationUseCaseContract =
        useCaseCreator.createFormatTrackDurationUseCase()

    // Состояния для наблюдения из UI
    private val _searchState = MutableLiveData<SearchState>(SearchState.Idle)
    /** Состояние поиска (загрузка, результаты, ошибка) */
    val searchState: LiveData<SearchState> = _searchState

    private val _historyState = MutableLiveData<HistoryState>(HistoryState.Loading)
    /** Состояние истории поиска (загрузка, пустая, загруженная, очищенная) */
    val historyState: LiveData<HistoryState> = _historyState

    private val _trackToOpen = MutableLiveData<Track?>()
    /** Трек, который нужно открыть в аудиоплеере */
    val trackToOpen: LiveData<Track?> = _trackToOpen

    // Вспомогательные данные
    private var filteredTracks: List<Track> = emptyList()
    /** Последний поисковый запрос (публичное поле) */
    var lastSearchQuery: String? = null
    private var _isLastSearchFailed = false

    /** Флаг, указывающий, завершился ли последний поиск ошибкой */
    val isLastSearchFailed: Boolean
        get() = _isLastSearchFailed

    /**
     * Выполняет поиск треков по запросу.
     * - устанавливает состояние загрузки;
     * - вызывает Use Case для поиска;
     * - обновляет состояние с результатами или ошибкой.
     *
     * @param query поисковый запрос
     */
    fun performSearch(query: String) {
        if (query.isEmpty()) return
        lastSearchQuery = query
        _isLastSearchFailed = false  // Сбрасываем флаг при новом поиске
        viewModelScope.launch {
            _searchState.value = SearchState.Loading
            val result = searchTracksUseCase(query)
            _searchState.value = if (result.isSuccess) {
                val tracks = result.getOrNull() ?: emptyList()
                filteredTracks = tracks
                SearchState.Results(tracks)
            } else {
                _isLastSearchFailed = true  // Устанавливаем флаг при ошибке
                val exception = result.exceptionOrNull() as? Exception
                SearchState.Error(exception)
            }
        }
    }

    /**
     * Загружает историю поиска из хранилища.
     * Устанавливает состояние загрузки, затем обновляет состояние
     * с историей или пустым состоянием.
     */
    fun loadHistory() {
        viewModelScope.launch {
            _historyState.value = HistoryState.Loading
            val history = getSearchHistoryUseCase()
            _historyState.value = if (history.isEmpty()) {
                HistoryState.Empty
            } else {
                HistoryState.HistoryLoaded(history)
            }
        }
    }

    /**
     * Повторяет последний неудачный поиск.
     * Сбрасывает флаг ошибки и вызывает `performSearch` с последним запросом.
     */
    fun retryLastSearch() {
        if (_isLastSearchFailed && lastSearchQuery != null) {
            _isLastSearchFailed = false  // Сброс флага перед повторной попыткой
            performSearch(lastSearchQuery!!)
        }
    }

    /**
     * Очищает историю поиска.
     * Вызывает Use Case для очистки, обновляет состояние истории и перезагружает её.
     */
    fun clearHistory() {
        viewModelScope.launch {
            clearSearchHistoryUseCase()
            _historyState.value = HistoryState.HistoryCleared
            _isLastSearchFailed = false  // Ошибки прошлого поиска больше не актуальны
            loadHistory()
        }
    }

    /**
     * Фильтрует локальные треки по запросу и обновляет состояние поиска.
     * Используется для мгновенной фильтрации при пустом запросе.
     *
     * @param query строка для фильтрации
     */
    fun filterAndUpdateTracks(query: String) {
        filteredTracks = filterTracksUseCase(tracks = filteredTracks, query = query)
        _searchState.value = SearchState.Results(filteredTracks)
    }

    /**
     * Обрабатывает клик по треку:
     * - добавляет трек в историю с задержкой;
     * - перезагружает историю;
     * - передаёт трек в аудиоплеер через LiveData.
     *
     * @param track выбранный трек
     */
    fun onTrackClicked(track: Track) {
        viewModelScope.launch {
            delayedTrackActionUseCase(
                track = track,
                delayMillis = 500L,
                onDelayedAction = { delayedTrack ->
                    viewModelScope.launch {
                        addTrackToHistoryUseCase(delayedTrack)
                        loadHistory()
                        _trackToOpen.value = delayedTrack
                    }
                }
            )
        }
    }

    /**
     * Сбрасывает состояние трека для открытия.
     * Вызывается после обработки трека в UI.
     */
    fun resetTrackToOpen() {
        _trackToOpen.value = null
    }
}

/**
 * Состояния поиска для наблюдения из UI.
 */
sealed class SearchState {
    /** Начальное состояние, поиск не выполнялся */
    object Idle : SearchState()
    /** Состояние загрузки во время выполнения поиска */
    object Loading : SearchState()
    /** Успешный результат поиска с списком треков */
    data class Results(val tracks: List<Track>) : SearchState()
    /** Ошибка поиска с исключением */
    data class Error(val exception: Exception?) : SearchState()
}

/**
 * Состояния истории поиска для наблюдения из UI.
 */
sealed class HistoryState {
    /** Состояние загрузки истории */
    object Loading : HistoryState()
    /** История пуста */
    object Empty : HistoryState()
    /** История загружена с данными */
    data class HistoryLoaded(val history: List<Track>) : HistoryState()
    /** История была очищена */
    object HistoryCleared : HistoryState()
}
