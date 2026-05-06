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
import com.practicum.playlistmaker.core.models.parcel.toParcelable
import com.practicum.playlistmaker.core.ui.adapter.TrackListAdapter
import com.practicum.playlistmaker.mediateka.ui.view.FavoritesState
import com.practicum.playlistmaker.mediateka.ui.view.FragmentFavoritesViewModel
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentFavorites : Fragment() {

    private val viewModel: FragmentFavoritesViewModel by viewModel()
    private lateinit var emptyStateLayout: View
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var adapter: TrackListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)

        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        favoritesRecyclerView = view.findViewById(R.id.favoritesRecyclerView)
        favoritesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FavoritesState.Empty -> showEmptyState()
                is FavoritesState.Loaded -> showFavoritesList(state.tracks)
            }
        }
        return view
    }

    private fun showEmptyState() {
        favoritesRecyclerView.visibility = View.GONE
        emptyStateLayout.visibility = View.VISIBLE
    }

    private fun showFavoritesList(tracks: List<Track>) {
        emptyStateLayout.visibility = View.GONE
        favoritesRecyclerView.visibility = View.VISIBLE

        val formatDurationUseCase: FormatTrackDurationUseCaseContract = get()

        adapter = TrackListAdapter(
            tracks = tracks,
            formatDurationUseCase = formatDurationUseCase,
            onItemClick = { selectedTrack ->
                // Преобразуем Track в ParcelableTrack
                val parcelableTrack = selectedTrack.toParcelable()

                // Создаём Bundle для передачи аргумента
                val bundle = Bundle().apply {
                    putParcelable("trackParcelable", parcelableTrack)
                }

                // Выполняем навигацию с передачей Bundle
                findNavController().navigate(
                    R.id.action_fragmentFavorites_to_audioPlayerFragment,
                    bundle
                )
            }
        )
        favoritesRecyclerView.adapter = adapter
    }

    companion object {
        fun newInstance(): Fragment {
            return FragmentFavorites()
        }
    }
}
