package com.practicum.playlistmaker.player.data.repository


import android.media.MediaPlayer
import android.util.Log
import com.practicum.playlistmaker.player.domain.repository.PlayerRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepositoryImpl @Inject constructor() : PlayerRepository {

    private var mediaPlayer: MediaPlayer? = null
    private var savedPosition: Long = 0L
    private var isPrepared: Boolean = false
    private var completionListener: (() -> Unit)? = null
    private val lock = Any()


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
                    // После подготовки устанавливаем сохранённую позицию
                    if (savedPosition > 0L) {
                        seekTo(savedPosition.toInt())
                    }
                }

                setOnCompletionListener {
                    completionListener?.invoke()
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

    override suspend fun play() {
        playWithPosition(null)
    }

    override suspend fun playWithPosition(resumePosition: Long?) {
        mediaPlayer?.let { player ->
            if (!isPrepared) {
                // Если не подготовлен — ждём подготовки
                player.setOnPreparedListener {
                    resumeWithPosition(player, resumePosition)
                }
                return
            }
            resumeWithPosition(player, resumePosition)
        } ?: Log.e("PlayerRepository", "MediaPlayer is null in play()")
    }

    private fun resumeWithPosition(player: MediaPlayer, resumePosition: Long?) {
        val targetPosition = resumePosition ?: savedPosition
        if (targetPosition > 0L) {
            player.seekTo(targetPosition.toInt())
            savedPosition = targetPosition
        }
        player.start()
        Log.d("PlayerRepository", "play() called, isPlaying=${player.isPlaying}, position=${player.currentPosition}")
    }

    override suspend fun pause() {
        mediaPlayer?.pause()
        // Сохраняем текущую позицию при паузе
        mediaPlayer?.currentPosition?.toLong()?.let { savedPosition = it }
    }

    override suspend fun stop() {
        mediaPlayer?.pause()
        // Не сбрасываем позицию на 0 — сохраняем для возобновления
        mediaPlayer?.currentPosition?.toLong()?.let { savedPosition = it }
        // Не вызываем reset() — оставляем подготовленным
    }

    override suspend fun reset() {
        mediaPlayer?.release()
        mediaPlayer = null
        completionListener = null
        savedPosition = 0L
        isPrepared = false
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    override fun getCurrentPosition(): Long {
        return mediaPlayer?.currentPosition?.toLong() ?: savedPosition
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        completionListener = listener
        mediaPlayer?.setOnCompletionListener { listener() }
    }

    override fun seekTo(position: Long) {
        mediaPlayer?.seekTo(position.toInt())
        savedPosition = position
    }
}
