package com.practicum.playlistmaker

import Track
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.text.SimpleDateFormat
import java.util.Locale

class AlbumViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val albumImageView: ImageView = itemView.findViewById(R.id.album)
    private val textTrackName: TextView = itemView.findViewById(R.id.textTrackName)
    private val  textArtistName: TextView = itemView.findViewById(R.id.textArtistName)
    private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
    private val trackTimeMillisTextView: TextView = itemView.findViewById(R.id.trackTimeMillis)
    private val collectionNameTextView: TextView = itemView.findViewById(R.id.collectionName)
    private val releaseDateTextView: TextView = itemView.findViewById(R.id.releaseDate)
    private val primaryGenreNameTextView: TextView = itemView.findViewById(R.id.primaryGenreName)
    private val countryTextView: TextView = itemView.findViewById(R.id.country)
    private val dateFormatter = DateFormatter()

    fun bind(track: Track) {

        // Выносим форматирование времени в отдельную переменную
        val formattedTrackTime = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)

        textTrackName.text = track.trackName
        textArtistName.text = track.artistName
        timeTextView.text = formattedTrackTime
        trackTimeMillisTextView.text = formattedTrackTime
        releaseDateTextView.text = dateFormatter.formatReleaseDate(track.releaseDate)
        collectionNameTextView.text = track.collectionName
        primaryGenreNameTextView.text = track.primaryGenreName
        countryTextView.text = track.country

        // Определяем радиус скругления в пикселях
        val cornerRadiusDp = 8 // Значение в dp
        val density = itemView.context.resources.displayMetrics.density
        val cornerRadiusPx = (cornerRadiusDp * density).toInt()


        // Загрузка изображения с Glide
        Glide.with(itemView.context)
            .load(track.getHighQualityArtworkUrl())
            .placeholder(R.drawable.ic_placeholder_312)
            .error(R.drawable.ic_placeholder_312)
            .centerCrop()
            .transform(RoundedCorners(cornerRadiusPx))
            .into(albumImageView)
    }
}