package com.practicum.playlistmaker.mediateka.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.models.toParcelable
import com.practicum.playlistmaker.core.utils.FormatTrackDurationUseCase
import com.practicum.playlistmaker.mediateka.data.db.PlaylistTrackEntity
import com.practicum.playlistmaker.mediateka.ui.PlaylistDetailUiState
import com.practicum.playlistmaker.mediateka.ui.adapter.PlaylistTracksAdapter
import com.practicum.playlistmaker.mediateka.ui.view.PlaylistDetailViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistDetailFragment : Fragment() {

    private val viewModel: PlaylistDetailViewModel by viewModel()
    private val formatDurationUseCase: FormatTrackDurationUseCase by inject()

    // Поля для UI
    private lateinit var playlistName: TextView
    private lateinit var playlistDescription: TextView
    private lateinit var playlistDuration: TextView
    private lateinit var playlistTrackCount: TextView
    private lateinit var coverImageView: com.google.android.material.imageview.ShapeableImageView

    private lateinit var shareButton: Button
    private lateinit var menuButton: Button

    private lateinit var menuBottomSheetFrame: View
    private lateinit var menuBehavior: BottomSheetBehavior<View>

    private lateinit var rootView: View

    private lateinit var tracksRecyclerView: RecyclerView
    private var adapter: PlaylistTracksAdapter? = null

    private var currentPlaylistId: Long? = null
    private lateinit var overlayDim: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playlist_detail, container, false)

        rootView = view
        overlayDim = view.findViewById(R.id.overlayDim)

        shareButton = view.findViewById(R.id.shareButton)
        menuButton = view.findViewById(R.id.menuButton)

        // Настройка BottomSheet для меню (будет использоваться в будущем)
        menuBottomSheetFrame = view.findViewById(R.id.menuBottomSheetFrame)
        menuBehavior = BottomSheetBehavior.from(menuBottomSheetFrame)
        menuBehavior.isHideable = true
        menuBehavior.skipCollapsed = false

        playlistName = view.findViewById(R.id.playlistName)
        playlistDescription = view.findViewById(R.id.playlistDescription)
        playlistDuration = view.findViewById(R.id.playlistDuration)
        playlistTrackCount = view.findViewById(R.id.playlistTrackCount)
        coverImageView = view.findViewById(R.id.playlistCover)

        tracksRecyclerView = view.findViewById(R.id.tracksRecyclerView)
        tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Настройка BottomSheet для списка треков (по ТЗ: не отображается и не скроллится)
        val bottomSheetFrame = view.findViewById<View>(R.id.bottomSheetFrame)
        val behavior = BottomSheetBehavior.from(bottomSheetFrame)
        behavior.isHideable = false

        arguments?.getString("playlistId")?.let { idString ->
            currentPlaylistId = idString.toLongOrNull()
        }

        // Адаптер оставляем (чтобы не ломать верстку), но список будет пустым по ТЗ
        adapter = PlaylistTracksAdapter(
            onItemClick = { track ->
                // Эта логика останется, но по ТЗ спринта список треков не показывается —
                // поэтому сюда код никогда не попадёт при текущем поведении.
                val bundle = Bundle().apply {
                    putParcelable("trackParcelable", track.toParcelable())
                }
                findNavController().navigate(
                    R.id.action_playlistDetailFragment_to_audioPlayerFragment,
                    bundle
                )
            },
            onItemLongClick = { _, _ ->
                // По ТЗ: список треков не отображается, поэтому этот блок не нужен.
                // Оставляем сигнатуру, чтобы компилировалось, но тело пустое.
            },
            formatDurationUseCase = formatDurationUseCase
        )
        tracksRecyclerView.adapter = adapter

        val backButton = view.findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                PlaylistDetailUiState.Loading -> { /* опционально: прогресс */ }

                is PlaylistDetailUiState.Success -> {
                    val playlist = state.playlist
                    playlistName.text = playlist.name
                    playlistDescription.text = playlist.description ?: ""

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

                    // ТЗ: список треков НЕ отображается и НЕ скроллится.
                    adapter?.submitList(emptyList())
                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED

                    // Если меню было открыто — закрываем его
                    if (menuBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                        menuBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        overlayDim.visibility = View.GONE
                        overlayDim.alpha = 0f
                    }
                }

                is PlaylistDetailUiState.Error -> {
                    val errorMessage = state.error.message ?: "Не удалось загрузить плейлист"
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }

                // Остальные состояния (ShareReady, Deleted) по ТЗ этого спринта не нужны.
                else -> {}
            }
        }

        currentPlaylistId?.let {
            viewModel.loadPlaylist(it.toString())
        }

        return view
    }
}
