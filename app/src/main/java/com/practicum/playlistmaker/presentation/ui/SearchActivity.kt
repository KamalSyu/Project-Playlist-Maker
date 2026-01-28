package com.practicum.playlistmaker.presentation.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.domain.repository.HistoryRepository
import com.practicum.playlistmaker.domain.repository.ItunesRepository
import com.practicum.playlistmaker.domain.usecase.AddTrackToHistoryUseCase
import com.practicum.playlistmaker.domain.usecase.ClearSearchHistoryUseCase
import com.practicum.playlistmaker.domain.usecase.FilterTracksUseCase
import com.practicum.playlistmaker.domain.usecase.GetSearchHistoryUseCase
import com.practicum.playlistmaker.domain.usecase.SearchTracksUseCase
import com.practicum.playlistmaker.domain.usecase.UseCaseCreator
import com.practicum.playlistmaker.presentation.adapter.TrackAdapter
import com.practicum.playlistmaker.presentation.util.Constants.Companion.SEARCH_QUERY_KEY
import com.practicum.playlistmaker.presentation.util.Constants.Companion.VIEW_TYPE_TRACK
import com.practicum.playlistmaker.presentation.util.DateFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.view.inputmethod.InputMethodManager
import com.practicum.playlistmaker.domain.repository.PlayerRepository
import com.practicum.playlistmaker.domain.repository.SettingsRepository
import javax.inject.Inject

@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {

    @Inject lateinit var searchTracksUseCase: SearchTracksUseCase
    @Inject lateinit var addTrackToHistoryUseCase: AddTrackToHistoryUseCase
    @Inject lateinit var getSearchHistoryUseCase: GetSearchHistoryUseCase
    @Inject lateinit var clearSearchHistoryUseCase: ClearSearchHistoryUseCase
    @Inject lateinit var filterTracksUseCase: FilterTracksUseCase

    // Вьюшки
    private lateinit var backTextView: TextView
    private lateinit var searchEditText: EditText
    private lateinit var resetButton: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var noResultsLayout: LinearLayout
    private lateinit var errorLayout: LinearLayout
    private lateinit var updateButton: Button
    private lateinit var hintMessage: TextView
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initViews()
        setupClickListeners()
        setupTextWatchers()
        restoreState(savedInstanceState)
        loadHistory()
    }

    private fun initViews() {
        Log.d("SearchActivity", "initViews() called")
        backTextView = findViewById(R.id.back)
        searchEditText = findViewById(R.id.search_edit_text)
        resetButton = findViewById(R.id.reset_button)
        recyclerView = findViewById(R.id.recyclerView)
        noResultsLayout = findViewById(R.id.no_results_layout)
        errorLayout = findViewById(R.id.error_layout)
        updateButton = findViewById(R.id.refresh_button)
        hintMessage = findViewById(R.id.searchHint)
        historyRecyclerView = findViewById(R.id.history_recycler_view)
        clearHistoryButton = findViewById(R.id.clear_history_button)
        historyRecyclerViewKit = findViewById(R.id.search_history_layout)
        progressBar = findViewById(R.id.progressBar)


        tracksAdapter = TrackAdapter(
            tracks = emptyList(),
            viewType = VIEW_TYPE_TRACK,
            onTrackClick = { track -> onTrackClicked(track) },
            onClickPlayButton = {}
        )
        recyclerView.adapter = tracksAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        Log.d("SearchActivity", "RecyclerView initialized")

        historyAdapter = TrackAdapter(
            tracks = emptyList(),
            viewType = VIEW_TYPE_TRACK,
            onTrackClick = { track -> openAudioPlayer(track) },
            onClickPlayButton = {}
        )
        historyRecyclerView.adapter = historyAdapter
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        Log.d("SearchActivity", "HistoryRecyclerView initialized")

    }

    private fun setupClickListeners() {
        Log.d("SearchActivity", "setupClickListeners() called")

        backTextView.setOnClickListener {
            Log.d("SearchActivity", "backTextView clicked → finish()")
            finish() }
        resetButton.setOnClickListener {
            Log.d("SearchActivity", "resetButton clicked → clear text and UI")
            searchEditText.setText("")
            updateTracksList(emptyList())
            hideKeyboard()
        }
        updateButton.setOnClickListener {
            Log.d("SearchActivity", "updateButton clicked → retry search")
            if (isLastSearchFailed && lastSearchQuery != null) {
                performSearch(lastSearchQuery!!)
            }
        }
        clearHistoryButton.setOnClickListener {
            Log.d("SearchActivity", "clearHistoryButton clicked → clear history")

            lifecycleScope.launch {
                clearSearchHistoryUseCase()
                loadHistory()
            }
        }
    }

    private fun setupTextWatchers() {
        Log.d("SearchActivity", "setupTextWatchers() called")

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                Log.d("SearchActivity", "IME_ACTION_DONE → performSearch()")

                performSearch(searchEditText.text.toString())
                hideKeyboard()
                true
            } else false
        }
        searchEditText.doOnTextChanged { text, _, _, _ ->
            val query = text?.toString() ?: ""
            searchQuery = query
            Log.d("SearchActivity", "Text changed: query='$query'")
            resetButton.visibility = if (query.isNotEmpty()) View.VISIBLE else View.INVISIBLE
            filterAndUpdateTracks(query)
            updateHintVisibility(query.isEmpty())
            updateHistoryVisibility(query.isEmpty())
        }
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            Log.d("SearchActivity", "Focus changed: hasFocus=$hasFocus")

            updateHintVisibility(hasFocus && searchEditText.text.isEmpty())
            updateHistoryVisibility(hasFocus && searchEditText.text.isEmpty())
        }
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        Log.d("SearchActivity", "restoreState() called")

        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
            searchEditText.setText(searchQuery)
            if (searchQuery.isNotEmpty()) performSearch(searchQuery)
        }else {
            Log.d("SearchActivity", "No savedInstanceState → skip restore")
        }

    }

    private fun loadHistory() {
        if (!::getSearchHistoryUseCase.isInitialized) {
            Log.e("SearchActivity", "getSearchHistoryUseCase НЕ инициализирован!")
            return
        }
        lifecycleScope.launch {
            val history = getSearchHistoryUseCase()
            historyAdapter.updateList(history)
            historyRecyclerViewKit.visibility = if (history.isNotEmpty()) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun performSearch(query: String) {
        Log.d("SearchActivity", "performSearch() called with query: '$query'")

        if (query.isEmpty()) return
        showLoading()
        lastSearchQuery = query
        Log.d("SearchActivity", "Установлен lastSearchQuery: '$lastSearchQuery'")

        lifecycleScope.launch {
            Log.d("SearchActivity", "Запуск поиска треков для запроса: '$query'")

            val result = searchTracksUseCase(query)
            if (result.isSuccess) {
                isLastSearchFailed = false
                filteredTracks = result.getOrThrow()
                Log.d("SearchActivity", "Поиск успешен, найдено треков: ${filteredTracks.size}")

                updateTracksList(filteredTracks)
                showNoResults(filteredTracks.isEmpty())
            } else {
                isLastSearchFailed = true
                Log.e("SearchActivity", "Поиск не удался: ${result.exceptionOrNull()?.message}")

                showError()
            }
            hideLoading()
        }
    }

    private fun filterAndUpdateTracks(query: String) {
        Log.d("SearchActivity", "filterAndUpdateTracks() called with query: '$query'")

        filteredTracks = filterTracksUseCase(tracks = filteredTracks, query = query)
        Log.d("SearchActivity", "Отфильтровано треков: ${filteredTracks.size}")

        updateTracksList(filteredTracks)
    }

    private fun updateTracksList(tracks: List<Track>) {
        Log.d("SearchActivity", "updateTracksList() called, размер списка: ${tracks.size}")

        tracksAdapter.updateList(tracks)
        recyclerView.visibility = if (tracks.isNotEmpty()) View.VISIBLE else View.GONE
        showNoResults(tracks.isEmpty() && searchQuery.isNotEmpty())
    }

    private fun onTrackClicked(track: Track) {
        Log.d("SearchActivity", "onTrackClicked() called for track: ")

        lifecycleScope.launch {
            addTrackToHistoryUseCase(track)
            Log.d("SearchActivity", "Трек добавлен в историю: ")

            loadHistory()
            openAudioPlayer(track)
        }
    }

    private fun openAudioPlayer(track: Track) {
        Log.d("SearchActivity", "openAudioPlayer() called for track: ")

        val intent = Intent(this, AudioPlayerActivity::class.java)
        intent.putExtra("track", track)
        startActivity(intent)
    }

    private fun showLoading() {
        Log.d("SearchActivity", "showLoading() → показываем прогресс")

        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.INVISIBLE
        noResultsLayout.visibility = View.INVISIBLE
        errorLayout.visibility = View.INVISIBLE
    }

    private fun hideLoading() {
        Log.d("SearchActivity", "hideLoading() → скрываем прогресс")

        progressBar.visibility = View.INVISIBLE
    }

    private fun showError() {
        Log.d("SearchActivity", "showError() → отображаем экран ошибки")

        errorLayout.visibility = View.VISIBLE
    }

    private fun showNoResults(show: Boolean) {
        Log.d("SearchActivity", "showNoResults(show=$show)")

        noResultsLayout.visibility = if (show) View.VISIBLE else View.GONE
    }


    private fun updateHintVisibility(show: Boolean) {
        Log.d("SearchActivity", "updateHintVisibility(show=$show)")

        hintMessage.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun updateHistoryVisibility(show: Boolean) {
        Log.d("SearchActivity", "updateHistoryVisibility(show=$show), itemCount=${historyAdapter.itemCount}")

        historyRecyclerViewKit.visibility = if (show && historyAdapter.itemCount > 0) View.VISIBLE else View.GONE
    }

    private fun hideKeyboard() {
        Log.d("SearchActivity", "hideKeyboard() → скрываем клавиатуру")

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d("SearchActivity", "onSaveInstanceState() called")

        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchQuery)
        Log.d("SearchActivity", "Сохранён searchQuery: '$searchQuery'")

    }

    override fun onResume() {
        Log.d("SearchActivity", "onResume() called")

        super.onResume()
        loadHistory()
    }
}