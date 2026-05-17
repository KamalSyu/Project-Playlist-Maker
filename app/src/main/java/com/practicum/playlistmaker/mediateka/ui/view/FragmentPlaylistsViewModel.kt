package com.practicum.playlistmaker.mediateka.ui.view

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.mediateka.domain.model.PlaylistData
import com.practicum.playlistmaker.mediateka.domain.repository.PlaylistsRepository
import kotlinx.coroutines.launch
import java.io.File

class FragmentPlaylistsViewModel(
    private val playlistsRepository: PlaylistsRepository
) : ViewModel() {

    // Состояние загрузки
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Список плейлистов
    private val _playlists = MutableLiveData<List<PlaylistData>>()
    val playlists: LiveData<List<PlaylistData>> = _playlists

    // Ошибки
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun createNewPlaylist(
        playlistName: String,
        playlistDescription: String = "",
        coverUri: Uri? = null,
        context: Context
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val coverPath = coverUri?.let { copyImageToAppStorage(it, context) }

                if (coverUri != null && coverPath == null) {
                    _error.value = "Не удалось сохранить обложку. Проверьте доступ к файлам."
                    _isLoading.value = false
                    return@launch
                }

                val newPlaylist = PlaylistData(
                    name = playlistName,
                    description = playlistDescription,
                    coverPath = coverPath,
                    trackIds = "[]",
                    trackCount = 0,
                    createdAt = System.currentTimeMillis()
                )

                val playlistId = playlistsRepository.addPlaylist(newPlaylist)
                loadPlaylists()

            } catch (e: Exception) {
                _error.value = "Ошибка при создании плейлиста: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun copyImageToAppStorage(uri: Uri, context: Context): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val fileName = "playlist_cover_${System.currentTimeMillis()}.jpg"
                val outputFile = File(context.filesDir, fileName)
                outputFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                outputFile.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun loadPlaylists() {
            viewModelScope.launch {
                try {
                    val loadedPlaylists = playlistsRepository.getPlaylists()
                    _playlists.value = loadedPlaylists
                } catch (e: Exception) {
                    _error.value = "Ошибка загрузки плейлистов: ${e.message}"
                    e.printStackTrace()
                }
            }
        }
}
