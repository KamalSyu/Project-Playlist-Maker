package com.practicum.playlistmaker.mediateka.ui.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.mediateka.data.db.PlaylistTrackEntity
import com.practicum.playlistmaker.mediateka.domain.usecase.LoadPlaylistByIdUseCase
import com.practicum.playlistmaker.mediateka.ui.PlaylistDetailUiState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PlaylistDetailViewModel(
    private val loadPlaylistByIdUseCase: LoadPlaylistByIdUseCase
    // УДАЛИЛИ: removeTrackFromPlaylistUseCase, sharePlaylistUseCase, deletePlaylistUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<PlaylistDetailUiState>()
    val uiState: LiveData<PlaylistDetailUiState> = _uiState

    fun loadPlaylist(playlistIdString: String) {
        val playlistId = playlistIdString.toLongOrNull() ?: run {
            _uiState.value = PlaylistDetailUiState.Error(IllegalArgumentException("Некорректный ID плейлиста"))
            return
        }
        viewModelScope.launch {
            try {
                val state = loadPlaylistByIdUseCase(playlistId).first()
                _uiState.value = state
            } catch (e: Exception) {
                _uiState.value = PlaylistDetailUiState.Error(e)
            }
        }
    }
}
