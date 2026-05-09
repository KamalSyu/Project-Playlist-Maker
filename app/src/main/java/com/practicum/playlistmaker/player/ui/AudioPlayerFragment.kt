package com.practicum.playlistmaker.player.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.constants.Constants
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.main.ui.MainActivity
import com.practicum.playlistmaker.player.domain.model.PlaybackState
import com.practicum.playlistmaker.player.ui.adapter.PlayerTrackAdapter
import com.practicum.playlistmaker.player.ui.view.AudioPlayerViewModel
import com.practicum.playlistmaker.search.ui.parcel.ParcelableTrack
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AudioPlayerFragment : Fragment() {

    private val viewModel: AudioPlayerViewModel by viewModel()
    private lateinit var recyclerViewAudioPlayer: RecyclerView
    private lateinit var adapter: PlayerTrackAdapter
    private var lastKnownIsPlaying: Boolean = false
    private lateinit var track: Track
    private lateinit var favoriteButton: ImageButton


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_audio_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val backButton: TextView = requireView().findViewById(R.id.back)
        backButton.setOnClickListener {
            handleBackPress()
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        ViewCompat.setOnApplyWindowInsetsListener(view) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBar.top)
            insets
        }

        track = getTrackFromIntent()
        viewModel.setCurrentTrack(track)

        setupRecyclerView(track)
        setupFavoriteButtonClickListener()

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            updateUI(state)
            if (state.shouldPoll && state.playbackState.isPlaying) {
                viewModel.startProgressUpdates()
            } else {
                viewModel.stopProgressUpdates()
            }
        }

        viewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
            updateFavoriteButtonState(isFavorite)
        }

        viewModel.setupPlaybackCompletionListener()
        if (savedInstanceState == null) {
            viewModel.initPlayback(track.previewUrl)
        } else {
            val isPlaying = savedInstanceState.getBoolean(Constants.KEY_IS_PLAYING)
            val savedPosition = savedInstanceState.getLong(Constants.KEY_SAVED_POSITION)
            viewModel.restorePlaybackState(isPlaying, savedPosition)
        }
    }

    private fun getTrackFromIntent(): Track {
        val parcelableTrack: ParcelableTrack = arguments?.getParcelable("track")
            ?: throw IllegalArgumentException("Track is required but not provided in arguments.")
        return viewModel.processTrack(parcelableTrack)
    }

    private fun setupRecyclerView(track: Track) {
        recyclerViewAudioPlayer = requireView().findViewById(R.id.recyclerViewAudioPlayer)
        recyclerViewAudioPlayer.layoutManager = LinearLayoutManager(requireContext())
        adapter = PlayerTrackAdapter(
            tracks = listOf(track),
            onClickPlayButton = { _ -> togglePlayback() },
            onAddToPlaylist = { track ->
                Log.d("AudioPlayer", "Add to playlist: ${track.trackName}")
            },
            onFavorite = { track ->
                Log.d("AudioPlayer", "Favorite: ${track.trackName}")
            },
            formatDurationUseCase = viewModel.formatTrackDurationUseCase
        )
        recyclerViewAudioPlayer.adapter = adapter

        recyclerViewAudioPlayer.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val firstItem = layoutManager.findViewByPosition(firstVisibleItemPosition)

                if ((firstVisibleItemPosition <= 0) &&
                    dy < -5 &&
                    firstItem != null) {

                    val visibleHeight = firstItem.height - firstItem.top
                    if (visibleHeight > firstItem.height * 0.5f) {
                        showToolbarWithAutoHide()
                    }
                }
            }
        })
    }

    private fun setupFavoriteButtonClickListener() {
        favoriteButton = requireView().findViewById(R.id.ic_button_like)
        favoriteButton.setOnClickListener {
            viewModel.onFavoriteClicked(track)
        }
    }

    private fun updateFavoriteButtonState(isFavorite: Boolean) {
        val drawableRes = if (isFavorite) {
            R.drawable.ic_heart_filled
        } else {
            R.drawable.ic_button_like
        }
        favoriteButton.setImageResource(drawableRes)
    }

    private fun togglePlayback() {
        val currentState = viewModel.uiState.value?.playbackState ?: PlaybackState(false, 0L)
        val resumePosition = if (!currentState.isPlaying && currentState.position > 0L) currentState.position else null
        viewModel.togglePlayback(resumePosition)
    }

    private fun updateUI(state: PlayerUiState) {
        if (state.error != null) {
            handlePlaybackError("Ошибка воспроизведения", state.error)
            viewModel.clearError()
            return
        }
        adapter.updateCurrentTime(state.formattedTime)
        val isPlaying = state.playbackState.isPlaying
        if (lastKnownIsPlaying != isPlaying) {
            adapter.notifyDataSetChangedWithState(
                isPlaying = isPlaying,
                currentTimeMillis = state.playbackState.position,
                position = 0,
                formattedTime = state.formattedTime
            )
        }
        lastKnownIsPlaying = isPlaying
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val currentState = viewModel.uiState.value?.playbackState ?: PlaybackState(false, 0L)
        outState.putBoolean(Constants.KEY_IS_PLAYING, currentState.isPlaying)
        outState.putLong(Constants.KEY_SAVED_POSITION, currentState.position)
    }

    override fun onPause() {
        super.onPause()
        val currentState = viewModel.uiState.value?.playbackState ?: PlaybackState(false, 0L)
        if (currentState.isPlaying) {
            viewModel.saveCurrentPosition()
            viewModel.stopPlayback()
        }
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        val currentState = viewModel.uiState.value ?: return
        updateUI(currentState)
    }

    private fun showError(message: String) {
        Log.e("AudioPlayerFragment", "Ошибка: $message")
    }

    private fun handlePlaybackError(message: String, e: Throwable) {
        Log.e("AudioPlayerFragment", message, e)
        showError(message)
    }

    private fun handleBackPress() {
        viewModel.resetPlaybackToStart()
        findNavController().popBackStack()
    }

    private fun showToolbarWithAutoHide() {
        val activity = requireActivity() as MainActivity
        val toolbar = activity.getToolbar()

        activity.setSupportActionBar(toolbar)
        toolbar.visibility = View.VISIBLE
        activity.supportActionBar?.show()

        viewLifecycleOwner.lifecycleScope.launch {
            delay(Constants.TOOLBAR_AUTO_HIDE_DELAY_MS)
            if (isResumed) {
                toolbar.visibility = View.GONE
                activity.supportActionBar?.hide()
            }
        }
    }
}