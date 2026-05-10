package com.practicum.playlistmaker.mediateka.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.contract.FormatTrackDurationUseCaseContract
import com.practicum.playlistmaker.mediateka.ui.view.FavoriteViewHolder

class FavoriteTrackAdapter(
    private val onTrackClick: (Track) -> Unit,
    private val formatDurationUseCase: FormatTrackDurationUseCaseContract
) : ListAdapter<Track, FavoriteViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_track, parent, false)
        return FavoriteViewHolder(view, formatDurationUseCase)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val track = getItem(position)
        holder.bind(track)
        holder.itemView.setOnClickListener { onTrackClick(track) }
    }
}

class DiffCallback : DiffUtil.ItemCallback<Track>() {
    override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean =
        oldItem.trackId == newItem.trackId

    override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean =
        oldItem == newItem
}

