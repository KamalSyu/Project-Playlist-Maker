package com.practicum.playlistmaker.mediateka.ui.view

import com.practicum.playlistmaker.core.models.Track

sealed class FavoritesState {
    object Empty : FavoritesState()
    data class Loaded(val tracks: List<Track>) : FavoritesState ()
}
