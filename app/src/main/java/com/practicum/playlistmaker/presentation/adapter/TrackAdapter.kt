package com.practicum.playlistmaker.presentation.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.practicum.playlistmaker.R
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.presentation.viewholder.AlbumViewHolder
import com.practicum.playlistmaker.presentation.viewholder.TrackViewHolder
import com.practicum.playlistmaker.presentation.util.Constants.Companion.VIEW_TYPE_ALBUM
import com.practicum.playlistmaker.presentation.util.Constants.Companion.VIEW_TYPE_TRACK

class TrackAdapter(
    private var tracks: List<Track> = emptyList(),
    private val viewType: Int,
    private var onTrackClick: (Track) -> Unit = {},
    private var onClickPlayButton: (Track) -> Unit = {},
    private var onAddToPlaylist: (Track) -> Unit = {},
    private var onFavorite: (Track) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var isPlaying: Boolean = false
    var currentTimeMillis: Long = 0
    var currentPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d("TrackAdapter", "Current viewType: $this.viewType")  // <-- Добавить
        return when (this.viewType) {
            VIEW_TYPE_TRACK -> createTrackViewHolder(parent)
            VIEW_TYPE_ALBUM -> createAlbumViewHolder(parent)
            else -> throw IllegalArgumentException("Unsupported view type: $viewType")
        }
    }

    private fun createTrackViewHolder(parent: ViewGroup): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    private fun createAlbumViewHolder(parent: ViewGroup): AlbumViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_audioplayer, parent, false)
        return AlbumViewHolder(view, onTrackClick, onClickPlayButton, onAddToPlaylist, onFavorite)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val track = tracks[position]
        when (holder) {
            is TrackViewHolder -> bindTrackViewHolder(holder, track, position)
            is AlbumViewHolder -> bindAlbumViewHolder(holder, track, position)
        }
        holder.itemView.setOnClickListener { onTrackClick(track) }
        holder.itemView.tag = viewType
    }

    private fun bindTrackViewHolder(holder: TrackViewHolder, track: Track, position: Int) {
        holder.bind(track)

        // Если это текущий играющий трек — показываем состояние плеера
        if (position == currentPosition) {
            holder.showPlayingState(isPlaying, currentTimeMillis)
        } else {
            holder.hidePlayingState()
        }
    }

    private fun bindAlbumViewHolder(holder: AlbumViewHolder, track: Track, position: Int) {
        holder.bind(
            track = track,
            isPlaying = isPlaying,
            currentTimeMillis = currentTimeMillis,
            onTrackClick = onTrackClick,
            onPlayButtonClick = onClickPlayButton,
            onAddToPlaylistClick = onAddToPlaylist,
            onFavoriteClick = onFavorite
        )
    }

    override fun getItemCount(): Int = tracks.size

    fun updateList(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }
    fun setOnItemClickListener(listener: (Track) -> Unit) {
        onTrackClick = listener
    }
    fun notifyDataSetChangedWithState(
        isPlaying: Boolean,
        currentTimeMillis: Long = 0,
        position: Int = -1
    ) {
        this.isPlaying = isPlaying
        this.currentTimeMillis = currentTimeMillis
        this.currentPosition = position

        if (position != -1 && position < itemCount) {
            notifyItemChanged(position)
        } else {
            notifyDataSetChanged()
        }
    }
}
