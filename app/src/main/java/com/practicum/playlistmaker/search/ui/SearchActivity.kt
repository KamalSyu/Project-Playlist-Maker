package com.practicum.playlistmaker.search.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.player.ui.AudioPlayerActivity
import com.practicum.playlistmaker.search.ui.adapter.TrackAdapter
import com.practicum.playlistmaker.search.ui.parcel.toParcelable
import com.practicum.playlistmaker.core.constants.Constants.Companion.SEARCH_QUERY_KEY
import com.practicum.playlistmaker.core.constants.Constants.Companion.VIEW_TYPE_TRACK
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {

    private val viewModel: SearchViewModel by viewModels()

    // Вьюшки
    private lateinit var backTextView: TextView
    private lateinit var searchEditText: EditText
    private lateinit var resetButton: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var noResultsLayout: LinearLayout
    private lateinit var errorLayout: LinearLayout
    private lateinit var updateButton: Button
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var clearHistoryButton: Button
    private lateinit var historyRecyclerViewKit: LinearLayout
    private lateinit var progressBar: ProgressBar

    // Адаптеры
    private lateinit var tracksAdapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    // Данные
    private var searchQuery: String = ""
    private var clickJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initViews()
        setupClickListeners()
        setupTextWatchers()
        restoreState(savedInstanceState)
        observeViewModel()
        viewModel.loadHistory()
    }

    private fun initViews() {
        backTextView = findViewById(R.id.back)
        searchEditText = findViewById(R.id.search_edit_text)
        resetButton = findViewById(R.id.reset_button)
        recyclerView = findViewById(R.id.recyclerView)
        noResultsLayout = findViewById(R.id.no_results_layout)
        errorLayout = findViewById(R.id.error_layout)
        updateButton = findViewById(R.id.refresh_button)
        historyRecyclerView = findViewById(R.id.history_recycler_view)
        clearHistoryButton = findViewById(R.id.clear_history_button)
        historyRecyclerViewKit = findViewById(R.id.search_history_layout)
        progressBar = findViewById(R.id.progressBar)

        tracksAdapter = TrackAdapter(
            tracks = emptyList(),
            viewType = VIEW_TYPE_TRACK,
            onTrackClick = { track ->
                viewModel.onTrackClicked(track)
            },
            onClickPlayButton = {},
            onAddToPlaylist = {},
            onFavorite = {},
            formatDurationUseCase = viewModel.formatTrackDurationUseCase
        )
        recyclerView.adapter = tracksAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        historyAdapter = TrackAdapter(
            tracks = emptyList(),
            viewType = VIEW_TYPE_TRACK,
            onTrackClick = { track -> openAudioPlayer(track) },
            onClickPlayButton = {},
            onAddToPlaylist = {},
            onFavorite = {},
            formatDurationUseCase = viewModel.formatTrackDurationUseCase
        )
        historyRecyclerView.adapter = historyAdapter
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupClickListeners() {
        backTextView.setOnClickListener { finish() }
        resetButton.setOnClickListener {
            searchEditText.setText("")
            updateTracksList(emptyList())
            hideKeyboard()
            showNoResults(false)
        }
        updateButton.setOnClickListener {
            if (viewModel.isLastSearchFailed && viewModel.lastSearchQuery != null) {
                viewModel.performSearch(viewModel.lastSearchQuery!!)
            }
        }


        clearHistoryButton.setOnClickListener {
            viewModel.clearHistory()
            historyRecyclerViewKit.visibility = View.GONE // Восстанавливаем видимость после очистки
        }

    }

    private fun setupTextWatchers() {
        var searchJob: Job? = null
        searchEditText.doOnTextChanged { text, _, _, _ ->
            val query = text?.toString()?.trim() ?: ""
            searchQuery = query
            resetButton.visibility = if (query.isNotEmpty()) View.VISIBLE else View.INVISIBLE
            updateHistoryVisibility()
            searchJob?.cancel()

            if (query.isNotEmpty()) {
                searchJob = lifecycleScope.launch {
                    delay(2000)
                    viewModel.performSearch(query)
                }
            } else {
                viewModel.filterAndUpdateTracks(query)
                showNoResults(true)
            }
        }

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            updateHistoryVisibility()
        }
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
            searchEditText.setText(searchQuery)
            if (searchQuery.isNotEmpty()) viewModel.performSearch(searchQuery)
        }
    }

    private fun observeViewModel() {
        viewModel.searchState.observe(this) { state ->
            when (state) {
                SearchState.Idle -> {}
                is SearchState.Loading -> {
                    showLoading()
                }
                is SearchState.Results -> {
                    hideLoading()
                    updateTracksList(state.tracks)
                    // Используем публичный геттер isLastSearchFailed
                    showNoResults(state.tracks.isEmpty() && searchQuery.isNotEmpty())
                }
                is SearchState.Error -> {
                    hideLoading()
                    showError()
                }
            }
        }

        viewModel.historyState.observe(this) { state ->
            when (state) {
                HistoryState.Loading -> {}
                HistoryState.Empty -> {
                    historyAdapter.updateList(emptyList())
                    updateHistoryVisibility()
                }
                is HistoryState.HistoryLoaded -> {
                    historyAdapter.updateList(state.history)
                    updateHistoryVisibility()
                }
                HistoryState.HistoryCleared -> {
                    // Исправление: было View.VISIBLE, теперь View.GONE
                    historyRecyclerViewKit.visibility = View.GONE
                    updateHistoryVisibility()
                }
            }
        }

        viewModel.trackToOpen.observe(this) { track ->
            track?.let {
                openAudioPlayer(it)
                // Сбрасываем состояние после обработки
                viewModel.resetTrackToOpen()
            }
        }
    }


    private fun updateTracksList(tracks: List<Track>) {
        tracksAdapter.updateList(tracks)
        recyclerView.visibility = if (tracks.isNotEmpty()) View.VISIBLE else View.GONE
        showNoResults(tracks.isEmpty() && searchQuery.isNotEmpty())
    }



    private fun openAudioPlayer(track: Track) {
        val intent = Intent(this, AudioPlayerActivity::class.java)
        val parcelableTrack = track.toParcelable()
        intent.putExtra("track", parcelableTrack)
        startActivity(intent)
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.INVISIBLE
        noResultsLayout.visibility = View.INVISIBLE
        errorLayout.visibility = View.INVISIBLE
    }


    private fun hideLoading() {
        progressBar.visibility = View.INVISIBLE  // Просто скрываем прогресс‑бар
    }

    private fun showError() {
        errorLayout.visibility = View.VISIBLE
        noResultsLayout.visibility = View.GONE
    }


    private fun showNoResults(show: Boolean) {
        noResultsLayout.visibility = if (show && !viewModel.isLastSearchFailed) View.VISIBLE else View.GONE
        errorLayout.visibility = View.GONE
    }






    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    private fun updateHistoryVisibility() {
        val isEmptyQuery = searchEditText.text.isEmpty()
        val hasFocus = searchEditText.hasFocus()
        val hasHistory = historyAdapter.itemCount > 0

        historyRecyclerViewKit.visibility = if (isEmptyQuery && hasFocus && hasHistory) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchQuery)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadHistory()
        updateHistoryVisibility()
    }

    override fun onDestroy() {
        super.onDestroy()
        clickJob?.cancel()
    }
}
