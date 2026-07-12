package com.practicum.playlistmaker.mediateka.ui.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.core.utils.FormatTrackDurationUseCase
import com.practicum.playlistmaker.mediateka.domain.repository.PlaylistsRepositoryMedia
import com.practicum.playlistmaker.mediateka.domain.usecase.LoadPlaylistByIdUseCase
import com.practicum.playlistmaker.mediateka.domain.usecase.RemoveTrackFromPlaylistUseCase
import com.practicum.playlistmaker.mediateka.ui.PlaylistDetailUiState
import kotlinx.coroutines.launch
import java.util.Locale

class PlaylistDetailViewModel(
    private val loadPlaylistByIdUseCase: LoadPlaylistByIdUseCase,
    private val formatTrackDurationUseCase: FormatTrackDurationUseCase,
    private val playlistsRepositoryMedia: PlaylistsRepositoryMedia,
    private val removeTrackFromPlaylistUseCase: RemoveTrackFromPlaylistUseCase

) : ViewModel() {

    private val _uiState = MutableLiveData<PlaylistDetailUiState>()
    val uiState: LiveData<PlaylistDetailUiState> = _uiState

    fun loadPlaylist(playlistIdString: String) {
        _uiState.value = PlaylistDetailUiState.Loading
        val playlistId = playlistIdString.toLongOrNull()
        if (playlistId == null) {
            _uiState.value = PlaylistDetailUiState.Error(IllegalArgumentException("Некорректный ID плейлиста"))
            return
        }
        viewModelScope.launch {
            loadPlaylistByIdUseCase(playlistId).collect { state ->
                _uiState.value = state
            }
        }
    }
    fun removeTrack(playlistId: Long, trackId: String) {
        viewModelScope.launch {
            removeTrackFromPlaylistUseCase(playlistId, trackId)
            // После удаления сразу запрашиваем обновлённый список треков,
            // чтобы UI отобразил актуальное состояние.
            val tracks = playlistsRepositoryMedia.getPlaylistTracks(playlistId)

            // Получаем текущее состояние плейлиста (чтобы сохранить name/desc/duration и т.п.)
            val entity = playlistsRepositoryMedia.getPlaylistById(playlistId)
            if (entity != null) {
                val totalMillis = tracks.sumOf { (it.duration * 1_000).toLong() }
                val minutes = totalMillis / 60_000
                val durationFormatted = String.format(Locale.US, "%d", minutes)

                val playlist = Playlist(
                    id = entity.id.toString(),
                    name = entity.name,
                    description = entity.description,
                    coverPath = entity.coverPath,
                    trackCount = tracks.size,
                    createdAt = entity.createdAt,
                    durationFormatted = durationFormatted
                )
                _uiState.value = PlaylistDetailUiState.Success(
                    playlist = playlist,
                    tracks = tracks
                )
            }
        }
    }

}

