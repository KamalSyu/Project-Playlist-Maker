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

    private val _favoriteTracks = MutableLiveData<List<Track>>()
    val favoriteTracks: LiveData<List<Track>> = _favoriteTracks


    private val _error = MutableLiveData<Exception?>()
    val error: LiveData<Exception?> = _error

    init {
        loadFavoriteTracks()
    }

    fun loadFavoriteTracks() {
        viewModelScope.launch {
            try {
                getFavoriteTracksUseCase.execute()
                    .collect { tracks ->
                        _favoriteTracks.value = tracks
                        _error.value = null
                    }
            } catch (e: Exception) {
                _error.value = e
                _favoriteTracks.value = emptyList()
            }
        }
    }

    fun refresh() {
        loadFavoriteTracks()
    }
}
