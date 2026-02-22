package com.practicum.playlistmaker.search.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.contract.FormatTrackDurationUseCaseContract
import com.practicum.playlistmaker.search.ui.viewholder.AlbumViewHolder
import com.practicum.playlistmaker.search.ui.viewholder.TrackViewHolder
import com.practicum.playlistmaker.core.constants.Constants

class TrackAdapter(
    private var tracks: List<Track> = emptyList(),
    private val viewType: Int,
    private var onTrackClick: (Track) -> Unit = {},
    private var onClickPlayButton: (Track) -> Unit = {},
    private var onAddToPlaylist: (Track) -> Unit = {},
    private var onFavorite: (Track) -> Unit = {},
    private val formatDurationUseCase: FormatTrackDurationUseCaseContract
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var isPlaying: Boolean = false
    var currentTimeMillis: Long = 0
    var currentPosition: Int = -1
    private var formattedTime: String = "00:00" // ДОБАВЛЕНО: поле для хранения форматированного времени

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (this.viewType) {
            Constants.Companion.VIEW_TYPE_TRACK -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_track, parent, false)
                TrackViewHolder(view, formatDurationUseCase)
            }
            Constants.Companion.VIEW_TYPE_ALBUM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_audioplayer, parent, false)
                AlbumViewHolder(
                    view,
                    onTrackClick,
                    onClickPlayButton,
                    onAddToPlaylist,
                    onFavorite,
                    formatDurationUseCase
                )
            }
            else -> throw IllegalArgumentException("Unsupported view type: $this.viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val track = tracks[position]
        when (holder) {
            is TrackViewHolder -> bindTrackViewHolder(holder, track, position)
            is AlbumViewHolder -> bindAlbumViewHolder(holder, track, position)
        }
        holder.itemView.setOnClickListener { onTrackClick(track) }
    }

    private fun bindTrackViewHolder(holder: TrackViewHolder, track: Track, position: Int) {
        holder.bind(track)
        if (position == currentPosition) {
            holder.showPlayingState(isPlaying, currentTimeMillis, formattedTime) // ИЗМЕНЕНО: добавлен formattedTime
        } else {
            holder.hidePlayingState()
        }
    }

    private fun bindAlbumViewHolder(holder: AlbumViewHolder, track: Track, position: Int) {
        holder.bind(
            track = track,
            isPlaying = isPlaying,
            currentTimeMillis = currentTimeMillis,
            formattedTime = formattedTime // ИЗМЕНЕНО: добавлен formattedTime
        )
        Log.d("TrackAdapter", "Bound track at position $position, isPlaying=$isPlaying, time=$formattedTime")
    }

    override fun getItemCount(): Int = tracks.size

    fun updateList(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }

    /**
     * ИЗМЕНЕНО: Добавлен параметр formattedTime для поддержки единого состояния UI
     */
    fun notifyDataSetChangedWithState(
        isPlaying: Boolean,
        currentTimeMillis: Long,
        position: Int = -1,
        formattedTime: String
    ) {
        this.isPlaying = isPlaying
        this.currentTimeMillis = currentTimeMillis
        this.currentPosition = position
        this.formattedTime = formattedTime

        if (position != -1 && position < itemCount) {
            notifyItemChanged(position)
        } else {
            notifyDataSetChanged()
        }
    }


    fun getTracks(): List<Track> {
        return tracks
    }
}
