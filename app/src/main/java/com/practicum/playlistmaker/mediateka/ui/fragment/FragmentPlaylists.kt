package com.practicum.playlistmaker.mediateka.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.mediateka.ui.view.FragmentPlaylistsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

class FragmentPlaylists : Fragment() {

    private val viewModel: FragmentPlaylistsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_playlists, container, false)
    }

    companion object {
        fun newInstance(): Fragment {
            return FragmentPlaylists()
        }
    }
}
