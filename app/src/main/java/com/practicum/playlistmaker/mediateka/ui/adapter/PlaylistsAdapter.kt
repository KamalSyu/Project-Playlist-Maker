package com.practicum.playlistmaker.mediateka.ui.adapter

import android.app.Notification.Action
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.mediateka.ui.view.PlaylistViewHolder

class PlaylistsAdapter(
    private var playlists: List<Playlist> = emptyList(),
    private val onPlaylistClick: (Playlist) -> Unit,
    private val onPlaylistAction: (Playlist, Action) -> Unit
) : RecyclerView.Adapter<PlaylistViewHolder>() {

    enum class Action {
        RENAME,
        DELETE
    }

    fun updatePlaylists(newPlaylists: List<Playlist>) {
        this.playlists = newPlaylists
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }
    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position])
        holder.itemView.setOnClickListener {
            onPlaylistClick(playlists[position])
        }
    }
    override fun getItemCount(): Int = playlists.size
}
