package com.practicum.playlistmaker.presentation.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.model.Track
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


    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapter
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audioplayer)

        // Получение трека из Intent
        val track = intent.getParcelableExtra<Track>("track")
            ?: throw IllegalArgumentException("Track cannot be null")


        // Настройка RecyclerView
        recyclerView = findViewById(R.id.recyclerViewAudioPlayer)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TrackAdapter(
            listOf(track),
            VIEW_TYPE_ALBUM,
            { /* onClick не используется */ },
            { togglePlayback() }  // Обработчик кнопки воспроизведения
        )
        recyclerView.adapter = adapter

        // Подготовка плеера к воспроизведению
        prepareAndPlay(track)
    }

    private fun prepareAndPlay(track: Track) {
        lifecycleScope.launch {
            try {
                preparePlaybackUseCase(track.previewUrl)  // invoke()
                isPlaying = true
                updateUI()
                startPolling()
            } catch (e: Exception) {
                isPlaying = false
                updateUI()
                // Можно показать ошибку пользователю
            }
        }
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
                // Обработка ошибки
            }
        }
    }

    private fun updateUI() {
        lifecycleScope.launch {
            val position = getCurrentPositionUseCase()
            adapter.notifyDataSetChangedWithState(isPlaying, position)
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

    private fun stopPolling() {
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
    }

    override fun onPause() {
        super.onPause()
        stopPolling()  // Останавливаем обновление UI
    }

    override fun onStop() {
        super.onStop()
        lifecycleScope.launch { stopPlaybackUseCase() }  // Останавливаем воспроизведение
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPolling()
    }
}
