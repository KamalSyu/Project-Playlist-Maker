package com.practicum.playlistmaker.mediateka.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AlertDialog

import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.mediateka.ui.PlaylistsUiState
import com.practicum.playlistmaker.mediateka.ui.adapter.PlaylistsAdapter
import com.practicum.playlistmaker.mediateka.ui.view.PlaylistsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.practicum.playlistmaker.core.utils.GridSpacingItemDecoration

class FragmentPlaylists : Fragment() {
    private val viewModel: PlaylistsViewModel by viewModel()
    private lateinit var newPlaylistButton: Button
    private lateinit var playlistsRecyclerView: RecyclerView
    private lateinit var emptyPlaylistsLayout: View
    private lateinit var emptyStateImage: ImageView
    private lateinit var emptyStateText: TextView

    private lateinit var playlistsAdapter: PlaylistsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playlists, container, false)
        newPlaylistButton = view.findViewById(R.id.newPlaylistButton)
        playlistsRecyclerView = view.findViewById(R.id.playlistsRecyclerView)
        emptyPlaylistsLayout = view.findViewById(R.id.emptyPlaylistsLayout)
        emptyStateImage = view.findViewById(R.id.emptyStateImage)
        emptyStateText = view.findViewById(R.id.emptyStateText)
        newPlaylistButton.setOnClickListener {
            findNavController().navigate(R.id.action_mediatekaFragment_to_createPlaylistFragment)
        }
        val spanCount = 2
        val edgeSpacing = resources.getDimensionPixelSize(R.dimen.spacing_16)
        val columnSpacing = resources.getDimensionPixelSize(R.dimen.spacing_8)
        playlistsRecyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)
        playlistsRecyclerView.addItemDecoration(
            GridSpacingItemDecoration(spanCount, edgeSpacing, columnSpacing)
        )
        playlistsAdapter = PlaylistsAdapter(
            playlists = emptyList(),
            onPlaylistAction = { playlist, action ->
                when (action) {
                    PlaylistsAdapter.Action.RENAME -> showRenameDialog(playlist)
                    PlaylistsAdapter.Action.DELETE -> showDeleteDialog(playlist)
                }
            },
            onPlaylistClick = { playlist ->
                val bundle = Bundle().apply {
                    putString("playlistId", playlist.id)
                }
                findNavController().navigate(
                    R.id.action_mediatekaFragment_to_playlistDetailFragment,
                    bundle
                )
            }
        )
        playlistsRecyclerView.adapter = playlistsAdapter

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                PlaylistsUiState.Loading -> showLoading()
                is PlaylistsUiState.Success -> {
                    playlistsAdapter.submitList(state.playlists)
                    showPlaylistsList()
                }
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
        emptyStateImage.visibility = View.VISIBLE
        emptyStateText.visibility = View.VISIBLE

    }
    private fun showPlaylistsList() {
        emptyPlaylistsLayout.visibility = View.GONE
        emptyStateImage.visibility = View.GONE
        emptyStateText.visibility = View.GONE
        playlistsRecyclerView.visibility = View.VISIBLE
    }
    private fun showLoading() {
        playlistsRecyclerView.visibility = View.GONE
        emptyPlaylistsLayout.visibility = View.VISIBLE
        emptyStateImage.visibility = View.GONE
        emptyStateText.visibility = View.GONE
    }
    private fun showError(error: Throwable) {
        Toast.makeText(requireContext(), "Ошибка загрузки: ${error.message}", Toast.LENGTH_LONG).show()
        showEmptyState()
    }
    private fun showRenameDialog(playlist: Playlist) {
        val input = EditText(requireContext()).apply {
            setText(playlist.name)
            setSelection(playlist.name.length)
            inputType = android.text.InputType.TYPE_CLASS_TEXT
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Переименовать плейлист")
            .setView(input)
            .setPositiveButton("ОК") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    viewModel.renamePlaylist(playlist.id, newName)
                } else {
                    Toast.makeText(requireContext(), "Имя не может быть пустым", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    private fun showDeleteDialog(playlist: Playlist) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить плейлист")
            .setMessage("Вы уверены, что хотите удалить плейлист «${playlist.name}»?")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deletePlaylist(playlist.id)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    override fun onResume() {
        super.onResume()
        viewModel.loadPlaylists()
    }
    companion object {
        fun newInstance(): Fragment = FragmentPlaylists()
    }
}
