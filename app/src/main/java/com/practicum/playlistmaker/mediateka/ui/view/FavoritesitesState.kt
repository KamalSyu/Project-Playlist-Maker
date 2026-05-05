package com.practicum.playlistmaker.mediateka.ui.view

import com.practicum.playlistmaker.core.models.Track

sealed class FavoritesitesState {
    object Empty : FavoritesitesState()
    data class Loaded(val tracks: List<Track>) : AnotherState()
}
