
package com.practicum.playlistmaker

import Track
import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.Constants.Companion.VIEW_TYPE_ALBUM
import java.io.IOException

class AudioPlayer: AppCompatActivity()  {
    @SuppressLint("MissingInflatedId")
    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
    }

    private lateinit var recyclerView: RecyclerView
    private var mediaPlayer = MediaPlayer()
    private var playerState = STATE_DEFAULT


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audioplayer)

        recyclerView = findViewById(R.id.recyclerViewAudioPlayer)

        mediaPlayer = MediaPlayer()

        mediaPlayer.setOnPreparedListener {
            // После подготовки автоматически начинаем воспроизведение
            mediaPlayer.start()
            playerState = STATE_PLAYING
            // Обновите UI при необходимости
        }

        mediaPlayer.setOnCompletionListener {
            // Когда трек завершен, обновляем состояние
            playerState = STATE_PREPARED
            // Обновите UI при необходимости
        }

        val track = intent.getSerializableExtra("track") as Track
        val trackList = ArrayList<Track>().apply { add(track) } // Добавляем трек в список

        val adapter = TrackAdapter(trackList, VIEW_TYPE_ALBUM) { track ->
            playTrack(track.previewUrl)

        }
        recyclerView.adapter = adapter


        findViewById<TextView>(R.id.back).setOnClickListener {
            finish()
        }
    }
    private fun preparePlayer(url: String?) {
        if (url.isNullOrEmpty()) return
        mediaPlayer.reset()
        try {
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepareAsync()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    // Функция воспроизведения трека
    private fun playTrack(previewUrl: String?) {
        if (previewUrl.isNullOrEmpty()) return

        when (playerState) {
            STATE_DEFAULT -> {
                preparePlayer(previewUrl)
            }
            STATE_PREPARED -> {
                mediaPlayer.start()
                playerState = STATE_PLAYING
            }
            STATE_PAUSED -> {
                mediaPlayer.start()
                playerState = STATE_PLAYING
            }
            STATE_PLAYING -> {
                mediaPlayer.pause()
                playerState = STATE_PAUSED
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
    }

}

