package com.practicum.playlistmaker.presentation.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.data.repository.PlayerRepositoryImpl
import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.domain.repository.PlayerRepository
import com.practicum.playlistmaker.domain.usecase.*
import com.practicum.playlistmaker.presentation.adapter.TrackAdapter
import com.practicum.playlistmaker.presentation.util.Constants.Companion.VIEW_TYPE_ALBUM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AudioPlayerActivity : AppCompatActivity() {

    // Внедряем UseCase‑ы через Hilt
    @Inject lateinit var preparePlaybackUseCase: PreparePlaybackUseCase
    @Inject lateinit var togglePlaybackUseCase: TogglePlaybackUseCase
    @Inject lateinit var stopPlaybackUseCase: StopPlaybackUseCase
    @Inject lateinit var getCurrentPositionUseCase: GetCurrentPositionUseCase


    private lateinit var recyclerViewAudioPlayer: RecyclerView
    private lateinit var playerRepository: PlayerRepository
    private lateinit var adapter: TrackAdapter
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audioplayer)

        playerRepository = PlayerRepositoryImpl()

        val track = savedInstanceState?.getParcelable<Track>("track")
            ?: intent.getParcelableExtra<Track>("track")
            ?: throw IllegalArgumentException("Track cannot be null")
        isPlaying = savedInstanceState?.getBoolean("isPlaying") ?: false

        setupRecyclerView(track)
        if (savedInstanceState == null) {
            prepareAndPlay(track)
        } else {
            updateUI()  // Обновляем UI с текущим isPlaying
            if (isPlaying) startPolling() else stopPolling()
        }

        setupPlaybackListener()

        val backButton = findViewById<TextView>(R.id.back)

        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("track", intent.getParcelableExtra<Track>("track"))
        outState.putBoolean("isPlaying", isPlaying)
    }

    private fun setupRecyclerView(track: Track) {
        recyclerViewAudioPlayer = findViewById(R.id.recyclerViewAudioPlayer)

        recyclerViewAudioPlayer.layoutManager = LinearLayoutManager(this)

        adapter = TrackAdapter(
            tracks = listOf(track),
            viewType = VIEW_TYPE_ALBUM,
            onTrackClick = { },
            onClickPlayButton = { togglePlayback() },
            onAddToPlaylist = { },
            onFavorite = {}
        )
        recyclerViewAudioPlayer.adapter = adapter

        Log.d("AudioPlayerActivity", "RecyclerView инициализирован с адаптером")
    }
    private fun prepareAndPlay(track: Track) {
        lifecycleScope.launch {
            try {// Проверяем наличие previewUrl
                if (track.previewUrl.isNullOrBlank()) {
                    showError("Отрывок недоступен")
                    return@launch
                }
                preparePlaybackUseCase(track.previewUrl)
                updateUI()
                startPolling()
            } catch (e: Exception) {
                isPlaying = false
                updateUI()
                showError("Не удалось воспроизвести отрывок")
            }
        }
    }
    // Метод для отображения ошибки (можно заменить на диалог/Snackbar)
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.e("AudioPlayerActivity", "Ошибка воспроизведения: $message")
    }

    private fun togglePlayback() {
        lifecycleScope.launch {
            try {
                val result = togglePlaybackUseCase()  // invoke()
                if (result.isSuccess) {
                    isPlaying = result.getOrThrow()
                    updateUI()
                    if (isPlaying) startPolling() else stopPolling()
                }
            } catch (e: Exception) {
                isPlaying = false
                updateUI()
                stopPolling()
            }
        }
    }

    private fun updateUI() {
        lifecycleScope.launch {
            val currentPosition = getCurrentPositionUseCase()
            adapter.notifyDataSetChangedWithState(isPlaying, currentPosition)
        }
    }

    private fun startPolling() {
        updateRunnable = Runnable {
            if (isPlaying) {
                updateUI()
                handler.postDelayed(updateRunnable!!, 1000)
            }
        }
        handler.post(updateRunnable!!)
    }
    private fun setupPlaybackListener() {
        lifecycleScope.launch {
            playerRepository.setOnCompletionListener {
                lifecycleScope.launch {
//                    playerRepository.reset()
                    isPlaying = false
                    updateUI()
//                    adapter.notifyDataSetChangedWithState(isPlaying, 0)
                    stopPolling()
                    playerRepository.reset()

                }
            }
        }
    }


    private fun stopPolling() {
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
    }

    override fun onPause() {
        super.onPause()
        stopPolling()
    }

    override fun onStop() {
        super.onStop()
        lifecycleScope.launch { stopPlaybackUseCase() }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPolling()
        lifecycleScope.launch {
            playerRepository.setOnCompletionListener {}  // Обнуляем слушатель
        }
    }

    override fun onBackPressed() {
        stopPlaybackAndCleanup()
        super.onBackPressed()
    }

    private fun stopPlaybackAndCleanup() {
        lifecycleScope.launch {
            stopPlaybackUseCase()
        }
        stopPolling()
    }
}
