package com.practicum.playlistmaker.mediateka.ui.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.utils.FormatTrackDurationUseCase
import com.practicum.playlistmaker.mediateka.data.db.PlaylistTrackEntity

class PlaylistTracksViewHolder(
    itemView: View,
    private val formatDurationUseCase: FormatTrackDurationUseCase
) : RecyclerView.ViewHolder(itemView) {

    private val artworkImageView: ImageView = itemView.findViewById(R.id.artwork_image)
    private val trackNameTextView: TextView = itemView.findViewById(R.id.track_name)
    private val artistNameTextView: TextView = itemView.findViewById(R.id.artist_name)
    private val trackTimeTextView: TextView = itemView.findViewById(R.id.track_time)

    fun bind(track: Track) {
        trackNameTextView.text = track.trackName
        artistNameTextView.text = track.artistName

        track.trackTimeMillis?.let { timeMillis ->
            trackTimeTextView.text = formatDurationUseCase.invoke(timeMillis)
        } ?: run {
            trackTimeTextView.text = ""
        }
        if (track.artworkUrl100.isNullOrBlank()) {
            artworkImageView.setImageResource(R.drawable.ic_placeholder_312)
        } else {
            Glide.with(itemView.context)
                .load(track.getHighQualityArtworkUrl())
                .placeholder(R.drawable.ic_placeholder_312)
                .error(R.drawable.ic_placeholder_312)
                .centerCrop()
                .transform(RoundedCorners(2))
                .into(artworkImageView)
        }
    }

}
