package com.practicum.playlistmaker.mediateka.ui.view

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.mediateka.domain.model.PlaylistData

class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val coverImage: ImageView = itemView.findViewById(R.id.playlistCover)
    private val nameText: TextView = itemView.findViewById(R.id.playlistName)

    fun bind(playlist: PlaylistData) {
        nameText.text = playlist.name

        // Устанавливаем заглушку, если нет обложки
        if (!playlist.coverPath.isNullOrEmpty()) {
            // Здесь должна быть логика загрузки изображения (Glide/Picasso)
            // Для примера просто устанавливаем путь как текст
            coverImage.setImageURI(Uri.parse(playlist.coverPath))
        } else {
            coverImage.setImageResource(R.drawable.ic_placeholder_312) // Заглушка
        }
    }
}
