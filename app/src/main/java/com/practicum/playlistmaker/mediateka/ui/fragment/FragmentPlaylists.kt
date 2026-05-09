package com.practicum.playlistmaker.mediateka.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.mediateka.ui.view.FragmentPlaylistsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentPlaylists : Fragment() {

    private val viewModel: FragmentPlaylistsViewModel by viewModel()
    private lateinit var newPlaylistButton: View
    private lateinit var playlistsRecyclerView: RecyclerView
    private lateinit var emptyPlaylistsLayout: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playlists, container, false)
        newPlaylistButton = view.findViewById(R.id.newPlaylistButton)
        playlistsRecyclerView = view.findViewById(R.id.playlistsRecyclerView)
        emptyPlaylistsLayout = view.findViewById(R.id.emptyPlaylistsLayout)
        newPlaylistButton.setOnClickListener {
        }
        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            if (playlists.isEmpty()) {
                showEmptyState()
            } else {
                showPlaylistsList(playlists)
            }
        }
        return view
    }

    private fun showEmptyState() {
        playlistsRecyclerView.visibility = View.GONE
        emptyPlaylistsLayout.visibility = View.VISIBLE
    }

    private fun showPlaylistsList(playlists: List<Track>) {
        emptyPlaylistsLayout.visibility = View.GONE
        playlistsRecyclerView.visibility = View.VISIBLE
    }

    companion object {
        fun newInstance(): Fragment {
            return FragmentPlaylists()
        }
    }
}