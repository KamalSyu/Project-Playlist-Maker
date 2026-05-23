package com.practicum.playlistmaker.mediateka.ui.view

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.domain.Playlist

class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val coverImage: ImageView = itemView.findViewById(R.id.playlistCover)
    private val nameText: TextView = itemView.findViewById(R.id.playlistName)
    private val trackCountText: TextView = itemView.findViewById(R.id.playlistTrackCount)

    fun bind(playlist: Playlist) {
        nameText.text = playlist.name
        trackCountText.text = "${playlist.trackCount} треков"

        if (!playlist.coverPath.isNullOrEmpty()) {
            coverImage.setImageURI(Uri.parse(playlist.coverPath))
        } else {
            coverImage.setImageResource(R.drawable.ic_placeholder_312) // Заглушка
        }
    }
}