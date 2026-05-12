package com.practicum.playlistmaker.search.domain.usecase.search

import com.practicum.playlistmaker.core.models.Track

class FilterTracksUseCaseImpl () : FilterTracksUseCase {

    override operator fun invoke(tracks: List<Track>, query: String): List<Track> {
        if (query.isEmpty()) return emptyList()
        val lowerQuery = query.lowercase()
        return tracks.filter { track ->
            track.trackName.lowercase().contains(lowerQuery) ||
                    track.artistName.lowercase().contains(lowerQuery)
        }
    }
}