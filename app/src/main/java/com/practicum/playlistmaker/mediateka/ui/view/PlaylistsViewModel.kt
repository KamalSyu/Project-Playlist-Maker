package com.practicum.playlistmaker.mediateka.ui.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.mediateka.domain.usecase.LoadPlaylistsUseCase
import com.practicum.playlistmaker.mediateka.ui.PlaylistsUiState
import kotlinx.coroutines.launch

class PlaylistsViewModel(
    private val loadPlaylistsUseCase: LoadPlaylistsUseCase
) : ViewModel() {

    private var isObserving = false

    private val _uiState = MutableLiveData<PlaylistsUiState>(PlaylistsUiState.Loading)
    val uiState: LiveData<PlaylistsUiState> = _uiState

    init {
        loadPlaylists()  // Автоматически подписываемся на обновления
    }

    fun loadPlaylists() {
        if (isObserving) return

        _uiState.value = PlaylistsUiState.Loading
        isObserving = true

        loadPlaylistsUseCase().observeForever { playlists ->
            viewModelScope.launch {
                if (playlists.isEmpty()) {
                    _uiState.value = PlaylistsUiState.Empty
                } else {
                    _uiState.value = PlaylistsUiState.Success(playlists)
                }
            }
        }
    }
}

