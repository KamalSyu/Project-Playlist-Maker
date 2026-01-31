package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.model.Track
import javax.inject.Inject

class FilterTracksUseCase @Inject constructor() : FilterTracksUseCaseContract{

    override operator fun invoke(tracks: List<Track>, query: String): List<Track> {
        if (query.isEmpty()) return emptyList()
        val lowerQuery = query.lowercase()
        return tracks.filter { track ->
            track.trackName.lowercase().contains(lowerQuery) ||
                    track.artistName.lowercase().contains(lowerQuery)
        }
    }
}
