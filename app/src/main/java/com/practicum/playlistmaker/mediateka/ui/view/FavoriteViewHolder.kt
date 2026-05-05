package com.practicum.playlistmaker.mediateka.ui.view

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R

class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val artworkImageView: ImageView = itemView.findViewById(R.id.artwork_image)
    val trackNameTextView: TextView = itemView.findViewById(R.id.track_name)
    val artistNameTextView: TextView = itemView.findViewById(R.id.artist_name)
    val trackTimeTextView: TextView = itemView.findViewById(R.id.track_time)
}
