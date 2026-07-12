package com.practicum.playlistmaker.search.ui.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.utils.FormatTrackDurationUseCase
import com.practicum.playlistmaker.player.domain.usecase.utils.DelayedTrackActionUseCase
import com.practicum.playlistmaker.search.domain.usecase.history.AddTrackToHistoryUseCase
import com.practicum.playlistmaker.search.domain.usecase.history.ClearSearchHistoryUseCase
import com.practicum.playlistmaker.search.domain.usecase.history.GetSearchHistoryUseCase
import com.practicum.playlistmaker.search.domain.usecase.search.FilterTracksUseCase
import com.practicum.playlistmaker.search.domain.usecase.search.SearchTracksUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch


class SearchViewModel(
    private val searchTracksUseCase: SearchTracksUseCase,
    private val addTrackToHistoryUseCase: AddTrackToHistoryUseCase,
    private val getSearchHistoryUseCase: GetSearchHistoryUseCase,
    private val clearSearchHistoryUseCase: ClearSearchHistoryUseCase,
    private val filterTracksUseCase: FilterTracksUseCase,
    private val delayedTrackActionUseCase: DelayedTrackActionUseCase,
    val formatTrackDurationUseCase: FormatTrackDurationUseCase
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
