package com.practicum.playlistmaker.mediateka.ui.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.core.contract.GetFavoriteTracksUseCaseContract
import com.practicum.playlistmaker.core.models.Track
import kotlinx.coroutines.launch

class FavoriteTracksViewModel(
    private val getFavoriteTracksUseCase: GetFavoriteTracksUseCaseContract
) : ViewModel() {

    sealed class State {
        object Empty : State()
        data class WithTracks(val tracks: List<Track>) : State()
    }

    private val _state = MutableLiveData<State>(State.Empty)
    val state: LiveData<State> = _state

    init {
        loadFavoriteTracks()
    }

    private fun loadFavoriteTracks() = viewModelScope.launch {
        getFavoriteTracksUseCase().collect { tracks ->
            _state.postValue(
                if (tracks.isEmpty()) State.Empty
                else State.WithTracks(tracks.sortedByDescending { it.trackId })
            )
        }
    }
}

