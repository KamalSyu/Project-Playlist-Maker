package com.practicum.playlistmaker.mediateka.ui.view

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.domain.Playlist

class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val coverImage: ImageView = itemView.findViewById(R.id.playlistCover)
    private val nameText: TextView = itemView.findViewById(R.id.playlistName)
    private val trackCountText: TextView = itemView.findViewById(R.id.playlistTrackCount)
    fun bind(playlist: Playlist) {
        nameText.text = playlist.name
        trackCountText.text = when (playlist.trackCount) {
            0 -> "Нет треков"
            1 -> "${playlist.trackCount} трек"
            2, 3, 4 -> "${playlist.trackCount} трека"
            else -> "${playlist.trackCount} треков"
        }
        if (!playlist.coverPath.isNullOrEmpty()) {
            Glide.with(coverImage)
                .load(Uri.parse(playlist.coverPath))
                .placeholder(R.drawable.ic_placeholder_312)
                .error(R.drawable.ic_placeholder_312)
                .into(coverImage)
        } else {
            coverImage.setImageResource(R.drawable.ic_placeholder_312)
        }
    }
}

