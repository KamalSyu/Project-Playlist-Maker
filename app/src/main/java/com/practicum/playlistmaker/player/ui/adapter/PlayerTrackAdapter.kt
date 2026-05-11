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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_audioplayer, parent, false)
        return PlayerViewHolder(AlbumViewHolder(
            view,
            onClickListener = onClickPlayButton,  // (Track) -> Unit
            onPlayButtonClick = onClickPlayButton,  // (Track) -> Unit
            onAddToPlaylistClick = onAddToPlaylist,  // (Track) -> Unit
            onFavoriteClick = onFavorite,  // (Track) -> Unit
            formatDurationUseCase = formatDurationUseCase  // FormatTrackDurationUseCaseContract
        ))
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.bind(tracks[position], position)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            val payload = payloads[0]
            when (payload) {
                is Boolean -> {
                    holder.viewHolder.updateFavoriteState(payload)
                }
                is UpdatePlaybackStatePayload -> {
                    holder.viewHolder.updatePlayButtonState(isPlaying)
                    holder.viewHolder.updateCurrentTime(formattedTime)
                }
                else -> {
                    super.onBindViewHolder(holder, position, payloads)
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemCount(): Int = tracks.size

    fun notifyDataSetChangedWithState(
        isPlaying: Boolean,
        currentTimeMillis: Long,
        position: Int,
        formattedTime: String
    ) {
        this.isPlaying = isPlaying
        this.currentTimeMillis = currentTimeMillis
        this.currentPosition = position
        this.formattedTime = formattedTime

        if (position != -1 && position < itemCount) {
            notifyItemChanged(position, UpdatePlaybackStatePayload)
        } else {
            notifyDataSetChanged()
        }
    }

    inner class PlayerViewHolder(
        val viewHolder: AlbumViewHolder
    ) : RecyclerView.ViewHolder(viewHolder.itemView) {

        fun bind(track: Track, position: Int) {
            viewHolder.bind(
                track = track,
                isPlaying = isPlaying,
                currentTimeMillis = currentTimeMillis,
                formattedTime = formattedTime,
                isFavorite = track.isFavorite
            )
        }
    }
    fun updateCurrentTime(formattedTime: String) {
        currentPlayerViewHolder?.updateCurrentTime(formattedTime)
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
    fun updateFavoriteStateForTrack(trackId: String, isFavorite: Boolean) {
        val index = tracks.indexOfFirst { it.trackId == trackId }
        if (index != -1) {
            tracks[index].isFavorite = isFavorite
            notifyItemChanged(index)
        }
    }
}