package com.practicum.playlistmaker.data.repository

import android.media.MediaPlayer
import android.util.Log
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
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(url)
                    Log.d("PlayerRepository", "setDataSource: $url")

                    setOnErrorListener { mp, what, extra ->
                        Log.e("PlayerRepository", "MediaPlayer error: what=$what, extra=$extra")
                        false
                    }

                    prepareAsync()
                    Log.d("PlayerRepository", "prepareAsync() called")

                    setOnCompletionListener {
                        completionListener?.invoke()
                    }
                }
            } catch (e: Exception) {
                Log.e("PlayerRepository", "Prepare failed", e)
                mediaPlayer?.release()
                mediaPlayer = null
            }
        }
    }



    override suspend fun play() {
        synchronized(lock) {
            mediaPlayer?.start()?.also {
                Log.d("PlayerRepository", "play() called, isPlaying=${mediaPlayer?.isPlaying}")
            } ?: Log.e("PlayerRepository", "MediaPlayer is null in play()")
        }
    }

    override suspend fun pause() {
        synchronized(lock) {
            mediaPlayer?.pause()
        }
    }

    override suspend fun stop() {
        synchronized(lock) {
            mediaPlayer?.reset()
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
