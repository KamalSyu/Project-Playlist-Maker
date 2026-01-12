package com.practicum.playlistmaker

import com.practicum.playlistmaker.Track
import TrackViewHolder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.Constants.Companion.VIEW_TYPE_ALBUM
import com.practicum.playlistmaker.Constants.Companion.VIEW_TYPE_TRACK

class TrackAdapter(private var tracks: List<Track>,
                   private val viewType: Int,
                   private var onTrackClick: (Track) -> Unit,
                   private var onClickPlayButton: (Track) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var isPlaying = false
    private var currentTime: Long = 0


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (this.viewType) {
            VIEW_TYPE_TRACK -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
                TrackViewHolder(view)
            }
            VIEW_TYPE_ALBUM -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_audioplayer, parent, false)
                AlbumViewHolder(view, onTrackClick, onClickPlayButton)  // передаём обе лямбды
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }
    fun
            notifyDataSetChangedWithState(isPlaying: Boolean, currentTimeMillis: Long = 0) {
        this.isPlaying = isPlaying
        this.currentTime = currentTimeMillis  // Сохраняем текущее время
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val track = tracks[position]
        when (holder) {
            is TrackViewHolder -> {
                holder.bind(track)
            }

            is AlbumViewHolder -> {
                holder.bind(track, isPlaying, currentTime)
            }
        }
        holder.itemView.setOnClickListener {
            onTrackClick(track) // вызываем лямбду с текущим треком

        }
    }

    override fun getItemCount(): Int = tracks.size

    fun updateList(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (Track) -> Unit) {
        this.onTrackClick = listener
        notifyDataSetChanged()  // чтобы обновить все элементы с новым обработчиком
    }

    fun notifyDataSetChangedWithState(isPlaying: Boolean) {
        this.isPlaying = isPlaying
        notifyDataSetChanged()
    }

}
