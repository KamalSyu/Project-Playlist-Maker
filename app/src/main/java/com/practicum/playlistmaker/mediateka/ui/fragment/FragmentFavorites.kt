package com.practicum.playlistmaker.mediateka.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.models.toParcelable
import com.practicum.playlistmaker.core.utils.FormatTrackDurationUseCase
import com.practicum.playlistmaker.mediateka.ui.adapter.FavoriteTrackAdapter
import com.practicum.playlistmaker.mediateka.ui.view.FavoriteTracksViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class FragmentFavorites : Fragment() {

    private val viewModel: FavoriteTracksViewModel by viewModel()
    private lateinit var emptyStateLayout: View
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var adapter: FavoriteTrackAdapter

    private val formatDurationUseCase: FormatTrackDurationUseCase by inject()

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
        adapter = FavoriteTrackAdapter(
            onTrackClick = { track -> navigateToAudioPlayer(track) },
            formatDurationUseCase = formatDurationUseCase
        )
        favoritesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        favoritesRecyclerView.adapter = adapter
        // Сразу можно передать пустой список, если нужно
        adapter.submitList(emptyList())
    }



    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FavoriteTracksViewModel.State.Empty -> showEmptyState()
                is FavoriteTracksViewModel.State.WithTracks -> showFavoritesList(state.tracks)
                is FavoriteTracksViewModel.State.Error -> showErrorState(state.message)
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
        adapter.submitList(tracks)
    }

    private fun navigateToAudioPlayer(track: Track) {
        val parcelableTrack = track.toParcelable()
        if (parcelableTrack == null) {
            throw IllegalArgumentException("Cannot convert Track to ParcelableTrack")
        }

        val bundle = Bundle().apply {
            putParcelable("track", parcelableTrack)
        }

        try {
            findNavController().navigate(R.id.audioPlayerFragment, bundle)
        } catch (e: Exception) {
            Log.e("Navigation", "Failed to navigate to audio player", e)
        }
    }

    private fun showErrorState(errorMessage: String) {
        favoritesRecyclerView.visibility = View.GONE
        emptyStateLayout.visibility = View.VISIBLE
    }
    companion object {
        fun newInstance(): Fragment {
            return FragmentFavorites()
        }
    }
}