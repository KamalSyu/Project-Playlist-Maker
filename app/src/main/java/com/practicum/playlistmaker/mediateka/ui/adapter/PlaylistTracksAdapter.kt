package com.practicum.playlistmaker.mediateka.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.utils.FormatTrackDurationUseCase

class PlaylistTracksAdapter(
    private val onItemClick: (Track) -> Unit,
    private val onItemLongClick: (Long, Track) -> Unit,
    private val formatDurationUseCase: FormatTrackDurationUseCase // <-- Передаём сюда
) : ListAdapter<Track, PlaylistTracksViewHolder>(DiffCallback) {

    companion object {
        object DiffCallback : DiffUtil.ItemCallback<Track>() {
            override fun areItemsTheSame(oldItem: Track, newItem: Track) =
                oldItem.trackId == newItem.trackId

            override fun areContentsTheSame(oldItem: Track, newItem: Track) =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistTracksViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        // Теперь мы легально передаём UseCase в ViewHolder
        return PlaylistTracksViewHolder(view, formatDurationUseCase)
    }

    override fun onBindViewHolder(holder: PlaylistTracksViewHolder, position: Int) {
        val track = getItem(position)
        holder.bind(track)

        holder.itemView.setOnClickListener { onItemClick(track) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(0L, track) // playlistId подставится из фрагмента
            true
        }
    }
}
