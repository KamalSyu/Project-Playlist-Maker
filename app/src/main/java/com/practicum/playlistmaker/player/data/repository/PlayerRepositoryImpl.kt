package com.practicum.playlistmaker.player.data.repository

import android.media.MediaPlayer
import android.util.Log
import com.practicum.playlistmaker.player.domain.repository.PlayerRepository

class PlayerRepositoryImpl () : PlayerRepository {

    private var mediaPlayer: MediaPlayer? = null
    private var savedPosition: Long = 0L
    private var isPrepared: Boolean = false
    private var completionListener: (() -> Unit)? = null

    override suspend fun prepare(url: String?) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                setOnErrorListener { mp, what, extra ->
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
    }

    override suspend fun pause() {
        mediaPlayer?.pause()
        mediaPlayer?.currentPosition?.toLong()?.let { savedPosition = it }
    }

    override suspend fun stop() {
        mediaPlayer?.pause()
        mediaPlayer?.currentPosition?.toLong()?.let { savedPosition = it }
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
