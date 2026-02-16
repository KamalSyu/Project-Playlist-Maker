package com.practicum.playlistmaker.search.ui.viewholder

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.FormatTrackDurationUseCaseContract
import com.practicum.playlistmaker.core.utils.DateFormatter

class AlbumViewHolder(
    itemView: View,
    private val onClickListener: (Track) -> Unit,
    private val onPlayButtonClick: (Track) -> Unit,
    private val onAddToPlaylistClick: (Track) -> Unit,
    private val onFavoriteClick: (Track) -> Unit,
    private val formatDurationUseCase: FormatTrackDurationUseCaseContract
) : RecyclerView.ViewHolder(itemView) {

    private val albumImageView: ImageView = itemView.findViewById(R.id.album)
    private val textTrackName: TextView = itemView.findViewById(R.id.textTrackName)
    private val textArtistName: TextView = itemView.findViewById(R.id.textArtistName)
    private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
    private val trackTimeMillisTextView: TextView = itemView.findViewById(R.id.trackTimeMillis)
    private val collectionNameTextView: TextView = itemView.findViewById(R.id.collectionName)
    private val releaseDateTextView: TextView = itemView.findViewById(R.id.releaseDate)
    private val primaryGenreNameTextView: TextView = itemView.findViewById(R.id.primaryGenreName)
    private val countryTextView: TextView = itemView.findViewById(R.id.country)
    private val playButton: ImageButton = itemView.findViewById(R.id.ic_play_button)
    private val plusButton: Button = itemView.findViewById(R.id.ic_button_plus)
    private val likeButton: Button = itemView.findViewById(R.id.ic_button_like)

    private val dateFormatter = DateFormatter()
    private var currentTrack: Track? = null

    fun bind(
        track: Track,
        isPlaying: Boolean,
        currentTimeMillis: Long = 0
    ) {
        currentTrack = track

        textTrackName.text = track.trackName
        textArtistName.text = track.artistName
        releaseDateTextView.text = dateFormatter.formatReleaseDate(track.releaseDate)
        collectionNameTextView.text = track.collectionName
        primaryGenreNameTextView.text = track.primaryGenreName
        countryTextView.text = track.country

        timeTextView.text = formatDurationUseCase.invoke(currentTimeMillis)

        trackTimeMillisTextView.text = track.trackTimeMillis?.let {
            formatDurationUseCase.invoke(it)
        } ?: ""

        val cornerRadiusPx = (8 * itemView.resources.displayMetrics.density).toInt()
        if (track.artworkUrl100 != null) {
            Glide.with(itemView.context)
                .load(track.getHighQualityArtworkUrl())
                .placeholder(R.drawable.ic_placeholder_312)
                .error(R.drawable.ic_placeholder_312)
                .centerCrop()
                .transform(RoundedCorners(cornerRadiusPx))
                .into(albumImageView)
        } else {
            albumImageView.setImageResource(R.drawable.ic_placeholder_312)
        }

        updatePlayButtonState(isPlaying)
        setupClickListeners(track)
    }

    private fun setupClickListeners(track: Track) {
        itemView.setOnClickListener { onClickListener(track) }
        playButton.setOnClickListener { onPlayButtonClick(track) }
        plusButton.setOnClickListener { onAddToPlaylistClick(track) }
        likeButton.setOnClickListener { onFavoriteClick(track) }
    }

    fun updatePlayButtonState(isPlaying: Boolean) {
        Log.d("AlbumViewHolder", "updatePlayButtonState: isPlaying=$isPlaying")
        playButton.setImageResource(
            if (isPlaying) R.drawable.ic_pause_button
            else R.drawable.ic_play_button
        )
    }
}