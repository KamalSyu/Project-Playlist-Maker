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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchTracksUseCase: SearchTracksUseCaseContract,
    private val addTrackToHistoryUseCase: AddTrackToHistoryUseCaseContract,
    private val getSearchHistoryUseCase: GetSearchHistoryUseCaseContract,
    private val clearSearchHistoryUseCase: ClearSearchHistoryUseCaseContract,
    private val filterTracksUseCase: FilterTracksUseCaseContract,
    private val delayedTrackActionUseCase: DelayedTrackActionUseCaseContract,
    val formatTrackDurationUseCase: FormatTrackDurationUseCaseContract
) : ViewModel() {

    private val _screenState = MutableLiveData<ScreenState>(ScreenState.Initial)
    val screenState: LiveData<ScreenState> = _screenState

    private var filteredTracks: List<Track> = emptyList()
    var lastSearchQuery: String? = null
    private var _isLastSearchFailed = false

    val isLastSearchFailed: Boolean
        get() = _isLastSearchFailed

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            getSearchHistoryUseCase()
                .collect { history ->
                    updateHistoryOnly(history)
                }
        }
    }

    fun performSearch(query: String) {
        if (query.isEmpty()) return
        lastSearchQuery = query
        _isLastSearchFailed = false

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _screenState.value = ScreenState.Loading

            val currentTrackToOpen = getCurrentTrackToOpen()

            searchTracksUseCase(query)
                .catch { e ->
                    _isLastSearchFailed = true
                    _screenState.value = ScreenState.Error(
                        SearchState.Error(e as? Exception),
                        getHistoryState(),
                        currentTrackToOpen
                    )
                }
                .collect { result ->
                    _screenState.value = result.fold(
                        onSuccess = { tracks: List<Track> -> // ← Получаем List<Track>, а не SearchResponse
                            filteredTracks = tracks
                            ScreenState.Results(
                                SearchState.Results(tracks),
                                getHistoryState(),
                                currentTrackToOpen
                            )
                        },
                        onFailure = { error: Throwable ->
                            _isLastSearchFailed = true
                            ScreenState.Error(
                                SearchState.Error(error as? Exception),
                                getHistoryState(),
                                currentTrackToOpen
                            )
                        }
                    )
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
        }
    }

    fun filterAndUpdateTracks(query: String) {
        filteredTracks = filterTracksUseCase(tracks = filteredTracks, query = query)
        val currentTrackToOpen = getCurrentTrackToOpen()
        _screenState.value = ScreenState.Results(
            SearchState.Results(filteredTracks),
            getHistoryState(),
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
                        _screenState.value = _screenState.value?.let { current ->
                            when (current) {
                                is ScreenState.Results -> current.copy(trackToOpen = delayedTrack)
                                is ScreenState.Error -> current.copy(trackToOpen = delayedTrack)
                                is ScreenState.Idle -> current.copy(trackToOpen = delayedTrack)
                                null -> ScreenState.Initial
                                else -> current
                            }
                        } ?: ScreenState.Initial
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
        } ?: ScreenState.Initial
    }


    private fun updateHistoryOnly(history: List<Track>) {
        val newHistoryState = if (history.isEmpty()) HistoryState.Empty else HistoryState.HistoryLoaded(history)
        val current = _screenState.value

        _screenState.value = when (current) {
            is ScreenState.Results -> current.copy(historyState = newHistoryState)
            is ScreenState.Error -> current.copy(historyState = newHistoryState)
            is ScreenState.Idle -> current.copy(historyState = newHistoryState)
            ScreenState.Initial, ScreenState.Loading -> {
                ScreenState.Idle(
                    searchState = SearchState.Idle,
                    historyState = newHistoryState
                )
            }
            null -> ScreenState.Initial
        }
    }

    private fun getHistoryState(): HistoryState {
        return when (val current = _screenState.value) {
            is ScreenState.Results -> current.historyState
            is ScreenState.Error -> current.historyState
            is ScreenState.Idle -> current.historyState
            ScreenState.Initial, ScreenState.Loading -> HistoryState.Loading
            null -> HistoryState.Loading
        }
    }

    private fun getCurrentTrackToOpen(): Track? {
        return when (val current = _screenState.value) {
            is ScreenState.Results -> current.trackToOpen
            is ScreenState.Error -> current.trackToOpen
            is ScreenState.Idle -> current.trackToOpen
            else -> null
        }
    }
}

// === Состояния экрана ===

sealed class ScreenState {
    object Initial : ScreenState()
    object Loading : ScreenState()

    data class Idle(
        val searchState: SearchState,
        val historyState: HistoryState,
        val trackToOpen: Track? = null
    ) : ScreenState()

    data class Results(
        val searchState: SearchState,
        val historyState: HistoryState,
        val trackToOpen: Track? = null
    ) : ScreenState()

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
