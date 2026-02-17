package com.practicum.playlistmaker.player.ui

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
import com.practicum.playlistmaker.search.data.mapper.DtoMapper
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.contract.FormatTrackDurationUseCaseContract
import com.practicum.playlistmaker.core.contract.GetCurrentPositionUseCaseContract
import com.practicum.playlistmaker.core.contract.HandlePlaybackCompletionUseCaseContract
import com.practicum.playlistmaker.core.contract.PreparePlaybackUseCaseContract
import com.practicum.playlistmaker.core.contract.SetPlaybackCompletionListenerUseCaseContract
import com.practicum.playlistmaker.core.contract.StopPlaybackUseCaseContract
import com.practicum.playlistmaker.core.contract.TogglePlaybackUseCaseContract
import com.practicum.playlistmaker.core.usecase.UseCaseCreator
import com.practicum.playlistmaker.search.ui.adapter.TrackAdapter
import com.practicum.playlistmaker.search.ui.parcel.ParcelableTrack
import com.practicum.playlistmaker.core.constants.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AudioPlayerActivity : AppCompatActivity() {

    @Inject
    lateinit var useCaseCreator: UseCaseCreator
    @Inject
    lateinit var dtoMapper: DtoMapper

    private lateinit var recyclerViewAudioPlayer: RecyclerView
    private lateinit var adapter: TrackAdapter
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    private var isPlaying = false
    private var savedPosition: Long = 0L
    private var isTrackFromIntent: Boolean = false


    // Use Cases через интерфейсы
    private lateinit var preparePlaybackUseCase: PreparePlaybackUseCaseContract
    private lateinit var togglePlaybackUseCase: TogglePlaybackUseCaseContract
    private lateinit var stopPlaybackUseCase: StopPlaybackUseCaseContract
    private lateinit var getCurrentPositionUseCase: GetCurrentPositionUseCaseContract
    private lateinit var handleCompletionUseCase: HandlePlaybackCompletionUseCaseContract
    private lateinit var formatTrackDurationUseCase: FormatTrackDurationUseCaseContract
    private lateinit var setCompletionListenerUseCase: SetPlaybackCompletionListenerUseCaseContract

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audioplayer)

        initUseCases()
        val track = getTrackFromIntentOrSavedState(savedInstanceState)
        setupRecyclerView(track)

        if (savedInstanceState == null) {
            isPlaying = false
            savedPosition = 0L
            lifecycleScope.launch {
                try {
                    if (track.previewUrl.isNullOrBlank()) {
                        showError("Отрывок недоступен")
                    } else {
                        val result = preparePlaybackUseCase(track.previewUrl)
                        if (!result.isSuccess) {
                            handlePlaybackError("Не удалось подготовить воспроизведение", result.exceptionOrNull() ?: return@launch)
                        }
                    }
                } catch (e: Exception) {
                    handlePlaybackError("Ошибка при подготовке воспроизведения", e)
                }
            }
            updateUI()
        } else {
            isPlaying = savedInstanceState.getBoolean("isPlaying")
            savedPosition = savedInstanceState.getLong("savedPosition")
            updateUI(resetTime = false)

            if (isPlaying) {
                startPolling()
            }
        }

        setupPlaybackListener()
        setupBackButton()
    }

    private fun initUseCases() {
        preparePlaybackUseCase = useCaseCreator.createPreparePlaybackUseCase()
        togglePlaybackUseCase = useCaseCreator.createTogglePlaybackUseCase()
        stopPlaybackUseCase = useCaseCreator.createStopPlaybackUseCase()
        getCurrentPositionUseCase = useCaseCreator.createGetCurrentPositionUseCase()
        handleCompletionUseCase = useCaseCreator.createHandlePlaybackCompletionUseCase()
        formatTrackDurationUseCase = useCaseCreator.createFormatTrackDurationUseCase()
        setCompletionListenerUseCase = useCaseCreator.createSetPlaybackCompletionListenerUseCase()
    }

    private fun getTrackFromIntentOrSavedState(savedState: Bundle?): Track {
        val parcelable = intent.getParcelableExtra<ParcelableTrack>("track")
        if (parcelable != null) {
            Log.d("AudioPlayer", "Track received from Intent")
            return dtoMapper.toDomain(parcelable)
        }
        if (savedState != null) {
            val savedParcelable = savedState.getParcelable<ParcelableTrack>("track")
            if (savedParcelable != null) {
                Log.d("AudioPlayer", "Track restored from savedInstanceState (fallback)")
                return dtoMapper.toDomain(savedParcelable)
            }
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
            formatDurationUseCase = formatTrackDurationUseCase
        )
        recyclerViewAudioPlayer.adapter = adapter
    }

    private fun togglePlayback() = lifecycleScope.launch {
        try {
            val resumePosition = if (isPlaying) {
                null
            } else if (savedPosition > 0L && !isPlaying) {
                savedPosition
            } else {
                0L
            }

            val result = togglePlaybackUseCase(resumePosition)

            if (result.isSuccess) {
                isPlaying = result.getOrThrow()
                if (isPlaying) {
                    startPolling()
                    if (resumePosition == 0L) {
                        savedPosition = 0L
                    }
                } else {
                    savedPosition = getCurrentPositionUseCase()
                    stopPolling()
                }
                updateUI()
            } else {
                handlePlaybackError("Ошибка переключения воспроизведения", result.exceptionOrNull() ?: return@launch)
            }
        } catch (t: Throwable) {
            handlePlaybackError("Ошибка при переключении воспроизведения", t)
        }
    }


    private fun updateUI(resetTime: Boolean = false) = lifecycleScope.launch {
        val currentPosition = if (resetTime) {
            0L
        } else if (!isPlaying && savedPosition > 0L) {
            savedPosition
        } else {
            getCurrentPositionUseCase()
        }
        adapter.notifyDataSetChangedWithState(isPlaying, currentPosition)
    }

    private fun startPolling() {
        Log.d("AudioPlayer", "startPolling called, isPlaying=$isPlaying")
        updateRunnable = Runnable {
            if (isPlaying) {
                updateUI()
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

    private fun setupPlaybackListener() = lifecycleScope.launch {
        setCompletionListenerUseCase {
            lifecycleScope.launch {
                isPlaying = false
                savedPosition = 0L
                stopPolling()
                updateUI(resetTime = true)
                handleCompletionUseCase()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (!isTrackFromIntent) {
            val currentTrack = adapter.getTracks().firstOrNull()
            if (currentTrack != null) {
                outState.putParcelable("track", dtoMapper.toParcelableTrack(currentTrack))
            }
        }

        outState.putBoolean("isPlaying", isPlaying)
        outState.putLong("savedPosition", savedPosition)
    }


    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            if (isPlaying) {
                val pauseResult = togglePlaybackUseCase(null)
                if (pauseResult.isSuccess) {
                    isPlaying = false
                    savedPosition = getCurrentPositionUseCase()
                }
            }
            stopPolling()
            updateUI()
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
        updateUI()
        if (isPlaying) {
            startPolling()
        }
    }

    override fun onBackPressed() {
        stopPlaybackAndCleanup()
        super.onBackPressed()
    }

    private fun stopPlaybackAndCleanup() = lifecycleScope.launch {
        if (isPlaying) {
            savedPosition = getCurrentPositionUseCase()
            val pauseResult = togglePlaybackUseCase()
            if (pauseResult.isSuccess) {
                isPlaying = false
            }
        }
        stopPlaybackUseCase()
        handleCompletionUseCase()
        updateUI()
        stopPolling()
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