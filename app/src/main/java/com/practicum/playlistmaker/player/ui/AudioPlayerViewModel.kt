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

    // Use Case для операций плеера
    private val preparePlaybackUseCase = useCaseCreator.createPreparePlaybackUseCase()
    private val togglePlaybackUseCase = useCaseCreator.createTogglePlaybackUseCase()
    private val stopPlaybackUseCase = useCaseCreator.createStopPlaybackUseCase()
    private val getCurrentPositionUseCase = useCaseCreator.createGetCurrentPositionUseCase()
    private val handleCompletionUseCase = useCaseCreator.createHandlePlaybackCompletionUseCase()
    private val setCompletionListenerUseCase = useCaseCreator.createSetPlaybackCompletionListenerUseCase()
    private val getPlaybackPositionUseCase = useCaseCreator.createGetPlaybackPositionUseCase()

    // Форматирование времени трека (доступно извне)
    val formatTrackDurationUseCase: FormatTrackDurationUseCaseContract =
        useCaseCreator.createFormatTrackDurationUseCase()

    // Внутреннее состояние UI
    private val _uiState = MutableLiveData<PlayerUiState>(
        PlayerUiState(
            playbackState = PlaybackState(isPlaying = false, position = 0L),
            formattedTime = formatTrackDurationUseCase(0L),
            playbackCompleted = false
        )
    )

    // Публичное состояние UI (только чтение)
    val uiState: LiveData<PlayerUiState> = _uiState

    /** Восстановление состояния после поворота экрана */
    fun restorePlaybackState(isPlaying: Boolean, savedPosition: Long) {
        _uiState.value = PlayerUiState(
            playbackState = PlaybackState(isPlaying, savedPosition),
            formattedTime = formatTrackDurationUseCase(savedPosition),
            playbackCompleted = false
        )
    }

    /** Очистка ошибки из состояния */
    fun clearError() {
        _uiState.value = _uiState.value?.copy(error = null)
    }

    /** Сохранение текущей позиции воспроизведения */
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

    /** Обновление позиции в UI */
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

    /** Инициализация воспроизведения трека */
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

    /** Переключение воспроизведения (старт/пауза) */
    fun togglePlayback(resumePosition: Long? = null) = viewModelScope.launch {
        try {
            val currentState = _uiState.value?.playbackState ?: PlaybackState(false, 0L)

            if (currentState.isPlaying) {
                // Пауза
                val result = togglePlaybackUseCase(null)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value?.copy(
                        playbackState = currentState.copy(isPlaying = false),
                        shouldPoll = false
                    )
                }
            } else {
                // Старт воспроизведения
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

    /** Остановка воспроизведения */
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

    /** Сброс состояния плеера */
    fun resetPlaybackState() {
        _uiState.value = PlayerUiState(
            playbackState = PlaybackState(isPlaying = false, position = 0L),
            formattedTime = formatTrackDurationUseCase(0L),
            playbackCompleted = false,
            shouldPoll = false,
            error = null
        )
    }

    /** Сброс флага завершения воспроизведения */
    fun onPlaybackCompletedReset() {
        val currentState = _uiState.value ?: PlayerUiState(
            playbackState = PlaybackState(isPlaying = false, position = 0L),
            formattedTime = formatTrackDurationUseCase(0L),
            playbackCompleted = false
        )
        _uiState.value = currentState.copy(playbackCompleted = false)
    }

    /** Установка слушателя завершения воспроизведения */
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
    /** Сброс воспроизведения до начала */
    fun resetPlaybackToStart() = viewModelScope.launch {
        try {
            // Сначала останавливаем воспроизведение
            stopPlaybackUseCase().isSuccess

            // Сбрасываем состояние в репозитории
            useCaseCreator.createResetPlaybackUseCase().invoke()

            // Обновляем UI‑состояние
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
}

