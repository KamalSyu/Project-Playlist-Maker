package com.practicum.playlistmaker.mediateka.domain.usecase

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
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
                context.contentResolver.openInputStream(coverUri)?.use { inputStream ->
                    // Пытаемся получить имя файла из контент-провайдера
                    val fileName = context.contentResolver
                        .query(coverUri, null, null, null, null)
                        ?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                if (nameIndex != -1) cursor.getString(nameIndex) else null
                            } else {
                                null
                            }
                        } ?: "cover_${System.currentTimeMillis()}.jpg"

                    val tempFile = File(context.cacheDir, fileName)
                    tempFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }

                    playlistsRepositoryMedia.safeCopyToPrivateStorage(tempFile.absolutePath)
                        .also {
                            // Удаляем временный файл после копирования (даже если копирование неудачно, это ок)
                            tempFile.delete()
                        }
                        .also { path ->
                            Log.d("CreatePlaylistUseCase", "Обложка успешно скопирована, путь: $path")

                            // --- ДИАГНОСТИКА: проверяем, что файл реально есть и не пустой ---
                            if (!path.isNullOrEmpty()) {
                                val file = File(path)
                                Log.d("CreatePlaylistUseCase", "file.exists(): ${file.exists()}")
                                Log.d("CreatePlaylistUseCase", "file.absolutePath: ${file.absolutePath}")
                                Log.d("CreatePlaylistUseCase", "file.length(): ${file.length()}")

                                if (!file.exists()) {
                                    Log.w("CreatePlaylistUseCase", "Файл по пути не найден — возможно, проблема в safeCopyToPrivateStorage")
                                }
                                if (file.length() == 0L) {
                                    Log.w("CreatePlaylistUseCase", "Файл существует, но пустой (0 байт)")
                                }
                            } else {
                                Log.w("CreatePlaylistUseCase", "path оказался null или пустым после копирования")
                            }
                            // -----------------------------------------------------------------
                        }
                }
            } catch (e: Exception) {
                Log.e("CreatePlaylistUseCase", "Не удалось скопировать обложку из Uri: $coverUri", e)
                null
            }
        } else {
            null
        }

        // Если была обложка, но сохранить не удалось — сразу возвращаем ошибку
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
