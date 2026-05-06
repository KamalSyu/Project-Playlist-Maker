package com.practicum.playlistmaker.mediateka.ui.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.core.contract.GetFavoriteTracksUseCaseContract
import com.practicum.playlistmaker.core.models.Track
import kotlinx.coroutines.launch

class FragmentFavoritesViewModel(
    private val getFavoriteTracksUseCase: GetFavoriteTracksUseCaseContract
) : ViewModel() {

    private val _state = MutableLiveData<FavoritesState>()
    val state: LiveData<FavoritesState> = _state


    private val _error = MutableLiveData<Exception?>()
    val error: LiveData<Exception?> = _error

    init {
        loadFavoriteTracks()
    }

    fun loadFavoriteTracks() {
        viewModelScope.launch {
            getFavoriteTracksUseCase.execute().collect { tracks ->
                _state.value = if (tracks.isEmpty()) {
                    FavoritesState.Empty
                } else {
                    FavoritesState.Loaded(tracks)
                }
            }
        }
    }

    fun refresh() {
        loadFavoriteTracks()
    }
}
