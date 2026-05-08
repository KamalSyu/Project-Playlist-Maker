package com.practicum.playlistmaker.mediateka.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.contract.FormatTrackDurationUseCaseContract
import com.practicum.playlistmaker.mediateka.ui.view.FavoriteViewHolder
import com.practicum.playlistmaker.R

class FavoriteTrackAdapter(
    private val tracks: List<Track>,
    private val formatDurationUseCase: FormatTrackDurationUseCaseContract,
    private val onItemClick: (Track) -> Unit
) : RecyclerView.Adapter<FavoriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_track, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val track = tracks[position]

        holder.itemView.setOnClickListener { onItemClick(track) }
        // Привязка данных к View
        holder.trackNameTextView.text = track.trackName
        holder.artistNameTextView.text = track.artistName

        // Форматирование длительности
        track.trackTimeMillis?.let { timeMillis ->
            holder.trackTimeTextView.text = formatDurationUseCase.invoke(timeMillis)
        } ?: run {
            holder.trackTimeTextView.text = "-"
        }

        // Загрузка обложки через Glide
        Glide.with(holder.itemView.context)
            .load(track.artworkUrl100)
            .placeholder(R.drawable.ic_placeholder_45)
            .error(R.drawable.ic_placeholder_45)
            .centerCrop()
            .transform(RoundedCorners(2))
            .into(holder.artworkImageView)
    }

    override fun getItemCount(): Int = tracks.size
}
