package com.practicum.playlistmaker.mediateka.ui.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.models.toParcelable
import com.practicum.playlistmaker.core.utils.FormatTrackDurationUseCase
import com.practicum.playlistmaker.mediateka.data.db.PlaylistTrackEntity
import com.practicum.playlistmaker.mediateka.ui.PlaylistDetailUiState
import com.practicum.playlistmaker.mediateka.ui.adapter.PlaylistTracksAdapter
import com.practicum.playlistmaker.mediateka.ui.view.PlaylistDetailViewModel
import com.practicum.playlistmaker.search.ui.parcel.ParcelableTrack
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

    private lateinit var tracksRecyclerView: RecyclerView
    private var adapter: PlaylistTracksAdapter? = null

    private var currentPlaylistId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playlist_detail, container, false)

        playlistName = view.findViewById(R.id.playlistName)
        playlistDescription = view.findViewById(R.id.playlistDescription)
        playlistDuration = view.findViewById(R.id.playlistDuration)
        playlistTrackCount = view.findViewById(R.id.playlistTrackCount)
        coverImageView = view.findViewById(R.id.playlistCover)

        tracksRecyclerView = view.findViewById(R.id.tracksRecyclerView)
        tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // BottomSheet: нельзя скрыть
        val bottomSheetFrame = view.findViewById<View>(R.id.bottomSheetFrame)
        val behavior = BottomSheetBehavior.from(bottomSheetFrame)
        behavior.isHideable = false

        arguments?.getString("playlistId")?.let { idString ->
            currentPlaylistId = idString.toLongOrNull()
        }

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
                currentPlaylistId?.let { pid ->
                    showDeleteTrackDialog(pid, track)
                }
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

                    // Обложка: если нет — показываем плейсхолдер
                    Glide.with(this)
                        .load(playlist.coverPath)
                        .placeholder(R.drawable.ic_placeholder_312)
                        .error(R.drawable.ic_placeholder_312)
                        .centerCrop()
                        .into(coverImageView)

                    playlistDuration.text = playlist.durationFormatted
                    playlistTrackCount.text = "${playlist.trackCount} треков"

                    // МАППИНГ: все поля конструктора Track явно переданы, без ошибок компиляции
                    val tracksForUi = state.tracks.map { entity ->
                        Track(
                            trackId = entity.trackId,
                            trackName = entity.title,
                            artistName = entity.artist,
                            trackTimeMillis = (entity.duration * 1000).toLong(),      // Long (не nullable)
                            artworkUrl100 = null,                                     // нет в entity → null
                            releaseDate = null,
                            collectionName = null,
                            primaryGenreName = null,
                            country = null,
                            previewUrl = null,
                            addedDate = 0L,                                            // нет в entity → 0L
                            isFavorite = false                                       // дефолт, можно не писать, но явно — понятнее
                        )
                    }


                    adapter?.submitList(tracksForUi)

                    if (tracksForUi.isNotEmpty()) {
                        behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    } else {
                        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                }
                is PlaylistDetailUiState.Error -> {
                    val errorMessage = state.error.message ?: "Не удалось загрузить плейлист"
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }

            }
        }

        currentPlaylistId?.let {
            viewModel.loadPlaylist(it.toString())
        }

        return view
    }

    private fun showDeleteTrackDialog(playlistId: Long, track: Track) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Хотите удалить трек?")
        builder.setMessage("Трек «${track.trackName}» будет удалён из плейлиста.")
        builder.setPositiveButton("ДА") { dialog, which ->
            viewModel.removeTrack(playlistId, track.trackId)
        }
        builder.setNegativeButton("НЕТ", null)

        val dialog = builder.create()
        dialog.show()
    }



}
