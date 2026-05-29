package com.practicum.playlistmaker.player.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.player.domain.model.PlaylistForPlayer
import com.practicum.playlistmaker.player.ui.view.PlaylistSelectionViewHolder

class PlaylistSelectionAdapter(
    private val onPlaylistClick: (PlaylistForPlayer) -> Unit
) : RecyclerView.Adapter<PlaylistSelectionViewHolder>() {
    private var playlists: List<PlaylistForPlayer> = emptyList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistSelectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.playlist_selection_item, parent, false)
        return PlaylistSelectionViewHolder(view, onPlaylistClick)
    }
    override fun onBindViewHolder(holder: PlaylistSelectionViewHolder, position: Int) {
        holder.bind(playlists[position])
    }
    override fun getItemCount(): Int = playlists.size
    fun updatePlaylists(newPlaylists: List<PlaylistForPlayer>) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }
}
