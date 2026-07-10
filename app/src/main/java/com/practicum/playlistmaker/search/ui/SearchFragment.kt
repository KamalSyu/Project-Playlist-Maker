package com.practicum.playlistmaker.search.ui

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.search.ui.adapter.SearchTrackAdapter
import com.practicum.playlistmaker.search.ui.parcel.toParcelable
import com.practicum.playlistmaker.search.ui.view.HistoryState
import com.practicum.playlistmaker.search.ui.view.ScreenState
import com.practicum.playlistmaker.search.ui.view.SearchState
import com.practicum.playlistmaker.search.ui.view.SearchViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    companion object {
        private const val SEARCH_QUERY_KEY = "search_query"
    }

    private val viewModel: SearchViewModel by viewModel()

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
    private lateinit var historyTitle: TextView


    private lateinit var tracksAdapter: SearchTrackAdapter
    private lateinit var historyAdapter: SearchTrackAdapter
    private var searchQuery: String = ""
    private var clickJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->

            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.updatePadding(top = statusBar.top)
            val isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val hasSearchFocus = searchEditText.hasFocus()
            updateBottomNavVisibility(isKeyboardVisible, hasSearchFocus)
            insets
        }
        initViews(view)
        setupClickListeners()
        setupTextWatchers()
        restoreState(savedInstanceState)
        observeViewModel()
    }

    private fun initViews(view: View) {
        backTextView = view.findViewById(R.id.back)
        searchEditText = view.findViewById(R.id.search_edit_text)
        resetButton = view.findViewById(R.id.reset_button)
        recyclerView = view.findViewById(R.id.recyclerView)
        noResultsLayout = view.findViewById(R.id.no_results_layout)
        errorLayout = view.findViewById(R.id.error_layout)
        updateButton = view.findViewById(R.id.refresh_button)
        historyRecyclerView = view.findViewById(R.id.history_recycler_view)
        clearHistoryButton = view.findViewById(R.id.clear_history_button)
        historyRecyclerViewKit = view.findViewById(R.id.search_history_layout)
        progressBar = view.findViewById(R.id.progressBar)
        historyTitle = view.findViewById(R.id.history_title)


        tracksAdapter = SearchTrackAdapter(
            tracks = emptyList(),
            onTrackClick = { track -> viewModel.onTrackClicked(track) },
            formatDurationUseCase = viewModel.formatTrackDurationUseCase
        )
        recyclerView.adapter = tracksAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        historyAdapter = SearchTrackAdapter(
            tracks = emptyList(),
            onTrackClick = { track -> openAudioPlayer(track) },
            formatDurationUseCase = viewModel.formatTrackDurationUseCase
        )
        historyRecyclerView.adapter = historyAdapter
        historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupClickListeners() {
        backTextView.setOnClickListener { requireActivity().onBackPressed() }

        resetButton.setOnClickListener {
            // 1) Очищаем поисковый запрос
            searchEditText.setText("")

            // Сразу убираем фокус, чтобы гарантированно скрыть клавиатуру
            searchEditText.clearFocus()

            // 2) Прячем клавиатуру
            hideKeyboard()

            // 3) Прячем кнопку (x)
            resetButton.visibility = View.GONE

            // 4) Прячем список треков (чтобы не было «старых» результатов)
            recyclerView.visibility = View.GONE

            // Сбрасываем список в адаптере (опционально, но надёжно)
            tracksAdapter.updateList(emptyList())
            viewModel.refreshHistory()

            updateHistoryVisibility()

        }

        updateButton.setOnClickListener {
            viewModel.retryLastSearch()
        }

        clearHistoryButton.setOnClickListener {
            viewModel.clearHistory()

            // Мгновенно скрываем контейнер истории (включая кнопку, заголовок и RecyclerView)
            historyRecyclerViewKit.visibility = View.GONE
            historyTitle.visibility = View.GONE
        }
    }


    private fun setupTextWatchers() {
        var searchJob: Job? = null
        searchEditText.doOnTextChanged { text, _, _, _ ->
            val query = text?.toString()?.trim() ?: ""
            searchQuery = query
            resetButton.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
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
            if (hasFocus || !searchEditText.text.isBlank()) {
                updateHistoryVisibility()
            }
        }

    }

    private fun restoreState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
            searchEditText.setText(searchQuery)
            if (savedInstanceState.getBoolean("isLoading", false)) {
                showLoading()
            }
            if (searchQuery.isNotEmpty()) viewModel.performSearch(searchQuery)
        }
    }

    private fun observeViewModel() {
        viewModel.screenState.observe(viewLifecycleOwner) { state ->
            when (state) {
                ScreenState.Initial -> {
                    hideLoading()
                    hideError()
                    updateTracksList(emptyList())
                    showNoResults(false)
                    updateHistoryVisibility()
                }
                ScreenState.Loading -> {
                    if (searchQuery.isNotEmpty() && searchEditText.hasFocus()) {
                        showLoading()
                        hideError()
                        showNoResults(false)
                    } else {
                        hideLoading()
                    }
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

//    private fun updateTracksList(tracks: List<Track>) {
//        tracksAdapter.updateList(tracks)
//        recyclerView.visibility = if (tracks.isNotEmpty()) View.VISIBLE else View.GONE
//        showNoResults(tracks.isEmpty() && searchQuery.isNotEmpty())
//    }

    private fun updateTracksList(tracks: List<Track>) {
        tracksAdapter.updateList(tracks)

        // Требование 1: если есть хотя бы одна песня — показываем список
        recyclerView.visibility = if (tracks.isNotEmpty()) View.VISIBLE else View.GONE

        // Показываем «Ничего не нашлось» только если:
        // - список пустой, И
        // - пользователь ввёл запрос (не пустая строка)
        val shouldShowNoResults = tracks.isEmpty() && !searchEditText.text.isNullOrBlank()
        noResultsLayout.visibility = if (shouldShowNoResults) View.VISIBLE else View.GONE
    }


    private fun openAudioPlayer(track: Track) {
        val bundle = Bundle().apply {
            putParcelable("track", track.toParcelable())
        }

        findNavController().navigate(
            R.id.action_searchFragment_to_audioPlayerFragment,
            bundle
        )
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        noResultsLayout.visibility = View.GONE
        errorLayout.visibility = View.GONE
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
    }

    private fun showError() {
        errorLayout.visibility = View.VISIBLE
        noResultsLayout.visibility = View.GONE
        recyclerView.visibility = View.GONE
    }

    private fun showNoResults(show: Boolean) {
        noResultsLayout.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun hideError() {
        errorLayout.visibility = View.GONE
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    // В SearchFragment.kt
    private fun updateHistoryVisibility() {
        val currentState = viewModel.screenState.value
        Log.d("SearchDebug", "=== updateHistoryVisibility START ===")
        Log.d("SearchDebug", "currentState = ${currentState?.javaClass?.simpleName}")

        if (currentState == null) {
            Log.d("SearchDebug", "currentState is null -> exit")
            return
        }

        val historyState = when (currentState) {
            is ScreenState.Results -> {
                Log.d("SearchDebug", "ScreenState.Results: historyState = ${currentState.historyState}")
                currentState.historyState
            }
            is ScreenState.Error -> {
                Log.d("SearchDebug", "ScreenState.Error: historyState = ${currentState.historyState}")
                currentState.historyState
            }
            is ScreenState.Idle -> {
                Log.d("SearchDebug", "ScreenState.Idle: historyState = ${currentState.historyState}")
                currentState.historyState
            }
            else -> {
                Log.d("SearchDebug", "Other state: historyState = HistoryState.Empty")
                HistoryState.Empty
            }
        }

        Log.d("SearchDebug", "final historyState = $historyState")

        // ГЛАВНАЯ ПРОВЕРКА: если истории нет — сразу скрываем
        if (historyState == HistoryState.Empty) {
            Log.d("SearchDebug", "historyState == Empty -> hide history")
            historyRecyclerViewKit.visibility = View.GONE
            historyTitle.visibility = View.GONE
            return
        } else {
            Log.d("SearchDebug", "historyState != Empty -> continue checks")
        }

        val queryText = searchEditText.text.toString().trim()
        val isEmptyQuery = queryText.isBlank()
        val hasFocus = searchEditText.hasFocus() && searchEditText.windowToken != null

        Log.d(
            "SearchDebug",
            "queryText='$queryText', isEmpty=$isEmptyQuery, hasFocus=$hasFocus"
        )

        val shouldShowHistory = when (historyState) {
            HistoryState.Loading -> {
                Log.d("SearchDebug", "HistoryState.Loading -> shouldShow=false")
                false
            }
            is HistoryState.HistoryLoaded -> {
                val result = isEmptyQuery && hasFocus && historyState.history.isNotEmpty()
                Log.d(
                    "SearchDebug",
                    "HistoryLoaded: isEmpty=$isEmptyQuery, hasFocus=$hasFocus, history.size=${historyState.history.size}, shouldShow=$result"
                )
                result
            }
            else -> {
                Log.d("SearchDebug", "Other historyState -> shouldShow=false")
                false
            }
        }

        historyRecyclerViewKit.visibility = if (shouldShowHistory) View.VISIBLE else View.GONE
        historyTitle.visibility = if (shouldShowHistory) View.VISIBLE else View.GONE

        Log.d("SearchDebug", "FINAL: historyRecyclerViewKit.visibility=${historyRecyclerViewKit.visibility}, historyTitle.visibility=${historyTitle.visibility}")
        Log.d("SearchDebug", "=== updateHistoryVisibility END ===")
    }




    private fun updateHistoryState(historyState: HistoryState) {
        when (historyState) {
            HistoryState.Loading -> {
                // Ничего не делаем: видимость управляет updateHistoryVisibility() по данным
            }
            HistoryState.Empty -> {
                historyAdapter.updateList(emptyList())
                updateHistoryVisibility()
            }
            is HistoryState.HistoryLoaded -> {
                historyAdapter.updateList(historyState.history)
                updateHistoryVisibility()
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchQuery)
        outState.putBoolean("isLoading", progressBar.visibility == View.VISIBLE)
    }

    override fun onResume() {
        super.onResume()
        updateHistoryVisibility()
    }


    private fun updateUIWithCurrentState() {
        val currentState = viewModel.screenState.value ?: return
        when (currentState) {
            ScreenState.Initial -> {
                hideLoading()
                hideError()
                updateTracksList(emptyList())
                showNoResults(false)
                updateHistoryVisibility()
            }
            ScreenState.Loading -> {
                if (searchQuery.isNotEmpty() && searchEditText.hasFocus()) {
                    showLoading()
                    hideError()
                    showNoResults(false)
                } else {
                    hideLoading()
                }
            }

            is ScreenState.Idle -> {
                hideLoading()
                updateTracksList(emptyList())
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
                updateTracksList(emptyList())
                updateHistoryState(currentState.historyState)
                showError()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clickJob?.cancel()
    }

    private fun updateBottomNavVisibility(isKeyboardVisible: Boolean, hasSearchFocus: Boolean) {
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        if (bottomNav != null) {
            val shouldHide = isKeyboardVisible && hasSearchFocus
            bottomNav.visibility = if (isKeyboardVisible) View.GONE else View.VISIBLE
        }
    }

}