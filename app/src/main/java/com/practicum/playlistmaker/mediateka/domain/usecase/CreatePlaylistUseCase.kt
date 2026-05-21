package com.practicum.playlistmaker.mediateka.domain.usecase

import android.content.Context
import android.net.Uri
import com.practicum.playlistmaker.mediateka.domain.model.PlaylistData
import com.practicum.playlistmaker.mediateka.domain.repository.PlaylistsRepository
import java.io.File

class CreatePlaylistUseCase(
    private val playlistsRepository: PlaylistsRepository
) {
    suspend operator fun invoke(
        playlistName: String,
        playlistDescription: String = "",
        coverUri: Uri?,
        context: Context
    ): Long {
        // Копируем изображение в приватное хранилище
        val coverPath = coverUri?.let { copyImageToAppStorage(it, context) }

        // Формируем данные плейлиста
        val newPlaylist = PlaylistData(
            name = playlistName,
            description = playlistDescription,
            coverPath = coverPath,
            trackIds = "[]", // Изначально пустой список треков
            trackCount = 0,
            createdAt = System.currentTimeMillis()
        )

        // Сохраняем в БД и возвращаем ID
        return playlistsRepository.addPlaylist(newPlaylist)
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
