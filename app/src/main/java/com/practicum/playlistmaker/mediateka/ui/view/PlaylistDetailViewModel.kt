package com.practicum.playlistmaker.mediateka.ui.view

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.mediateka.data.db.PlaylistTrackEntity
import com.practicum.playlistmaker.mediateka.domain.usecase.DeletePlaylistUseCase
import com.practicum.playlistmaker.mediateka.domain.usecase.LoadPlaylistByIdUseCase
import com.practicum.playlistmaker.mediateka.domain.usecase.RemoveTrackFromPlaylistUseCase
import com.practicum.playlistmaker.mediateka.ui.PlaylistDetailUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class PlaylistDetailViewModel(
    private val loadPlaylistByIdUseCase: LoadPlaylistByIdUseCase,
    private val removeTrackFromPlaylistUseCase: RemoveTrackFromPlaylistUseCase,
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
            Log.d("PlaylistDebug", "ViewModel: запрашиваем плейлист с ID=$playlistId")

            loadPlaylistByIdUseCase(playlistId)
                .flowOn(Dispatchers.IO)
                .collect { state ->
                    Log.d("PlaylistDebug", "ViewModel: получили состояние: ${state::class.simpleName}")
                    _uiState.value = state
                }
        }
    }


    fun removeTrack(playlistId: Long, trackId: String) {
        viewModelScope.launch {
            try {
                removeTrackFromPlaylistUseCase(playlistId, trackId)
                loadPlaylist(playlistId.toString())
            } catch (e: Exception) {
                _uiState.value = PlaylistDetailUiState.Error(e)
            }
        }
    }
    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                deletePlaylistUseCase.invoke(playlistId)
                _uiState.value = PlaylistDetailUiState.Deleted
            } catch (e: Exception) {
                _uiState.value = PlaylistDetailUiState.Error(e)
            }
        }
    }


}
