package com.practicum.playlistmaker.mediateka.ui.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.core.models.Track

class FragmentPlaylistsViewModel : ViewModel() {

    private val _playlists = MutableLiveData<List<Track>>()
    val playlists: LiveData<List<Track>> = _playlists
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadPlaylists()
    }

    fun loadPlaylists() {
        _isLoading.value = true
        _playlists.value = emptyList()
        _isLoading.value = false
    }

    fun createNewPlaylist(playlistName: String) {
        loadPlaylists()
    }
}