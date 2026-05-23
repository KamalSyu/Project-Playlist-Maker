package com.practicum.playlistmaker.player.ui.view

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.player.domain.model.PlaylistForPlayer

class PlaylistSelectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val playlistCover: ImageView = itemView.findViewById(R.id.playlistCover)
    private val playlistName: TextView = itemView.findViewById(R.id.playlistName)

    fun bind(playlist: PlaylistForPlayer) {
        playlistName.text = playlist.name
        Glide.with(itemView.context)
            .load(playlist.coverPath)
            .placeholder(R.drawable.ic_placeholder_312)
            .into(playlistCover)
    }
}