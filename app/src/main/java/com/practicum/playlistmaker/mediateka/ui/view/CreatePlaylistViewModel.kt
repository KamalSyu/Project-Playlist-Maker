package com.practicum.playlistmaker.mediateka.ui.view

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.mediateka.ui.CreatePlaylistUiState
import com.practicum.playlistmaker.player.data.storage.FileStorageService
import com.practicum.playlistmaker.player.domain.repository.PlaylistRepository
import kotlinx.coroutines.launch

class CreatePlaylistViewModel(
    private val playlistRepository: PlaylistRepository
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
        val description = currentState.playlistDescription
        val coverUri = currentState.selectedCoverUri

        // Валидация: название не должно быть пустым
        if (name.isEmpty()) {
            _uiState.value = currentState.copy(error = context.getString(R.string.playlist_name_required))
            return@launch
        }

        _uiState.value = currentState.copy(isLoading = true, error = null)

        try {
            // Копируем обложку в приватное хранилище
            val coverPath = coverUri?.let { uri ->
                FileStorageService(context).copyToPrivateStorage(uri.toString())
            }

            // Создаём плейлист
            val playlistId = playlistRepository.createPlaylist(name, coverPath)
            _uiState.value = CreatePlaylistUiState(
                isCreated = true,
                playlistId = playlistId,
                successMessage = context.getString(R.string.playlist_created, name)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = currentState.copy(
                isLoading = false,  // Обязательно сбрасываем isLoading
                error = context.getString(R.string.error_creating_playlist)
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