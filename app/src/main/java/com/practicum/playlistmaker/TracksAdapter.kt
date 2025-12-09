package com.practicum.playlistmaker

import Track
import TrackViewHolder
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.Constants.Companion.VIEW_TYPE_ALBUM
import com.practicum.playlistmaker.Constants.Companion.VIEW_TYPE_TRACK


class TrackAdapter(private var tracks: List<Track>, private var onTrackClick: (Track) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


//    override fun getItemViewType(position: Int): Int {
//        // Здесь определяем логику для выбора типа ViewHolde
//        return if (position % 2 == 0) VIEW_TYPE_TRACK else VIEW_TYPE_ALBUM
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TRACK -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
                TrackViewHolder(view)
            }
            VIEW_TYPE_ALBUM -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mediateca, parent, false)
                AlbumViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val track = tracks[position]
        when (holder) {
            is TrackViewHolder -> holder.bind(track)
            is AlbumViewHolder -> holder.bind(track)
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

    }
}

//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
//        return TrackViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
//        val track = tracks[position]
//        holder.bind(track)
//        holder.itemView.setOnClickListener {
//            onTrackClick(track) // вызываем лямбду с текущим треком
//        }
//    }
//    override fun getItemCount(): Int = tracks.size
//
//    fun updateList(newTracks: List<Track>) {
//        tracks = newTracks
//        notifyDataSetChanged()
//    }
//
//    fun setOnItemClickListener(listener: (Track) -> Unit) {
//        this.onTrackClick = listener
//
//    }
//}


