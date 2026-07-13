package com.practicum.playlistmaker.mediateka.domain.usecase

import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.mediateka.domain.repository.PlaylistsRepositoryMedia
import com.practicum.playlistmaker.mediateka.ui.PlaylistDetailUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow


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

        val totalMillis = tracks.sumOf { (it.duration * 1_000).toLong() }
        val totalSeconds = totalMillis / 1_000

        val secondsTotal = totalSeconds.toInt()
        val minutes = secondsTotal / 60
        val seconds = secondsTotal % 60
        val durationFormatted = String.format("%02d:%02d", minutes, seconds)
        // -----------------------

        val playlist = Playlist(
            id = entity.id.toString(),
            name = entity.name,
            description = entity.description,
            coverPath = entity.coverPath,
            trackCount = entity.trackCount,
            createdAt = entity.createdAt,
            durationFormatted = durationFormatted
        )

        emit(PlaylistDetailUiState.Success(playlist, tracks))
    }.catch { error ->
        emit(PlaylistDetailUiState.Error(error))
    }
}



