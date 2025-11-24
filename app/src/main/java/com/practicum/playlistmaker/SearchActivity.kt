package com.practicum.playlistmaker

import Track
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
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
    private val itunesServiceBaseUrl = "https://itunes.apple.com"

    val retrofit = Retrofit.Builder()
        .baseUrl(itunesServiceBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val itunesService = retrofit.create(ItunesService::class.java)

    companion object {
        private const val SEARCH_QUERY_KEY = "search_query"
    }

    private var searchQuery: String = ""
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapter

    // Теперь нет локальной базы данных моковых данных. Все сохраняется в этой переменной
    private var allTracks: List<Track> = emptyList()
    private var filteredTracks: List<Track> = emptyList()

    private var lastSearchQuery: String? = null
    private var isLastSearchFailed = false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Восстановление сохраненного состояния
        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY) ?: ""
        }

        findViewById<TextView>(R.id.back).setOnClickListener { finish() }

        val searchEditText = findViewById<EditText>(R.id.search_edit_text)
        val resetButton = findViewById<ImageView>(R.id.reset_button)
        recyclerView = findViewById(R.id.recyclerView)

        adapter = TrackAdapter(emptyList()) // начально пустой список
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Восстановление текста поиска
        searchEditText.setText(searchQuery)

        resetButton.setOnClickListener {
            searchEditText.setText("")
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
        }

        searchEditText.doOnTextChanged { text, _, _, _ ->
            resetButton.visibility = if (text?.isNotEmpty() == true) View.VISIBLE else View.GONE
            searchQuery = text.toString()

            val query = searchQuery.lowercase()

            // Фильтрация по текущим данным
            filteredTracks = if (query.isEmpty()) {
                emptyList()
            } else {
                allTracks.filter {
                    it.trackName.lowercase().contains(query) ||
                            it.artistName.lowercase().contains(query)
                }
            }
            adapter.updateList(filteredTracks)

            // Скрываем сообщения-заглушки при пустом поиске
            if (query.isEmpty()) {
                findViewById<LinearLayout>(R.id.no_results_layout).visibility = View.GONE
                findViewById<LinearLayout>(R.id.error_layout).visibility = View.GONE
            }
        }

        // Обработка нажатия на кнопку Done
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch(searchQuery)

                // Скрываем клавиатуру
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
                true
            } else {
                false
            }
        }

        // Обработка кнопки "Обновить"
        val updateButton = findViewById<Button>(R.id.refresh_button)
        updateButton.setOnClickListener {
            if (isLastSearchFailed && lastSearchQuery != null) {
                performSearch(lastSearchQuery!!)
            }
        }

        // Если есть сохраненный запрос, повторно запускаем поиск
        if (searchQuery.isNotEmpty()) {
            performSearch(searchQuery)
        }
    }

    private fun performSearch(query: String) {
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

                        val filterQuery = searchQuery.lowercase()
                        filteredTracks = if (filterQuery.isEmpty()) {
                            emptyList()
                        } else {
                            allTracks.filter {
                                it.trackName.lowercase().contains(filterQuery) ||
                                        it.artistName.lowercase().contains(filterQuery)
                            }
                        }
                        adapter.updateList(filteredTracks)

                        // Проверка на пустой результат
                        if (searchResponse.results.isEmpty()) {
                            findViewById<LinearLayout>(R.id.no_results_layout).visibility = View.VISIBLE
                            findViewById<LinearLayout>(R.id.error_layout).visibility = View.GONE
                        } else {
                            findViewById<LinearLayout>(R.id.no_results_layout).visibility = View.GONE
                            findViewById<LinearLayout>(R.id.error_layout).visibility = View.GONE
                        }
                    }
                } else {
                    // Ошибка сервера
                    isLastSearchFailed = true
                    findViewById<LinearLayout>(R.id.error_layout).visibility = View.VISIBLE
                    findViewById<LinearLayout>(R.id.no_results_layout).visibility = View.GONE
                }
            }

            override fun onFailure(call: retrofit2.Call<SearchResponse>, t: Throwable) {
                // Ошибка сети или другой сбой
                isLastSearchFailed = true
                findViewById<LinearLayout>(R.id.error_layout).visibility = View.VISIBLE
                findViewById<LinearLayout>(R.id.no_results_layout).visibility = View.GONE
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchQuery)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY) ?: ""
        findViewById<EditText>(R.id.search_edit_text).setText(searchQuery)
        if (searchQuery.isNotEmpty()) {
            performSearch(searchQuery)
        }
    }
}