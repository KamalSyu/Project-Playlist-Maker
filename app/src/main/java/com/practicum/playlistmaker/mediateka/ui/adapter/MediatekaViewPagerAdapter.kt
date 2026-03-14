package com.practicum.playlistmaker.mediateka.ui.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.practicum.playlistmaker.mediateka.ui.fragment.FragmentFavorites
import com.practicum.playlistmaker.mediateka.ui.fragment.FragmentPlaylists

class MediatekaViewPagerAdapter(
    private val activity: AppCompatActivity
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentFavorites.newInstance()
            1 -> FragmentPlaylists.newInstance()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}
