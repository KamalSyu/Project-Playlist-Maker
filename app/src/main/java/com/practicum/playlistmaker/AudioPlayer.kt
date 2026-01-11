
package com.practicum.playlistmaker

import Track
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


        val track = intent.getSerializableExtra("track") as Track
        val trackList = ArrayList<Track>().apply { add(track) } // Добавляем трек в список

        adapter = TrackAdapter(trackList, VIEW_TYPE_ALBUM, { track ->
        }, { track ->
            playbackControl(track) // Вызов метода для управления воспроизведением
        })
        recyclerView.adapter = adapter

        refreshAdapter()

        findViewById<TextView>(R.id.back).setOnClickListener {
            finish()
        }
    }


    override fun onPause() {
        super.onPause()
        pausePlayer()
        refreshAdapter()
    }

    override fun onDestroy() {
        super.onDestroy()
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
                startPlayer()  // автоматически начинает воспроизведение
                refreshAdapter()
                startUpdatingTime()  // Запускаем обновление времени
            }

            mediaPlayer.setOnCompletionListener {
                playerState = STATE_PREPARED
                refreshAdapter()
                stopUpdatingTime()  // Останавливаем обновление
            }

            mediaPlayer.setOnErrorListener { mp, what, extra ->
                Log.e("AudioPlayer", "Prepare error: what=$what, extra=$extra")
                playerState = STATE_DEFAULT
                refreshAdapter()
                stopUpdatingTime()  // Останавливаем обновление
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
        startUpdatingTime()  // Начинаем обновление времени
    }

    private fun pausePlayer() {
        mediaPlayer.pause()
        playerState = STATE_PAUSED
        refreshAdapter()
        stopUpdatingTime()  // Останавливаем обновление
    }

    private fun refreshAdapter() {
        // Передаём текущее время воспроизведения (в мс)
        adapter.notifyDataSetChangedWithState(
            playerState == STATE_PLAYING,
            mediaPlayer.currentPosition.toLong()
        )
    }
    private fun startUpdatingTime() {
        updateRunnable = Runnable {
            if (playerState == STATE_PLAYING) {
                refreshAdapter()  // Обновляем адаптер каждую секунду
                handler.postDelayed(updateRunnable!!, 1000)
            }
        }
        handler.post(updateRunnable!!)
    }

    private fun stopUpdatingTime() {
        handler.removeCallbacks(updateRunnable!!)
        updateRunnable = null
    }
}

//class AudioPlayer : AppCompatActivity() {
//
//    companion object {
//        private const val STATE_DEFAULT = 0
//        private const val STATE_PREPARED = 1
//        private const val STATE_PLAYING = 2
//        private const val STATE_PAUSED = 3
//    }
//
//    private var playerState = STATE_DEFAULT
//    private lateinit var mediaPlayer: MediaPlayer
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var adapter: TrackAdapter
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_audioplayer)
//
//        recyclerView = findViewById(R.id.recyclerViewAudioPlayer)
//        val layoutManager = LinearLayoutManager(this)
//        recyclerView.layoutManager = layoutManager
//        mediaPlayer = MediaPlayer()
//
//
//        val track = intent.getSerializableExtra("track") as Track
//        val trackList = ArrayList<Track>().apply { add(track) } // Добавляем трек в список
//
//        adapter = TrackAdapter(trackList, VIEW_TYPE_ALBUM, { track ->
//        }, { track ->
//            playbackControl(track) // Вызов метода для управления воспроизведением
//        })
//        recyclerView.adapter = adapter
//
//        refreshAdapter()
//
//        findViewById<TextView>(R.id.back).setOnClickListener {
//            finish()
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//        pausePlayer()
//        refreshAdapter()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        mediaPlayer.release()
//    }
//
//    private fun playbackControl(track: Track) {
//        when (playerState) {
//            STATE_PLAYING -> pausePlayer()
//            STATE_PREPARED, STATE_PAUSED -> startPlayer()
//            else -> preparePlayer(track.previewUrl)
//        }
//    }
//
//    private fun preparePlayer(url: String?) {
//        if (url.isNullOrEmpty()) {
//            Log.e("AudioPlayer", "URL is null or empty")
//            return
//        }
//
//        try {
//            mediaPlayer.reset()
//            mediaPlayer.setDataSource(url)
//            mediaPlayer.prepareAsync()
//
//            mediaPlayer.setOnPreparedListener {
//                playerState = STATE_PREPARED
//                startPlayer()  // автоматически начинает воспроизведение
//                refreshAdapter()
//            }
//
//            mediaPlayer.setOnCompletionListener {
//                playerState = STATE_PREPARED
//                refreshAdapter()
//            }
//
//            mediaPlayer.setOnErrorListener { mp, what, extra ->
//                Log.e("AudioPlayer", "Prepare error: what=$what, extra=$extra")
//                playerState = STATE_DEFAULT
//                refreshAdapter()
//                true
//            }
//
//        } catch (e: Exception) {
//            Log.e("AudioPlayer", "Failed to prepare player", e)
//            playerState = STATE_DEFAULT
//            refreshAdapter()
//        }
//    }
//
//    private fun startPlayer() {
//        mediaPlayer.start()
//        playerState = STATE_PLAYING
//        refreshAdapter()
//    }
//
//    private fun pausePlayer() {
//        mediaPlayer.pause()
//        playerState = STATE_PAUSED
//        refreshAdapter()
//    }
//
//    private fun refreshAdapter() {
//        adapter.notifyDataSetChangedWithState(playerState == STATE_PLAYING)
//    }
//}

