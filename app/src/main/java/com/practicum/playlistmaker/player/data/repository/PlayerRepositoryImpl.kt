package com.practicum.playlistmaker.player.data.repository

import android.media.MediaPlayer
import android.util.Log
import com.practicum.playlistmaker.player.domain.repository.PlayerRepository

class PlayerRepositoryImpl () : PlayerRepository {

    private var mediaPlayer: MediaPlayer? = null
    private var savedPosition: Long = 0L
    private var isPrepared: Boolean = false
    private var completionListener: (() -> Unit)? = null

    /**
     * Инициализирует MediaPlayer и подготавливает аудиофайл к воспроизведению.
     * Устанавливает обработчики ошибок, готовности и завершения воспроизведения.
     */
    override suspend fun prepare(url: String?) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                Log.d("PlayerRepository", "setDataSource: $url")

                setOnErrorListener { mp, what, extra ->
                    Log.e("PlayerRepository", "MediaPlayer error: what=$what, extra=$extra")
                    false
                }

                setOnPreparedListener {
                    isPrepared = true
                    if (savedPosition > 0L) {
                        seekTo(savedPosition.toInt())
                    }
                }

                setOnCompletionListener {
                    completionListener?.invoke()
                    savedPosition = 0L
                }

                prepareAsync()
                Log.d("PlayerRepository", "prepareAsync() called")
            }
        } catch (e: Exception) {
            Log.e("PlayerRepository", "Prepare failed", e)
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    /**
     * Запускает воспроизведение с сохранённой позиции (если есть) или с начала.
     */
    override suspend fun play() {
        playWithPosition(null)
    }

    /**
     * Запускает воспроизведение с указанной позиции или сохранённой позиции.
     * Если медиаплеер ещё не готов, ожидает готовности перед воспроизведением.
     */
    override suspend fun playWithPosition(resumePosition: Long?) {
        mediaPlayer?.let { player ->
            if (!isPrepared) {
                player.setOnPreparedListener {
                    resumeWithPosition(player, resumePosition)
                }
                return
            }
            resumeWithPosition(player, resumePosition)
        } ?: Log.e("PlayerRepository", "MediaPlayer is null in play()")
    }

    /**
     * Вспомогательный метод для запуска воспроизведения с заданной позиции.
     * Выполняет seekTo() при необходимости и запускает воспроизведение.
     */
    private fun resumeWithPosition(player: MediaPlayer, resumePosition: Long?) {
        val targetPosition = resumePosition ?: savedPosition
        if (targetPosition > 0L) {
            player.seekTo(targetPosition.toInt())
            savedPosition = targetPosition
        }
        player.start()
        Log.d("PlayerRepository", "play() called, isPlaying=${player.isPlaying}, position=${player.currentPosition}")
    }

    /**
     * Приостанавливает воспроизведение и сохраняет текущую позицию.
     */
    override suspend fun pause() {
        mediaPlayer?.pause()
        mediaPlayer?.currentPosition?.toLong()?.let { savedPosition = it }
    }

    /**
     * Останавливает воспроизведение и сохраняет текущую позицию.
     * По сути, работает аналогично pause(), но  более полную остановку.
     */
    override suspend fun stop() {
        mediaPlayer?.pause()
        mediaPlayer?.currentPosition?.toLong()?.let { savedPosition = it }
    }

    /**
     * Полностью сбрасывает состояние медиаплеера: освобождает ресурсы,
     * обнуляет сохранённую позицию и флаги состояния.
     */
    override suspend fun reset() {
        mediaPlayer?.release()
        mediaPlayer = null
        completionListener = null
        savedPosition = 0L
        isPrepared = false
    }

    /**
     * Проверяет, воспроизводится ли аудио в данный момент.
     * @return true, если медиаплеер воспроизводит аудио, иначе false.
     */
    override fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    /**
     * Возвращает текущую позицию воспроизведения в миллисекундах.
     * Если медиаплеер недоступен, возвращает сохранённую позицию.
     * @return текущая позиция воспроизведения или сохранённая позиция.
     */
    override fun getCurrentPosition(): Long {
        return mediaPlayer?.currentPosition?.toLong() ?: savedPosition
    }

    /**
     * Устанавливает слушатель завершения воспроизведения.
     * Сохраняет лямбду для вызова при завершении и привязывает её к MediaPlayer.
     */
    override fun setOnCompletionListener(listener: () -> Unit) {
        completionListener = listener
        mediaPlayer?.setOnCompletionListener { listener() }
    }

    /**
     * Перемещает воспроизведение на указанную позицию в миллисекундах.
     * Обновляет сохранённую позицию после seekTo().
     */
    override fun seekTo(position: Long) {
        mediaPlayer?.seekTo(position.toInt())
        savedPosition = position
    }
}
