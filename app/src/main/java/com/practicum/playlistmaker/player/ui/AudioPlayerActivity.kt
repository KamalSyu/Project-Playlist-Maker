package com.practicum.playlistmaker.player.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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


    // Локальное поле playbackState удалено — все данные берём из LiveData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audioplayer)

        setupRecyclerView(getTrackFromIntent())
        setupBackButton()

        // Подписываемся на изменения состояния воспроизведения
        viewModel.playbackState.observe(this) { newState ->
            updateUI()
        }

        // Подписываемся на событие завершения воспроизведения
        viewModel.playbackCompleted.observe(this) {
            lifecycleScope.launch {
                stopPolling()
                updateUI()
            }
        }

        // Инициализация при первом запуске
        if (savedInstanceState == null) {
            val track = getTrackFromIntent()
            viewModel.initPlayback(track.previewUrl)
            setupCompletionListener()
        } else {
            // Восстановление состояния воспроизведения
            val isPlaying = savedInstanceState.getBoolean("isPlaying")
            val savedPosition = savedInstanceState.getLong("savedPosition")
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
            formatDurationUseCase = viewModel.getFormatTrackDurationUseCase()
        )
        recyclerViewAudioPlayer.adapter = adapter
    }

    private fun togglePlayback() {
        val currentState = viewModel.playbackState.value ?: PlaybackState(false, 0L)
        val resumePosition = when {
            !currentState.isPlaying && currentState.position > 0L -> currentState.position
            else -> null
        }
        viewModel.togglePlayback(resumePosition)
    }

    private fun stopPlaybackAndCleanup() {
        viewModel.stopPlayback()
    }

    private fun updateUI() {
        val currentState = viewModel.playbackState.value ?: PlaybackState(false, 0L)
        val currentTimeMillis = viewModel.currentPositionMillis.value ?: 0L  // берём Long
        val formattedTime = viewModel.formattedTime.value ?: "00:00"

        adapter.notifyDataSetChangedWithState(
            isPlaying = currentState.isPlaying,
            currentTimeMillis = currentTimeMillis
        )
    }


    private fun startPolling() {
        updateRunnable = Runnable {
            if ((viewModel.playbackState.value ?: PlaybackState(false, 0L)).isPlaying) {
                handler.postDelayed(updateRunnable!!, 1000)
            } else {
                stopPolling()
            }
        }
        handler.post(updateRunnable!!)
    }

    private fun stopPolling() {
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
    }

    private fun setupCompletionListener() {
        viewModel.setupCompletionListener()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isPlaying", (viewModel.playbackState.value ?: PlaybackState(false, 0L)).isPlaying)
        outState.putLong("savedPosition", (viewModel.playbackState.value ?: PlaybackState(false, 0L)).position)
    }

    override fun onPause() {
        super.onPause()
        stopPolling()
        if (isFinishing) {
            stopPlaybackAndCleanup()
        }
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPolling()
    }

    override fun onResume() {
        super.onResume()
        if ((viewModel.playbackState.value ?: PlaybackState(false, 0L)).isPlaying) {
            startPolling()
        }
    }

    override fun onBackPressed() {
        stopPlaybackAndCleanup()
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
