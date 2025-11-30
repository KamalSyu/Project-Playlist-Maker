package com.practicum.playlistmaker

import Track
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class SearchActivity : AppCompatActivity() {
    // Объявляем переменные для вьюшек
    private lateinit var backTextView: TextView
    private lateinit var searchEditText: EditText
    private lateinit var resetButton: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var noResultsLayout: LinearLayout
    private lateinit var errorLayout: LinearLayout
    private lateinit var updateButton: Button

    // Другие переменные
    private lateinit var adapter: TrackAdapter
    private var allTracks: List<Track> = listOf()
    private var filteredTracks: List<Track> = listOf()
    private var searchQuery: String = ""
    private var lastSearchQuery: String? = null
    private var isLastSearchFailed: Boolean = false

    companion object {
        const val SEARCH_QUERY_KEY = "SEARCH_QUERY"
    }

        private val itunesServiceBaseUrl = "https://itunes.apple.com"

    val retrofit = Retrofit.Builder()
        .baseUrl(itunesServiceBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val itunesService = retrofit.create(ItunesService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Инициализация вьюшек
        backTextView = findViewById(R.id.back)
        searchEditText = findViewById(R.id.search_edit_text)
        resetButton = findViewById(R.id.reset_button)
        recyclerView = findViewById(R.id.recyclerView)
        noResultsLayout = findViewById(R.id.no_results_layout)
        errorLayout = findViewById(R.id.error_layout)
        updateButton = findViewById(R.id.refresh_button)

        // Восстановление состояния поиска
        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
        }

        // Настройка обработки кликов
        backTextView.setOnClickListener { finish() }

        // Установка текста поиска
        searchEditText.setText(searchQuery)

        // Обработка reset
        resetButton.setOnClickListener {
            searchEditText.setText("")
            filteredTracks = listOf()
            adapter.updateList(filteredTracks)
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
        }

        // Обработка изменения текста
        searchEditText.doOnTextChanged { text, _, _, _ ->
            resetButton.visibility = if (!text.isNullOrEmpty()) View.VISIBLE else View.GONE
            searchQuery = text.toString()

            val queryLower = searchQuery.lowercase()

            // Фильтрация данных
            filteredTracks = if (queryLower.isEmpty()) {
                listOf()
            } else {
                allTracks.filter {
                    it.trackName.lowercase().contains(queryLower) || it.artistName.lowercase().contains(queryLower)
                }
            }
            adapter.updateList(filteredTracks)

            // Скрытие сообщений при пустом поиске
            if (searchQuery.isEmpty()) {
                noResultsLayout.visibility = View.GONE
                errorLayout.visibility = View.GONE
            }
        }

        // Обработка кнопки "Готово" на клавиатуре
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch(searchQuery)

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
                true
            } else {
                false
            }
        }

        // Инициализация адаптера с обработчиком клика
        adapter = TrackAdapter(emptyList()) { track ->
            saveTrackToHistory(track)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Если есть сохранённый запрос, делаем поиск
        if (searchQuery.isNotEmpty()) {
            performSearch(searchQuery)
        }

        // Обработка кнопки "Обновить"
        updateButton.setOnClickListener {
            Log.d("MyApp", "Кнопка Обновить нажата")
            if (isLastSearchFailed && lastSearchQuery != null) {
                Log.d("MyApp", "Выполняем повторный поиск: $lastSearchQuery")
                performSearch(lastSearchQuery!!)
            }else {
                Log.d("MyApp", "Условие не выполнено: isLastSearchFailed = $isLastSearchFailed, lastSearchQuery = $lastSearchQuery")
            }

        }

        // Устанавливаем слушатель изменения фокуса для searchEditText
        searchEditText.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (!hasFocus && searchEditText.text.isEmpty()) {
                showSearchHistory() // Показываем историю поиска, если нет фокуса и текст пуст
            } else {
                hideSearchHistory() // Скрываем историю поиска в остальных случаях
            }
        }
    }

    private fun saveTrackToHistory(track: Track) {
        // Создаем экземпляр класса SearchHistory, передавая ему SharedPreferences для сохранения истории поиска
        val searchHistory = SearchHistory(getSharedPreferences("search_history_preferences", Context.MODE_PRIVATE))
        // Сохраняем трек в историю поиска
        searchHistory.addTrackToHistory(track)
    }

    private fun showSearchHistory() {
        // Получаем RecyclerView, который используется для отображения истории поиска
        val searchHistoryContainer = findViewById<RecyclerView>(R.id.search_history_recycler_view)
        // Делаем RecyclerView видимым, чтобы показать историю поиска
        searchHistoryContainer.visibility = View.VISIBLE
    }

    private fun hideSearchHistory() {
        // Получаем RecyclerView, который используется для отображения истории поиска
        val searchHistoryContainer = findViewById<RecyclerView>(R.id.search_history_recycler_view)
        // Скрываем RecyclerView, чтобы скрыть историю поиска
        searchHistoryContainer.visibility = View.GONE
    }

    private fun performSearch(query: String) {
        lastSearchQuery = query
        val call = itunesService.search(query)
        call.enqueue(object : retrofit2.Callback<SearchResponse> {
            override fun onResponse(
                call: retrofit2.Call<SearchResponse>,
                response: retrofit2.Response<SearchResponse>
            ) {
                if (response.isSuccessful) {
                    val searchResponse = response.body()
                    if (searchResponse != null) {
                        allTracks = searchResponse.results
                        lastSearchQuery = query
                        isLastSearchFailed = false
                        Log.d("MyApp", "Поиск успешен, lastSearchQuery: $lastSearchQuery, isLastSearchFailed: $isLastSearchFailed")

                        val filterQuery = searchQuery.lowercase()
                        filteredTracks = if (filterQuery.isEmpty()) {
                            listOf()
                        } else {
                            allTracks.filter {
                                it.trackName.lowercase().contains(filterQuery) ||
                                        it.artistName.lowercase().contains(filterQuery)
                            }
                        }
                        adapter.updateList(filteredTracks)

                        // Проверка на пустой результат
                        if (searchResponse.results.isEmpty()) {
                            noResultsLayout.visibility = View.VISIBLE
                            errorLayout.visibility = View.GONE
                        } else {
                            noResultsLayout.visibility = View.GONE
                            errorLayout.visibility = View.GONE
                        }
                    }
                } else {
                    // Ошибка сервера
                    isLastSearchFailed = true
                    Log.d("MyApp", "Ошибка сервера, isLastSearchFailed: $isLastSearchFailed")
                    errorLayout.visibility = View.VISIBLE
                    noResultsLayout.visibility = View.GONE
                }
            }

            override fun onFailure(call: retrofit2.Call<SearchResponse>, t: Throwable) {
                // Ошибка сети или сбой
                isLastSearchFailed = true
                errorLayout.visibility = View.VISIBLE
                noResultsLayout.visibility = View.GONE
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchQuery)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
        searchEditText.setText(searchQuery)
        if (searchQuery.isNotEmpty()) {
            performSearch(searchQuery)
        }
    }
}
