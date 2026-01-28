package com.practicum.playlistmaker.presentation.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.domain.usecase.FormatTrackDurationUseCase
import java.text.SimpleDateFormat
import java.util.Locale

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val artworkImageView: ImageView = itemView.findViewById(R.id.artwork_image)
    private val trackNameTextView: TextView = itemView.findViewById(R.id.track_name)
    private val artistNameTextView: TextView = itemView.findViewById(R.id.artist_name)
    private val trackTimeTextView: TextView = itemView.findViewById(R.id.track_time)

    fun bind(track: Track) {
        trackNameTextView.text = track.trackName
        artistNameTextView.text = track.artistName

        // Безопасная обработка nullable-значения
        track.trackTimeMillis?.let { timeMillis ->
            val formattedTime = FormatTrackDurationUseCase()(timeMillis)
            trackTimeTextView.text = formattedTime
        } ?: run {
            trackTimeTextView.text = ""
        }

        // Загружаем обложку через Glide
        Glide.with(itemView.context)
            .load(track.artworkUrl100)
            .placeholder(R.drawable.ic_placeholder_45)
            .error(R.drawable.ic_placeholder_45)
            .centerCrop()
            .transform(RoundedCorners(2))
            .into(artworkImageView)
    }

    /**
     * Отображает состояние плеера (иконка/текст) для текущего трека.
     * @param isPlaying — играет ли трек
     * @param currentTimeMillis — текущее время воспроизведения
     */
    fun showPlayingState(isPlaying: Boolean, currentTimeMillis: Long) {
        // Здесь можно добавить анимацию/иконку, если нужно
        // Например:
        // if (isPlaying) trackTimeTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_playing, 0, 0, 0)
        // Или обновить текст:
        if (currentTimeMillis > 0) {
            val formatted = SimpleDateFormat("mm:ss", Locale.getDefault()).format(currentTimeMillis)
            trackTimeTextView.text = formatted
        }
    }

    /**
     * Сбрасывает состояние плеера (например, при остановке).
     */
    fun hidePlayingState() {
        // Возвращаем исходное время трека
        if (itemView.tag is Track) {
            val track = itemView.tag as Track
            if (track.trackTimeMillis != null) {
                val formatted = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)
                trackTimeTextView.text = formatted
            }
        }
    }
}
