package com.practicum.playlistmaker.data.repository

import android.media.MediaPlayer
import com.practicum.playlistmaker.domain.repository.PlayerRepository

class PlayerRepositoryImpl : PlayerRepository {
    private var mediaPlayer: MediaPlayer? = null

    override suspend fun prepare(url: String?) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepare() // prepare() вместо prepareAsync() для простоты (можно заменить)
        }
    }

    override suspend fun play() { mediaPlayer?.start() }
    override suspend fun pause() { mediaPlayer?.pause() }
    override suspend fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false
    override fun getCurrentPosition(): Long = mediaPlayer?.currentPosition?.toLong() ?: 0L
}
