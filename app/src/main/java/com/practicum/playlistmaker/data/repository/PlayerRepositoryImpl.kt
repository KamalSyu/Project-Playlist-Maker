package com.practicum.playlistmaker.data.repository

import android.media.MediaPlayer
import com.practicum.playlistmaker.domain.repository.PlayerRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepositoryImpl @Inject constructor() : PlayerRepository {

    private var mediaPlayer: MediaPlayer? = null
    private var completionListener: (() -> Unit)? = null
    private val lock = Any()

    override suspend fun prepare(url: String?) {
        synchronized(lock) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                prepare()
                setOnCompletionListener {
                    completionListener?.invoke()
                }
            }
        }
    }

    override suspend fun play() {
        synchronized(lock) {
            mediaPlayer?.start()
        }
    }

    override suspend fun pause() {
        synchronized(lock) {
            mediaPlayer?.pause()
        }
    }

    override suspend fun stop() {
        synchronized(lock) {
            mediaPlayer?.pause()
        }
    }

    override suspend fun reset() {
        synchronized(lock) {
            mediaPlayer?.release()
            mediaPlayer = null
            completionListener = null
        }
    }

    override fun isPlaying(): Boolean {
        return synchronized(lock) {
            mediaPlayer?.isPlaying ?: false
        }
    }

    override fun getCurrentPosition(): Long {
        return synchronized(lock) {
            mediaPlayer?.currentPosition?.toLong() ?: 0L
        }
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        synchronized(lock) {
            completionListener = listener
            mediaPlayer?.setOnCompletionListener { listener() }
        }
    }
    override fun seekTo(position: Long) {
        synchronized(lock) {
            mediaPlayer?.seekTo(position.toInt())
        }
    }

}
