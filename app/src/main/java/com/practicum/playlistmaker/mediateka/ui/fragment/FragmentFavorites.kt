package com.practicum.playlistmaker.mediateka.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.mediateka.ui.view.FragmentFavoritesViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentFavorites : Fragment() {

    private val viewModel: FragmentFavoritesViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    companion object {
        fun newInstance(): Fragment {
            return FragmentFavorites()
        }
    }
}

