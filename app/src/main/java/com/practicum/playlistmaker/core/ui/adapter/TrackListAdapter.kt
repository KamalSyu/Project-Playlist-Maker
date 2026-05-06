package com.practicum.playlistmaker.core.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.contract.FormatTrackDurationUseCaseContract
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.ui.viewholder.TrackViewHolder

class TrackListAdapter(
    private var tracks: List<Track>,
    private val formatDurationUseCase: FormatTrackDurationUseCaseContract,
    private val onItemClick: (Track) -> Unit,
    // Опциональные параметры для состояния воспроизведения
    private var currentPlayingPosition: Int = -1,
    private var isPlaying: Boolean = false,
    private var currentTimeMillis: Long = 0,
    private var formattedTime: String = "00:00"
) : RecyclerView.Adapter<TrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view, formatDurationUseCase, onItemClick)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks[position])

        // Логика отображения состояния воспроизведения (только для экрана поиска)
        if (position == currentPlayingPosition) {
            holder.showPlayingState(isPlaying, currentTimeMillis, formattedTime)
        } else {
            holder.hidePlayingState()
        }
    }

    override fun getItemCount(): Int = tracks.size

    fun updateTracks(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }

    // Методы для управления состоянием воспроизведения
    fun setPlayingState(
        position: Int,
        isPlaying: Boolean,
        currentTimeMillis: Long,
        formattedTime: String
    ) {
        this.currentPlayingPosition = position
        this.isPlaying = isPlaying
        this.currentTimeMillis = currentTimeMillis
        this.formattedTime = formattedTime
        notifyItemChanged(position)
    }

    fun clearPlayingState() {
        if (currentPlayingPosition != -1) {
            val oldPosition = currentPlayingPosition
            currentPlayingPosition = -1
            notifyItemChanged(oldPosition)
        }
    }
}
