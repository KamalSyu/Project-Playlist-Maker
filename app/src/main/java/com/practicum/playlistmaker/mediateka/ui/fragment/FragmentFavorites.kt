package com.practicum.playlistmaker.mediateka.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.mediateka.ui.view.FragmentFavoritesViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentFavorites : Fragment() {

    private val viewModel: FragmentFavoritesViewModel by viewModel()
    private lateinit var emptyStateLayout: View
    private lateinit var favoritesRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)

        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        favoritesRecyclerView = view.findViewById(R.id.favoritesRecyclerView)
        viewModel.favorites.observe(viewLifecycleOwner) { tracks: List<Track> ->
            if (tracks.isEmpty()) {
                showEmptyState()
            } else {
                showFavoritesList(tracks)
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
    }

    companion object {
        fun newInstance(): Fragment {
            return FragmentFavorites()
        }
    }
}
