package com.practicum.playlistmaker.search.domain.usecase

import com.practicum.playlistmaker.core.contract.FilterTracksUseCaseContract
import com.practicum.playlistmaker.core.models.Track

class FilterTracksUseCase () : FilterTracksUseCaseContract {

    override operator fun invoke(tracks: List<Track>, query: String): List<Track> {
        if (query.isEmpty()) return emptyList()
        val lowerQuery = query.lowercase()
        return tracks.filter { track ->
            track.trackName.lowercase().contains(lowerQuery) ||
                    track.artistName.lowercase().contains(lowerQuery)
        }
    }
}