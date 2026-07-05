package com.practicum.playlistmaker.mediateka.ui.view

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.mediateka.ui.adapter.PlaylistsAdapter
import java.io.File

class PlaylistViewHolder(
    itemView: View,
    private val onAction: (Playlist, PlaylistsAdapter.Action) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val coverImage: ImageView = itemView.findViewById(R.id.playlistCover)
    private val nameText: TextView = itemView.findViewById(R.id.playlistName)
    private val trackCountText: TextView = itemView.findViewById(R.id.playlistTrackCount)

    fun bind(playlist: Playlist) {
        nameText.text = playlist.name
        trackCountText.text = when (playlist.trackCount) {
            0 -> "Нет треков"
            1 -> "${playlist.trackCount} трек"
            2, 3, 4 -> "${playlist.trackCount} трека"
            else -> "${playlist.trackCount} треков"
        }

        val cornerRadiusPx = (8 * itemView.resources.displayMetrics.density).toInt()

        if (!playlist.coverPath.isNullOrEmpty()) {

            Glide.with(coverImage)
                .load(playlist.coverPath)
                .placeholder(R.drawable.ic_placeholder_312)
                .error(R.drawable.ic_placeholder_312)
                .into(coverImage)
        } else {
            coverImage.setImageResource(R.drawable.ic_placeholder_312)
        }

        itemView.setOnClickListener {
            showPlaylistActionsDialog(playlist)
        }
    }

    private fun showPlaylistActionsDialog(playlist: Playlist) {
        AlertDialog.Builder(itemView.context)
            .setTitle(playlist.name)
            .setItems(arrayOf("Переименовать", "Удалить")) { dialog, which ->
                when (which) {
                    0 -> onAction(playlist, PlaylistsAdapter.Action.RENAME)
                    1 -> onAction(playlist, PlaylistsAdapter.Action.DELETE)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}


