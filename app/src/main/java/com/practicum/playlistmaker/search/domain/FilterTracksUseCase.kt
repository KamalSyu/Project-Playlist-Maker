package com.practicum.playlistmaker.search.domain

import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.FilterTracksUseCaseContract
import javax.inject.Inject

class FilterTracksUseCase @Inject constructor(
) : FilterTracksUseCaseContract {

    override operator fun invoke(tracks: List<Track>, query: String): List<Track> {
        if (query.isEmpty()) return emptyList()
        val lowerQuery = query.lowercase()
        return tracks.filter { track ->
            track.trackName.lowercase().contains(lowerQuery) ||
                    track.artistName.lowercase().contains(lowerQuery)
        }
    }
}