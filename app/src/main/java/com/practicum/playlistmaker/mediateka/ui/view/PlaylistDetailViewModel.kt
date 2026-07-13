package com.practicum.playlistmaker.mediateka.ui.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.mediateka.domain.usecase.LoadPlaylistByIdUseCase
import com.practicum.playlistmaker.mediateka.domain.usecase.RemoveTrackFromPlaylistUseCase
import com.practicum.playlistmaker.mediateka.ui.PlaylistDetailUiState
import kotlinx.coroutines.launch
class PlaylistDetailViewModel(
    private val loadPlaylistByIdUseCase: LoadPlaylistByIdUseCase,
    private val removeTrackFromPlaylistUseCase: RemoveTrackFromPlaylistUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<PlaylistDetailUiState>()
    val uiState: LiveData<PlaylistDetailUiState> = _uiState

    private var currentPlaylistId: Long? = null

    fun loadPlaylist(playlistIdString: String) {
        _uiState.value = PlaylistDetailUiState.Loading

        val playlistId = playlistIdString.toLongOrNull()
        if (playlistId == null) {
            _uiState.value = PlaylistDetailUiState.Error(IllegalArgumentException("Некорректный ID плейлиста"))
            return
        }

        currentPlaylistId = playlistId

        viewModelScope.launch {
            try {
                // LoadPlaylistByIdUseCase возвращает Flow<PlaylistDetailUiState>.
                // Нам нужно собрать первый элемент, потому что внутри уже есть Loading/Success/Error.
                loadPlaylistByIdUseCase(playlistId).collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = PlaylistDetailUiState.Error(e)
            }
        }
    }

    fun removeTrack(playlistId: Long, trackId: String) {
        viewModelScope.launch {
            removeTrackFromPlaylistUseCase(playlistId, trackId)
            currentPlaylistId?.let { id ->
                loadPlaylist(id.toString())
            } ?: run {
                _uiState.value = PlaylistDetailUiState.Error(IllegalStateException("ID плейлиста потерян"))
            }
        }
    }
}
