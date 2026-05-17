package com.practicum.playlistmaker.mediateka.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.mediateka.domain.model.PlaylistData
import com.practicum.playlistmaker.mediateka.ui.adapter.PlaylistsAdapter
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
            findNavController().navigate(
                R.id.action_mediatekaFragment_to_createPlaylistFragment
            )
        }

        viewModel.loadPlaylists()

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

    private fun showPlaylistsList(playlists: List<PlaylistData>) {
        emptyPlaylistsLayout.visibility = View.GONE
        playlistsRecyclerView.visibility = View.VISIBLE

        // Устанавливаем GridLayoutManager для двух колонок
        playlistsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        // Создаём и устанавливаем адаптер
        val adapter = PlaylistsAdapter()
        playlistsRecyclerView.adapter = adapter

        // Обновляем данные в адаптере
        adapter.updatePlaylists(playlists)
    }

    companion object {
        fun newInstance(): Fragment {
            return FragmentPlaylists()
        }
    }
}
