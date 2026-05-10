package com.practicum.playlistmaker.mediateka.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.contract.FormatTrackDurationUseCaseContract
import com.practicum.playlistmaker.mediateka.ui.view.FavoriteViewHolder

class FavoriteTrackAdapter(
    private var tracks: MutableList<Track> = mutableListOf(),
    private val onTrackClick: (Track) -> Unit,
    private val formatDurationUseCase: FormatTrackDurationUseCaseContract
) : RecyclerView.Adapter<FavoriteViewHolder>() { // Изменено: теперь ссылается на внешний FavoriteViewHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_track, parent, false)
        return FavoriteViewHolder(view, formatDurationUseCase)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track)
        holder.itemView.setOnClickListener { onTrackClick(track) }
    }

    override fun getItemCount(): Int = tracks.size

    fun updateList(newTracks: List<Track>) {
        tracks.clear()
        tracks.addAll(newTracks)
        notifyDataSetChanged()
    }
}
