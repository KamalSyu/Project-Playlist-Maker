package com.practicum.playlistmaker.search.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.search.data.mapper.DtoMapper
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.AddTrackToHistoryUseCaseContract
import com.practicum.playlistmaker.ClearSearchHistoryUseCaseContract
import com.practicum.playlistmaker.DelayedTrackActionUseCaseContract
import com.practicum.playlistmaker.FilterTracksUseCaseContract
import com.practicum.playlistmaker.FormatTrackDurationUseCaseContract
import com.practicum.playlistmaker.GetSearchHistoryUseCaseContract
import com.practicum.playlistmaker.SearchTracksUseCaseContract
import com.practicum.playlistmaker.UseCaseCreator
import com.practicum.playlistmaker.player.ui.AudioPlayerActivity
import com.practicum.playlistmaker.search.ui.adapter.TrackAdapter
import com.practicum.playlistmaker.search.ui.parcel.toParcelable
import com.practicum.playlistmaker.core.constants.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {

    @Inject
    lateinit var useCaseCreator: UseCaseCreator
    @Inject
    lateinit var dtoMapper: DtoMapper

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
    private var filteredTracks: List<Track> = emptyList()
    private var searchQuery: String = ""
    private var lastSearchQuery: String? = null
    private var isLastSearchFailed: Boolean = false
    private var clickJob: Job? = null

    // Use Cases через Creator
    private lateinit var searchTracksUseCase: SearchTracksUseCaseContract
    private lateinit var addTrackToHistoryUseCase: AddTrackToHistoryUseCaseContract
    private lateinit var getSearchHistoryUseCase: GetSearchHistoryUseCaseContract
    private lateinit var clearSearchHistoryUseCase: ClearSearchHistoryUseCaseContract
    private lateinit var filterTracksUseCase: FilterTracksUseCaseContract
    private lateinit var formatTrackDurationUseCase: FormatTrackDurationUseCaseContract
    private lateinit var delayedTrackActionUseCase: DelayedTrackActionUseCaseContract

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Инициализация Use Cases через Creator
        searchTracksUseCase = useCaseCreator.createSearchTracksUseCase()
        addTrackToHistoryUseCase = useCaseCreator.createAddTrackToHistoryUseCase()
        getSearchHistoryUseCase = useCaseCreator.createGetSearchHistoryUseCase()
        clearSearchHistoryUseCase = useCaseCreator.createClearSearchHistoryUseCase()
        filterTracksUseCase = useCaseCreator.createFilterTracksUseCase()
        formatTrackDurationUseCase = useCaseCreator.createFormatTrackDurationUseCase()
        delayedTrackActionUseCase = useCaseCreator.createDelayedTrackActionUseCase()

        initViews()
        setupClickListeners()
        setupTextWatchers()
        restoreState(savedInstanceState)
        loadHistory()
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
            viewType = Constants.Companion.VIEW_TYPE_TRACK,
            onTrackClick = { track ->
                onTrackClicked(track)
            },
            onClickPlayButton = { track ->
            },
            onAddToPlaylist = { track ->
            },
            onFavorite = { track ->
            },
            formatDurationUseCase = formatTrackDurationUseCase
        )

        recyclerView.adapter = tracksAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        historyAdapter = TrackAdapter(
            tracks = emptyList(),
            viewType = Constants.Companion.VIEW_TYPE_TRACK,
            onTrackClick = { track -> openAudioPlayer(track) },
            onClickPlayButton = {},
            onAddToPlaylist = {},
            onFavorite = {},
            formatDurationUseCase = formatTrackDurationUseCase
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
            if (isLastSearchFailed && lastSearchQuery != null) {
                performSearch(lastSearchQuery!!)
            }
        }
        clearHistoryButton.setOnClickListener {
            lifecycleScope.launch {
                clearSearchHistoryUseCase()
                loadHistory()
                historyRecyclerViewKit.visibility = View.GONE
            }
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
                    performSearch(query)
                }
            } else {
                updateTracksList(emptyList())
                showNoResults(true)
            }
        }
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            updateHistoryVisibility()
        }
    }
    private fun restoreState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(Constants.Companion.SEARCH_QUERY_KEY, "")
            searchEditText.setText(searchQuery)
            if (searchQuery.isNotEmpty()) performSearch(searchQuery)
        }
    }
    private fun loadHistory() {
        lifecycleScope.launch {
            val history = getSearchHistoryUseCase()
            historyAdapter.updateList(history)
            updateHistoryVisibility()
        }
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) return
        showLoading()
        lastSearchQuery = query
        lifecycleScope.launch {
            val result = searchTracksUseCase(query)
            if (result.isSuccess) {
                isLastSearchFailed = false
                errorLayout.visibility = View.GONE
                filteredTracks = result.getOrThrow()
                if (filteredTracks.isNotEmpty()) {
                    updateTracksList(filteredTracks)
                    showNoResults(false)
                } else {
                    showNoResults(true)
                }
            } else {
                isLastSearchFailed = true
                showError()
            }
            hideLoading()
        }
    }
    private fun filterAndUpdateTracks(query: String) {
        filteredTracks = filterTracksUseCase(tracks = filteredTracks, query = query)
        updateTracksList(filteredTracks)
    }

    private fun updateTracksList(tracks: List<Track>) {
        tracksAdapter.updateList(tracks)
        recyclerView.visibility = if (tracks.isNotEmpty()) View.VISIBLE else View.GONE
        showNoResults(tracks.isEmpty() && searchQuery.isNotEmpty())
    }

    private fun onTrackClicked(track: Track) {
        clickJob?.cancel()
        clickJob = lifecycleScope.launch {
            delayedTrackActionUseCase.invoke(
                track = track,
                delayMillis = 500L,
                onDelayedAction = { delayedTrack ->
                    lifecycleScope.launch {
                        addTrackToHistoryUseCase(delayedTrack)
                        loadHistory()
                        openAudioPlayer(delayedTrack)
                    }
                }
            )
        }
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
        progressBar.visibility = View.INVISIBLE
    }

    private fun showError() {
        errorLayout.visibility = View.VISIBLE
        noResultsLayout.visibility = View.GONE
    }

    private fun showNoResults(show: Boolean) {
        noResultsLayout.visibility = if (show && !isLastSearchFailed) View.VISIBLE else View.GONE
        errorLayout.visibility = View.GONE
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

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(Constants.Companion.SEARCH_QUERY_KEY, searchQuery)
    }

    override fun onResume() {
        super.onResume()
        loadHistory()
        updateHistoryVisibility()
    }

    override fun onDestroy() {
        super.onDestroy()
        clickJob?.cancel()
    }
}