package com.practicum.playlistmaker.mediateka.ui.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.models.domain.AddTrackStatus
import com.practicum.playlistmaker.mediateka.domain.interactor.PlaylistInteractor
import com.practicum.playlistmaker.mediateka.domain.usecase.LoadPlaylistsUseCase
import com.practicum.playlistmaker.mediateka.ui.PlaylistsUiState
import kotlinx.coroutines.launch

class PlaylistsViewModel(
    private val loadPlaylistsUseCase: LoadPlaylistsUseCase,
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {
    private val _uiState = MutableLiveData<PlaylistsUiState>(PlaylistsUiState.Loading)
    val uiState: LiveData<PlaylistsUiState> = _uiState

    init {
        loadPlaylists()
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            try {
                loadPlaylistsUseCase()
                    .collect { playlists ->
                        val state = if (playlists.isEmpty()) {
                            PlaylistsUiState.Empty
                        } else {
                            PlaylistsUiState.Success(playlists)
                        }
                        _uiState.value = state
                    }
            } catch (e: Exception) {
                _uiState.value = PlaylistsUiState.Error(e)
            }
        }
    }

    fun addTrackToPlaylist(playlistId: String, track: Track) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is PlaylistsUiState.Success) {
                _uiState.value = currentState.copy(addTrackStatus = null)
            }
            try {
                val status = playlistInteractor.addTrackToPlaylist(playlistId, track)
                if (currentState is PlaylistsUiState.Success) {
                    _uiState.value = currentState.copy(addTrackStatus = status)
                }
            } catch (e: Exception) {
                if (currentState is PlaylistsUiState.Success) {
                    _uiState.value = currentState.copy(addTrackStatus = AddTrackStatus.ERROR)
                }
            }
        }
    }

    fun renamePlaylist(playlistId: String, newName: String) = viewModelScope.launch {
        playlistInteractor.renamePlaylist(playlistId, newName)
        loadPlaylists()
    }

    fun deletePlaylist(playlistId: String) = viewModelScope.launch {
        playlistInteractor.deletePlaylist(playlistId)
        loadPlaylists()
    }

    fun createPlaylist(name: String, coverPath: String?) = viewModelScope.launch {
        try {
            playlistInteractor.createPlaylist(name, coverPath)
            loadPlaylists()
        } catch (e: Exception) {
        }
    }
}
