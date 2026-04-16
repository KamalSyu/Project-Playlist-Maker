package com.practicum.playlistmaker.player.ui.view

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.core.constants.Constants
import com.practicum.playlistmaker.core.contract.FormatTrackDurationUseCaseContract
import com.practicum.playlistmaker.core.contract.GetCurrentPositionUseCaseContract
import com.practicum.playlistmaker.core.contract.PreparePlaybackUseCaseContract
import com.practicum.playlistmaker.core.contract.ResetPlaybackUseCaseContract
import com.practicum.playlistmaker.core.contract.SetPlaybackCompletionListenerUseCaseContract
import com.practicum.playlistmaker.core.contract.StopPlaybackUseCaseContract
import com.practicum.playlistmaker.core.contract.TogglePlaybackUseCaseContract
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.player.data.mapper.TrackParcelableMapper
import com.practicum.playlistmaker.player.domain.model.PlaybackState
import com.practicum.playlistmaker.player.ui.PlayerUiState
import com.practicum.playlistmaker.search.ui.parcel.ParcelableTrack
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AudioPlayerViewModel(
    private val preparePlaybackUseCase: PreparePlaybackUseCaseContract,
    private val togglePlaybackUseCase: TogglePlaybackUseCaseContract,
    private val stopPlaybackUseCase: StopPlaybackUseCaseContract,
    private val getCurrentPositionUseCase: GetCurrentPositionUseCaseContract,
    private val setCompletionListenerUseCase: SetPlaybackCompletionListenerUseCaseContract,
    private val resetPlaybackUseCase: ResetPlaybackUseCaseContract,
    val formatTrackDurationUseCase: FormatTrackDurationUseCaseContract,
    private val trackParcelableMapper: TrackParcelableMapper
) : ViewModel() {

    private val _uiState = MutableLiveData<PlayerUiState>(
        PlayerUiState(
            playbackState = PlaybackState(isPlaying = false, position = 0L),
            formattedTime = formatTrackDurationUseCase(0L),
            playbackCompleted = false
        )
    )

    private var pollingJob: Job? = null
    private val _currentTrack = MutableLiveData<Track>()
    val currentTrack: LiveData<Track> = _currentTrack
    val uiState: LiveData<PlayerUiState> = _uiState

    fun restorePlaybackState(isPlaying: Boolean, savedPosition: Long) {
        _uiState.value = PlayerUiState(
            playbackState = PlaybackState(isPlaying, savedPosition),
            formattedTime = formatTrackDurationUseCase(savedPosition),
            playbackCompleted = false
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value?.copy(error = null)
    }

    fun saveCurrentPosition() = viewModelScope.launch {
        try {
            val currentPosition = getCurrentPositionUseCase()
            _uiState.value = _uiState.value?.copy(
                playbackState = _uiState.value?.playbackState?.copy(position = currentPosition) ?: PlaybackState(
                    false,
                    currentPosition
                )
            )
        } catch (e: Exception) {
            Log.e("AudioPlayerViewModel", "Ошибка сохранения позиции", e)
        }
    }

    fun initPlayback(previewUrl: String?) = viewModelScope.launch {
        try {
            val result = preparePlaybackUseCase(previewUrl)
            if (result.isSuccess) {
                _uiState.value = PlayerUiState(
                    playbackState = PlaybackState(isPlaying = false, position = 0L),
                    formattedTime = formatTrackDurationUseCase(0L),
                    playbackCompleted = false,
                    shouldPoll = false,
                    error = null,
                    isInitialized = true
                )
            } else {
                Log.e("AudioPlayerViewModel", "Не удалось подготовить воспроизведение")
                _uiState.value = _uiState.value?.copy(
                    error = result.exceptionOrNull()
                )
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
                    playbackState = _uiState.value?.playbackState?.copy(isPlaying = false) ?: PlaybackState(
                        false,
                        0L
                    ),
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
        _currentTrack.value = track
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
                delay(Constants.PROGRESS_UPDATE_INTERVAL_MS)
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
}
