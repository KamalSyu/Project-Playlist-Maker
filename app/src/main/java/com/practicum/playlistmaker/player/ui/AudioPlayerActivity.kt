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
import com.practicum.playlistmaker.search.data.mapper.DtoMapper
import com.practicum.playlistmaker.search.ui.adapter.TrackAdapter
import com.practicum.playlistmaker.search.ui.parcel.ParcelableTrack
import com.practicum.playlistmaker.search.ui.viewholder.AlbumViewHolder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * Экран аудиоплеера: воспроизведение трека, отображение прогресса, управление воспроизведением.
 */
@AndroidEntryPoint
class AudioPlayerActivity : AppCompatActivity() {

    private val viewModel: AudioPlayerViewModel by viewModels()

    @Inject
    lateinit var dtoMapper: DtoMapper

    // UI-компоненты
    private lateinit var recyclerViewAudioPlayer: RecyclerView
    private lateinit var adapter: TrackAdapter

    // Для периодического обновления прогресса воспроизведения
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audioplayer)

        val track = getTrackFromIntent() // Получаем трек из интента
        setupRecyclerView(track) // Настраиваем RecyclerView с треком
        setupBackButton() // Кнопка «Назад»

        // Подписываемся на изменения состояния UI
        viewModel.uiState.observe(this) { state ->
            updateUI(state)
        }

        viewModel.setupPlaybackCompletionListener() // Слушатель завершения воспроизведения

        if (savedInstanceState == null) {
            // Первое открытие: инициализируем воспроизведение
            viewModel.initPlayback(track.previewUrl)
        } else {
            // Восстановление состояния после поворота экрана
            val isPlaying = savedInstanceState.getBoolean("isPlaying")
            val savedPosition = savedInstanceState.getLong("savedPosition")
            viewModel.restorePlaybackState(isPlaying, savedPosition)
            if (isPlaying) startPolling() // Запускаем обновление прогресса
        }
    }

    /** Получаем трек из интента и преобразуем в доменную модель */
    private fun getTrackFromIntent(): Track {
        val parcelable = intent.getParcelableExtra<ParcelableTrack>("track")
        return if (parcelable != null) {
            Log.d("AudioPlayer", "Track received from Intent")
            dtoMapper.toDomain(parcelable)
        } else {
            throw IllegalArgumentException("Track is required but not provided.")
        }
    }

    /** Настройка кнопки «Назад» */
    private fun setupBackButton() {
        findViewById<TextView>(R.id.back).setOnClickListener { onBackPressed() }
    }

    /** Инициализация RecyclerView с адаптером */
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

    /** Переключение воспроизведения (старт/пауза) */
    private fun togglePlayback() {
        val currentState = viewModel.uiState.value?.playbackState ?: PlaybackState(false, 0L)
        val resumePosition = if (!currentState.isPlaying && currentState.position > 0L) currentState.position else null
        viewModel.togglePlayback(resumePosition)
    }

    /** Обновление UI на основе текущего состояния */
    private fun updateUI(state: PlayerUiState) {
        if (state.error != null) {
            handlePlaybackError("Ошибка воспроизведения", state.error!!)
            viewModel.clearError()
            return
        }

        // Обновляем адаптер и кнопку воспроизведения
        adapter.notifyDataSetChangedWithState(
            isPlaying = state.playbackState.isPlaying,
            currentTimeMillis = state.playbackState.position,
            position = 0,
            formattedTime = state.formattedTime
        )

        val viewHolder = recyclerViewAudioPlayer.findViewHolderForAdapterPosition(0) as? AlbumViewHolder
        viewHolder?.updatePlayButtonState(state.playbackState.isPlaying)

        // Запускаем/останавливаем опрос прогресса
        if (state.shouldPoll && state.playbackState.isPlaying) {
            startPolling()
        } else {
            stopPolling()
        }
    }

    /** Запуск периодического опроса прогресса воспроизведения (каждую секунду) */
    private fun startPolling() {
        stopPolling() // Останавливаем предыдущий polling

        updateRunnable = Runnable {
            viewModel.updateCurrentPosition()

            val currentState = viewModel.uiState.value?.playbackState ?: PlaybackState(false, 0L)
            if (currentState.isPlaying) {
                handler.postDelayed(updateRunnable!!, 1000) // Следующий опрос через 1 с
            } else {
                stopPolling()
            }
        }
        handler.post(updateRunnable!!) // Первый опрос сразу
    }

    /** Останавливаем опрос прогресса */
    private fun stopPolling() {
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
    }

    /** Сохраняем состояние при повороте экрана */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val currentState = viewModel.uiState.value?.playbackState ?: PlaybackState(false, 0L)
        outState.putBoolean("isPlaying", currentState.isPlaying)
        outState.putLong("savedPosition", currentState.position)
    }

    /** Приостанавливаем воспроизведение при уходе в фон */
    override fun onPause() {
        super.onPause()
        val currentState = viewModel.uiState.value?.playbackState ?: PlaybackState(false, 0L)
        if (currentState.isPlaying) {
            viewModel.saveCurrentPosition() // Сохраняем позицию
            viewModel.stopPlayback() // Останавливаем
        }
        stopPolling()
    }

    /** Очистка при остановке */
    override fun onStop() {
        super.onStop()
        stopPolling()
    }

    /** Полная очистка ресурсов при уничтожении */
    override fun onDestroy() {
        super.onDestroy()
        stopPolling()
        handler.removeCallbacksAndMessages(null)
    }

    /** Обновляем UI при возврате на экран */
    override fun onResume() {
        super.onResume()
        val currentState = viewModel.uiState.value ?: return
        updateUI(currentState)
        if (currentState.playbackState.isPlaying) startPolling()
    }

    /** Обработка нажатия кнопки «Назад»: сбрасываем воспроизведение */
    override fun onBackPressed() {
        viewModel.resetPlaybackToStart()
        super.onBackPressed()
    }

    /** Показать Toast с ошибкой */
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.e("AudioPlayerActivity", "Ошибка: $message")
    }

    /** Логируем и показываем ошибку воспроизведения */
    private fun handlePlaybackError(message: String, e: Throwable) {
        Log.e("AudioPlayerActivity", message, e)
        showError(message)
    }
}