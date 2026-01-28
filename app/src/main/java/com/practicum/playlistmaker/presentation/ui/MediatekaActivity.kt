package com.practicum.playlistmaker.presentation.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.usecase.GetSearchHistoryUseCase
import com.practicum.playlistmaker.presentation.adapter.TrackAdapter
import com.practicum.playlistmaker.presentation.util.Constants.Companion.VIEW_TYPE_ALBUM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MediatekaActivity : AppCompatActivity() {

    @Inject lateinit var getSearchHistoryUseCase: GetSearchHistoryUseCase  // ← Внедряем через Hilt


    private lateinit var adapter: TrackAdapter
    private lateinit var recyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mediateca)

        // Находим RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TrackAdapter(mutableListOf(), VIEW_TYPE_ALBUM, {}, {})
        recyclerView.adapter = adapter

        // Кнопка "Назад"
        findViewById<View>(R.id.back).setOnClickListener { finish() }


        loadHistory()
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            try {
                val history = getSearchHistoryUseCase()
                adapter.updateList(history)
            } catch (e: Exception) {
                // Можно показать ошибку (например, Snackbar)
            }
        }
    }
}
