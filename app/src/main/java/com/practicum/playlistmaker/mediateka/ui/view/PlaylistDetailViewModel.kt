package com.practicum.playlistmaker.mediateka.ui.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.mediateka.data.db.PlaylistTrackEntity
import com.practicum.playlistmaker.mediateka.domain.usecase.DeletePlaylistUseCase
import com.practicum.playlistmaker.mediateka.domain.usecase.LoadPlaylistByIdUseCase
import com.practicum.playlistmaker.mediateka.domain.usecase.RemoveTrackFromPlaylistUseCase
import com.practicum.playlistmaker.mediateka.domain.usecase.SharePlaylistUseCase
import com.practicum.playlistmaker.mediateka.ui.PlaylistDetailUiState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PlaylistDetailViewModel(
    private val loadPlaylistByIdUseCase: LoadPlaylistByIdUseCase,
    private val removeTrackFromPlaylistUseCase: RemoveTrackFromPlaylistUseCase,
    private val sharePlaylistUseCase: SharePlaylistUseCase,
    private val deletePlaylistUseCase: DeletePlaylistUseCase
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
                // Собираем первый элемент из Flow — это и есть загрузка экрана
                val state = loadPlaylistByIdUseCase(playlistId).first()
                _uiState.value = state
            } catch (e: Exception) {
                _uiState.value = PlaylistDetailUiState.Error(e)
            }
        }
    }

    fun removeTrack(playlistId: Long, trackId: String) {
        viewModelScope.launch {
            removeTrackFromPlaylistUseCase(playlistId, trackId)
            loadPlaylist(playlistId.toString())
        }
    }

    fun sharePlaylist(playlist: Playlist, tracks: List<PlaylistTrackEntity>) {
        viewModelScope.launch {
            try {
                val text = sharePlaylistUseCase(playlist, tracks)
                _uiState.value = PlaylistDetailUiState.ShareReady(text)
            } catch (e: Exception) {
                _uiState.value = PlaylistDetailUiState.Error(e)
            }
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                deletePlaylistUseCase(playlistId)
                _uiState.value = PlaylistDetailUiState.Deleted
            } catch (e: Exception) {
                _uiState.value = PlaylistDetailUiState.Error(e)
            }
        }
    }
}
