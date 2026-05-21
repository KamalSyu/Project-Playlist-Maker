package com.practicum.playlistmaker.mediateka.ui.view

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.mediateka.domain.usecase.CreatePlaylistUseCase
import kotlinx.coroutines.launch
import java.io.File

class CreatePlaylistViewModel(
    private val createPlaylistUseCase: CreatePlaylistUseCase
) : ViewModel() {
    private val _success = MutableLiveData<String?>()
    val success: LiveData<String?> = _success
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun createPlaylist(
        playlistName: String,
        playlistDescription: String,
        coverUri: Uri?,
        context: Context
    ) {
        viewModelScope.launch {
            try {
                val playlistId = createPlaylistUseCase(playlistName, playlistDescription, coverUri, context)
                _success.value = context.getString(R.string.playlist_created, playlistName)
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = context.getString(R.string.error_creating_playlist)
            } finally {
                _success.value = null
                _error.value = null
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
}
