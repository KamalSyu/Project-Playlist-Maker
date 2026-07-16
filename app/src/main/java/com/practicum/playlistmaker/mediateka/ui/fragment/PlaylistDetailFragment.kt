package com.practicum.playlistmaker.mediateka.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.toParcelable
import com.practicum.playlistmaker.core.utils.FormatTrackDurationUseCase
import com.practicum.playlistmaker.mediateka.ui.PlaylistDetailUiState
import com.practicum.playlistmaker.mediateka.ui.adapter.PlaylistTracksAdapter
import com.practicum.playlistmaker.mediateka.ui.view.PlaylistDetailViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistDetailFragment : Fragment() {

    private val viewModel: PlaylistDetailViewModel by viewModel()
    private val formatDurationUseCase: FormatTrackDurationUseCase by inject()
    private lateinit var playlistName: TextView
    private lateinit var playlistYear: TextView
    private lateinit var playlistDuration: TextView
    private lateinit var playlistTrackCount: TextView
    private lateinit var coverImageView: com.google.android.material.imageview.ShapeableImageView
    private lateinit var shareIcon: ImageView
    private lateinit var menuIcon: ImageView
    private lateinit var tracksRecyclerView: RecyclerView
    private var adapter: PlaylistTracksAdapter? = null
    private lateinit var bottomSheetFrame: View
    private lateinit var behavior: BottomSheetBehavior<View>
    private var currentPlaylistId: Long? = null
    private lateinit var overlayDim: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playlist_detail, container, false)

        overlayDim = view.findViewById(R.id.overlayDim)

        shareIcon = view.findViewById(R.id.shareIcon)
        menuIcon = view.findViewById(R.id.menuIcon)

        playlistName = view.findViewById(R.id.playlistName)
        playlistYear = view.findViewById(R.id.playlistYear)
        playlistDuration = view.findViewById(R.id.playlistDuration)
        playlistTrackCount = view.findViewById(R.id.playlistTrackCount)
        coverImageView = view.findViewById(R.id.playlistCover)

        tracksRecyclerView = view.findViewById(R.id.tracksRecyclerView)
        tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        bottomSheetFrame = view.findViewById(R.id.bottomSheetFrame)
        behavior = BottomSheetBehavior.from(bottomSheetFrame)
        behavior.isHideable = false
        behavior.peekHeight = 200 // PeekHeight только здесь, в коде
        arguments?.getString("playlistId")?.let { idString ->
            currentPlaylistId = idString.toLongOrNull()
            Log.d("PlaylistDebug", "✅ ID получен во Fragment: $idString (parsed=${currentPlaylistId})")
        } ?: Log.e("PlaylistDebug", "❌ Аргумент playlistId НЕ найден в arguments!")

        adapter = PlaylistTracksAdapter(
            onItemClick = { track ->
                val bundle = Bundle().apply {
                    putParcelable("trackParcelable", track.toParcelable())
                }
                findNavController().navigate(
                    R.id.action_playlistDetailFragment_to_audioPlayerFragment,
                    bundle
                )
            },
            onItemLongClick = { _, track ->
                Toast.makeText(requireContext(), "Long click: ${track.trackName}", Toast.LENGTH_SHORT).show()
            },
            formatDurationUseCase = formatDurationUseCase
        )
        tracksRecyclerView.adapter = adapter
        val backButton = view.findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        shareIcon.setOnClickListener {
            Toast.makeText(requireContext(), "Поделиться", Toast.LENGTH_SHORT).show()
        }
        menuIcon.setOnClickListener {
            Toast.makeText(requireContext(), "Меню", Toast.LENGTH_SHORT).show()
        }
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                PlaylistDetailUiState.Loading -> {
                    // При необходимости можно показать индикатор загрузки
                }

                is PlaylistDetailUiState.Success -> {
                    val playlist = state.playlist
                    Log.d("PlaylistDebug", "UI: playlist.coverPath = '${playlist.coverPath}'")

                    playlistName.text = playlist.name
                    playlistYear.text = "2026"

                    if (playlist.coverPath.isNullOrBlank()) {
                        coverImageView.setImageResource(R.drawable.ic_placeholder_312)
                    } else {
                        Glide.with(this)
                            .load(playlist.coverPath)
                            .placeholder(R.drawable.ic_placeholder_312)
                            .error(R.drawable.ic_placeholder_312)
                            .centerCrop()
                            .into(coverImageView)
                    }

                    playlistDuration.text = playlist.durationFormatted
                    playlistTrackCount.text = "${playlist.trackCount} треков"

                    adapter?.submitList(state.tracks)

                    // Требования 1 и 2: список виден при наличии треков, скрыть нельзя
                    behavior.isHideable = false
                    if (state.tracks.isEmpty()) {
                        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    } else {
                        behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                    }
                }

                is PlaylistDetailUiState.Error -> {
                    val errorMessage = state.error.message ?: "Не удалось загрузить плейлист"
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }

        currentPlaylistId?.let {
            viewModel.loadPlaylist(it.toString())
        }
        return view
    }
}
