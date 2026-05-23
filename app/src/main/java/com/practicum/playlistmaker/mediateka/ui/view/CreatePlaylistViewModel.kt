package com.practicum.playlistmaker.mediateka.ui.view

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.mediateka.domain.usecase.CreatePlaylistUseCase
import com.practicum.playlistmaker.mediateka.ui.CreatePlaylistUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreatePlaylistViewModel(
    private val createPlaylistUseCase: CreatePlaylistUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePlaylistUiState())
    val uiState: StateFlow<CreatePlaylistUiState> = _uiState

    fun updatePlaylistName(name: String) {
        _uiState.value = _uiState.value.copy(playlistName = name)
    }

    fun updatePlaylistDescription(description: String) {
        _uiState.value = _uiState.value.copy(playlistDescription = description)
    }

    fun updateSelectedCoverUri(uri: Uri?) {
        _uiState.value = _uiState.value.copy(selectedCoverUri = uri)
    }

    fun createPlaylist(context: Context) = viewModelScope.launch {
        val currentState = _uiState.value
        val playlistName = currentState.playlistName.trim()
        val playlistDescription = currentState.playlistDescription
        val coverUri = currentState.selectedCoverUri

        // Валидация: название не должно быть пустым
        if (playlistName.isEmpty()) {
            _uiState.value = currentState.copy(createPlaylistError = context.getString(R.string.playlist_name_required))
            return@launch
        }

        try {
            createPlaylistUseCase(playlistName, playlistDescription, coverUri, context)
            _uiState.value = CreatePlaylistUiState(
                createPlaylistSuccess = context.getString(R.string.playlist_created, playlistName)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = currentState.copy(createPlaylistError = context.getString(R.string.error_creating_playlist))
        }
    }

    fun clearCreatePlaylistState() {
        _uiState.value = CreatePlaylistUiState()
    }

    // В ViewModel
    fun clearForm() {
        _uiState.value = CreatePlaylistUiState()
    }

    fun clearSuccess() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(createPlaylistSuccess = null)
    }

    fun clearError() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(createPlaylistError = null)
    }
}
