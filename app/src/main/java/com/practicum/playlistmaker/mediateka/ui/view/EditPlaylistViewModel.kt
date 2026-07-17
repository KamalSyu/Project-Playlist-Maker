package com.practicum.playlistmaker.mediateka.ui.view

import androidx.lifecycle.SavedStateHandle
import com.practicum.playlistmaker.mediateka.domain.interactor.PlaylistInteractor

class EditPlaylistViewModel(
    savedStateHandle: SavedStateHandle,
    playlistInteractor: PlaylistInteractor,
) : CreatePlaylistViewModel(savedStateHandle, playlistInteractor) {

    init {
        val playlistId = savedStateHandle.get<Long>("playlistId")

        playlistId?.let {
            setEditMode(it)
        }
    }
}
