package com.practicum.playlistmaker.mediateka.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentPlaylistDetailBinding
import com.practicum.playlistmaker.mediateka.ui.PlaylistDetailUiState
import com.practicum.playlistmaker.mediateka.ui.view.PlaylistDetailViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistDetailFragment : Fragment() {
    private var _binding: FragmentPlaylistDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistDetailViewModel by viewModel()

    // Сохраняем ID в поле, чтобы при пересоздании фрагмента не потерять его
    private var currentPlaylistId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.GONE
        _binding = FragmentPlaylistDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем ID и сохраняем в поле
        currentPlaylistId = arguments?.getString("playlistId")
            ?: throw IllegalStateException("playlistId не передан")

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // Каждый раз при onViewCreated запускаем загрузку — это гарантирует актуальное состояние
        viewModel.loadPlaylist(currentPlaylistId!!)

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PlaylistDetailUiState.Loading -> {
                    // опционально: прогресс/заглушка
                }
                is PlaylistDetailUiState.Success -> {
                    val p = state.playlist
                    binding.playlistName.text = p.name
                    binding.playlistDescription.text = p.description ?: ""
                    binding.playlistTrackCount.text = "${p.trackCount} треков"

                    if (p.coverPath != null) {
                        Glide.with(this)
                            .load(p.coverPath)
                            .placeholder(R.drawable.ic_placeholder_312)
                            .error(R.drawable.ic_placeholder_312)
                            .into(binding.playlistCover)
                    } else {
                        binding.playlistCover.setImageResource(R.drawable.ic_placeholder_312)
                    }

                    // Длительность: заглушка, пока нет данных по трекам
                    binding.playlistDuration.text = p.durationFormatted
                }
                is PlaylistDetailUiState.Error -> {
                    Toast.makeText(requireContext(), "Ошибка загрузки плейлиста", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
        }

        binding.shareButton.setOnClickListener { /* TODO: логика Share */ }
        binding.menuButton.setOnClickListener { /* TODO: логика Меню */ }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.bottom_navigation)?.visibility = View.VISIBLE
        _binding = null
    }
}
