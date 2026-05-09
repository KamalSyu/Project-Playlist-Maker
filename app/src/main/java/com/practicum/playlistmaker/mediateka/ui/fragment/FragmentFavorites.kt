package com.practicum.playlistmaker.mediateka.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.contract.FormatTrackDurationUseCaseContract
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.models.toParcelable
import com.practicum.playlistmaker.mediateka.ui.view.FavoriteTracksViewModel
import com.practicum.playlistmaker.player.ui.adapter.PlayerTrackAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentFavorites : Fragment() {

    private val viewModel: FavoriteTracksViewModel by viewModel()
    private lateinit var emptyStateLayout: View
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var adapter: PlayerTrackAdapter

    // Внедряем UseCase для форматирования длительности трека через Koin
    private lateinit var formatDurationUseCase: FormatTrackDurationUseCaseContract

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_favorites, container, false)
        setupViews(view)
        setupRecyclerView()
        observeViewModel()
        return view
    }

    private fun setupViews(view: View) {
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        favoritesRecyclerView = view.findViewById(R.id.favoritesRecyclerView)
    }

    private fun setupRecyclerView() {
        adapter = PlayerTrackAdapter(
            tracks = emptyList(),
            onClickPlayButton = { track ->
                navigateToAudioPlayer(track)
            },
            onAddToPlaylist = {  },
            onFavorite = { track ->},
            formatDurationUseCase = formatDurationUseCase
        )

        favoritesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@adapter
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FavoriteTracksViewModel.State.Empty -> showEmptyState()
                is FavoriteTracksViewModel.State.WithTracks -> showFavoritesList(state.tracks)
            }
        }
    }

    private fun showEmptyState() {
        favoritesRecyclerView.visibility = View.GONE
        emptyStateLayout.visibility = View.VISIBLE
    }

    private fun showFavoritesList(tracks: List<Track>) {
        emptyStateLayout.visibility = View.GONE
        favoritesRecyclerView.visibility = View.VISIBLE
        adapter.updateList(tracks)
    }

    private fun navigateToAudioPlayer(track: Track) {
        // Используем навигацию через Directions
        val action = MediatekaFragmentDirections.actionMediatekaToAudioPlayer(
            trackParcelable = track.toParcelable()
        )
        findNavController().navigate(action)
    }
    companion object {
        fun newInstance(): Fragment {
            return FragmentFavorites()
        }
    }
}