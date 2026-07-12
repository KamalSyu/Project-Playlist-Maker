package com.practicum.playlistmaker.mediateka.ui.view

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.utils.FormatTrackDurationUseCase

class FavoriteViewHolder(
    itemView: View,
    private val formatDurationUseCase: FormatTrackDurationUseCase
) : RecyclerView.ViewHolder(itemView) {
    private val artworkImageView: ImageView = itemView.findViewById(R.id.artwork_image)
    private val trackNameTextView: TextView = itemView.findViewById(R.id.track_name)
    private val artistNameTextView: TextView = itemView.findViewById(R.id.artist_name)
    private val trackTimeTextView: TextView = itemView.findViewById(R.id.track_time)
    private var track: Track? = null
    fun bind(track: Track) {
        this.track = track
        trackNameTextView.text = track.trackName
        artistNameTextView.text = track.artistName
        track.trackTimeMillis?.let { timeMillis ->
            trackTimeTextView.text = formatDurationUseCase.invoke(timeMillis)
        } ?: run {
            trackTimeTextView.text = ""
        }
        Glide.with(itemView.context)
            .load(track.artworkUrl100)
            .placeholder(R.drawable.ic_placeholder_45)
            .error(R.drawable.ic_placeholder_45)
            .centerCrop()
            .transform(RoundedCorners(2))
            .into(artworkImageView)
    }
}
