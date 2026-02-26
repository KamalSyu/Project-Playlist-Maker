package com.practicum.playlistmaker.search.ui

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Context
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
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.player.ui.AudioPlayerActivity
import com.practicum.playlistmaker.search.ui.adapter.SearchTrackAdapter
import com.practicum.playlistmaker.search.ui.parcel.toParcelable
import com.practicum.playlistmaker.core.constants.Constants.Companion.SEARCH_QUERY_KEY
import com.practicum.playlistmaker.search.ui.view.HistoryState
import com.practicum.playlistmaker.search.ui.view.ScreenState
import com.practicum.playlistmaker.search.ui.view.SearchState
import com.practicum.playlistmaker.search.ui.view.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Активность для поиска треков и отображения истории поиска.
 * Реализует функционал:
 * - поиск треков с задержкой ввода;
 * - отображение результатов поиска, ошибок и состояния загрузки;
 * - управление историей поиска (просмотр, очистка);
 * - навигация к аудиоплееру при клике на трек.
 *
 * Использует ViewModel для управления состоянием и взаимодействия с бизнес‑логикой.
 */
@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {

    /** ViewModel для управления поиском и историей */
    private val viewModel: SearchViewModel by viewModels()

    // UI‑компоненты активности
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

    // Адаптеры для RecyclerView
    private lateinit var tracksAdapter: SearchTrackAdapter
    private lateinit var historyAdapter: SearchTrackAdapter

    // Вспомогательные данные
    private var searchQuery: String = ""
    private var clickJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_search)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBar.top)
            insets
        }

        initViews()
        setupClickListeners()
        setupTextWatchers()
        restoreState(savedInstanceState)
        observeViewModel()
        viewModel.loadHistory()
    }

    /**
     * Инициализирует и настраивает все UI‑компоненты:
     * - находит вью по ID;
     * - создаёт и настраивает адаптеры для списков треков и истории;
     * - устанавливает LayoutManager для RecyclerView.
     */
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

        // Адаптер для результатов поиска
        tracksAdapter = SearchTrackAdapter(
            tracks = emptyList(),
            onTrackClick = { track -> viewModel.onTrackClicked(track) },
            formatDurationUseCase = viewModel.formatTrackDurationUseCase
        )

        recyclerView.adapter = tracksAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Адаптер для истории поиска
        historyAdapter = SearchTrackAdapter(
            tracks = emptyList(),
            onTrackClick = { track -> openAudioPlayer(track) },
            formatDurationUseCase = viewModel.formatTrackDurationUseCase
        )
        historyRecyclerView.adapter = historyAdapter
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    /**
     * Настраивает обработчики кликов для кнопок:
     * - «назад» — закрывает активность;
     * - сброс — очищает поле поиска, скрывает клавиатуру, обновляет UI;
     * - обновление — повторяет поиск при ошибке;
     * - очистка истории — вызывает очистку истории в ViewModel.
     */
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
            historyRecyclerViewKit.visibility = View.GONE // Скрываем блок истории после очистки
        }
    }

    /**
     * Настраивает наблюдение за вводом текста в поле поиска:
     * - отображает/скрывает кнопку сброса;
     * - обновляет видимость истории;
     * - реализует debounce  перед отправкой запроса;
     * - фильтрует локальные треки при пустом запросе.
     */
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
                showNoResults(false)
            }
        }

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            updateHistoryVisibility()
        }
    }

    /**
     * Восстанавливает состояние активности из сохранённых данных (например, при повороте экрана).
     * Устанавливает текст поиска и запускает поиск, если запрос не пуст.
     *
     * @param savedInstanceState пакет с сохранённым состоянием
     */
    private fun restoreState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
            searchEditText.setText(searchQuery)
            if (searchQuery.isNotEmpty()) viewModel.performSearch(searchQuery)
        }
    }

    /**
     * Подписывается на изменения состояния ViewModel:
     * - searchState: отображает загрузку, результаты, ошибки, отсутствие результатов;
     * - historyState: обновляет список истории и её видимость;
     * - trackToOpen: открывает аудиоплеер для выбранного трека.
     */
    private fun observeViewModel() {
        viewModel.screenState.observe(this) { state ->
            when (state) {
                ScreenState.Initial -> {
                    hideLoading()
                    hideError()
                    updateTracksList(emptyList())
                    showNoResults(false)
                    updateHistoryVisibility()
                }

                ScreenState.Loading -> {
                    showLoading()
                    hideError()
                    showNoResults(false)
                }

                is ScreenState.Idle -> {
                    hideLoading()
                    updateTracksList(emptyList())
                    updateHistoryState(state.historyState)
                    showNoResults(false)
                    state.trackToOpen?.let { track ->
                        openAudioPlayer(track)
                        viewModel.resetTrackToOpen()
                    }
                }

                is ScreenState.Results -> {
                    hideLoading()
                    val tracks = when (state.searchState) {
                        is SearchState.Results -> state.searchState.tracks
                        else -> emptyList()
                    }
                    updateTracksList(tracks)
                    updateHistoryState(state.historyState)
                    showNoResults(tracks.isEmpty() && searchQuery.isNotEmpty())
                    state.trackToOpen?.let { track ->
                        openAudioPlayer(track)
                        viewModel.resetTrackToOpen()
                    }
                }

                is ScreenState.Error -> {
                    hideLoading()
                    updateTracksList(emptyList())
                    updateHistoryState(HistoryState.Empty)
                    showError()
                    state.trackToOpen?.let { track ->
                        openAudioPlayer(track)
                        viewModel.resetTrackToOpen()
                    }
                }
            }
        }
    }

    /**
     * Обновляет список треков в адаптере и управляет видимостью RecyclerView и макета «нет результатов».
     *
     * @param tracks список треков для отображения
     */
    private fun updateTracksList(tracks: List<Track>) {
        tracksAdapter.updateList(tracks)
        recyclerView.visibility = if (tracks.isNotEmpty()) View.VISIBLE else View.GONE
        showNoResults(tracks.isEmpty() && searchQuery.isNotEmpty())
    }

    /**
     * Открывает экран аудиоплеера для выбранного трека.
     * Передаёт трек через Intent с использованием Parcelable.
     *
     * @param track трек для воспроизведения
     */
    private fun openAudioPlayer(track: Track) {
        val intent = Intent(this, AudioPlayerActivity::class.java)
        val parcelableTrack = track.toParcelable()
        intent.putExtra("track", parcelableTrack)
        startActivity(intent)
    }

    /**
     * Отображает индикатор загрузки и скрывает другие элементы интерфейса.
     */
    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.INVISIBLE
        noResultsLayout.visibility = View.INVISIBLE
        errorLayout.visibility = View.INVISIBLE
    }

    /**
     * Скрывает индикатор загрузки.
     */
    private fun hideLoading() {
        progressBar.visibility = View.INVISIBLE
    }

    /**
     * Отображает макет с сообщением об ошибке сети/API.
     */
    private fun showError() {
        errorLayout.visibility = View.VISIBLE
        noResultsLayout.visibility = View.GONE
        recyclerView.visibility = View.GONE
        historyRecyclerViewKit.visibility = View.GONE
    }

    /**
     * Управляет видимостью макета «нет результатов» в зависимости от условий.
     * Показывает, если:
     * - нет результатов;
     * - запрос не пуст;
     * - последний поиск не завершился ошибкой.
     *
     * @param show флаг, указывающий, нужно ли показать макет
     */
    private fun showNoResults(show: Boolean) {
        noResultsLayout.visibility = if (show) View.VISIBLE else View.GONE
        errorLayout.visibility = View.GONE
    }

    /**
     * Скрывает виртуальную клавиатуру.
     */
    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    /**
     * Обновляет видимость блока истории поиска в зависимости от:
     * - наличия фокуса на поле поиска;
     * - пустоты запроса;
     * - наличия записей в истории.
     */
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

    /**
     * Обновляет список истории и её видимость на основе HistoryState
     *
     * @param historyState текущее состояние истории
     */
    private fun updateHistoryState(historyState: HistoryState) {
        when (historyState) {
            HistoryState.Loading -> {}
            HistoryState.Empty -> {
                historyAdapter.updateList(emptyList())
                updateHistoryVisibility()
            }
            is HistoryState.HistoryLoaded -> {
                historyAdapter.updateList(historyState.history)
                updateHistoryVisibility()
            }
            HistoryState.HistoryCleared -> {
                historyRecyclerViewKit.visibility = View.GONE
                updateHistoryVisibility()
            }
        }
    }

    private fun updateUIWithCurrentState() {
        val currentState = viewModel.screenState.value
        if (currentState != null) {
            when (currentState) {
                ScreenState.Initial -> {
                    hideLoading()
                    hideError()
                    updateTracksList(emptyList())
                    showNoResults(false)
                    updateHistoryVisibility()
                }
                ScreenState.Loading -> {
                    showLoading()
                    hideError()
                    showNoResults(false)
                }
                is ScreenState.Idle -> {
                    hideLoading()
                    updateHistoryState(currentState.historyState)
                    showNoResults(false)
                }
                is ScreenState.Results -> {
                    hideLoading()
                    val tracks = when (currentState.searchState) {
                        is SearchState.Results -> currentState.searchState.tracks
                        else -> emptyList()
                    }
                    updateTracksList(tracks)
                    updateHistoryState(currentState.historyState)
                    showNoResults(tracks.isEmpty() && searchQuery.isNotEmpty())
                }
                is ScreenState.Error -> {
                    hideLoading()
                    updateHistoryState(currentState.historyState)
                    showError()
                }
            }
        }
    }

    /**
     * Скрывает макет с ошибкой
     */
    private fun hideError() {
        errorLayout.visibility = View.GONE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchQuery)
    }

    override fun onResume() {
        super.onResume()
        // Если есть последний поисковый запрос и текущее состояние не является результатами поиска,
        // выполняем повторный поиск, чтобы восстановить список треков
        if (viewModel.lastSearchQuery != null && viewModel.lastSearchQuery!!.isNotEmpty()) {
            val currentState = viewModel.screenState.value
            if (currentState !is ScreenState.Results) {
                viewModel.performSearch(viewModel.lastSearchQuery!!)
            } else {
                // Если уже в состоянии Results, просто обновляем UI с текущими данными
                updateUIWithCurrentState()
            }
        } else {
            // Если нет поискового запроса, просто обновляем UI
            updateUIWithCurrentState()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clickJob?.cancel()
    }
}
