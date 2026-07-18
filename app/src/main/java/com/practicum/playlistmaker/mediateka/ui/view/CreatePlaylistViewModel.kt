package com.practicum.playlistmaker.mediateka.ui.view

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.mediateka.domain.interactor.PlaylistInteractor
import com.practicum.playlistmaker.mediateka.ui.CreatePlaylistUiState
import kotlinx.coroutines.launch

open class CreatePlaylistViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val playlistInteractor: PlaylistInteractor,
) : ViewModel() {

    var editPlaylistId: Long = -1L

    private val _uiState = MutableLiveData<CreatePlaylistUiState>(CreatePlaylistUiState())
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

    fun setEditMode(playlistId: Long) {
        editPlaylistId = playlistId
        _uiState.value = _uiState.value?.copy(
            successMessage = null,
            error = null,
            isLoading = true
        )

        viewModelScope.launch {
            val playlist = playlistInteractor.getPlaylistById(playlistId)
            if (playlist != null) {
                _uiState.value = CreatePlaylistUiState( isLoading = false,
                    error = null, isCreated = false,
                    playlistId = playlist.id.toString(),
                    playlistName = playlist.name,
                    playlistDescription = playlist.description.orEmpty(),
                    coverFilePath = playlist.coverPath,
                    selectedCoverUri = playlist.coverPath?.let { Uri.parse(it) },
                    successMessage = null
                )
            } else {
                _uiState.value = _uiState.value?.copy(
                    error = "Плейлист не найден",
                    isLoading = false
                )
            }
        }
    }
    fun createPlaylist() = viewModelScope.launch {
        val currentState = _uiState.value ?: return@launch
        val name = currentState.playlistName.trim()

        if (name.isEmpty()) {
            _uiState.value = currentState.copy(error = "Название плейлиста обязательно")
            return@launch
        }

        _uiState.value = currentState.copy(isLoading = true, error = null)

        try {
            playlistInteractor.createPlaylist(
                name = name,
                coverPath = currentState.selectedCoverUri?.toString()
            )
            _uiState.value = currentState.copy(
                isCreated = true,
                successMessage = "Плейлист создан",
                isLoading = false
            )
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = currentState.copy(
                isLoading = false,
                error = e.message ?: "Ошибка при создании плейлиста"
            )
        }
    }

    fun updatePlaylist() = viewModelScope.launch {
        val currentState = _uiState.value ?: return@launch
        val playlistId = editPlaylistId
        val name = currentState.playlistName.trim()

        if (playlistId == -1L || name.isEmpty()) {
            _uiState.value = currentState.copy(error = "Некорректные данные для обновления")
            return@launch
        }

        _uiState.value = currentState.copy(isLoading = true, error = null)

        try {
            playlistInteractor.updatePlaylistFull(
                playlistId = playlistId,
                name = name,
                description = currentState.playlistDescription,
                coverPath = currentState.selectedCoverUri?.toString()
            )
            _uiState.value = currentState.copy(
                isCreated = true,
                successMessage = "Плейлист обновлён",
                isLoading = false
            )
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = currentState.copy(
                isLoading = false,
                error = e.message ?: "Ошибка при обновлении плейлиста"
            )
        }
    }

    fun clearForm() {

        _uiState.value = CreatePlaylistUiState(
            playlistName = "",
            playlistDescription = "",
            selectedCoverUri = null
        )

        editPlaylistId = -1L
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
