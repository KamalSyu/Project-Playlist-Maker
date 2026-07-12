package com.practicum.playlistmaker.mediateka.domain.usecase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.mediateka.domain.repository.PlaylistsRepositoryMedia
import java.io.File

class CreatePlaylistUseCase(
    private val playlistsRepositoryMedia: PlaylistsRepositoryMedia
) {
    suspend operator fun invoke(
        playlistName: String,
        playlistDescription: String = "",
        coverUri: Uri?,
        context: Context
    ): Result<String> {
        val trimmedName = playlistName.trim()
        if (trimmedName.isBlank()) {
            Log.e("CreatePlaylistUseCase", "Название плейлиста пустое")
            return Result.failure(IllegalArgumentException("Название плейлиста не может быть пустым"))
        }
        val coverPathResult = if (coverUri != null) {
            try {
                playlistsRepositoryMedia.safeCopyToPrivateStorageFromUri(coverUri)
                    .also { path ->
                        if (!path.isNullOrEmpty()) {
                            val file = File(path)
                            Log.d("CreatePlaylistUseCase", "Путь сохранён: $path")
                            Log.d("CreatePlaylistUseCase", "file.exists(): ${file.exists()}")
                            Log.d("CreatePlaylistUseCase", "file.length(): ${file.length()}")
                        } else {
                            Log.w("CreatePlaylistUseCase", "Не удалось получить путь к обложке")
                        }
                    }
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
        if (coverUri != null && coverPathResult == null) {
            return Result.failure(
                IllegalStateException("Не удалось скопировать обложку в приватное хранилище")
            )
        }
        val coverPath = coverPathResult
        return try {
            val newPlaylist = Playlist(
                id = "0",
                name = trimmedName,
                description = playlistDescription,
                coverPath = coverPath,
                trackCount = 0,
                createdAt = System.currentTimeMillis()
            )
            val resultId = playlistsRepositoryMedia.addPlaylist(newPlaylist)
            Result.success(resultId.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
