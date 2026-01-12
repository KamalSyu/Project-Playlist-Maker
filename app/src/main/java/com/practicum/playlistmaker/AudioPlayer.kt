
package com.practicum.playlistmaker


import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.Constants.Companion.VIEW_TYPE_ALBUM

class AudioPlayer : AppCompatActivity() {

    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
    }

    private var playerState = STATE_DEFAULT
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapter
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audioplayer)

        recyclerView = findViewById(R.id.recyclerViewAudioPlayer)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        mediaPlayer = MediaPlayer()

        val track = intent.getParcelableExtra<Track>("track")
            ?: throw IllegalArgumentException("Track cannot be null")
//        val trackList = ArrayList<Track>().apply { add(track) }
        val trackList = mutableListOf(track)

        adapter = TrackAdapter(trackList, VIEW_TYPE_ALBUM, { track ->
        }, { track ->
            playbackControl(track)
        })
        recyclerView.adapter = adapter

        refreshAdapter()

        findViewById<TextView>(R.id.back).setOnClickListener {
            stopPlaybackAndFinish()  // Новый метод — гарантированно останавливает и закрывает
        }
    }

    override fun onPause() {
        super.onPause()
        // Не останавливаем плеер здесь — пусть работает в фоне (если нужно)
        // Но обновляем UI
        refreshAdapter()
    }

    override fun onStop() {
        super.onStop()
        // При уходе с экрана — останавливаем воспроизведение
        stopPlayback()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()  // Гарантированное освобождение
    }

    // Основной метод для кнопки «назад»
    private fun stopPlaybackAndFinish() {
        stopPlayback()
        finish()  // Закрываем Activity
    }

    // Останавливает воспроизведение и освобождает ресурсы
    private fun stopPlayback() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        playerState = STATE_DEFAULT
        refreshAdapter()
        stopUpdatingTime()
    }

    // Полностью освобождает MediaPlayer
    private fun releasePlayer() {
        mediaPlayer.reset()
        mediaPlayer.release()
    }

    private fun playbackControl(track: Track) {
        when (playerState) {
            STATE_PLAYING -> pausePlayer()
            STATE_PREPARED, STATE_PAUSED -> startPlayer()
            else -> preparePlayer(track.previewUrl)
        }
    }

    private fun preparePlayer(url: String?) {
        if (url.isNullOrEmpty()) {
            Log.e("AudioPlayer", "URL is null or empty")
            return
        }

        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepareAsync()

            mediaPlayer.setOnPreparedListener {
                playerState = STATE_PREPARED
                startPlayer()
                refreshAdapter()
                startUpdatingTime()
            }

            mediaPlayer.setOnCompletionListener {
                playerState = STATE_PREPARED
                refreshAdapter()
                stopUpdatingTime()
                adapter.notifyDataSetChangedWithState(playerState == STATE_PLAYING, 0)
            }

            mediaPlayer.setOnErrorListener { mp, what, extra ->
                Log.e("AudioPlayer", "Prepare error: what=$what, extra=$extra")
                playerState = STATE_DEFAULT
                refreshAdapter()
                stopUpdatingTime()
                true
            }

        } catch (e: Exception) {
            Log.e("AudioPlayer", "Failed to prepare player", e)
            playerState = STATE_DEFAULT
            refreshAdapter()
        }
    }

    private fun startPlayer() {
        mediaPlayer.start()
        playerState = STATE_PLAYING
        refreshAdapter()
        startUpdatingTime()
    }

    private fun pausePlayer() {
        mediaPlayer.pause()
        playerState = STATE_PAUSED
        refreshAdapter()
        stopUpdatingTime()
    }

    private fun refreshAdapter() {
        adapter.notifyDataSetChangedWithState(
            playerState == STATE_PLAYING,
            mediaPlayer.currentPosition.toLong()
        )
    }

    private fun startUpdatingTime() {
        updateRunnable = Runnable {
            if (playerState == STATE_PLAYING) {
                refreshAdapter()
                handler.postDelayed(updateRunnable!!, 1000)
            }
        }
        handler.post(updateRunnable!!)
    }

    private fun stopUpdatingTime() {
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
        val currentPosition = mediaPlayer.currentPosition.toLong()
        adapter.notifyDataSetChangedWithState(playerState == STATE_PLAYING, currentPosition)
    }
}


