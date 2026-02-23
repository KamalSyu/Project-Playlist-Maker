package com.practicum.playlistmaker.player.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.contract.FormatTrackDurationUseCaseContract
import com.practicum.playlistmaker.player.ui.view.AlbumViewHolder

class PlayerTrackAdapter(
    private var tracks: List<Track> = emptyList(),
    private var onClickPlayButton: (Track) -> Unit = {},
    private var onAddToPlaylist: (Track) -> Unit = {},
    private var onFavorite: (Track) -> Unit = {},
    private val formatDurationUseCase: FormatTrackDurationUseCaseContract
) : RecyclerView.Adapter<PlayerTrackAdapter.PlayerViewHolder>() {

    // Сохраняем все поля состояния
    var isPlaying: Boolean = false
    var currentTimeMillis: Long = 0
    var currentPosition: Int = -1
    private var formattedTime: String = "00:00"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_audioplayer, parent, false)
        return PlayerViewHolder(AlbumViewHolder(
            view,
            { }, // onTrackClick не нужен в плеере
            onClickPlayButton,
            onAddToPlaylist,
            onFavorite,
            formatDurationUseCase
        ))
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.bind(tracks[position], position)
    }

    override fun getItemCount(): Int = tracks.size

    fun updateList(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }

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
            notifyItemChanged(position)
        } else {
            notifyDataSetChanged()
        }
    }

    inner class PlayerViewHolder(
        private val viewHolder: AlbumViewHolder
    ) : RecyclerView.ViewHolder(viewHolder.itemView) {

        fun bind(track: Track, position: Int) {
            viewHolder.bind(
                track = track,
                isPlaying = isPlaying,
                currentTimeMillis = currentTimeMillis,
                formattedTime = formattedTime
            )
        }
    }
}
