package com.practicum.playlistmaker.player.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.constants.Constants
import com.practicum.playlistmaker.core.models.PlaybackState
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.usecase.UseCaseCreator
import com.practicum.playlistmaker.search.data.mapper.DtoMapper
import com.practicum.playlistmaker.search.ui.adapter.TrackAdapter
import com.practicum.playlistmaker.search.ui.parcel.ParcelableTrack
import com.practicum.playlistmaker.search.ui.viewholder.AlbumViewHolder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AudioPlayerActivity : AppCompatActivity() {

    private val viewModel: AudioPlayerViewModel by viewModels()

    @Inject
    lateinit var useCaseCreator: UseCaseCreator
    @Inject
    lateinit var dtoMapper: DtoMapper

    private lateinit var recyclerViewAudioPlayer: RecyclerView
    private lateinit var adapter: TrackAdapter
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audioplayer)

        setupRecyclerView(getTrackFromIntent())
        setupBackButton()

        viewModel.uiState.observe(this) { state ->
            updateUI(state)
        }

        // Добавляем установку слушателя завершения
        viewModel.setupPlaybackCompletionListener()

        if (savedInstanceState == null) {
            val track = getTrackFromIntent()
            viewModel.initPlayback(track.previewUrl)
            // Не запускаем воспроизведение автоматически — ждём нажатия кнопки
        } else {
            val isPlaying = savedInstanceState.getBoolean("isPlaying")
            val savedPosition = savedInstanceState.getLong("savedPosition")
            viewModel.restorePlaybackState(isPlaying, savedPosition)
            if (isPlaying) {
                startPolling()
            }
        }
    }

    private fun getTrackFromIntent(): Track {
        val parcelable = intent.getParcelableExtra<ParcelableTrack>("track")
        if (parcelable != null) {
            Log.d("AudioPlayer", "Track received from Intent")
            return dtoMapper.toDomain(parcelable)
        }
        throw IllegalArgumentException("Track is required but not provided.")
    }

    private fun setupBackButton() {
        findViewById<TextView>(R.id.back).setOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView(track: Track) {
        recyclerViewAudioPlayer = findViewById(R.id.recyclerViewAudioPlayer)
        recyclerViewAudioPlayer.layoutManager = LinearLayoutManager(this)
        adapter = TrackAdapter(
            tracks = listOf(track),
            viewType = Constants.Companion.VIEW_TYPE_ALBUM,
            onTrackClick = {},
            onClickPlayButton = { _ -> togglePlayback() },
            onAddToPlaylist = {},
            onFavorite = {},
            formatDurationUseCase = viewModel.formatTrackDurationUseCase
        )
        recyclerViewAudioPlayer.adapter = adapter
    }

    private fun togglePlayback() {
        val currentState = viewModel.uiState.value?.playbackState ?: PlaybackState(false, 0L)
        val resumePosition = when {
            !currentState.isPlaying && currentState.position > 0L -> currentState.position
            else -> null
        }
        viewModel.togglePlayback(resumePosition)
    }

    private fun stopPlaybackAndCleanup() {
        viewModel.stopPlayback()
        viewModel.resetPlaybackState() // Гарантированный сброс состояния при закрытии
    }

    private fun updateUI(state: PlayerUiState) {
        if (state.error != null) {
            handlePlaybackError("Ошибка воспроизведения", state.error!!)
            viewModel.clearError()
            return
        }

        adapter.notifyDataSetChangedWithState(
            isPlaying = state.playbackState.isPlaying,
            currentTimeMillis = state.playbackState.position,
            position = 0,
            formattedTime = state.formattedTime
        )

        val viewHolder = recyclerViewAudioPlayer.findViewHolderForAdapterPosition(0) as? AlbumViewHolder
        viewHolder?.updatePlayButtonState(state.playbackState.isPlaying)

        if (state.shouldPoll && state.playbackState.isPlaying) {
            startPolling()
        } else {
            stopPolling()
        }
    }

    private fun startPolling() {
        stopPolling() // Останавливаем предыдущий polling

        updateRunnable = Runnable {
            viewModel.updateCurrentPosition()

            // Проверяем текущее состояние — если воспроизведение остановлено, останавливаем polling
            val currentState = viewModel.uiState.value?.playbackState ?: PlaybackState(false, 0L)
            if (currentState.isPlaying) {
                // Запускаем следующий опрос через 1 секунду
                handler.postDelayed(updateRunnable!!, 1000)
            } else {
                stopPolling()
            }
        }

        // Запускаем первый опрос сразу
        handler.post(updateRunnable!!)
    }

    private fun stopPolling() {
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val currentState = viewModel.uiState.value?.playbackState ?: PlaybackState(false, 0L)
        outState.putBoolean("isPlaying", currentState.isPlaying)
        outState.putLong("savedPosition", currentState.position)
    }

    override fun onPause() {
        super.onPause()
        val currentState = viewModel.uiState.value?.playbackState ?: PlaybackState(false, 0L)
        if (currentState.isPlaying) {
            viewModel.saveCurrentPosition() // Сохраняем текущую позицию
            viewModel.stopPlayback() // Останавливаем воспроизведение
        }
        stopPolling()
    }

    override fun onStop() {
        super.onStop()
        stopPolling()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPolling()
        handler.removeCallbacksAndMessages(null) // Полная очистка Handler
    }

    override fun onResume() {
        super.onResume()
        // Всегда обновляем UI из актуального состояния ViewModel
        val currentState = viewModel.uiState.value ?: return
        updateUI(currentState)

        // Перезапускаем polling, если воспроизведение активно
        if (currentState.playbackState.isPlaying) {
            startPolling()
        }
    }

    override fun onBackPressed() {
        viewModel.resetPlaybackToStart()  // Делегируем всю логику ViewModel
        super.onBackPressed()
    }


    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.e("AudioPlayerActivity", "Ошибка: $message")
    }

    private fun handlePlaybackError(message: String, e: Throwable) {
        Log.e("AudioPlayerActivity", message, e)
        showError(message)
    }
}
