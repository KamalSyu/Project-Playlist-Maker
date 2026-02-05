package com.practicum.playlistmaker.presentation.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.domain.usecase.FormatTrackDurationUseCaseContract
import com.practicum.playlistmaker.presentation.viewholder.AlbumViewHolder
import com.practicum.playlistmaker.presentation.viewholder.TrackViewHolder
import com.practicum.playlistmaker.utils.Constants.Companion.VIEW_TYPE_ALBUM
import com.practicum.playlistmaker.utils.Constants.Companion.VIEW_TYPE_TRACK


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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (this.viewType) {
            VIEW_TYPE_TRACK -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_track, parent, false)
                TrackViewHolder(view, formatDurationUseCase)
            }
            VIEW_TYPE_ALBUM -> {
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
        holder.itemView.tag = this.viewType
    }

    private fun bindTrackViewHolder(holder: TrackViewHolder, track: Track, position: Int) {
        holder.bind(track)

        if (position == currentPosition) {
            holder.showPlayingState(isPlaying, currentTimeMillis)
        } else {
            holder.hidePlayingState()
        }
    }

    private fun bindAlbumViewHolder(holder: AlbumViewHolder, track: Track, position: Int) {
        // ✅ Передаём только те параметры, которые есть в AlbumViewHolder.bind()
        holder.bind(
            track = track,
            isPlaying = isPlaying,
            currentTimeMillis = currentTimeMillis
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

    fun setOnPlayButtonClickListener(listener: (Track) -> Unit) {
        onClickPlayButton = listener
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
