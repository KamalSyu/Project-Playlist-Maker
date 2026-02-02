package com.practicum.playlistmaker.data.repository

import android.media.MediaPlayer
import android.util.Log
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


//@Singleton
//
//class PlayerRepositoryImpl @Inject constructor(): PlayerRepository {
//
//    private var mediaPlayer: MediaPlayer? = null // Экземпляр MediaPlayer для работы с аудио
//    private var completionListener: (() -> Unit)? = null  // Слушатель завершения воспроизведения
//
//    override suspend fun play() {
//        if (mediaPlayer?.isPlaying == false) {
//            mediaPlayer?.start()
//        }
//    }
//
//    override suspend fun pause() {
//        if (mediaPlayer?.isPlaying == true) {
//            mediaPlayer?.pause()
//        }
//    }
//
//    // Подготавливает MediaPlayer к воспроизведению по указанному URL.
//    override suspend fun prepare(url: String?) {
//        // Освобождаем старый плеер, если он есть
//        reset()
//
//        mediaPlayer = MediaPlayer().apply {
//            setDataSource(url)
//            prepare()
//            setOnCompletionListener {
//                completionListener?.invoke()
//            }
//        }
//    }
//
//
//    override suspend fun stop() {
//        mediaPlayer?.apply {
//            stop()
//            release()
//        }
//        mediaPlayer = null
//        completionListener = null
//    }
//
//    override suspend fun reset() {
//        withContext(Dispatchers.Main) {
//            mediaPlayer?.apply {
//                stop()
//                release()
//            }
//            mediaPlayer = null
//            // Явно сбрасываем слушатель
//            completionListener = null
//        }
//    }
//
//
//    override fun setOnCompletionListener(listener: () -> Unit) {
//        completionListener = listener
//        mediaPlayer?.setOnCompletionListener { listener() }
//    }
//
//    override fun isPlaying(): Boolean {
//        val result = mediaPlayer?.isPlaying ?: false
//        Log.d("PlayerRepository", "isPlaying: $result (mediaPlayer=${mediaPlayer != null})")
//        return result
//    }
//    override fun getCurrentPosition(): Long = mediaPlayer?.currentPosition?.toLong() ?: 0L
//}
