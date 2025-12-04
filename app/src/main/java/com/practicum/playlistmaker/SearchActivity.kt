package com.practicum.playlistmaker

import Track
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.practicum.playlistmaker.Constants.Companion.SEARCH_QUERY_KEY
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
    private lateinit var hintMessage: TextView
    private lateinit var searchHistory: SearchHistory
    private lateinit var historyRecyclerView: RecyclerView
    private var trackList = listOf<Track>()
    private lateinit var clearHistoryButton: Button
    private lateinit var historyRecyclerViewKit: LinearLayout

    // Другие переменные
    private lateinit var adapter: TrackAdapter
    private var allTracks: List<Track> = listOf()
    private var filteredTracks: List<Track> = listOf()
    private var searchQuery: String = ""
    private var lastSearchQuery: String? = null
    private var isLastSearchFailed: Boolean = false
    private lateinit var historyAdapter: TrackAdapter

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
        hintMessage = findViewById(R.id.searchHint)
        historyRecyclerView = findViewById(R.id.history_recycler_view)
        historyRecyclerViewKit = findViewById(R.id.search_history_layout)
        clearHistoryButton = findViewById(R.id.clear_history_button)

        // Восстановление состояния поиска
        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
        }

        // Настройка обработки кликов.
        backTextView.setOnClickListener { finish() } //при нажатии на элемент, связанный с backTextView, приложение или экран будут закрыты,вернётся к предыдущему экрану.

        // Установка текста поиска
        searchEditText.setText(searchQuery)

        // Обработка reset
        resetButton.setOnClickListener { // Происходит сброс состояния поиска: очищаются поле ввода и список отфильтрованных треко́в, а также скрывается клавиатура.
            searchEditText.setText("")
            filteredTracks = listOf()
            adapter.updateList(filteredTracks)
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
        }

        // Обработка изменения текста
        searchEditText.doOnTextChanged { text, _, _, _ ->
            // При каждом изменении текста в searchEditText выполняется блок кода внутри фигурных скобок.

            resetButton.visibility = if (!text.isNullOrEmpty()) View.VISIBLE else View.GONE
            // Если текст в searchEditText не пустой, то кнопка сброса (resetButton) становится видимой, иначе — скрытой.

            searchQuery = text.toString()
            // Текущее значение текста в searchEditText сохраняется в переменную searchQuery.

            val queryLower = searchQuery.lowercase()
            // Строка searchQuery преобразуется в нижний регистр и сохраняется в новую переменную queryLower.

            // Фильтрация данных
            filteredTracks = if (queryLower.isEmpty()) {
                listOf()
            } else {
                allTracks.filter {
                    it.trackName.lowercase().contains(queryLower) || it.artistName.lowercase()
                        .contains(queryLower)
                }
            }
            // Если queryLower пустая, filteredTracks устанавливается как пустой список.
            // Иначе происходит фильтрация списка allTracks: выбираются треки, в названии или имени исполнителя которых содержится queryLower (без учёта регистра).

            adapter.updateList(filteredTracks)
            // Обновляется список в адаптере с отфильтрованными треками.

            // Скрытие сообщений при пустом поиске
            if (searchQuery.isEmpty()) {
                noResultsLayout.visibility = View.GONE
                errorLayout.visibility = View.GONE
//                historyRecyclerViewKit.visibility = View.VISIBLE // Показываем историю поиска
            } else {
                historyRecyclerViewKit.visibility = View.GONE // Скрываем историю поиска
            }
            // Если searchQuery пустая, скрываются сообщения об отсутствии результатов и об ошибке, а также показывается история поиска.
            // Иначе история поиска скрывается.
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

        // Если есть сохранённый запрос, делаем поиск
        if (searchQuery.isNotEmpty()) {
            performSearch(searchQuery)
        }

        // Инициализация истории поиска и проверка на наличие сохранённой истории
       // searchHistory = SearchHistory(this)
        //val history = searchHistory.getHistory()

        // Обработка кнопки "Обновить"
        updateButton.setOnClickListener {
            if (isLastSearchFailed && lastSearchQuery != null) {
                performSearch(lastSearchQuery!!)
            }

        }

        searchHistory = SearchHistory(this)
        // Инициализация адаптера
        adapter = TrackAdapter(trackList) { track ->
        }
        adapter.updateList(trackList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        historyAdapter = TrackAdapter(searchHistory.getHistory()) { track ->
        }
        // После того, как  создали свой adapter
        adapter.setOnItemClickListener { track ->
            // Добавляем трек в историю поиска
            searchHistory.addTrack(track)
            // Обновляем список истории и уведомляем адаптер
            historyAdapter.updateList(searchHistory.getHistory())
        }
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter

        clearHistoryButton.setOnClickListener {
            searchHistory.clearHistory()
            historyAdapter.updateList(searchHistory.getHistory())
            if (searchHistory.getHistory().isEmpty()) {
                historyRecyclerViewKit.visibility = View.GONE
            }
        }

        //Устанавливаем слушатель изменения фокуса для отображения подсказки
        searchEditText.setOnFocusChangeListener { view, hasFocus ->
            hintMessage.visibility =
                if (hasFocus && searchEditText.text.isEmpty()) View.VISIBLE else View.GONE
            historyRecyclerViewKit.visibility =
                if(hasFocus && searchEditText.text.isEmpty() && searchHistory.getHistory().isNotEmpty()) View.VISIBLE else View.GONE
        }

        // Слушатель отслеживает изменения текста и управляет видимостью подсказки
        searchEditText.addTextChangedListener(object : TextWatcher {
            // Создаём анонимный объект, реализующий интерфейс TextWatcher

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Этот метод вызывается перед тем, как текст изменится. В данном случае он пуст.
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Этот метод вызывается, когда текст изменяется. Здесь проверяется, должен ли отображаться hintMessage.
                hintMessage.visibility =
                    if (searchEditText.hasFocus() && p0?.isEmpty() == true) View.VISIBLE else View.GONE
                // Если searchField имеет фокус и текст в нём пустой, то hintMessage становится видимым, иначе - невидимым.
                historyRecyclerViewKit.visibility =
                    if (searchEditText.hasFocus() && p0?.isEmpty() == true && searchHistory.getHistory().isNotEmpty()) View.VISIBLE else View.GONE
            }

            override fun afterTextChanged(p0: Editable?) {
                // Этот метод вызывается после того, как текст изменился. В данном случае он тоже пуст.
            }
        })



    }

    override fun onResume() {
        super.onResume()
        // Обновляем видимость historyRecyclerView при возобновлении активности
        if (searchQuery.isEmpty()) {
            historyRecyclerView.visibility = View.VISIBLE
        } else {
            historyRecyclerView.visibility = View.GONE
        }
    }

    // Поиск треков по заданному запросу query.
    // Он отправляет запрос к сервису iTunes и обрабатывает ответ,
    // обновляя список треков в адаптере и отображая соответствующие сообщения об отсутствии результатов или об ошибке.
    private fun performSearch(query: String) {
        lastSearchQuery = query // Сохраняем текущий запрос поиска
        val call = itunesService.search(query) // Делаем запрос к сервису iTunes для поиска
        call.enqueue(object : retrofit2.Callback<SearchResponse> {
            override fun onResponse(
                call: retrofit2.Call<SearchResponse>,
                response: retrofit2.Response<SearchResponse>
            ) {
                if (response.isSuccessful) { // Если запрос выполнен успешно
                    val searchResponse = response.body()
                    if (searchResponse != null) {
                        allTracks = searchResponse.results // Получаем все треки из ответа
                        lastSearchQuery = query
                        isLastSearchFailed = false // Устанавливаем флаг успешности поиска

                        val filterQuery =
                            searchQuery.lowercase() // Преобразуем запрос в нижний регистр для фильтрации
                        filteredTracks = if (filterQuery.isEmpty()) {
                            listOf() // Если запрос пустой, возвращаем пустой список
                        } else {
                            allTracks.filter { // Фильтруем треки по названию и артисту
                                it.trackName.lowercase().contains(filterQuery) ||
                                        it.artistName.lowercase().contains(filterQuery)
                            }
                        }
                        adapter.updateList(filteredTracks) // Обновляем список в адаптере

                        // Проверка на пустой результат
                        if (searchResponse.results.isEmpty()) {
                            noResultsLayout.visibility =
                                View.VISIBLE // Показываем сообщение об отсутствии результатов
                            errorLayout.visibility = View.GONE // Скрываем сообщение об ошибке
                        } else {
                            noResultsLayout.visibility = View.GONE
                            errorLayout.visibility = View.GONE
                        }
                    }
                } else {
                    // Ошибка сервера
                    isLastSearchFailed = true // Устанавливаем флаг ошибки
                    errorLayout.visibility = View.VISIBLE // Показываем сообщение об ошибке
                    noResultsLayout.visibility = View.GONE
                }
            }

            override fun onFailure(call: retrofit2.Call<SearchResponse>, t: Throwable) {
                // Ошибка сети или сбой
                isLastSearchFailed = true
                errorLayout.visibility = View.VISIBLE // Показываем сообщение об ошибке
                noResultsLayout.visibility = View.GONE
            }
        })
    }

    //Метод сохраняет текущее состояние приложения, в частности, текущий запрос поиска searchQuery, в Bundle.
    // Это необходимо для того, чтобы при изменении конфигурации состояние приложения могло быть восстановлено.
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(
            SEARCH_QUERY_KEY,
            searchQuery
        ) // Сохраняем текущий запрос поиска в Bundle
    }

    // Этот метод восстанавливает сохранённое состояние приложения из Bundle.
    // Он извлекает сохранённый запрос поиска и, если он не пустой, выполняет поиск по этому запросу.
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchQuery = savedInstanceState.getString(
            SEARCH_QUERY_KEY,
            ""
        ) // Восстанавливаем сохранённый запрос поиска
        searchEditText.setText(searchQuery)
        if (searchQuery.isNotEmpty()) {
            performSearch(searchQuery) // Если есть сохранённый запрос, выполняем поиск
        }
    }

}