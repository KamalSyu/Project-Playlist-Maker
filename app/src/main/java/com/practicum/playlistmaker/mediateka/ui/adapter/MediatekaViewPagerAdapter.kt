package com.practicum.playlistmaker.mediateka.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.practicum.playlistmaker.mediateka.ui.fragment.FragmentFavorites
import com.practicum.playlistmaker.mediateka.ui.fragment.FragmentPlaylists

class MediatekaViewPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentFavorites.newInstance()
            1 -> FragmentPlaylists.newInstance()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}