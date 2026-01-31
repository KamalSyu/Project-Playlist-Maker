package com.practicum.playlistmaker.data.repository

import android.media.MediaPlayer
import com.practicum.playlistmaker.domain.repository.PlayerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация репозитория для управления воспроизведением аудио.
 * Обеспечивает взаимодействие с MediaPlayer через контракт PlayerRepository.
 */
@Singleton

class PlayerRepositoryImpl @Inject constructor(): PlayerRepository {

    private var mediaPlayer: MediaPlayer? = null // Экземпляр MediaPlayer для работы с аудио
    private var completionListener: (() -> Unit)? = null  // Слушатель завершения воспроизведения

    override suspend fun play() {
        mediaPlayer?.start() }

    override suspend fun pause() {
        mediaPlayer?.pause() }

    // Подготавливает MediaPlayer к воспроизведению по указанному URL.
    override suspend fun prepare(url: String?) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepare()
            // Устанавливаем слушатель ПОСЛЕ создания MediaPlayer
            setOnCompletionListener {
                completionListener?.invoke()
            }
        }
    }

    override suspend fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
    override suspend fun reset() {
        withContext(Dispatchers.Main) {
            mediaPlayer?.seekTo(0)
            mediaPlayer?.stop() // если нужно полностью остановить
            mediaPlayer = null // Гарантируем, что isPlaying() вернёт false
        }
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        completionListener = listener
        mediaPlayer?.setOnCompletionListener { listener() }
    }

    override fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false
    override fun getCurrentPosition(): Long = mediaPlayer?.currentPosition?.toLong() ?: 0L
}
