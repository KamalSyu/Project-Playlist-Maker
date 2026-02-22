package com.practicum.playlistmaker.search.ui

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

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val useCaseCreator: UseCaseCreator
) : ViewModel() {

    private val searchTracksUseCase = useCaseCreator.createSearchTracksUseCase()
    private val addTrackToHistoryUseCase = useCaseCreator.createAddTrackToHistoryUseCase()
    private val getSearchHistoryUseCase = useCaseCreator.createGetSearchHistoryUseCase()
    private val clearSearchHistoryUseCase = useCaseCreator.createClearSearchHistoryUseCase()
    private val filterTracksUseCase = useCaseCreator.createFilterTracksUseCase()
    private val delayedTrackActionUseCase = useCaseCreator.createDelayedTrackActionUseCase()

    val formatTrackDurationUseCase: FormatTrackDurationUseCaseContract =
        useCaseCreator.createFormatTrackDurationUseCase()

    private val _searchState = MutableLiveData<SearchState>(SearchState.Idle)
    val searchState: LiveData<SearchState> = _searchState
    private val _historyState = MutableLiveData<HistoryState>(HistoryState.Loading)
    val historyState: LiveData<HistoryState> = _historyState

    private val _trackToOpen = MutableLiveData<Track?>()
    val trackToOpen: LiveData<Track?> = _trackToOpen

    private var filteredTracks: List<Track> = emptyList()
    var lastSearchQuery: String? = null  // Теперь поле публичное
    private var _isLastSearchFailed = false

    val isLastSearchFailed: Boolean
        get() = _isLastSearchFailed


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

    fun retryLastSearch() {
        if (_isLastSearchFailed && lastSearchQuery != null) {
            _isLastSearchFailed = false  // Сброс флага перед повторной попыткой
            performSearch(lastSearchQuery!!)
        }
    }


    fun clearHistory() {
        viewModelScope.launch {
            clearSearchHistoryUseCase()
            _historyState.value = HistoryState.HistoryCleared
            _isLastSearchFailed = false  // Ошибки прошлого поиска больше не актуальны
            loadHistory()
        }
    }


    fun filterAndUpdateTracks(query: String) {
        filteredTracks = filterTracksUseCase(tracks = filteredTracks, query = query)
        _searchState.value = SearchState.Results(filteredTracks)
        // Добавляем вызов для обновления видимости noResults
        _searchState.value = SearchState.Results(filteredTracks)
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
}
