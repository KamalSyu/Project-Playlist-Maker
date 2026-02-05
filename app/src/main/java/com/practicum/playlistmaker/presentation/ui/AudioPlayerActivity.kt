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
import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.domain.repository.PlayerRepository
import com.practicum.playlistmaker.domain.usecase.*
import com.practicum.playlistmaker.presentation.adapter.TrackAdapter
import com.practicum.playlistmaker.presentation.parcel.ParcelableTrack
import com.practicum.playlistmaker.presentation.parcel.toDomain
import com.practicum.playlistmaker.utils.Constants.Companion.VIEW_TYPE_ALBUM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AudioPlayerActivity : AppCompatActivity() {

    @Inject
    lateinit var useCaseCreator: UseCaseCreator

    @Inject
    lateinit var playerRepository: PlayerRepository

    private lateinit var recyclerViewAudioPlayer: RecyclerView
    private lateinit var adapter: TrackAdapter
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    private var isPlaying = false
    private var wasPausedInBackground = false
    private var savedPosition: Long = 0L  // Сохраняем позицию при паузе

    // Use Cases через Creator (все — интерфейсы)
    private lateinit var preparePlaybackUseCase: PreparePlaybackUseCaseContract
    private lateinit var togglePlaybackUseCase: TogglePlaybackUseCaseContract
    private lateinit var stopPlaybackUseCase: StopPlaybackUseCaseContract
    private lateinit var getCurrentPositionUseCase: GetCurrentPositionUseCaseContract
    private lateinit var handleCompletionUseCase: HandlePlaybackCompletionUseCaseContract
    private lateinit var formatTrackDurationUseCase: FormatTrackDurationUseCaseContract

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audioplayer)

        // Инициализация Use Cases через интерфейсы
        preparePlaybackUseCase = useCaseCreator.createPreparePlaybackUseCase()
        togglePlaybackUseCase = useCaseCreator.createTogglePlaybackUseCase()
        stopPlaybackUseCase = useCaseCreator.createStopPlaybackUseCase()
        getCurrentPositionUseCase = useCaseCreator.createGetCurrentPositionUseCase()
        handleCompletionUseCase = useCaseCreator.createHandlePlaybackCompletionUseCase()
        formatTrackDurationUseCase = useCaseCreator.createFormatTrackDurationUseCase()

        val track = getTrackFromIntentOrSavedState(savedInstanceState)

        // 1. Создаём RecyclerView и адаптер
        setupRecyclerView(track)

        // Устанавливаем isPlaying = false при старте (если нет savedInstanceState)
        if (savedInstanceState == null) {
            isPlaying = false
            prepareAndPlay(track)
        } else {
            // Восстанавливаем isPlaying из savedInstanceState
            isPlaying = savedInstanceState.getBoolean("isPlaying", false)
            updateUI()
            if (isPlaying) startPolling() else stopPolling()
        }

        setupPlaybackListener()
        setupBackButton()
    }

    private fun getTrackFromIntentOrSavedState(savedState: Bundle?): Track {
        if (savedState != null) {
            val parcelable = savedState.getParcelable<ParcelableTrack>("track")
            if (parcelable != null) {
                Log.d("AudioPlayer", "Track restored from savedInstanceState")
                return parcelable.toDomain()
            }
        }

        val parcelable = intent.getParcelableExtra<ParcelableTrack>("track")
        if (parcelable != null) {
            Log.d("AudioPlayer", "Track received from Intent")
            return parcelable.toDomain()
        }

        throw IllegalArgumentException(
            "Track is required but not provided. Check: " +
                    "1) Intent extra, 2) savedInstanceState, 3) Parcelable implementation"
        )
    }

    private fun setupBackButton() {
        findViewById<TextView>(R.id.back).setOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView(track: Track) {
        recyclerViewAudioPlayer = findViewById(R.id.recyclerViewAudioPlayer)
        recyclerViewAudioPlayer.layoutManager = LinearLayoutManager(this)


        adapter = TrackAdapter(
            tracks = listOf(track),
            viewType = VIEW_TYPE_ALBUM,
            onTrackClick = {},
            onClickPlayButton = { togglePlayback() },
            onAddToPlaylist = {},
            onFavorite = {},
            formatDurationUseCase = formatTrackDurationUseCase
        )
        recyclerViewAudioPlayer.adapter = adapter


        // Обновляем UI после создания адаптера
        updateUI()
    }

    private fun prepareAndPlay(track: Track) = lifecycleScope.launch {
        try {
            if (track.previewUrl.isNullOrBlank()) {
                showError("Отрывок недоступен")
                return@launch
            }
            preparePlaybackUseCase(track.previewUrl)
            // НЕ устанавливаем isPlaying = true здесь!
            // Воспроизведение начнётся только после явного вызова play()
            updateUI() // Теперь покажет кнопку «Играть»
        } catch (e: Exception) {
            handlePlaybackError("Не удалось воспроизвести отрывок", e)
            isPlaying = false
            updateUI()
        }
    }

    private fun togglePlayback() = lifecycleScope.launch {
        try {
            if (playerRepository.isPlaying()) {
                // Пауза: сохраняем текущую позицию
                savedPosition = getCurrentPositionUseCase()
                playerRepository.pause()
                isPlaying = false
                stopPolling()
            } else {
                // Воспроизведение: если есть сохранённая позиция — продолжаем с неё
                if (savedPosition > 0L) {
                    playerRepository.seekTo(savedPosition)
                }
                playerRepository.play()
                isPlaying = true
                startPolling()
            }
            updateUI()
        } catch (e: Exception) {
            handlePlaybackError("Ошибка при переключении воспроизведения", e)
        }
    }

    private fun updateUI(resetTime: Boolean = false) = lifecycleScope.launch {
        val currentPosition = if (resetTime) {
            0L
        } else {
            getCurrentPositionUseCase() // получает реальное время из MediaPlayer
        }
        adapter.notifyDataSetChangedWithState(isPlaying, currentPosition)
    }

    private fun startPolling() {
        updateRunnable = Runnable {
            if (isPlaying) {
                updateUI()
                handler.postDelayed(updateRunnable!!, 1000)
            } else {
                stopPolling() // Явно останавливаем, если isPlaying стал false
            }
        }
        handler.post(updateRunnable!!)
    }

    private fun stopPolling() {
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
    }

    private fun setupPlaybackListener() = lifecycleScope.launch {
        try {
            playerRepository.setOnCompletionListener {
                lifecycleScope.launch {
                    // Используем Use Case для обработки завершения
                    handleCompletionUseCase.invoke()

                    isPlaying = false
                    savedPosition = 0L // Сбрасываем позицию
                    updateUI(resetTime = true)
                    stopPolling()
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerActivity", "Failed to set completion listener", e)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("track", intent.getParcelableExtra("track"))
        outState.putBoolean("isPlaying", isPlaying)
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            if (playerRepository.isPlaying()) {
                playerRepository.pause()
                isPlaying = false
                updateUI()        // Обновляет кнопку и время
                stopPolling()     // Останавливает обновление прогресса
            } else {
                stopPolling()     // Если не играл — просто останавливаем polling
            }
        }
    }
    override fun onStop() {
        super.onStop()
        lifecycleScope.launch {
            stopPlaybackUseCase()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPolling()
        lifecycleScope.launch {
            playerRepository.reset() // Сброс плеера при уничтожении Activity
        }
    }

    override fun onResume() {
        super.onResume()
        if (isPlaying && !wasPausedInBackground) {
            startPolling()
        } else if (wasPausedInBackground) {
            wasPausedInBackground = false
        } else {
            stopPolling()
        }
    }

    override fun onBackPressed() {
        stopPlaybackAndCleanup()
        super.onBackPressed()
    }

    private fun stopPlaybackAndCleanup() = lifecycleScope.launch {
        if (playerRepository.isPlaying()) {
            savedPosition = getCurrentPositionUseCase()  // Сохраняем позицию перед выходом
        }
        playerRepository.stop()
        isPlaying = false
        updateUI()
        stopPolling()
        playerRepository.reset()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.e("AudioPlayerActivity", "Ошибка: $message")
    }

    private fun handlePlaybackError(message: String, e: Exception) {
        Log.e("AudioPlayerActivity", message, e)
        showError(message)
    }
}

