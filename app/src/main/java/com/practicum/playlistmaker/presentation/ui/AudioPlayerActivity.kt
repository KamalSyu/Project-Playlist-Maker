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
import com.practicum.playlistmaker.domain.usecase.GetCurrentPositionUseCaseContract
import com.practicum.playlistmaker.domain.usecase.PreparePlaybackUseCaseContract
import com.practicum.playlistmaker.domain.usecase.StopPlaybackUseCaseContract
import com.practicum.playlistmaker.domain.usecase.TogglePlaybackUseCaseContract
import com.practicum.playlistmaker.domain.usecase.UseCaseCreator
import com.practicum.playlistmaker.presentation.adapter.TrackAdapter
import com.practicum.playlistmaker.presentation.parcel.ParcelableTrack
import com.practicum.playlistmaker.presentation.util.Constants.Companion.VIEW_TYPE_ALBUM
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


    // Use Cases через Creator
    private lateinit var preparePlaybackUseCase: PreparePlaybackUseCaseContract
    private lateinit var togglePlaybackUseCase: TogglePlaybackUseCaseContract
    private lateinit var stopPlaybackUseCase: StopPlaybackUseCaseContract
    private lateinit var getCurrentPositionUseCase: GetCurrentPositionUseCaseContract


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audioplayer)

        // Инициализация Use Cases через Creator
        preparePlaybackUseCase = useCaseCreator.createPreparePlaybackUseCase()
        togglePlaybackUseCase = useCaseCreator.createTogglePlaybackUseCase()
        stopPlaybackUseCase = useCaseCreator.createStopPlaybackUseCase()
        getCurrentPositionUseCase = useCaseCreator.createGetCurrentPositionUseCase()


        val track = getTrackFromIntentOrSavedState(savedInstanceState)
        isPlaying = savedInstanceState?.getBoolean("isPlaying") ?: false

        setupRecyclerView(track)
        if (savedInstanceState == null) {
            prepareAndPlay(track)
        } else {
            updateUI()
            if (isPlaying) startPolling() else stopPolling()
        }

        setupPlaybackListener()
        setupBackButton()
    }

    // Безопасное получение Track
    private fun getTrackFromIntentOrSavedState(savedState: Bundle?): Track {
        return savedState?.getParcelable("track")
            ?: intent.getParcelableExtra("track")
            ?: throw IllegalArgumentException("Track is required but not provided")
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
            onFavorite = {}
        )
        recyclerViewAudioPlayer.adapter = adapter
    }

    private fun prepareAndPlay(track: Track) = lifecycleScope.launch {
        try {
            if (track.previewUrl.isNullOrBlank()) {
                showError("Отрывок недоступен")
                return@launch
            }
            preparePlaybackUseCase(track.previewUrl)
            isPlaying = true
            updateUI()
            startPolling()
        } catch (e: Exception) {
            handlePlaybackError("Не удалось воспроизвести отрывок", e)
        }
    }

    private fun togglePlayback() = lifecycleScope.launch {
        try {
            if (wasPausedInBackground) {
                val track = requireNotNull(intent.getParcelableExtra<ParcelableTrack>("track")) { "Track missing in intent" }
                preparePlaybackUseCase(track.previewUrl)
                wasPausedInBackground = false
            }
            val result = togglePlaybackUseCase()
            if (result.isSuccess) {
                isPlaying = result.getOrThrow()
                updateUI()
                if (isPlaying) startPolling() else stopPolling()
            }
        } catch (e: Exception) {
            handlePlaybackError("Ошибка при переключении воспроизведения", e)
        }
    }

    private fun updateUI() = lifecycleScope.launch {
        val currentPosition = getCurrentPositionUseCase()
        adapter.notifyDataSetChangedWithState(isPlaying, currentPosition)
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

    private fun setupPlaybackListener() = lifecycleScope.launch {
        try {
            playerRepository.setOnCompletionListener {
                lifecycleScope.launch {
                    isPlaying = false
                    updateUI()
                    stopPolling()
                    playerRepository.reset()
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
        if (isPlaying) {
            lifecycleScope.launch {
                togglePlaybackUseCase()  // Приостанавливаем
                wasPausedInBackground = true
                stopPolling()
            }
        } else {
            stopPolling()
        }
    }

    override fun onStop() {
        super.onStop()
        lifecycleScope.launch { stopPlaybackUseCase() }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPolling()
        lifecycleScope.launch {
            playerRepository.setOnCompletionListener {}
        }
    }

    override fun onResume() {
        super.onResume()
        if (isPlaying && !wasPausedInBackground) {
            startPolling()
        } else if (wasPausedInBackground) {
            wasPausedInBackground = false  // Сбрасываем флаг
        } else {
            stopPolling()
        }
    }

    override fun onBackPressed() {
        stopPlaybackAndCleanup()
        super.onBackPressed()
    }

    private fun stopPlaybackAndCleanup() = lifecycleScope.launch {
        stopPlaybackUseCase()
        isPlaying = false
        updateUI()
        stopPolling()
    }

    // Абстрагированный показ ошибок
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.e("AudioPlayerActivity", "Ошибка: $message")
    }

    // Единый обработчик ошибок воспроизведения
    private fun handlePlaybackError(message: String, e: Exception) {
        Log.e("AudioPlayerActivity", message, e)
        showError(message)
    }
}
