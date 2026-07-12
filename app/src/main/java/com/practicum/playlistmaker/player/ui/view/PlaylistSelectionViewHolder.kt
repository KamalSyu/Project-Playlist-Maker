package com.practicum.playlistmaker.player.ui.view

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.player.domain.model.PlaylistForPlayer

class PlaylistSelectionViewHolder(
    itemView: View,
    private val onPlaylistClick: (PlaylistForPlayer) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val playlistCover: ImageView = itemView.findViewById(R.id.playlistCover)
    private val playlistName: TextView = itemView.findViewById(R.id.playlistName)
    private val playlistTrackCount: TextView = itemView.findViewById(R.id.playlistTrackCount)

    fun bind(playlist: PlaylistForPlayer) {
        playlistName.text = playlist.name
        playlistTrackCount.text = formatTrackCount(playlist.trackCount)
        loadPlaylistCover(playlist.coverPath)
        itemView.setOnClickListener { onPlaylistClick(playlist) }
    }

    private fun loadPlaylistCover(coverPath: String?) {
        if (!coverPath.isNullOrEmpty()) {
            Glide.with(itemView.context)
                .load(coverPath)
                .placeholder(R.drawable.ic_placeholder_312)
                .error(R.drawable.ic_placeholder_312)
                .centerCrop()
                .transform(RoundedCorners(8))
                .into(playlistCover)
        } else {
            playlistCover.setImageResource(R.drawable.ic_placeholder_312)
        }
    }

    companion object {
        fun formatTrackCount(trackCount: Int): String = when (trackCount) {
            0 -> "Нет треков"
            1 -> "${trackCount} трек"
            2, 3, 4 -> "${trackCount} трека"
            else -> "${trackCount} треков"
        }
    }
}
