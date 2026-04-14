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
    private var currentPlayerViewHolder: AlbumViewHolder? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_audioplayer, parent, false)
        return PlayerViewHolder(AlbumViewHolder(
            view,
            { },
            onClickPlayButton,
            onAddToPlaylist,
            onFavorite,
            formatDurationUseCase
        ))
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.bind(tracks[position], position)
    }

    // Перегруженная версия с payload — для оптимизации
    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty() && payloads[0] == "UPDATE_PLAYBACK_STATE") {
            holder.viewHolder.updatePlayButtonState(isPlaying)
            holder.viewHolder.updateCurrentTime(formattedTime)
        } else {
            // Если payload пустой — делегируем в обычный bind
            super.onBindViewHolder(holder, position, payloads)
        }
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
            // Обновляем ТОЛЬКО время и состояние кнопки — без перерисовки всего ViewHolder
            notifyItemChanged(position, "UPDATE_PLAYBACK_STATE")
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
                formattedTime = formattedTime
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
}
