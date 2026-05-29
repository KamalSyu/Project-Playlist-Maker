package com.practicum.playlistmaker.player.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.models.domain.AddTrackStatus
import com.practicum.playlistmaker.main.ui.MainActivity
import com.practicum.playlistmaker.player.data.mapper.TrackParcelableMapper
import com.practicum.playlistmaker.player.domain.model.PlaybackState
import com.practicum.playlistmaker.player.domain.model.PlaylistForPlayer
import com.practicum.playlistmaker.player.ui.adapter.PlayerTrackAdapter
import com.practicum.playlistmaker.player.ui.adapter.PlaylistSelectionAdapter
import com.practicum.playlistmaker.player.ui.view.AudioPlayerViewModel
import com.practicum.playlistmaker.search.ui.parcel.ParcelableTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var newPlaylistButton: Button? = null
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
        val addToPlaylistButton: Button = requireView().findViewById(R.id.ic_button_plus)
        addToPlaylistButton.setOnClickListener {
            val track = getTrackFromIntent()
            showPlaylistSelectionBottomSheet(track)
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
    private fun showPlaylistSelectionBottomSheet(track: Track) {
        bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog?.setContentView(R.layout.bottom_sheet_playlists)
        val recyclerView = bottomSheetDialog?.findViewById<RecyclerView>(R.id.playlistsBottomSheetRecyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        val adapter = PlaylistSelectionAdapter { playlist: PlaylistForPlayer ->
            lifecycleScope.launch {
                val result = withContext(Dispatchers.IO) {
                    viewModel.addTrackToPlaylist(playlist.id, track)
                }
                when (result) {
                    AddTrackStatus.SUCCESS -> {
                        bottomSheetDialog?.dismiss()
                        Toast.makeText(
                            requireContext(),
                            "Добавлено в плейлист ${playlist.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    AddTrackStatus.ALREADY_EXISTS -> {
                        Toast.makeText(
                            requireContext(),
                            "Трек уже добавлен в плейлист ${playlist.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        Toast.makeText(
                            requireContext(),
                            "Ошибка при добавлении трека",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        recyclerView?.adapter = adapter
        lifecycleScope.launch {
            viewModel.playlists.collect { playlists ->
                adapter.updatePlaylists(playlists)
            }
        }
        val newPlaylistButton = bottomSheetDialog?.findViewById<Button>(R.id.newPlaylistBottomSheetButton)
        newPlaylistButton?.setOnClickListener {
            bottomSheetDialog?.dismiss()
            findNavController().navigate(R.id.action_audioPlayerFragment_to_createPlaylistFragment)
        }
        val bottomSheet = bottomSheetDialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = true
            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        bottomSheetDialog?.dismiss()
                    }
                }
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    if (slideOffset < -0.5f) {
                        behavior.state = BottomSheetBehavior.STATE_HIDDEN
                    }
                }
            })
        }
        bottomSheetDialog?.show()
    }
    private fun getTrackFromIntent(): Track {
        val parcelableTrack: ParcelableTrack = arguments?.getParcelable("track")
            ?: throw IllegalArgumentException("Track is required but not provided in arguments.")
        val mapper = TrackParcelableMapper()
        val track = mapper.toDomain(parcelableTrack)

        Log.d("AudioPlayerFragment", "Трек получен: ${track.trackName}, isFavorite=${track.isFavorite}")
        return track
    }
    private fun setupRecyclerView(track: Track) {
        recyclerViewAudioPlayer = requireView().findViewById(R.id.recyclerViewAudioPlayer)
        recyclerViewAudioPlayer.layoutManager = LinearLayoutManager(requireContext())
        adapter = PlayerTrackAdapter(
            tracks = mutableListOf(track),
            onClickPlayButton = { _ -> togglePlayback() },
            onAddToPlaylist = { track ->
                showPlaylistSelectionBottomSheet(track)
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