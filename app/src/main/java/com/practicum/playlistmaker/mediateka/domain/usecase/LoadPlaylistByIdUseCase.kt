package com.practicum.playlistmaker.mediateka.domain.usecase

import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.core.utils.FormatTrackDurationUseCase
import com.practicum.playlistmaker.mediateka.domain.repository.PlaylistsRepositoryMedia
import com.practicum.playlistmaker.mediateka.ui.PlaylistDetailUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import java.util.Locale

class LoadPlaylistByIdUseCase(
    private val playlistsRepositoryMedia: PlaylistsRepositoryMedia,
) {
    operator fun invoke(playlistId: Long): Flow<PlaylistDetailUiState> = flow {
        emit(PlaylistDetailUiState.Loading)

        val entity = playlistsRepositoryMedia.getPlaylistById(playlistId)
        if (entity == null) {
            emit(PlaylistDetailUiState.Error(IllegalStateException("Плейлист не найден")))
            return@flow
        }

        val tracks = playlistsRepositoryMedia.getPlaylistTracks(playlistId)

        // Считаем общую длительность в миллисекундах
        val totalMillis = tracks.sumOf { (it.duration * 1_000).toLong() }

        // Формат «mm» (только минуты) — точечно, без SimpleDateFormat
        val minutes = totalMillis / 60_000
        val durationFormatted = String.format(Locale.US, "%d", minutes)

        val playlist = Playlist(
            id = entity.id.toString(),
            name = entity.name,
            description = entity.description,
            coverPath = entity.coverPath,
            trackCount = entity.trackCount,
            createdAt = entity.createdAt,
            durationFormatted = durationFormatted
        )

        emit(PlaylistDetailUiState.Success(playlist))
    }.catch { error ->
        emit(PlaylistDetailUiState.Error(error))
    }
}


