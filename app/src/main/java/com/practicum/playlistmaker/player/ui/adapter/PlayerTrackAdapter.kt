package com.practicum.playlistmaker.player.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.utils.FormatTrackDurationUseCase
import com.practicum.playlistmaker.player.ui.view.AlbumViewHolder

class PlayerTrackAdapter(
    private var tracks: MutableList<Track>,
    private var onClickPlayButton: (Track) -> Unit = {},
    private var onAddToPlaylist: (Track) -> Unit = {},
    private var onFavorite: (Track) -> Unit = {},
    private val formatDurationUseCase: FormatTrackDurationUseCase
) : RecyclerView.Adapter<PlayerTrackAdapter.PlayerViewHolder>() {
    var isPlaying: Boolean = false
    var currentTimeMillis: Long = 0
    var currentPosition: Int = -1
    private var formattedTime: String = "00:00"
    private var currentPlayerViewHolder: AlbumViewHolder? = null
    data class UpdatePlaybackStatePayload(
        val isPlaying: Boolean,
        val formattedTime: String,
        val isFavorite: Boolean
    )
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_audioplayer, parent, false)
        return PlayerViewHolder(AlbumViewHolder(
            view,
            onClickListener = onClickPlayButton,
            onPlayButtonClick = onClickPlayButton,
            onAddToPlaylistClick = onAddToPlaylist,
            onFavoriteClick = onFavorite,
            formatDurationUseCase = formatDurationUseCase
        ))
    }
    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val track = tracks[position]
        val isFavorite = track.isFavorite
        holder.bind(track, position, isFavorite)
    }
    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }
        for (payload in payloads) {
            when (payload) {
                is UpdatePlaybackStatePayload -> {
                    holder.viewHolder.bind(
                        track = tracks[position],
                        isPlaying = payload.isPlaying,
                        currentTimeMillis = 0L,
                        formattedTime = payload.formattedTime,
                        isFavorite = payload.isFavorite
                    )
                }
                else -> super.onBindViewHolder(holder, position, payloads)
            }
        }
    }
    override fun getItemCount(): Int = tracks.size
    fun notifyDataSetChangedWithState(
        isPlaying: Boolean,
        currentTimeMillis: Long,
        position: Int,
        formattedTime: String,
        isFavorite: Boolean
    ) {
        this.isPlaying = isPlaying
        this.currentTimeMillis = currentTimeMillis
        this.currentPosition = position
        this.formattedTime = formattedTime

        if (position != -1 && position < itemCount) {
            notifyItemChanged(
                position,
                UpdatePlaybackStatePayload(isPlaying, formattedTime, isFavorite)
            )
        } else {
            notifyDataSetChanged()
        }
    }
    inner class PlayerViewHolder(
        val viewHolder: AlbumViewHolder
    ) : RecyclerView.ViewHolder(viewHolder.itemView) {

        fun bind(track: Track, position: Int, isFavorite: Boolean) {
            viewHolder.bind(
                track = track,
                isPlaying = isPlaying,
                currentTimeMillis = currentTimeMillis,
                formattedTime = formattedTime,
                isFavorite = isFavorite
            )
        }
    }
    override fun onViewAttachedToWindow(holder: PlayerViewHolder) {
        super.onViewAttachedToWindow(holder)
        currentPlayerViewHolder = holder.viewHolder
    }
    override fun onViewDetachedFromWindow(holder: PlayerViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (currentPlayerViewHolder == holder.viewHolder) {
            currentPlayerViewHolder = null
        }
    }
}