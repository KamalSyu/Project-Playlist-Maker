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

    // Единое состояние экрана
    private val _screenState = MutableLiveData<ScreenState>(ScreenState.Initial)
    /** Единое состояние экрана для наблюдения из UI */
    val screenState: LiveData<ScreenState> = _screenState

    // Вспомогательные данные
    private var filteredTracks: List<Track> = emptyList()
    /** Последний поисковый запрос (публичное поле) */
    var lastSearchQuery: String? = null
    private var _isLastSearchFailed = false

    /** Флаг, указывающий, завершился ли последний поиск ошибкой */
    val isLastSearchFailed: Boolean
        get() = _isLastSearchFailed

    fun performSearch(query: String) {
        if (query.isEmpty()) return
        lastSearchQuery = query
        _isLastSearchFailed = false
        viewModelScope.launch {
            _screenState.value = ScreenState.Loading
            val result = searchTracksUseCase(query)
            _screenState.value = if (result.isSuccess) {
                val tracks = result.getOrNull() ?: emptyList()
                filteredTracks = tracks
                ScreenState.Results(SearchState.Results(tracks), loadHistoryState())
            } else {
                _isLastSearchFailed = true
                val exception = result.exceptionOrNull() as? Exception
                ScreenState.Error(SearchState.Error(exception), loadHistoryState())
            }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            // Сохраняем текущее состояние поиска
            val currentSearchState = when (val current = _screenState.value) {
                is ScreenState.Results -> current.searchState
                is ScreenState.Error -> current.searchState  // ← исправление
                else -> SearchState.Idle
            }
            _screenState.value = ScreenState.Idle(currentSearchState, HistoryState.Loading)

            val history = getSearchHistoryUseCase()
            _screenState.value = when {
                history.isEmpty() -> ScreenState.Idle(currentSearchState, HistoryState.Empty)
                else -> ScreenState.Idle(currentSearchState, HistoryState.HistoryLoaded(history))
            }
        }
    }


    fun retryLastSearch() {
        if (_isLastSearchFailed && lastSearchQuery != null) {
            _isLastSearchFailed = false
            performSearch(lastSearchQuery!!)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            clearSearchHistoryUseCase()
            // Сохраняем текущее состояние поиска
            val currentSearchState = when (val current = _screenState.value) {
                is ScreenState.Results -> current.searchState
                is ScreenState.Error -> current.searchState
                else -> SearchState.Idle
            }
            _screenState.value = ScreenState.Idle(currentSearchState, HistoryState.HistoryCleared)
            _isLastSearchFailed = false
            loadHistory()
        }
    }

    fun filterAndUpdateTracks(query: String) {
        filteredTracks = filterTracksUseCase(tracks = filteredTracks, query = query)
        _screenState.value = ScreenState.Results(
            SearchState.Results(filteredTracks),
            loadHistoryState()
        )
    }

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

    fun resetTrackToOpen() {
        _trackToOpen.value = null
    }

    private fun loadHistoryState(): HistoryState {
        return when (val currentState = _screenState.value) {
            is ScreenState.Results -> currentState.historyState
            is ScreenState.Error -> currentState.historyState
            is ScreenState.Idle -> currentState.historyState
            else -> HistoryState.Loading
        }
    }

    private val _trackToOpen = MutableLiveData<Track?>()
    /** Трек, который нужно открыть в аудиоплеере */
    val trackToOpen: LiveData<Track?> = _trackToOpen
}

/**
 * Объединённое состояние экрана для наблюдения из UI.
 */
sealed class ScreenState {
    /** Начальное состояние */
    object Initial : ScreenState()

    /** Состояние загрузки */
    object Loading : ScreenState()

    /** Простое состояние без результатов поиска */
    data class Idle(
        val searchState: SearchState,
        val historyState: HistoryState
    ) : ScreenState()

    /** Успешный результат поиска с списком треков и состоянием истории */
    data class Results(
        val searchState: SearchState,
        val historyState: HistoryState
    ) : ScreenState()

    /** Ошибка поиска с исключением и состоянием истории */
    data class Error(
        val searchState: SearchState,
        val historyState: HistoryState
    ) : ScreenState()
}

sealed class SearchState {
    object Idle : SearchState()
    object Loading : SearchState()
    data class Results(val tracks: List<Track>) : SearchState()
    data class Error(val exception: Exception?) : SearchState()
}

sealed class HistoryState {
    object Loading : HistoryState()
    object Empty : HistoryState()
    data class HistoryLoaded(val history: List<Track>) : HistoryState()
    object HistoryCleared : HistoryState()
}
