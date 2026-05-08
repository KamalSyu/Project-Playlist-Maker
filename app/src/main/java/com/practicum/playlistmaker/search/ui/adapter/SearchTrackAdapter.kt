package com.practicum.playlistmaker.search.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.contract.FormatTrackDurationUseCaseContract
import com.practicum.playlistmaker.search.ui.view.TrackViewHolder


class SearchTrackAdapter(
    private var tracks: List<Track> = emptyList(),
    private var onTrackClick: (Track) -> Unit = {},
    private val formatDurationUseCase: FormatTrackDurationUseCaseContract
) : RecyclerView.Adapter<SearchTrackAdapter.SearchViewHolder>() {

    var isPlaying: Boolean = false
    var currentTimeMillis: Long = 0
    var currentPosition: Int = -1
    private var formattedTime: String = "00:00"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return SearchViewHolder(TrackViewHolder(view, formatDurationUseCase))
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(tracks[position], position)
    }

    override fun getItemCount(): Int = tracks.size

    fun updateList(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }

    inner class SearchViewHolder(
        private val viewHolder: TrackViewHolder
    ) : RecyclerView.ViewHolder(viewHolder.itemView) {

        fun bind(track: Track, position: Int) {
            viewHolder.bind(track)
            if (position == currentPosition) {
                viewHolder.showPlayingState(isPlaying, currentTimeMillis, formattedTime)
            } else {
                viewHolder.hidePlayingState()
            }
            itemView.setOnClickListener { onTrackClick(track) }
        }
    }
}