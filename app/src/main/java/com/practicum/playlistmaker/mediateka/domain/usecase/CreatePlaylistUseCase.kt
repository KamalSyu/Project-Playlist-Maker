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

        Log.d("CreatePlaylistUseCase", "Начинаем обработку обложки, coverUri: $coverUri")

        val coverPathResult = if (coverUri != null) {
            try {
                // Отдаём Uri напрямую в репозиторий — он сам скопирует в надёжное место (filesDir)
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
                Log.e("CreatePlaylistUseCase", "Ошибка при обработке обложки", e)
                null
            }
        } else {
            null
        }

        // Если была обложка, но не удалось сохранить — возвращаем ошибку
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
            Log.d("CreatePlaylistUseCase", "Создаём плейлист: $newPlaylist")

            val resultId = playlistsRepositoryMedia.addPlaylist(newPlaylist)
            Log.d("CreatePlaylistUseCase", "Плейлист создан, ID: $resultId")

            Result.success(resultId.toString())
        } catch (e: Exception) {
            Log.e("CreatePlaylistUseCase", "Ошибка при добавлении плейлиста в репозиторий", e)
            Result.failure(e)
        }
    }
}
