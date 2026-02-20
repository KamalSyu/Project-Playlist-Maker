package com.practicum.playlistmaker.player.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.core.models.PlaybackState
import com.practicum.playlistmaker.core.usecase.UseCaseCreator
import kotlinx.coroutines.launch
import com.practicum.playlistmaker.core.contract.FormatTrackDurationUseCaseContract
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AudioPlayerViewModel @Inject constructor(
    private val useCaseCreator: UseCaseCreator
) : ViewModel() {

    private val preparePlaybackUseCase = useCaseCreator.createPreparePlaybackUseCase()
    private val togglePlaybackUseCase = useCaseCreator.createTogglePlaybackUseCase()
    private val stopPlaybackUseCase = useCaseCreator.createStopPlaybackUseCase()
    private val getCurrentPositionUseCase = useCaseCreator.createGetCurrentPositionUseCase()
    private val handleCompletionUseCase = useCaseCreator.createHandlePlaybackCompletionUseCase()
    private val formatTrackDurationUseCase = useCaseCreator.createFormatTrackDurationUseCase()
    private val setCompletionListenerUseCase = useCaseCreator.createSetPlaybackCompletionListenerUseCase()
    private val getPlaybackPositionUseCase = useCaseCreator.createGetPlaybackPositionUseCase()

    // LiveData для передачи состояния в Activity
    private val _playbackState = MutableLiveData<PlaybackState>()
    val playbackState: LiveData<PlaybackState> = _playbackState

    // LiveData для форматированного времени (новое!)
    private val _formattedTime = MutableLiveData<String>()
    val formattedTime: LiveData<String> = _formattedTime

    private val _playbackCompleted = MutableLiveData<Unit>()
    val playbackCompleted: LiveData<Unit> = _playbackCompleted

    private val _currentPositionMillis = MutableLiveData<Long>()
    val currentPositionMillis: LiveData<Long> = _currentPositionMillis

    fun getFormatTrackDurationUseCase(): FormatTrackDurationUseCaseContract {
        return formatTrackDurationUseCase
    }

    fun initPlayback(previewUrl: String?) = viewModelScope.launch {
        val result = preparePlaybackUseCase(previewUrl)
        if (result.isSuccess) {
            _playbackState.value = PlaybackState(isPlaying = false, position = 0L)
            _currentPositionMillis.value = 0L  // добавляем
            _formattedTime.value = formatTrackDurationUseCase(0L)
        } else {
            _playbackState.value = PlaybackState(isPlaying = false, position = 0L)
            _currentPositionMillis.value = 0L  // добавляем
            _formattedTime.value = formatTrackDurationUseCase(0L)
        }
    }

    fun togglePlayback(resumePosition: Long? = null) = viewModelScope.launch {
        val result = togglePlaybackUseCase(resumePosition)
        if (result.isSuccess) {
            val isPlaying = result.getOrThrow()
            val currentPosition = if (isPlaying) 0L else getCurrentPositionUseCase()
            _playbackState.value = PlaybackState(isPlaying, currentPosition)
            _currentPositionMillis.value = currentPosition  // добавляем
            _formattedTime.value = formatTrackDurationUseCase(currentPosition)
        }
    }

    fun stopPlayback() = viewModelScope.launch {
        val result = stopPlaybackUseCase()
        if (result.isSuccess) {
            _playbackState.value = PlaybackState(false, 0L)
            _currentPositionMillis.value = 0L  // добавляем
            _formattedTime.value = formatTrackDurationUseCase(0L)
        }
    }

    fun setupCompletionListener() = viewModelScope.launch {
        setCompletionListenerUseCase {
            viewModelScope.launch {
                val currentPosition = getCurrentPositionUseCase()
                _playbackState.value = PlaybackState(isPlaying = false, position = currentPosition)
                _currentPositionMillis.value = currentPosition  // добавляем
                _formattedTime.value = formatTrackDurationUseCase(currentPosition)
                handleCompletionUseCase()
                _playbackCompleted.value = Unit
            }
        }
    }


}
