package com.practicum.playlistmaker.search.ui.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.core.contract.AddTrackToHistoryUseCaseContract
import com.practicum.playlistmaker.core.contract.ClearSearchHistoryUseCaseContract
import com.practicum.playlistmaker.core.contract.DelayedTrackActionUseCaseContract
import com.practicum.playlistmaker.core.contract.FilterTracksUseCaseContract
import com.practicum.playlistmaker.core.contract.FormatTrackDurationUseCaseContract
import com.practicum.playlistmaker.core.contract.GetSearchHistoryUseCaseContract
import com.practicum.playlistmaker.core.contract.SearchTracksUseCaseContract
import com.practicum.playlistmaker.core.models.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchTracksUseCase: SearchTracksUseCaseContract,
    private val addTrackToHistoryUseCase: AddTrackToHistoryUseCaseContract,
    private val getSearchHistoryUseCase: GetSearchHistoryUseCaseContract,
    private val clearSearchHistoryUseCase: ClearSearchHistoryUseCaseContract,
    private val filterTracksUseCase: FilterTracksUseCaseContract,
    private val delayedTrackActionUseCase: DelayedTrackActionUseCaseContract,
    val formatTrackDurationUseCase: FormatTrackDurationUseCaseContract
) : ViewModel() {

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

            // Извлекаем trackToOpen из текущего состояния
            val currentTrackToOpen = when (val current = _screenState.value) {
                is ScreenState.Results -> current.trackToOpen
                is ScreenState.Error -> current.trackToOpen
                is ScreenState.Idle -> current.trackToOpen
                else -> null
            }

            _screenState.value = if (result.isSuccess) {
                val tracks = result.getOrNull() ?: emptyList()
                filteredTracks = tracks
                ScreenState.Results(
                    SearchState.Results(tracks),
                    loadHistoryState(),
                    currentTrackToOpen
                )
            } else {
                _isLastSearchFailed = true
                val exception = result.exceptionOrNull() as? Exception
                ScreenState.Error(
                    SearchState.Error(exception),
                    loadHistoryState(),
                    currentTrackToOpen
                )
            }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            // Сохраняем текущее состояние поиска и trackToOpen
            val (currentSearchState, currentTrackToOpen) = when (val current = _screenState.value) {
                is ScreenState.Results -> current.searchState to current.trackToOpen
                is ScreenState.Error -> current.searchState to current.trackToOpen
                is ScreenState.Idle -> current.searchState to current.trackToOpen
                else -> SearchState.Idle to null
            }
            _screenState.value = ScreenState.Idle(currentSearchState, HistoryState.Loading, currentTrackToOpen)

            val history = getSearchHistoryUseCase()
            _screenState.value = when {
                history.isEmpty() -> ScreenState.Idle(currentSearchState, HistoryState.Empty, currentTrackToOpen)
                else -> ScreenState.Idle(currentSearchState, HistoryState.HistoryLoaded(history), currentTrackToOpen)
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
            // Сохраняем текущее состояние поиска и trackToOpen
            val (currentSearchState, currentTrackToOpen) = when (val current = _screenState.value) {
                is ScreenState.Results -> current.searchState to current.trackToOpen
                is ScreenState.Error -> current.searchState to current.trackToOpen
                else -> SearchState.Idle to null
            }
            _screenState.value = ScreenState.Idle(currentSearchState, HistoryState.HistoryCleared, currentTrackToOpen)
            _isLastSearchFailed = false
            loadHistory()
        }
    }

    fun filterAndUpdateTracks(query: String) {
        filteredTracks = filterTracksUseCase(tracks = filteredTracks, query = query)
        // Сохраняем текущий trackToOpen
        val currentTrackToOpen = when (val current = _screenState.value) {
            is ScreenState.Results -> current.trackToOpen
            is ScreenState.Error -> current.trackToOpen
            is ScreenState.Idle -> current.trackToOpen
            else -> null
        }
        _screenState.value = ScreenState.Results(
            SearchState.Results(filteredTracks),
            loadHistoryState(),
            currentTrackToOpen
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
                        _screenState.value = when (val current = _screenState.value) {
                            is ScreenState.Results -> current.copy(trackToOpen = delayedTrack)
                            is ScreenState.Error -> current.copy(trackToOpen = delayedTrack)
                            is ScreenState.Idle -> current.copy(trackToOpen = delayedTrack)
                            else -> ScreenState.Initial
                        }
                    }
                }
            )
        }
    }

    fun resetTrackToOpen() {
        _screenState.value = _screenState.value?.let { current ->
            when (current) {
                is ScreenState.Results -> current.copy(trackToOpen = null)
                is ScreenState.Error -> current.copy(trackToOpen = null)
                is ScreenState.Idle -> current.copy(trackToOpen = null)
                else -> current
            }
        } ?: _screenState.value
    }

    private fun loadHistoryState(): HistoryState {
        return when (val currentState = _screenState.value) {
            is ScreenState.Results -> currentState.historyState
            is ScreenState.Error -> currentState.historyState
            is ScreenState.Idle -> currentState.historyState
            else -> HistoryState.Loading
        }
    }
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
        val historyState: HistoryState,
        val trackToOpen: Track? = null
    ) : ScreenState()

    /** Успешный результат поиска с списком треков и состоянием истории */
    data class Results(
        val searchState: SearchState,
        val historyState: HistoryState,
        val trackToOpen: Track? = null
    ) : ScreenState()

    /** Ошибка поиска с исключением и состоянием истории */
    data class Error(
        val searchState: SearchState,
        val historyState: HistoryState,
        val trackToOpen: Track? = null
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
