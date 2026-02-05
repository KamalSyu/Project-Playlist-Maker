package com.practicum.playlistmaker.presentation.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.usecase.FormatTrackDurationUseCaseContract
import com.practicum.playlistmaker.domain.usecase.GetSearchHistoryUseCaseContract  // ✅ Изменили импорт
import com.practicum.playlistmaker.domain.usecase.UseCaseCreator
import com.practicum.playlistmaker.presentation.adapter.TrackAdapter
import com.practicum.playlistmaker.utils.Constants.Companion.VIEW_TYPE_ALBUM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MediatekaActivity : AppCompatActivity() {

    @Inject lateinit var useCaseCreator: UseCaseCreator

    private lateinit var formatTrackDurationUseCase: FormatTrackDurationUseCaseContract
    private lateinit var getSearchHistoryUseCase: GetSearchHistoryUseCaseContract  // ✅ Изменили тип
    private lateinit var adapter: TrackAdapter
    private lateinit var recyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mediateca)

        // Инициализация Use Case через Creator
        formatTrackDurationUseCase = useCaseCreator.createFormatTrackDurationUseCase()
        getSearchHistoryUseCase = useCaseCreator.createGetSearchHistoryUseCase()  // Теперь тип совпадает


        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)


        adapter = TrackAdapter(
            tracks = mutableListOf(),
            viewType = VIEW_TYPE_ALBUM,
            onTrackClick = { /* Пустой обработчик */ },
            onClickPlayButton = { /* Пустой обработчик */ },
            formatDurationUseCase = formatTrackDurationUseCase
        )
        recyclerView.adapter = adapter


        findViewById<View>(R.id.back).setOnClickListener { finish() }
        loadHistory()
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            try {
                val history = getSearchHistoryUseCase()  // Вызов через контракт
                adapter.updateList(history)
            } catch (e: Exception) {
                // Обработка ошибки (например, показать Snackbar)
            }
        }
    }
}
