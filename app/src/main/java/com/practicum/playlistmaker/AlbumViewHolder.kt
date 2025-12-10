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
    private val textAlbumName: TextView = itemView.findViewById(R.id.textAlbumName)
    private val  textSingerName: TextView = itemView.findViewById(R.id.textSingerName)
//    val icPlayButton: Button = itemView.findViewById(R.id.ic_play_button)
//    val icButtonPlus: Button = itemView.findViewById(R.id.ic_button_plus)
//    val icButtonLike: Button = itemView.findViewById(R.id.ic_button_like)
    private val timeTextView: TextView = itemView.findViewById(R.id.time)
    private val trackTimeMillisTextView: TextView = itemView.findViewById(R.id.trackTimeMillis)
    private val collectionNameTextView: TextView = itemView.findViewById(R.id.collectionName)
    private val releaseDateTextView: TextView = itemView.findViewById(R.id.releaseDate)
    private val primaryGenreNameTextView: TextView = itemView.findViewById(R.id.primaryGenreName)
    private val countryTextView: TextView = itemView.findViewById(R.id.country)

    fun bind(track: Track) {
        textAlbumName.text = track.trackName
        textSingerName.text = track.artistName
        timeTextView.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)
        trackTimeMillisTextView.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)
        collectionNameTextView.text = track.collectionName
        val releaseDateString = track.releaseDate
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy", Locale.getDefault())

        try {
            val date = inputFormat.parse(releaseDateString)
            releaseDateTextView.text = outputFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            releaseDateTextView.text = "Ошибка при парсинге даты"
        }
//        releaseDateTextView.text = track.releaseDate
        primaryGenreNameTextView.text = track.primaryGenreName
        countryTextView.text = track.country

        // Загрузка изображения с Glide
        Glide.with(itemView.context)
            .load(track.getHighQualityArtworkUrl())
            .placeholder(R.drawable.ic_placeholder_45)
            .error(R.drawable.ic_placeholder_45)
            .centerCrop()
            .transform(RoundedCorners(2))
            .into(albumImageView)
    }
}