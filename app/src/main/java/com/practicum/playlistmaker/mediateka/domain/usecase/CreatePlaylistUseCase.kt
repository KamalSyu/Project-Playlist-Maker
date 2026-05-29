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
            return Result.failure(IllegalArgumentException("Название плейлиста не может быть пустым"))
        }
        val coverPathResult = coverUri?.let { uri ->
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
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
                    playlistsRepositoryMedia.safeCopyToPrivateStorage(tempFile.absolutePath).also {
                        tempFile.delete()
                    }
                }
            } catch (e: Exception) {
                Log.e("CreatePlaylistUseCase", "Не удалось скопировать обложку из Uri: $uri", e)
                null
            }
        }
        val coverPath = coverPathResult
        return if (coverUri != null && coverPathResult == null) {
            Result.failure(IllegalStateException("Не удалось скопировать обложку в приватное хранилище"))
        } else try {
            val newPlaylist = Playlist(
                id = "",
                name = trimmedName,
                description = playlistDescription,
                coverPath = coverPath,
                trackCount = 0,
                createdAt = System.currentTimeMillis()
            )
            val playlistId = playlistsRepositoryMedia.addPlaylist(newPlaylist)
            Result.success(playlistId.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
