package com.practicum.playlistmaker.mediateka.domain.usecase

import android.util.Log
import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.mediateka.data.db.toTrack
import com.practicum.playlistmaker.mediateka.domain.repository.PlaylistsRepositoryMedia
import com.practicum.playlistmaker.mediateka.ui.PlaylistDetailUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

private const val TAG = "UseCaseDebug"

class LoadPlaylistByIdUseCase(
    private val playlistsRepositoryMedia: PlaylistsRepositoryMedia,
) {
    operator fun invoke(playlistId: Long): Flow<PlaylistDetailUiState> = flow {
        emit(PlaylistDetailUiState.Loading)

        try {
            Log.d(TAG, "🔍 Вызываем репозиторий для ID=$playlistId")
            val entity = playlistsRepositoryMedia.getPlaylistById(playlistId)

            Log.d(TAG, "🗃 Результат репозитория: ${if (entity != null) "НАЙДЕН (name=${entity.name})" else "NULL"}")

            if (entity == null) {
                Log.w(TAG, "⚠️ Плейлист не найден в БД")
                emit(PlaylistDetailUiState.Error(IllegalStateException("Плейлист не найден")))
                return@flow
            }

            val tracksEntities = playlistsRepositoryMedia.getPlaylistTracks(playlistId)
            val tracks = tracksEntities.map { it.toTrack() }

            val totalMillis = tracks.sumOf { it.trackTimeMillis ?: 0L }
            val secondsTotal = (totalMillis / 1_000).toInt()
            val minutes = secondsTotal / 60
            val seconds = secondsTotal % 60
            val durationFormatted = String.format("%02d:%02d", minutes, seconds)

            val playlist = Playlist(
                id = entity.id.toString(),
                name = entity.name,
                description = entity.description,
                coverPath = entity.coverPath,
                trackCount = entity.trackCount,
                createdAt = entity.createdAt,
                durationFormatted = durationFormatted
            )

            Log.d(TAG, "✅ Успешно собрали плейлист: name=${playlist.name}")
            emit(PlaylistDetailUiState.Success(playlist, tracks))

        } catch (e: Exception) {
            // Внутренний catch: гарантирует, что мы точно увидим ошибку в логах
            Log.e(TAG, "💥 Ошибка ВНУТРИ потока (try/catch): $e", e)
            emit(PlaylistDetailUiState.Error(e))
        }
    }.catch { error ->
        // Внешний catch: страховка на случай, если что-то сломалось вне try/catch внутри flow
        Log.e(TAG, "💥 Ошибка ВНЕ потока (.catch): $error", error)
        emit(PlaylistDetailUiState.Error(error))
    }
}
