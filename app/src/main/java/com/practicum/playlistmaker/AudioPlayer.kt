
package com.practicum.playlistmaker

import Track
import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.Constants.Companion.VIEW_TYPE_ALBUM
import java.io.IOException

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
            }

            mediaPlayer.setOnCompletionListener {
                playerState = STATE_PREPARED
                refreshAdapter()
            }

            mediaPlayer.setOnErrorListener { mp, what, extra ->
                Log.e("AudioPlayer", "Prepare error: what=$what, extra=$extra")
                playerState = STATE_DEFAULT
                refreshAdapter()
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
    }

    private fun pausePlayer() {
        mediaPlayer.pause()
        playerState = STATE_PAUSED
        refreshAdapter()
    }

    private fun refreshAdapter() {
        adapter.notifyDataSetChangedWithState(playerState == STATE_PLAYING)
    }
}


//class AudioPlayer: AppCompatActivity()  {
//    @SuppressLint("MissingInflatedId")
//
//    companion object {
//        private const val STATE_DEFAULT = 0
//        private const val STATE_PREPARED = 1
//        private const val STATE_PLAYING = 2
//        private const val STATE_PAUSED = 3
//    }
//
//    private var playerState = STATE_DEFAULT
//    private var mediaPlayer = MediaPlayer()
//
//    private lateinit var recyclerView: RecyclerView
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_audioplayer)
//
//        recyclerView = findViewById(R.id.recyclerViewAudioPlayer)
//
//        mediaPlayer = MediaPlayer()
//
//        mediaPlayer.setOnPreparedListener {
//            // После подготовки автоматически начинаем воспроизведение
//            mediaPlayer.start()
//            playerState = STATE_PLAYING
//            // Обновите UI при необходимости
//        }
//
//        mediaPlayer.setOnCompletionListener {
//            // Когда трек завершен, обновляем состояние
//            playerState = STATE_PREPARED
//            // Обновите UI при необходимости
//        }
//
//        val track = intent.getSerializableExtra("track") as Track
//        val trackList = ArrayList<Track>().apply { add(track) } // Добавляем трек в список
//
//        val adapter = TrackAdapter(trackList, VIEW_TYPE_ALBUM, { track ->
//            Log.d("AudioPlayer", "Clicked track with previewUrl: ${track.previewUrl}")
//
//            // Логика обработки клика по треку
//            preparePlayer(track.previewUrl)
//            startPlayer()
//        }, { track ->
//            if (playerState == STATE_PLAYING) {
//                pausePlayer()
//            } else {
//                startPlayer()
//            }
//        })
//
//        recyclerView.adapter = adapter
//
//        findViewById<TextView>(R.id.back).setOnClickListener {
//            finish()
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//        pausePlayer()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        mediaPlayer.release()
//    }
//
//    private fun startPlayer() {
//        try {
//            mediaPlayer.start()
//            // Обновляем UI для отображения состояния воспроизведения
//            playerState = STATE_PLAYING
//            findViewById<ImageButton>(R.id.ic_play_button).setImageResource(R.drawable.ic_pause_button)
//        } catch (e: Exception) {
//            Log.e("AudioPlayer", "Error starting player", e)
//        }
//    }
//
//    private fun pausePlayer() {
//        try {
//            mediaPlayer.pause()
//            // Обновляем UI для отображения состояния паузы
//            playerState = STATE_PAUSED
//            findViewById<ImageButton>(R.id.ic_play_button).setImageResource(R.drawable.ic_play_button)
//        } catch (e: Exception) {
//            Log.e("AudioPlayer", "Error pausing player", e)
//        }
//    }
//
//    private fun preparePlayer(url: String?) {
//        Log.d("AudioPlayer", "Preparing player with URL: $url")
//        if (url.isNullOrEmpty()) return
//        mediaPlayer.reset()
//        try {
//            mediaPlayer.setDataSource(url)
//            mediaPlayer.prepareAsync()
//
//            // Добавляем слушатель для события готовности
//            mediaPlayer.setOnPreparedListener {
//                playerState = STATE_PREPARED
//                startPlayer() // Начинаем воспроизведение после подготовки
//            }
//
//            // Добавляем слушатель для события завершения воспроизведения
//            mediaPlayer.setOnCompletionListener {
//                // Меняем иконку на кнопке воспроизведения
//                playerState = STATE_PREPARED
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//            Log.e("AudioPlayer", "Error setting data source: $url", e)
//
//        }
//    }
//}

