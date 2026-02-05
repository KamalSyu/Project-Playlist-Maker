package com.practicum.playlistmaker.presentation.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.presentation.adapter.TrackAdapter
import com.practicum.playlistmaker.presentation.parcel.toParcelable
import com.practicum.playlistmaker.utils.Constants.Companion.SEARCH_QUERY_KEY
import com.practicum.playlistmaker.utils.Constants.Companion.VIEW_TYPE_TRACK
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject
import android.view.inputmethod.InputMethodManager
import com.practicum.playlistmaker.domain.usecase.AddTrackToHistoryUseCaseContract
import com.practicum.playlistmaker.domain.usecase.ClearSearchHistoryUseCaseContract
import com.practicum.playlistmaker.domain.usecase.FilterTracksUseCaseContract
import com.practicum.playlistmaker.domain.usecase.FormatTrackDurationUseCaseContract
import com.practicum.playlistmaker.domain.usecase.GetSearchHistoryUseCaseContract
import com.practicum.playlistmaker.domain.usecase.SearchTracksUseCaseContract
import com.practicum.playlistmaker.domain.usecase.UseCaseCreator
import kotlinx.coroutines.Job


@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {

    @Inject
    lateinit var useCaseCreator: UseCaseCreator

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
    private var clickJob: Job? = null

    // Use Cases через Creator
    private lateinit var searchTracksUseCase: SearchTracksUseCaseContract
    private lateinit var addTrackToHistoryUseCase: AddTrackToHistoryUseCaseContract
    private lateinit var getSearchHistoryUseCase: GetSearchHistoryUseCaseContract
    private lateinit var clearSearchHistoryUseCase: ClearSearchHistoryUseCaseContract
    private lateinit var filterTracksUseCase: FilterTracksUseCaseContract
    private lateinit var formatTrackDurationUseCase: FormatTrackDurationUseCaseContract  // Новое поле

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
        hintMessage = findViewById(R.id.searchHint)
        historyRecyclerView = findViewById(R.id.history_recycler_view)
        clearHistoryButton = findViewById(R.id.clear_history_button)
        historyRecyclerViewKit = findViewById(R.id.search_history_layout)
        progressBar = findViewById(R.id.progressBar)


        tracksAdapter = TrackAdapter(
            tracks = emptyList(),
            viewType = VIEW_TYPE_TRACK,
            onTrackClick = { track -> onTrackClicked(track) },
            onClickPlayButton = {},
            formatDurationUseCase = formatTrackDurationUseCase  // Передача
        )
        recyclerView.adapter = tracksAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)


        historyAdapter = TrackAdapter(
            tracks = emptyList(),
            viewType = VIEW_TYPE_TRACK,
            onTrackClick = { track -> openAudioPlayer(track) },
            onClickPlayButton = {},
            formatDurationUseCase = formatTrackDurationUseCase  // Передача

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
            showNoResults(false)               // Скрываем заглушку (если была)
        }
        updateButton.setOnClickListener {
            if (isLastSearchFailed && lastSearchQuery != null) {
                performSearch(lastSearchQuery!!)
            }
        }

        clearHistoryButton.setOnClickListener {
            lifecycleScope.launch {
                clearSearchHistoryUseCase()
                loadHistory()  // Это обновит adapter, список станет пустым

                // Скрываем элементы
                historyRecyclerViewKit.visibility = View.GONE  // Контейнер истории
            }
        }

    }

    private fun setupTextWatchers() {
        var searchJob: Job? = null


        searchEditText.doOnTextChanged { text, _, _, _ ->
            val query = text?.toString()?.trim() ?: ""
            searchQuery = query

            // Показать/скрыть кнопку сброса
            resetButton.visibility = if (query.isNotEmpty()) View.VISIBLE else View.INVISIBLE

            // Обновить подсказки и историю
            updateHintVisibility(query.isEmpty())
            updateHistoryVisibility()

            // Отменить предыдущий debounce, если был
            searchJob?.cancel()

            if (query.isNotEmpty()) {
                // Запустить новый debounce (2 сек)
                searchJob = lifecycleScope.launch {
                    delay(2000) // 2 секунды
                    performSearch(query)
                }
            } else {
                // Если строка пуста — сразу очистить результаты
                updateTracksList(emptyList())
                showNoResults(true)
            }
        }

        // Обработчик фокуса
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            updateHintVisibility(hasFocus && searchEditText.text.isEmpty())
            updateHistoryVisibility()
        }
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
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
                    updateTracksList(filteredTracks)  // Показываем список
                    showNoResults(false)           // Скрываем заглушку
                } else {
                    showNoResults(true)             // Показываем заглушку
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
        // Отменяем предыдущую задачу, если она есть
        clickJob?.cancel()

        // Создаем новую задачу с задержкой
        clickJob = lifecycleScope.launch {
            delay(500) // Задержка 500 мс (можно настроить)

            // После задержки выполняем действия
            lifecycleScope.launch {
                addTrackToHistoryUseCase(track)
                loadHistory()
                openAudioPlayer(track)
            }
        }
    }


    private fun openAudioPlayer(track: Track) {
        val intent = Intent(this, AudioPlayerActivity::class.java)

        // Конвертируем Track в ParcelableTrack перед передачей
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

    private fun updateHintVisibility(show: Boolean) {
        hintMessage.visibility = if (show) View.VISIBLE else View.GONE
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
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchQuery)
    }

    override fun onResume() {
        super.onResume()
        loadHistory()
        updateHistoryVisibility()
    }

    override fun onDestroy() {
        super.onDestroy()
        clickJob?.cancel() // Отменяем задачу при уничтожении активности
    }
}
