package com.practicum.playlistmaker.search.domain.usecase.search

import com.practicum.playlistmaker.core.models.Track

interface FilterTracksUseCase {
    operator fun invoke(tracks: List<Track>, query: String): List<Track>
}