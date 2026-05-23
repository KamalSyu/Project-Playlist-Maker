package com.practicum.playlistmaker.mediateka.domain.usecase

import android.content.Context
import android.net.Uri
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
    ): Long {
        val coverPath = coverUri?.let { copyImageToAppStorage(it, context) }

        val newPlaylist = Playlist(
            id = "0",
            name = playlistName,
            description = playlistDescription,
            coverPath = coverPath,
            trackIds = "[]",
            trackCount = 0,
            createdAt = System.currentTimeMillis()
        )

        return playlistsRepositoryMedia.addPlaylist(newPlaylist)
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
