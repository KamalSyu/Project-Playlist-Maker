package com.practicum.playlistmaker.search.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.search.data.mapper.DtoMapper
import com.practicum.playlistmaker.core.contract.FormatTrackDurationUseCaseContract
import com.practicum.playlistmaker.core.contract.GetSearchHistoryUseCaseContract
import com.practicum.playlistmaker.core.usecase.UseCaseCreator
import com.practicum.playlistmaker.search.ui.adapter.TrackAdapter
import com.practicum.playlistmaker.core.constants.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MediatekaActivity : AppCompatActivity() {

    @Inject
    lateinit var useCaseCreator: UseCaseCreator
    @Inject
    lateinit var dtoMapper: DtoMapper

    private lateinit var formatTrackDurationUseCase: FormatTrackDurationUseCaseContract
    private lateinit var getSearchHistoryUseCase: GetSearchHistoryUseCaseContract
    private lateinit var adapter: TrackAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mediateca)

        formatTrackDurationUseCase = useCaseCreator.createFormatTrackDurationUseCase()
        getSearchHistoryUseCase = useCaseCreator.createGetSearchHistoryUseCase()

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = TrackAdapter(
            tracks = mutableListOf(),
            viewType = Constants.Companion.VIEW_TYPE_ALBUM,
            onTrackClick = { },
            onClickPlayButton = { },
            formatDurationUseCase = formatTrackDurationUseCase
        )
        recyclerView.adapter = adapter

        findViewById<View>(R.id.back).setOnClickListener { finish() }
        loadHistory()
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            try {
                val history = getSearchHistoryUseCase()
                adapter.updateList(history)
            } catch (e: Exception) {
            }
        }
    }

}