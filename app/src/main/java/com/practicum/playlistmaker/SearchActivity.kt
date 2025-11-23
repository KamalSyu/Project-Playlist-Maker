package com.practicum.playlistmaker

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
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
    private var searchQuery : String = ""
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapter
    private val allTracks = TrackList().tracksList
    private var filteredTracks = allTracks.toList()


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        filteredTracks = allTracks.toList()

        findViewById<TextView>(R.id.back).setOnClickListener {
            finish()
        }

        val searchEditText = findViewById<EditText>(R.id.search_edit_text)
        val resetButton = findViewById<ImageView>(R.id.reset_button)
        recyclerView = findViewById(R.id.recyclerView)

        adapter = TrackAdapter(filteredTracks)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        resetButton.setOnClickListener {
            searchEditText.setText("")
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
        }

        searchEditText.doOnTextChanged { text, _, _, _ ->
            resetButton.visibility = if (text?.isNotEmpty() == true) View.VISIBLE else View.GONE
            searchQuery = text.toString()

            val query = searchQuery.lowercase()

            // Фильтрация
            filteredTracks = if (query.isEmpty()) {
                allTracks
            } else {
                allTracks.filter {
                    it.trackName.lowercase().contains(query) ||
                            it.artistName.lowercase().contains(query)
                }
            }
            // Обновление адаптера
            adapter.updateList(filteredTracks)

            // Скрываем сообщения-заглушки при пустом поиске
            if (query.isEmpty()) {
                findViewById<LinearLayout>(R.id.no_results_layout).visibility = View.GONE
                findViewById<LinearLayout>(R.id.error_layout).visibility = View.GONE
            }

            // Отправка запроса на поиск при нажатии на кнопку Done
            searchEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    performSearch(searchQuery)
                    true
                }
                false
            }
        }
    }

    private fun performSearch(query: String) {
        val call = itunesService.search(query)
        call.enqueue(object : retrofit2.Callback<SearchResponse> {
            override fun onResponse(call: retrofit2.Call<SearchResponse>, response: retrofit2.Response<SearchResponse>) {
                if (response.isSuccessful) {
                    val searchResponse = response.body()
                    adapter.updateList(searchResponse?.results ?: emptyList())

                    // Проверка на пустой результат
                    if (searchResponse?.results.isNullOrEmpty()) {
                        findViewById<LinearLayout>(R.id.no_results_layout).visibility = View.VISIBLE
                        findViewById<LinearLayout>(R.id.error_layout).visibility = View.GONE
                    } else {
                        findViewById<LinearLayout>(R.id.no_results_layout).visibility = View.GONE
                        findViewById<LinearLayout>(R.id.error_layout).visibility = View.GONE
                    }
                } else {
                    findViewById<LinearLayout>(R.id.error_layout).visibility = View.VISIBLE
                    findViewById<LinearLayout>(R.id.no_results_layout).visibility = View.GONE
                }
            }

            override fun onFailure(call: retrofit2.Call<SearchResponse>, t: Throwable) {
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
    }
}
