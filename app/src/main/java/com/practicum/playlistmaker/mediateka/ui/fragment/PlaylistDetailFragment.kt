package com.practicum.playlistmaker.mediateka.ui.fragment

import android.os.Bundle
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

    // --- Views: информация о плейлисте ---
    private lateinit var playlistName: TextView
    private lateinit var playlistYear: TextView
    private lateinit var playlistDuration: TextView
    private lateinit var playlistTrackCount: TextView
    private lateinit var coverImageView: com.google.android.material.imageview.ShapeableImageView

    // --- Иконки (НЕ кнопки!) ---
    private lateinit var shareIcon: ImageView
    private lateinit var menuIcon: ImageView

    // --- Список треков ---
    private lateinit var tracksRecyclerView: RecyclerView
    private var adapter: PlaylistTracksAdapter? = null

    // --- BottomSheet: список треков ---
    private lateinit var bottomSheetFrame: View
    private lateinit var behavior: BottomSheetBehavior<View>

    // --- Прочее ---
    private var currentPlaylistId: Long? = null
    private lateinit var overlayDim: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playlist_detail, container, false)

        // --- Инициализация views ---
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

        // --- Настройка BottomSheet для списка треков ---
        bottomSheetFrame = view.findViewById(R.id.bottomSheetFrame)
        behavior = BottomSheetBehavior.from(bottomSheetFrame)
        behavior.isHideable = false
        behavior.peekHeight = 200 // PeekHeight только здесь, в коде

        // --- Чтение playlistId из аргументов ---
        arguments?.getString("playlistId")?.let { idString ->
            currentPlaylistId = idString.toLongOrNull()
        }

        // --- Создание адаптера ДО подписки на LiveData ---
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
                // Здесь будет логика долгого нажатия (например, удаление трека).
                Toast.makeText(requireContext(), "Long click: ${track.trackName}", Toast.LENGTH_SHORT).show()
            },
            formatDurationUseCase = formatDurationUseCase
        )
        tracksRecyclerView.adapter = adapter

        // --- Кнопка «Назад» (ImageView) ---
        val backButton = view.findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // --- Обработчик клика «Поделиться» ---
        shareIcon.setOnClickListener {
            Toast.makeText(requireContext(), "Поделиться", Toast.LENGTH_SHORT).show()
            // Сюда позже вставить логику ShareUseCase
        }

        // --- Обработчик клика «Меню» (три точки) ---
        menuIcon.setOnClickListener {
            // Здесь можно открыть PopupMenu или BottomSheet с действиями.
            // Пока заглушка — чтобы сигнатура совпадала.
            Toast.makeText(requireContext(), "Меню", Toast.LENGTH_SHORT).show()
        }

        // --- Подписка на UI State ---
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                PlaylistDetailUiState.Loading -> {
                    // Опционально: можно показать прогресс или оставить как есть
                }

                is PlaylistDetailUiState.Success -> {
                    val playlist = state.playlist

                    // Заполнение полей информацией плейлиста
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

                    // --- Отрисовка списка треков ---
                    adapter?.submitList(state.tracks)

                    // --- Логика состояния BottomSheet ---
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

        // Запуск загрузки плейлиста, если ID получен
        currentPlaylistId?.let {
            viewModel.loadPlaylist(it.toString())
        }

        return view
    }
}
