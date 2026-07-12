package com.practicum.playlistmaker.search.ui.view

import com.practicum.playlistmaker.core.models.Track
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