package com.practicum.playlistmaker

import Track
import TrackViewHolder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class TrackAdapter(private var tracks: List<Track>,
                   private var onTrackClick: (Track) -> Unit  // лямбда для обработки клика
) : RecyclerView.Adapter<TrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track)
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