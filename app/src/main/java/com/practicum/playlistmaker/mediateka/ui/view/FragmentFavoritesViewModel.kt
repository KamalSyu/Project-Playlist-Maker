package com.practicum.playlistmaker.mediateka.ui.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.core.models.Track

class FragmentFavoritesViewModel : ViewModel() {

    // Приватный MutableLiveData для внутреннего использования
    private val _favorites = MutableLiveData<List<Track>>()

    // Публичный LiveData для наблюдения извне
    val favorites: LiveData<List<Track>> = _favorites

    init {
        _favorites.value = emptyList()
    }
}
