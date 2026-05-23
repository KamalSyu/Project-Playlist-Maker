package com.practicum.playlistmaker.player.ui.view

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.utils.FormatTrackDurationUseCase
import com.practicum.playlistmaker.player.data.mapper.TrackParcelableMapper
import com.practicum.playlistmaker.player.data.repository.AddTrackStatus
import com.practicum.playlistmaker.player.domain.model.PlaybackState
import com.practicum.playlistmaker.player.domain.model.PlaylistForPlayer
import com.practicum.playlistmaker.player.domain.repository.PlaylistRepository
import com.practicum.playlistmaker.player.domain.usecase.favorite.AddToFavoritesUseCase
import com.practicum.playlistmaker.player.domain.usecase.favorite.IsTrackFavoriteUseCase
import com.practicum.playlistmaker.player.domain.usecase.favorite.RemoveFromFavoritesUseCase
import com.practicum.playlistmaker.player.domain.usecase.playback.GetCurrentPositionUseCase
import com.practicum.playlistmaker.player.domain.usecase.playback.PreparePlaybackUseCase
import com.practicum.playlistmaker.player.domain.usecase.playback.ResetPlaybackUseCase
import com.practicum.playlistmaker.player.domain.usecase.playback.SetPlaybackCompletionListenerUseCase
import com.practicum.playlistmaker.player.domain.usecase.playback.StopPlaybackUseCase
import com.practicum.playlistmaker.player.domain.usecase.playback.TogglePlaybackUseCase
import com.practicum.playlistmaker.player.domain.usecase.playlist.GetPlaylistsUseCase
import com.practicum.playlistmaker.player.ui.PlayerUiState
import com.practicum.playlistmaker.search.ui.parcel.ParcelableTrack
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AudioPlayerViewModel(
    private val preparePlaybackUseCase: PreparePlaybackUseCase,
    private val togglePlaybackUseCase: TogglePlaybackUseCase,
    private val stopPlaybackUseCase: StopPlaybackUseCase,
    private val getCurrentPositionUseCase: GetCurrentPositionUseCase,
    private val setCompletionListenerUseCase: SetPlaybackCompletionListenerUseCase,
    private val resetPlaybackUseCase: ResetPlaybackUseCase,
    val formatTrackDurationUseCase: FormatTrackDurationUseCase,
    private val trackParcelableMapper: TrackParcelableMapper,
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    private val isTrackFavoriteUseCase: IsTrackFavoriteUseCase,
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    companion object {
        private const val PROGRESS_UPDATE_INTERVAL_MS = 300L
    }

    private val _uiState = MutableLiveData<PlayerUiState>(
        PlayerUiState(
            playbackState = PlaybackState(isPlaying = false, position = 0L),
            formattedTime = formatTrackDurationUseCase(0L),
            playbackCompleted = false,
            shouldPoll = false,
            error = null,
            isInitialized = false,
            isFavorite = false,
            currentTrack = null,
            playlistsForBottomSheet = emptyList(),
            isBottomSheetExpanded = false,
            isLoadingPlaylists = false,
            addTrackStatus = null,
            isCreatingPlaylist = false
        )
    )


    private var currentTrackId: String? = null
    private var favoriteStatusJob: Job? = null
    private var pollingJob: Job? = null
    val uiState: LiveData<PlayerUiState> = _uiState

    fun onFavoriteClicked() = viewModelScope.launch {
        try {
            val currentTrack = _uiState.value?.currentTrack ?: return@launch
            val isFavoriteNow = isTrackFavoriteUseCase(currentTrack.trackId)
            if (isFavoriteNow) {
                removeFromFavoritesUseCase(currentTrack.trackId)
            } else {
                addToFavoritesUseCase(currentTrack)
            }
            _uiState.value = _uiState.value?.copy(isFavorite = !isFavoriteNow)
            Log.d("AudioPlayerViewModel", "Статус избранного обновлён для трека: ${currentTrack.trackName}")
        } catch (e: Exception) {
            Log.e("AudioPlayerViewModel", "Ошибка работы с избранным", e)
            _uiState.postValue(_uiState.value?.copy(error = e))
        }
    }

    fun checkTrackFavoriteStatus(trackId: String) = viewModelScope.launch {
        try {
            val isFavorite = isTrackFavoriteUseCase(trackId)
            _uiState.value = _uiState.value?.copy(isFavorite = isFavorite)

        } catch (e: Exception) {
            Log.e("AudioPlayerViewModel", "Ошибка проверки статуса избранного", e)
            _uiState.value = _uiState.value?.copy(isFavorite = false)
        }
    }

    fun restorePlaybackState(isPlaying: Boolean, savedPosition: Long) {
        _uiState.value = _uiState.value?.copy(
            playbackState = PlaybackState(isPlaying, savedPosition),
            formattedTime = formatTrackDurationUseCase(savedPosition),
            playbackCompleted = false,
            shouldPoll = false,
            error = null,
            isInitialized = true
        )
    }

    fun showPlaylistsBottomSheet() = viewModelScope.launch {
        _uiState.value = _uiState.value?.copy(
            isBottomSheetExpanded = true,
            isLoadingPlaylists = true
        )
        loadPlaylistsForBottomSheet()
    }

    private fun loadPlaylistsForBottomSheet() = viewModelScope.launch {
        try {
            _uiState.value = _uiState.value?.copy(isLoadingPlaylists = true)
            val playlists = getPlaylistsUseCase() // Вызов UseCase
            _uiState.value = _uiState.value?.copy(
                playlistsForBottomSheet = playlists,
                isLoadingPlaylists = false
            )
        } catch (e: Exception) {
            Log.e("AudioPlayerViewModel", "Failed to load playlists", e)
            _uiState.value = _uiState.value?.copy(
                isLoadingPlaylists = false,
                error = e
            )
        }
    }

    fun hidePlaylistsBottomSheet() {
        _uiState.value = _uiState.value?.copy(
            isBottomSheetExpanded = false
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value?.copy(error = null)
    }

    fun saveCurrentPosition() = viewModelScope.launch {
        try {
            val currentPosition = getCurrentPositionUseCase()
            _uiState.value = _uiState.value?.copy(
                playbackState = _uiState.value?.playbackState?.copy(position = currentPosition) ?: PlaybackState(false, currentPosition),
                formattedTime = formatTrackDurationUseCase(currentPosition)
            )
        } catch (e: Exception) {
            Log.e("AudioPlayerViewModel", "Ошибка сохранения позиции", e)
        }
    }

    fun initPlayback(previewUrl: String?) = viewModelScope.launch {
        try {
            val result = preparePlaybackUseCase(previewUrl)
            if (result.isSuccess) {
                _uiState.value = _uiState.value?.copy(
                    playbackState = PlaybackState(isPlaying = false, position = 0L),
                    formattedTime = formatTrackDurationUseCase(0L),
                    playbackCompleted = false,
                    shouldPoll = false,
                    error = null,
                    isInitialized = true
                )
            } else {
                Log.e("AudioPlayerViewModel", "Не удалось подготовить воспроизведение")
                _uiState.value = _uiState.value?.copy(error = result.exceptionOrNull())
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerViewModel", "Ошибка инициализации воспроизведения", e)
            _uiState.value = _uiState.value?.copy(error = e)
        }
    }

    fun togglePlayback(resumePosition: Long? = null) = viewModelScope.launch {
        try {
            val currentState = _uiState.value?.playbackState ?: PlaybackState(false, 0L)

            if (currentState.isPlaying) {
                val result = togglePlaybackUseCase(null)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value?.copy(
                        playbackState = currentState.copy(isPlaying = false),
                        shouldPoll = false
                    )
                }
            } else {
                val effectivePosition = if (currentState.position == 0L) null else resumePosition ?: currentState.position
                val result = togglePlaybackUseCase(effectivePosition)
                if (result.isSuccess) {
                    val finalPosition = effectivePosition ?: 0L
                    _uiState.value = _uiState.value?.copy(
                        playbackState = currentState.copy(
                            isPlaying = true,
                            position = finalPosition
                        ),
                        formattedTime = formatTrackDurationUseCase(finalPosition),
                        shouldPoll = true
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerViewModel", "Ошибка переключения воспроизведения", e)
            _uiState.value = _uiState.value?.copy(error = e)
        }
    }

    fun stopPlayback() = viewModelScope.launch {
        saveCurrentPosition()
        try {
            val result = stopPlaybackUseCase()
            if (result.isSuccess) {
                _uiState.value = _uiState.value?.copy(
                    playbackState = _uiState.value?.playbackState?.copy(isPlaying = false) ?: PlaybackState(false, 0L),
                    shouldPoll = false
                )
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerViewModel", "Ошибка остановки воспроизведения", e)
        }
    }

    fun setupPlaybackCompletionListener() = viewModelScope.launch {
        setCompletionListenerUseCase {
            viewModelScope.launch {
                _uiState.value = _uiState.value?.copy(
                    playbackCompleted = true,
                    shouldPoll = false,
                    playbackState = PlaybackState(isPlaying = false, position = 0L),
                    formattedTime = formatTrackDurationUseCase(0L)
                )
                stopPlaybackUseCase().isSuccess
            }
        }
    }

    fun resetPlaybackToStart() = viewModelScope.launch {
        try {
            stopPlaybackUseCase().isSuccess
            resetPlaybackUseCase.invoke()
            _uiState.value = PlayerUiState(
                playbackState = PlaybackState(isPlaying = false, position = 0L),
                formattedTime = formatTrackDurationUseCase(0L),
                playbackCompleted = false,
                shouldPoll = false,
                error = null,
                isInitialized = false
            )
        } catch (e: Exception) {
            Log.e("AudioPlayerViewModel", "Ошибка сброса воспроизведения до начала", e)
            _uiState.value = _uiState.value?.copy(error = e)
        }
    }

    fun processTrack(parcelableTrack: ParcelableTrack): Track {
        return trackParcelableMapper.toDomain(parcelableTrack)
    }

    fun setCurrentTrack(track: Track) {
        favoriteStatusJob?.cancel()
        currentTrackId = track.trackId
        _uiState.value = _uiState.value?.copy(currentTrack = track)
    }

    fun startProgressUpdates() {
        startPolling()
    }

    fun stopProgressUpdates() {
        stopPolling()
    }

    private fun startPolling() {
        stopPolling()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(PROGRESS_UPDATE_INTERVAL_MS)
                try {
                    val currentPosition = getCurrentPositionUseCase()
                    val currentState = _uiState.value ?: return@launch
                    if (currentState.shouldPoll) {
                        _uiState.value = currentState.copy(
                            playbackState = currentState.playbackState.copy(position = currentPosition),
                            formattedTime = formatTrackDurationUseCase(currentPosition)
                        )
                    }
                } catch (e: Exception) {
                    Log.e("AudioPlayerViewModel", "Ошибка при обновлении прогресса", e)
                }
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun updateFavoriteStatusAfterTrackSet() = viewModelScope.launch {
        favoriteStatusJob?.cancel()
        currentTrackId?.let { trackId ->
            try {
                val isFavorite = isTrackFavoriteUseCase(trackId)
                _uiState.value = _uiState.value?.copy(
                    isFavorite = isFavorite,
                    currentTrack = _uiState.value?.currentTrack?.copy(isFavorite = isFavorite)
                )
                Log.d("AudioPlayerViewModel", "Статус избранного обновлён: трек $trackId, isFavorite=$isFavorite")
            } catch (e: Exception) {
                Log.e("AudioPlayerViewModel", "Ошибка проверки статуса избранного для трека $trackId", e)
                _uiState.value = _uiState.value?.copy(isFavorite = false)
            }
        } ?: run {
            Log.w("AudioPlayerViewModel", "currentTrackId is null, cannot check favorite status")
        }
    }

    fun addTrackToPlaylist(playlist: PlaylistForPlayer) = viewModelScope.launch {
        val currentTrack = _uiState.value?.currentTrack ?: return@launch

        // Локальная проверка наличия трека в плейлисте (без обращения к репозиторию)
        if (playlist.trackIds?.contains(currentTrack.trackId) == true) {
            _uiState.value = _uiState.value?.copy(addTrackStatus = AddTrackStatus.ALREADY_EXISTS)
            return@launch
        }

        try {
            // Если трека нет, передаём в репозиторий для сохранения
            val status = playlistRepository.addTrackToPlaylist(playlist.playlistId, currentTrack)
            _uiState.value = _uiState.value?.copy(addTrackStatus = status)
        } catch (e: Exception) {
            Log.e("AudioPlayerViewModel", "Ошибка добавления трека в плейлист", e)
            _uiState.value = _uiState.value?.copy(addTrackStatus = AddTrackStatus.ERROR)
        }
    }

    // Метод для сброса статуса после отображения Toast
    fun clearAddTrackStatus() {
        _uiState.value = _uiState.value?.copy(addTrackStatus = null)
    }

}