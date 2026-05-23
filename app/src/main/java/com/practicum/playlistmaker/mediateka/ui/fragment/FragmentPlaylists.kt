package com.practicum.playlistmaker.mediateka.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.mediateka.ui.PlaylistsUiState
import com.practicum.playlistmaker.mediateka.ui.adapter.PlaylistsAdapter
import com.practicum.playlistmaker.mediateka.ui.view.PlaylistsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentPlaylists : Fragment() {

    private val viewModel: PlaylistsViewModel by viewModel()
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
            findNavController().navigate(
                R.id.action_mediatekaFragment_to_createPlaylistFragment
            )
        }
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                PlaylistsUiState.Loading -> showLoading()
                is PlaylistsUiState.Success -> showPlaylistsList(state.playlists)
                is PlaylistsUiState.Error -> showError(state.error)
                PlaylistsUiState.Empty -> showEmptyState()
            }
        }
        viewModel.loadPlaylists()
        return view
    }

    private fun showEmptyState() {
        playlistsRecyclerView.visibility = View.GONE
        emptyPlaylistsLayout.visibility = View.VISIBLE
    }

    fun refreshPlaylists() {
        viewModel.loadPlaylists()
    }

    private fun showPlaylistsList(playlists: List<Playlist>) {
        emptyPlaylistsLayout.visibility = View.GONE
        playlistsRecyclerView.visibility = View.VISIBLE
        playlistsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        val adapter = PlaylistsAdapter()
        playlistsRecyclerView.adapter = adapter
        adapter.updatePlaylists(playlists)
    }

    private fun setupNewPlaylistButton(view: View) {
        val newPlaylistButton: Button = view.findViewById(R.id.newPlaylistButton)
        newPlaylistButton.setOnClickListener {
            findNavController().navigate(R.id.action_mediatekaFragment_to_createPlaylistFragment)
        }
    }

    private fun showLoading() {
        playlistsRecyclerView.visibility = View.GONE
        emptyPlaylistsLayout.visibility = View.VISIBLE
    }

    private fun showError(error: Throwable) {
        Toast.makeText(requireContext(), "Ошибка загрузки: ${error.message}", Toast.LENGTH_LONG).show()
        showEmptyState()
    }

    companion object {
        fun newInstance(): Fragment {
            return FragmentPlaylists()
        }
    }
}
