package com.practicum.playlistmaker.player.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.main.ui.MainActivity
import com.practicum.playlistmaker.player.data.repository.AddTrackStatus
import com.practicum.playlistmaker.player.domain.model.PlaybackState
import com.practicum.playlistmaker.player.domain.model.PlaylistForPlayer
import com.practicum.playlistmaker.player.ui.adapter.PlayerTrackAdapter
import com.practicum.playlistmaker.player.ui.adapter.PlaylistSelectionAdapter
import com.practicum.playlistmaker.player.ui.view.AudioPlayerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AudioPlayerFragment : Fragment() {

    companion object {
        private const val KEY_IS_PLAYING = "isPlaying"
        private const val KEY_SAVED_POSITION = "savedPosition"
        private const val TOOLBAR_AUTO_HIDE_DELAY_MS = 2000L
    }

    private val viewModel: AudioPlayerViewModel by viewModel()
    private lateinit var recyclerViewAudioPlayer: RecyclerView
    private lateinit var adapter: PlayerTrackAdapter

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var overlay: View
    private lateinit var playlistsRecyclerView: RecyclerView
    private lateinit var newPlaylistButton: Button
    private lateinit var playlistsAdapter: PlaylistSelectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_audio_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewAudioPlayer = view.findViewById(R.id.recyclerViewAudioPlayer)

        bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.playlists_bottom_sheet))
        overlay = view.findViewById(R.id.overlay)
        playlistsRecyclerView = view.findViewById(R.id.playlistsSelectionRecyclerView)
        newPlaylistButton = view.findViewById(R.id.newPlaylistButton)

        setupBottomSheet()
        setupPlaylistsAdapter()
        setupBottomSheetListeners()
        setupAddToPlaylistButton()

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

        val track = getTrackFromIntent()
        viewModel.setCurrentTrack(track)
        viewModel.updateFavoriteStatusAfterTrackSet()

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            updateUI(state)
            if (state.shouldPoll && state.playbackState.isPlaying) {
                viewModel.startProgressUpdates()
            } else {
                viewModel.stopProgressUpdates()
            }
            updateBottomSheetState(state)

            state.addTrackStatus?.let { status ->
                when (status) {
                    AddTrackStatus.SUCCESS -> {
                        Toast.makeText(requireContext(), "Трек добавлен в плейлист", Toast.LENGTH_SHORT).show()
                    }
                    AddTrackStatus.ALREADY_EXISTS -> {
                        Toast.makeText(requireContext(), "Трек уже добавлен в плейлист", Toast.LENGTH_SHORT).show()
                    }
                    AddTrackStatus.ERROR -> {
                        Toast.makeText(requireContext(), "Ошибка добавления трека", Toast.LENGTH_SHORT).show()
                    }
                }
                viewModel.clearAddTrackStatus()
            }
        }
        setupRecyclerView(track)

        viewModel.setupPlaybackCompletionListener()
        if (savedInstanceState == null) {
            viewModel.initPlayback(track.previewUrl)
        } else {
            val isPlaying = savedInstanceState.getBoolean(KEY_IS_PLAYING)
            val savedPosition = savedInstanceState.getLong(KEY_SAVED_POSITION)
            viewModel.restorePlaybackState(isPlaying, savedPosition)
        }
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        overlay.visibility = View.GONE
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        overlay.visibility = View.VISIBLE
                    }
                    else -> {
                        overlay.visibility = if (bottomSheet.visibility == View.VISIBLE) View.VISIBLE else View.GONE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                overlay.alpha = (slideOffset + 1f) / 2f
            }
        })
    }


    private fun updateBottomSheetState(state: PlayerUiState) {
        if (state.isBottomSheetExpanded) {
            updatePlaylistsInBottomSheet(state.playlistsForBottomSheet)
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        } else {
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
    }

    private fun updatePlaylistsInBottomSheet(playlists: List<PlaylistForPlayer>) {
        playlistsAdapter.updatePlaylists(playlists)
    }

    private fun showPlaylistsBottomSheet() {
        viewModel.showPlaylistsBottomSheet()
    }

    private fun hidePlaylistsBottomSheet() {
        viewModel.hidePlaylistsBottomSheet()
    }

    private fun addTrackToPlaylist(playlist: PlaylistForPlayer) {
        Log.d("AudioPlayer", "Add track to playlist: ${playlist.name}")
        viewModel.addTrackToPlaylist(playlist)
        hidePlaylistsBottomSheet()
    }

    private fun getTrackFromIntent(): Track {
        val bundle = arguments ?: throw IllegalArgumentException("Arguments are required")

        return Track(
            trackId = bundle.getString("trackId") ?: throw IllegalArgumentException("Track ID is required"),
            trackName = bundle.getString("trackName") ?: "",
            artistName = bundle.getString("artistName") ?: "",
            collectionName = bundle.getString("collectionName"),
            artworkUrl100 = bundle.getString("artworkUrl100"),
            previewUrl = bundle.getString("previewUrl"),
            releaseDate = bundle.getString("releaseDate"),
            primaryGenreName = bundle.getString("primaryGenreName"),
            country = bundle.getString("country"),
            trackTimeMillis = bundle.getLong("trackTimeMillis", 0L),
            addedDate = bundle.getLong("addedDate", 0L)
        )
    }
    private fun setupRecyclerView(track: Track) {
        recyclerViewAudioPlayer.layoutManager = LinearLayoutManager(requireContext())
        adapter = PlayerTrackAdapter(
            tracks = mutableListOf(track),
            onClickPlayButton = { _ -> togglePlayback() },
            onAddToPlaylist = { track ->
                showPlaylistsBottomSheet()
                              },
            onFavorite = {
                viewModel.onFavoriteClicked()
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

        val currentPosition = 0

        state.currentTrack?.let { currentTrack ->
            adapter.notifyItemChanged(
                currentPosition,
                PlayerTrackAdapter.UpdatePlaybackStatePayload(
                    isPlaying = state.playbackState.isPlaying,
                    formattedTime = state.formattedTime,
                    isFavorite = state.isFavorite
                )
            )
        } ?: run {
            Log.w("AudioPlayerFragment", "Попытка обновления UI, но текущий трек не установлен")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val currentState = viewModel.uiState.value?.playbackState ?: PlaybackState(false, 0L)
        outState.putBoolean(KEY_IS_PLAYING, currentState.isPlaying)
        outState.putLong(KEY_SAVED_POSITION, currentState.position)
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

    private fun setupBottomSheetListeners() {
        newPlaylistButton.setOnClickListener {
            // Блокируем кнопку, чтобы избежать множественных нажатий
            newPlaylistButton.isClickable = false
            // Явно скрываем Bottom Sheet перед переходом
            hidePlaylistsBottomSheet()
            navigateToCreatePlaylistScreen()
        }
    }

    private fun navigateToCreatePlaylistScreen() {
        findNavController().navigate(
            R.id.action_audioPlayerFragment_to_createPlaylistFragment
        )
        // Разблокируем кнопку после перехода (если потребуется повторный переход)
        newPlaylistButton.isClickable = true
    }

    private fun setupPlaylistsAdapter() {
        playlistsAdapter = PlaylistSelectionAdapter { playlist ->
            addTrackToPlaylist(playlist)
        }
        playlistsRecyclerView.adapter = playlistsAdapter
        playlistsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupAddToPlaylistButton() {
        val addToPlaylistButton: Button = requireView().findViewById(R.id.add_to_playlist_button)
        addToPlaylistButton.setOnClickListener {
            showPlaylistsBottomSheet()
        }
    }

    private fun showToolbarWithAutoHide() {
        val activity = requireActivity() as MainActivity
        val toolbar = activity.getToolbar()

        activity.setSupportActionBar(toolbar)
        toolbar.visibility = View.VISIBLE
        activity.supportActionBar?.show()

        viewLifecycleOwner.lifecycleScope.launch {
            delay(TOOLBAR_AUTO_HIDE_DELAY_MS)
            if (isResumed) {
                toolbar.visibility = View.GONE
                activity.supportActionBar?.hide()
            }
        }
    }
}