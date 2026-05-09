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
        data class Error(val message: String) : State()
    }

    private val _state = MutableLiveData<State>(State.Empty)
    val state: LiveData<State> = _state

    init {
        loadFavoriteTracks()
    }

    private fun loadFavoriteTracks() = viewModelScope.launch {
        try {
            getFavoriteTracksUseCase().collect { tracks ->
                _state.postValue(
                    if (tracks.isEmpty()) State.Empty
                    else State.WithTracks(tracks.sortedByDescending { it.addedDate })
                )
            }
        } catch (e: Exception) {
            _state.postValue(State.Error("Не удалось загрузить избранные треки"))
        }
    }

}

