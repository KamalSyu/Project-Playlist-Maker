package com.practicum.playlistmaker.presentation.viewholder

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.presentation.util.DateFormatter
import java.text.SimpleDateFormat
import java.util.Locale


class AlbumViewHolder(
    itemView: View,
    private val onClickListener: (Track) -> Unit,
    private val onPlayButtonClick: (Track) -> Unit
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

    private val dateFormatter = DateFormatter()

    fun bind(track: Track, isPlaying: Boolean, currentTimeMillis: Long = 0) {
        textTrackName.text = track.trackName
        textArtistName.text = track.artistName


        // Время воспроизведения
        if (currentTimeMillis > 0) {
            val formattedCurrent = SimpleDateFormat("mm:ss", Locale.getDefault())
                .format(currentTimeMillis)
            timeTextView.text = formattedCurrent
        } else {
            timeTextView.text = ""
        }

        // Длительность трека
        if (track.trackTimeMillis != null) {
            val formattedTrack = SimpleDateFormat("mm:ss", Locale.getDefault())
                .format(track.trackTimeMillis)
            trackTimeMillisTextView.text = formattedTrack
        } else {
            trackTimeMillisTextView.text = ""
        }

        releaseDateTextView.text = dateFormatter.formatReleaseDate(track.releaseDate)
        collectionNameTextView.text = track.collectionName
        primaryGenreNameTextView.text = track.primaryGenreName
        countryTextView.text = track.country

        // Загрузка изображения
        val cornerRadiusPx = (8 * itemView.resources.displayMetrics.density).toInt()
        Glide.with(itemView.context)
            .load(track.getHighQualityArtworkUrl())
            .placeholder(R.drawable.ic_placeholder_312)
            .error(R.drawable.ic_placeholder_312)
            .centerCrop()
            .transform(RoundedCorners(cornerRadiusPx))
            .into(albumImageView)

        // Обновляем иконку кнопки плей/пауза
        updatePlayButtonState(isPlaying)

        // Настраиваем клики
        setupClickListeners(track)
    }

    private fun setupClickListeners(track: Track) {
        itemView.setOnClickListener { onClickListener(track) }
        playButton.setOnClickListener { onPlayButtonClick(track) }
    }

    fun updatePlayButtonState(isPlaying: Boolean) {
        playButton.setImageResource(
            if (isPlaying) R.drawable.ic_pause_button
            else R.drawable.ic_play_button
        )
    }
}
