package com.practicum.playlistmaker.mediateka

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.mediateka.ui.adapter.MediatekaViewPagerAdapter

class MediatekaFragment : Fragment() {

    private companion object {
        private const val SAVED_VIEWPAGER_POSITION = "saved_viewpager_position"
    }

    private lateinit var viewPager: ViewPager2
    private var currentPosition: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Удаляем дублирующий вызов WindowCompat.enableEdgeToEdge()
        return inflater.inflate(R.layout.fragment_mediateca, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Оставляем вызов WindowCompat.enableEdgeToEdge() только здесь — это оптимальное место
        requireActivity().window.let { window ->
            WindowCompat.enableEdgeToEdge(window)
        }

        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt(SAVED_VIEWPAGER_POSITION, 0)
        }

        setupViewPager(view)
        viewPager.setCurrentItem(currentPosition, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SAVED_VIEWPAGER_POSITION, currentPosition)
    }

    private fun setupViewPager(view: View) {
        viewPager = view.findViewById(R.id.viewPager)
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)

        // Исправление: используем childFragmentManager вместо requireFragmentManager()
        // Это правильный способ для управления фрагментами внутри ViewPager2
        val adapter = MediatekaViewPagerAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = adapter

        // Настройка связи между TabLayout и ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.favorites)
                1 -> tab.text = getString(R.string.playlists)
            }
        }.attach()

        // Отслеживание смены страниц для сохранения позиции
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPosition = position
            }
        })
    }
}
