package com.practicum.playlistmaker.mediateka.data.repository

import android.content.Context
import android.net.Uri
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.mediateka.data.db.PlaylistEntity
import com.practicum.playlistmaker.mediateka.data.db.PlaylistTrackEntity
import com.practicum.playlistmaker.mediateka.data.db.PlaylistsDao
import com.practicum.playlistmaker.mediateka.data.mapper.toDomain
import com.practicum.playlistmaker.mediateka.data.mapper.toEntity
import com.practicum.playlistmaker.mediateka.domain.repository.PlaylistsRepositoryMedia
import com.practicum.playlistmaker.player.data.storage.FileStorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class PlaylistsRepositoryImplMedia(
    private val dao: PlaylistsDao,
    private val context: Context
) : PlaylistsRepositoryMedia {

    private val fileStorageService = FileStorageService(context)

    override suspend fun safeCopyToPrivateStorage(sourcePath: String): String? {
        return try {
            val result = fileStorageService.copyToPrivateStorage(sourcePath)
            if (!result.isNullOrEmpty()) {
                val file = File(result)
                if (!file.exists() || file.length() == 0L) {
                    null
                } else {
                    result
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun safeCopyToPrivateStorageFromUri(uri: Uri): String? {
        return try {
            val fileName = uri.lastPathSegment ?: "cover_${System.currentTimeMillis()}"
            val destFile = File(context.filesDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            if (destFile.exists() && destFile.length() > 0) {
                destFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return dao.getAllPlaylists().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addPlaylist(playlist: Playlist): Long = withContext(Dispatchers.IO) {
        require(playlist.name.isNotBlank()) { "Название плейлиста не может быть пустым" }

        var coverPath = playlist.coverPath
        if (!coverPath.isNullOrBlank() && (coverPath.startsWith("content://") || coverPath.startsWith("media_picker"))) {
            val uri = Uri.parse(coverPath)
            coverPath = safeCopyToPrivateStorageFromUri(uri)
        }

        val entity = playlist.copy(coverPath = coverPath).toEntity()
        dao.insertPlaylist(entity)
    }

    override suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        dao.deletePlaylistById(playlistId)
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, track: Track) = withContext(Dispatchers.IO) {
        val isTrackPresent = dao.isTrackInPlaylist(playlistId, track.trackId) > 0
        if (isTrackPresent) {
            throw Exception("Трек уже добавлен в плейлист")
        }
        val playlistTrackEntity = PlaylistTrackEntity(
            id = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE,
            playlistId = playlistId,
            trackId = track.trackId,
            title = track.trackName,
            artist = track.artistName,
            duration = ((track.trackTimeMillis ?: 0L) / 1000).toInt(),
            artworkUrl100 = track.artworkUrl100,
            addedAt = System.currentTimeMillis()
        )
        dao.insertTrackToPlaylist(playlistTrackEntity)
        dao.incrementTrackCount(playlistId)
    }

    override suspend fun getPlaylistById(playlistId: Long): PlaylistEntity? {
        return dao.getPlaylistById(playlistId)
    }

    override suspend fun getTrackDurationsSeconds(playlistId: Long): List<Int> =
        dao.getTrackDurationsByPlaylistId(playlistId)

    override suspend fun getPlaylistTracks(playlistId: Long): List<PlaylistTrackEntity> =
        dao.getTracksByPlaylistId(playlistId)

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String) {
        dao.removeTrackFromPlaylist(playlistId, trackId)
        dao.decrementTrackCount(playlistId)
    }

    override suspend fun deleteTrackIfUnused(trackId: String) {
        val count = dao.countPlaylistsWithTrack(trackId)
        if (count == 0) {
            dao.deleteTrackByTrackId(trackId)
        }
    }

    override suspend fun deletePlaylistAndCleanup(playlistId: Long) =
        withContext(Dispatchers.IO) {

            val tracks = dao.getTracksByPlaylistId(playlistId)

            dao.deletePlaylistById(playlistId)

            tracks.forEach { track ->
                deleteTrackIfUnused(track.trackId)
            }
        }

    override suspend fun updatePlaylist(
        id: Long,
        name: String,
        description: String?,
        coverUri: android.net.Uri?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentEntity = dao.getPlaylistById(id)
                ?: return@withContext Result.failure(Exception("Плейлист не найден: $id"))

            var coverPath = currentEntity.coverPath
            if (coverUri != null) {
                coverPath = safeCopyToPrivateStorageFromUri(coverUri)
            }

            val updatedEntity = currentEntity.copy(
                name = name,
                description = description,
                coverPath = coverPath
            )

            dao.updatePlaylist(updatedEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun updatePlaylistName(
        id: Long,
        newName: String
    ) = withContext(Dispatchers.IO) {

        dao.updatePlaylistName(
            playlistId = id,
            newName = newName
        )
    }
}
