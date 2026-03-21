package com.practicum.playlistmaker.mediateka

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.mediateka.ui.adapter.MediatekaViewPagerAdapter

class MediatekaActivity : AppCompatActivity() {

    private companion object {
        private const val SAVED_VIEWPAGER_POSITION = "saved_viewpager_position"
    }

    private lateinit var viewPager: androidx.viewpager2.widget.ViewPager2
    private var currentPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_mediateca)

        // Восстановление позиции ViewPager, если Activity пересоздаётся
        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt(SAVED_VIEWPAGER_POSITION, 0)
        }

        // Обработка кнопки «Назад»
        findViewById<View>(R.id.back).setOnClickListener { finish() }

        // Настройка ViewPager и TabLayout
        setupViewPager()

        // Устанавливаем сохранённую позицию после настройки адаптера
        viewPager.setCurrentItem(currentPosition, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Сохраняем текущую позицию ViewPager
        outState.putInt(SAVED_VIEWPAGER_POSITION, currentPosition)
    }

    private fun setupViewPager() {
        viewPager = findViewById(R.id.viewPager)
        val tabLayout = findViewById<com.google.android.material.tabs.TabLayout>(R.id.tabLayout)

        val adapter = MediatekaViewPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.favorites)
                1 -> tab.text = getString(R.string.playlists)
            }
        }.attach()

        // Отслеживаем изменения позиции ViewPager
        viewPager.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPosition = position
            }
        })
    }
}
