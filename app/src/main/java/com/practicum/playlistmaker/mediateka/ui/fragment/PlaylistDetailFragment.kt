package com.practicum.playlistmaker.mediateka.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.core.models.toParcelable
import com.practicum.playlistmaker.core.utils.FormatTrackDurationUseCase
import com.practicum.playlistmaker.core.utils.toTracksText
import com.practicum.playlistmaker.mediateka.ui.PlaylistDetailUiState
import com.practicum.playlistmaker.mediateka.ui.adapter.PlaylistTracksAdapter
import com.practicum.playlistmaker.mediateka.ui.view.PlaylistDetailViewModel
import org.koin.android.ext.android.inject

class PlaylistDetailFragment : Fragment() {
    private val viewModel: PlaylistDetailViewModel by inject()
    private val formatDurationUseCase: FormatTrackDurationUseCase by inject()

    private lateinit var playlistName: TextView
    private lateinit var playlistDescription: TextView
    private lateinit var playlistDuration: TextView
    private lateinit var playlistTrackCount: TextView
    private lateinit var coverImageView: ShapeableImageView
    private lateinit var shareIcon: ImageView
    private lateinit var menuIcon: ImageView
    private lateinit var tracksRecyclerView: RecyclerView
    private lateinit var emptyTracksText: TextView
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var bottomSheetFrame: View
    private lateinit var overlayDim: View
    private lateinit var menuBottomSheetFrame: View
    private var menuBehavior: BottomSheetBehavior<View>? = null

    private lateinit var menuShareItem: TextView
    private lateinit var menuEditItem: TextView
    private lateinit var menuDeleteItem: TextView

    private lateinit var menuPlaylistCover: ShapeableImageView
    private lateinit var menuPlaylistName: TextView
    private lateinit var menuPlaylistTrackCount: TextView

    private var adapter: PlaylistTracksAdapter? = null
    private var currentPlaylistId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playlist_detail, container, false)

        overlayDim = view.findViewById(R.id.overlayDim)
        overlayDim.setOnClickListener { closeMenuWithAnimation() }

        menuBottomSheetFrame = view.findViewById(R.id.menuBottomSheetFrame)
        menuBehavior = BottomSheetBehavior.from(menuBottomSheetFrame)

        if (menuBehavior != null) {
            menuBehavior?.isHideable = true
            menuBehavior?.skipCollapsed = false
            menuBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            menuBottomSheetFrame.post {
                menuBehavior?.peekHeight =
                    resources.getDimensionPixelSize(R.dimen.menu_bottom_sheet_height)
            }
            menuBehavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_EXPANDED,
                        BottomSheetBehavior.STATE_HALF_EXPANDED,
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            overlayDim.visibility = View.VISIBLE
                            overlayDim.alpha = 0f
                            overlayDim.animate().alpha(1f).setDuration(200).start()
                        }
                        else -> {
                            overlayDim.animate().alpha(0f).setDuration(200).withEndAction {
                                overlayDim.visibility = View.GONE
                            }.start()
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })
        }

        val selectedPlaylistItem = view.findViewById<View>(R.id.selectedPlaylistItem)

        menuPlaylistCover = selectedPlaylistItem.findViewById(R.id.playlistCover)
        menuPlaylistName = selectedPlaylistItem.findViewById(R.id.playlistName)
        menuPlaylistTrackCount = selectedPlaylistItem.findViewById(R.id.playlistTrackCount)

        menuShareItem = view.findViewById(R.id.menuShareItem)
        menuEditItem = view.findViewById(R.id.menuEditItem)
        menuDeleteItem = view.findViewById(R.id.menuDeleteItem)

        shareIcon = view.findViewById(R.id.shareIcon)
        menuIcon = view.findViewById(R.id.menuIcon)

        playlistName = view.findViewById(R.id.playlistName)
        playlistDescription = view.findViewById(R.id.playlistDescription)
        playlistDuration = view.findViewById(R.id.playlistDuration)
        playlistTrackCount = view.findViewById(R.id.playlistTrackCount)
        coverImageView = view.findViewById(R.id.playlistCover)

        tracksRecyclerView = view.findViewById(R.id.tracksRecyclerView)
        tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        emptyTracksText = view.findViewById(R.id.emptyTracksText)

        bottomSheetFrame = view.findViewById(R.id.bottomSheetFrame)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetFrame)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.isHideable = false
        bottomSheetBehavior.skipCollapsed = false
        bottomSheetBehavior.isFitToContents = true
        bottomSheetFrame.post {
            bottomSheetBehavior.peekHeight =
                resources.getDimensionPixelSize(R.dimen.playlist_tracks_sheet_height)
        }
        arguments?.getString("playlistId")?.let { idString ->
            currentPlaylistId = idString.toLongOrNull()
        }

        currentPlaylistId?.let { id ->
            adapter = PlaylistTracksAdapter(
                currentPlaylistId = id,
                onItemClick = { track ->
                    val bundle = Bundle().apply {
                        putParcelable("track", track.toParcelable())
                    }
                    findNavController().navigate(
                        R.id.action_playlistDetailFragment_to_audioPlayerFragment,
                        bundle
                    )
                },
                onItemLongClick = { playlistId, track ->
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Хотите удалить трек?")
                        .setPositiveButton("ДА") { _, _ ->
                            viewModel.removeTrack(playlistId, track.trackId)
                        }
                        .setNegativeButton("НЕТ", null)
                        .show()
                },
                formatDurationUseCase = formatDurationUseCase
            )
            tracksRecyclerView.adapter = adapter
        } ?: run {
            findNavController().popBackStack()
        }

        val backButton = view.findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        menuIcon.setOnClickListener {
            val state = viewModel.uiState.value as? PlaylistDetailUiState.Success
            if (state == null) {
                Toast.makeText(requireContext(), "Данные ещё загружаются", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val playlist = state.playlist
            menuPlaylistName.text = playlist.name

            updateMenuPlaylistInfo(playlist)

            if (menuBehavior != null) {
                menuBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                Toast.makeText(requireContext(), "Ошибка: меню недоступно", Toast.LENGTH_LONG).show()
            }
        }

        shareIcon.setOnClickListener {
            val state = viewModel.uiState.value as? PlaylistDetailUiState.Success ?: return@setOnClickListener
            val playlist = state.playlist
            val tracks = state.tracks

            if (tracks.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "В этом плейлисте нет списка треков, которым можно поделиться",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val shareText = buildString {
                appendLine(playlist.name)
                appendLine(playlist.description ?: "")
                appendLine(playlist.trackCount.toTracksText())

                tracks.forEachIndexed { index, track ->
                    val durationStr = track.trackTimeMillis?.let { time ->
                        formatDurationUseCase.invoke(time)
                    } ?: ""
                    appendLine("${index + 1}. ${track.artistName} - ${track.trackName} ($durationStr)")
                }
            }

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            val chooserIntent = Intent.createChooser(sendIntent, "Поделиться плейлистом")
            startActivity(chooserIntent)
        }

        menuShareItem.setOnClickListener {
            closeMenuWithAnimation()
            shareIcon.performClick()
        }
        menuEditItem.setOnClickListener {
            closeMenuWithAnimation()
            currentPlaylistId?.let { id ->
                val bundle = Bundle().apply {
                    putLong("playlistId", id)
                }

                findNavController().navigate(
                    R.id.action_playlistDetailFragment_to_editPlaylistFragment,
                    bundle
                )
            }
        }
        menuDeleteItem.setOnClickListener {
            closeMenuWithAnimation()
            AlertDialog.Builder(requireContext())
                .setTitle("Удалить плейлист")
                .setMessage("Вы уверены, что хотите удалить этот плейлист?")
                .setPositiveButton("Удалить") { _, _ ->
                    currentPlaylistId?.let { id ->
                        viewModel.deletePlaylist(id)
                    }
                }
                .setNegativeButton("Отмена", null)
                .show()
        }
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                PlaylistDetailUiState.Loading -> {}

                is PlaylistDetailUiState.Success -> {

                    updatePlaylistInfo(state.playlist)

                    if (state.tracks.isEmpty()) {
                        emptyTracksText.visibility = View.VISIBLE
                        tracksRecyclerView.visibility = View.GONE
                    } else {
                        emptyTracksText.visibility = View.GONE
                        tracksRecyclerView.visibility = View.VISIBLE
                        adapter?.submitList(state.tracks)
                    }

                    bottomSheetBehavior.isHideable = false
                    bottomSheetBehavior.state =
                        BottomSheetBehavior.STATE_COLLAPSED
                }

                is PlaylistDetailUiState.Deleted -> {

                    findNavController().popBackStack(
                        R.id.mediatekaFragment,
                        false
                    )
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
    }

    private fun updateMenuPlaylistInfo(playlist: Playlist) {

        menuPlaylistName.text = playlist.name
        menuPlaylistTrackCount.text = playlist.trackCount.toTracksText()

        loadCover(
            imageView = menuPlaylistCover,
            coverPath = playlist.coverPath
        )
    }

    private fun updatePlaylistInfo(playlist: Playlist) {

        playlistName.text = playlist.name
        playlistDescription.text = playlist.description.orEmpty()
        playlistDuration.text = "${playlist.durationFormatted} минут"
        playlistTrackCount.text = playlist.trackCount.toTracksText()

        loadCover(
            imageView = coverImageView,
            coverPath = playlist.coverPath
        )
    }

    private fun loadCover(
        imageView: ImageView,
        coverPath: String?
    ) {

        if (coverPath.isNullOrBlank()) {

            imageView.setImageResource(
                R.drawable.ic_placeholder_312
            )

        } else {

            Glide.with(this)
                .load(coverPath)
                .placeholder(R.drawable.ic_placeholder_312)
                .error(R.drawable.ic_placeholder_312)
                .centerCrop()
                .into(imageView)
        }
    }

    private fun closeMenuWithAnimation() {
        overlayDim.animate().alpha(0f).setDuration(200).withEndAction {
            overlayDim.visibility = View.GONE
        }.start()
        menuBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun onResume() {
        super.onResume()

        currentPlaylistId?.let {
            viewModel.loadPlaylist(it.toString())
        }
    }
}
