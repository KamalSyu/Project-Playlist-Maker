package com.practicum.playlistmaker.mediateka.ui.view

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.mediateka.domain.model.PlaylistForMediateka

class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val coverImage: ImageView = itemView.findViewById(R.id.playlistCover)
    private val nameText: TextView = itemView.findViewById(R.id.playlistName)

    fun bind(playlist: PlaylistForMediateka) {
        nameText.text = playlist.name
        if (!playlist.coverPath.isNullOrEmpty()) {
            coverImage.setImageURI(Uri.parse(playlist.coverPath))
        } else {
            coverImage.setImageResource(R.drawable.ic_placeholder_312) // Заглушка
        }
    }
}
