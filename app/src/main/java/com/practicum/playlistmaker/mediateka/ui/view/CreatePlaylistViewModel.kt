package com.practicum.playlistmaker.mediateka.ui.view

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.mediateka.domain.interactor.PlaylistInteractor
import com.practicum.playlistmaker.mediateka.domain.usecase.CreatePlaylistUseCase
import com.practicum.playlistmaker.mediateka.ui.CreatePlaylistUiState
import kotlinx.coroutines.launch

class CreatePlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {
    private val _uiState = MutableLiveData<CreatePlaylistUiState>(
        CreatePlaylistUiState()
    )
    val uiState: LiveData<CreatePlaylistUiState> = _uiState
    fun updatePlaylistName(name: String) {
        _uiState.value = _uiState.value?.copy(playlistName = name)
    }
    fun updatePlaylistDescription(description: String) {
        _uiState.value = _uiState.value?.copy(playlistDescription = description)
    }
    fun updateSelectedCoverUri(uri: Uri?) {
        _uiState.value = _uiState.value?.copy(selectedCoverUri = uri)
    }
    fun createPlaylist(context: Context) = viewModelScope.launch {
        val currentState = _uiState.value ?: return@launch
        val name = currentState.playlistName.trim()

        if (name.isEmpty()) {
            _uiState.value = currentState.copy(error = context.getString(R.string.playlist_name_required))
            return@launch
        }

        _uiState.value = currentState.copy(isLoading = true, error = null)

        try {
            playlistInteractor.createPlaylist(
                name = name,
                coverPath = currentState.selectedCoverUri?.toString()
            )

            _uiState.value = CreatePlaylistUiState(
                isCreated = true,
                successMessage = context.getString(R.string.playlist_created, name)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = currentState.copy(
                isLoading = false,
                error = e.message ?: context.getString(R.string.error_creating_playlist)
            )
        }
    }
    fun clearForm() {
        _uiState.value = CreatePlaylistUiState()
    }
    fun clearSuccess() {
        val currentState = _uiState.value ?: return
        _uiState.value = currentState.copy(successMessage = null)
    }
    fun clearError() {
        val currentState = _uiState.value ?: return
        _uiState.value = currentState.copy(error = null)
    }
}