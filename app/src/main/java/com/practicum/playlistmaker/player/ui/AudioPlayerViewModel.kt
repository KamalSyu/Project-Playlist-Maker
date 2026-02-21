package com.practicum.playlistmaker.player.ui

import android.util.Log
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
    private val setCompletionListenerUseCase = useCaseCreator.createSetPlaybackCompletionListenerUseCase()
    private val getPlaybackPositionUseCase = useCaseCreator.createGetPlaybackPositionUseCase()

    private var playbackError: Exception? = null

    val formatTrackDurationUseCase: FormatTrackDurationUseCaseContract =
        useCaseCreator.createFormatTrackDurationUseCase()

    private val _uiState = MutableLiveData<PlayerUiState>(
        PlayerUiState(
            playbackState = PlaybackState(isPlaying = false, position = 0L),
            formattedTime = formatTrackDurationUseCase(0L),
            playbackCompleted = false
        )
    )

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
                playbackState = _uiState.value?.playbackState?.copy(position = currentPosition) ?: PlaybackState(false, currentPosition)
            )
        } catch (e: Exception) {
            Log.e("AudioPlayerViewModel", "Ошибка сохранения позиции", e)
        }
    }

    fun updateCurrentPosition() = viewModelScope.launch {
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
            Log.e("AudioPlayerViewModel", "Ошибка обновления позиции", e)
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
                // Не сбрасываем состояние — сохраняем текущую позицию
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
                // Ставим на паузу через Use Case
                val result = togglePlaybackUseCase(null) // пауза не требует позиции
                if (result.isSuccess) {
                    _uiState.value = _uiState.value?.copy(
                        playbackState = currentState.copy(isPlaying = false),
                        shouldPoll = false
                    )
                }
            } else {
                // Запускаем воспроизведение через Use Case
                val result = togglePlaybackUseCase(resumePosition)
                if (result.isSuccess) {
                    // Определяем позицию для UI
                    val effectivePosition = resumePosition ?: currentState.position
                    _uiState.value = _uiState.value?.copy(
                        playbackState = currentState.copy(
                            isPlaying = true,
                            position = effectivePosition
                        ),
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
        saveCurrentPosition() // Сохраняем текущую позицию
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

    fun resetPlaybackState() {
        _uiState.value = PlayerUiState(
            playbackState = PlaybackState(isPlaying = false, position = 0L),
            formattedTime = formatTrackDurationUseCase(0L),
            playbackCompleted = false,
            shouldPoll = false,
            error = null
        )
    }

    fun onPlaybackCompletedReset() {
        val currentState = _uiState.value ?: PlayerUiState(
            playbackState = PlaybackState(isPlaying = false, position = 0L),
            formattedTime = formatTrackDurationUseCase(0L),
            playbackCompleted = false
        )
        _uiState.value = currentState.copy(playbackCompleted = false)
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
            }
        }
    }
}



//@HiltViewModel
//class AudioPlayerViewModel @Inject constructor(
//    private val useCaseCreator: UseCaseCreator
//) : ViewModel() {
//
//    private val preparePlaybackUseCase = useCaseCreator.createPreparePlaybackUseCase()
//    private val togglePlaybackUseCase = useCaseCreator.createTogglePlaybackUseCase()
//    private val stopPlaybackUseCase = useCaseCreator.createStopPlaybackUseCase()
//    private val getCurrentPositionUseCase = useCaseCreator.createGetCurrentPositionUseCase()
//    private val handleCompletionUseCase = useCaseCreator.createHandlePlaybackCompletionUseCase()
//    private val setCompletionListenerUseCase = useCaseCreator.createSetPlaybackCompletionListenerUseCase()
//    private val getPlaybackPositionUseCase = useCaseCreator.createGetPlaybackPositionUseCase()
//
//    val formatTrackDurationUseCase: FormatTrackDurationUseCaseContract =
//        useCaseCreator.createFormatTrackDurationUseCase()
//
//    // LiveData для передачи состояния в Activity
//    private val _playbackState = MutableLiveData<PlaybackState>()
//    val playbackState: LiveData<PlaybackState> = _playbackState
//
//    // LiveData для форматированного времени
//    private val _formattedTime = MutableLiveData<String>()
//    val formattedTime: LiveData<String> = _formattedTime
//
//    private val _playbackCompleted = MutableLiveData<Unit>()
//    val playbackCompleted: LiveData<Unit> = _playbackCompleted
//
//    private val _currentPositionMillis = MutableLiveData<Long>()
//    val currentPositionMillis: LiveData<Long> = _currentPositionMillis
//
//    /**
//     * Восстанавливает состояние воспроизведения из savedInstanceState
//     */
//    fun restorePlaybackState(isPlaying: Boolean, savedPosition: Long) {
//        _playbackState.value = PlaybackState(isPlaying, savedPosition)
//        _currentPositionMillis.value = savedPosition
//        _formattedTime.value = formatTrackDurationUseCase(savedPosition)
//    }
//
//    /**
//     * Сохраняет текущую позицию воспроизведения
//     */
//    fun saveCurrentPosition() = viewModelScope.launch {
//        try {
//            val currentPosition = getCurrentPositionUseCase()
//            _playbackState.value = PlaybackState(
//                isPlaying = (_playbackState.value ?: PlaybackState(false, 0L)).isPlaying,
//                position = currentPosition
//            )
//            _currentPositionMillis.value = currentPosition
//            _formattedTime.value = formatTrackDurationUseCase(currentPosition)
//        } catch (e: Exception) {
//            Log.e("AudioPlayerViewModel", "Ошибка сохранения позиции", e)
//        }
//    }
//
//    /**
//     * Обновляет текущую позицию для polling
//     */
//    fun updateCurrentPosition() = viewModelScope.launch {
//        try {
//            val currentPosition = getCurrentPositionUseCase()
//            _currentPositionMillis.value = currentPosition
//            _formattedTime.value = formatTrackDurationUseCase(currentPosition)
//        } catch (e: Exception) {
//            Log.e("AudioPlayerViewModel", "Ошибка обновления позиции", e)
//        }
//    }
//
//    fun initPlayback(previewUrl: String?) = viewModelScope.launch {
//        val result = preparePlaybackUseCase(previewUrl)
//        if (result.isSuccess) {
//            _playbackState.value = PlaybackState(isPlaying = false, position = 0L)
//            _currentPositionMillis.value = 0L
//            _formattedTime.value = formatTrackDurationUseCase(0L)
//        } else {
//            // Оставляем состояние по умолчанию, но логируем ошибку
//            Log.e("AudioPlayerViewModel", "Не удалось подготовить воспроизведение: ${result.exceptionOrNull()?.message}")
//        }
//    }
//
//    fun togglePlayback(resumePosition: Long? = null) = viewModelScope.launch {
//        try {
//            val result = togglePlaybackUseCase(resumePosition)
//            if (result.isSuccess) {
//                val isPlaying = result.getOrThrow()
//                // Получаем актуальную позицию только если не воспроизводим с resumePosition
//                val currentPosition = if (resumePosition != null) resumePosition else getCurrentPositionUseCase()
//                _playbackState.value = PlaybackState(isPlaying, currentPosition)
//                _currentPositionMillis.value = currentPosition
//                _formattedTime.value = formatTrackDurationUseCase(currentPosition)
//            }
//        } catch (e: Exception) {
//            Log.e("AudioPlayerViewModel", "Ошибка переключения воспроизведения", e)
//        }
//    }
//
//    fun stopPlayback() = viewModelScope.launch {
//        try {
//            val result = stopPlaybackUseCase()
//            if (result.isSuccess) {
//                _playbackState.value = PlaybackState(false, 0L)
//                _currentPositionMillis.value = 0L
//                _formattedTime.value = formatTrackDurationUseCase(0L)
//            }
//        } catch (e: Exception) {
//            Log.e("AudioPlayerViewModel", "Ошибка остановки воспроизведения", e)
//        }
//    }
//
//    fun setupCompletionListener() = viewModelScope.launch {
//        try {
//            setCompletionListenerUseCase {
//                viewModelScope.launch {
//                    try {
//                        val currentPosition = getCurrentPositionUseCase()
//                        _playbackState.value = PlaybackState(isPlaying = false, position = currentPosition)
//                        _currentPositionMillis.value = currentPosition
//                        _formattedTime.value = formatTrackDurationUseCase(currentPosition)
//                        handleCompletionUseCase()
//                        _playbackCompleted.value = Unit
//                    } catch (e: Exception) {
//                        Log.e("AudioPlayerViewModel", "Ошибка в колбэке завершения", e)
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            Log.e("AudioPlayerViewModel", "Ошибка настройки слушателя завершения", e)
//        }
//    }
//}

//@HiltViewModel
//class AudioPlayerViewModel @Inject constructor(
//    private val useCaseCreator: UseCaseCreator
//) : ViewModel() {
//
//    private val preparePlaybackUseCase = useCaseCreator.createPreparePlaybackUseCase()
//    private val togglePlaybackUseCase = useCaseCreator.createTogglePlaybackUseCase()
//    private val stopPlaybackUseCase = useCaseCreator.createStopPlaybackUseCase()
//    private val getCurrentPositionUseCase = useCaseCreator.createGetCurrentPositionUseCase()
//    private val handleCompletionUseCase = useCaseCreator.createHandlePlaybackCompletionUseCase()
//    private val formatTrackDurationUseCase = useCaseCreator.createFormatTrackDurationUseCase()
//    private val setCompletionListenerUseCase = useCaseCreator.createSetPlaybackCompletionListenerUseCase()
//    private val getPlaybackPositionUseCase = useCaseCreator.createGetPlaybackPositionUseCase()
//
//    // LiveData для передачи состояния в Activity
//    private val _playbackState = MutableLiveData<PlaybackState>()
//    val playbackState: LiveData<PlaybackState> = _playbackState
//
//    // LiveData для форматированного времени (новое!)
//    private val _formattedTime = MutableLiveData<String>()
//    val formattedTime: LiveData<String> = _formattedTime
//
//    private val _playbackCompleted = MutableLiveData<Unit>()
//    val playbackCompleted: LiveData<Unit> = _playbackCompleted
//
//    private val _currentPositionMillis = MutableLiveData<Long>()
//    val currentPositionMillis: LiveData<Long> = _currentPositionMillis
//
//    fun getFormatTrackDurationUseCase(): FormatTrackDurationUseCaseContract {
//        return formatTrackDurationUseCase
//    }
//
//    fun initPlayback(previewUrl: String?) = viewModelScope.launch {
//        val result = preparePlaybackUseCase(previewUrl)
//        if (result.isSuccess) {
//            _playbackState.value = PlaybackState(isPlaying = false, position = 0L)
//            _currentPositionMillis.value = 0L  // добавляем
//            _formattedTime.value = formatTrackDurationUseCase(0L)
//        } else {
//            _playbackState.value = PlaybackState(isPlaying = false, position = 0L)
//            _currentPositionMillis.value = 0L  // добавляем
//            _formattedTime.value = formatTrackDurationUseCase(0L)
//        }
//    }
//
//
//    fun togglePlayback(resumePosition: Long? = null) = viewModelScope.launch {
//        val result = togglePlaybackUseCase(resumePosition)
//        if (result.isSuccess) {
//            val isPlaying = result.getOrThrow()
//            val currentPosition = if (isPlaying) 0L else getCurrentPositionUseCase()
//            _playbackState.value = PlaybackState(isPlaying, currentPosition)
//            _currentPositionMillis.value = currentPosition  // добавляем
//            _formattedTime.value = formatTrackDurationUseCase(currentPosition)
//        }
//    }
//
//    fun stopPlayback() = viewModelScope.launch {
//        val result = stopPlaybackUseCase()
//        if (result.isSuccess) {
//            _playbackState.value = PlaybackState(false, 0L)
//            _currentPositionMillis.value = 0L  // добавляем
//            _formattedTime.value = formatTrackDurationUseCase(0L)
//        }
//    }
//
//    fun setupCompletionListener() = viewModelScope.launch {
//        setCompletionListenerUseCase {
//            viewModelScope.launch {
//                val currentPosition = getCurrentPositionUseCase()
//                _playbackState.value = PlaybackState(isPlaying = false, position = currentPosition)
//                _currentPositionMillis.value = currentPosition  // добавляем
//                _formattedTime.value = formatTrackDurationUseCase(currentPosition)
//                handleCompletionUseCase()
//                _playbackCompleted.value = Unit
//            }
//        }
//    }
//
//
//}
