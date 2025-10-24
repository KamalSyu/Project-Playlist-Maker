package com.practicum.playlistmaker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged

class SearchActivity : AppCompatActivity() {

    companion object {
        private const val SEARCH_QUERY_KEY = "search_query"
    }
    private var searchQuery : String = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        findViewById<TextView>(R.id.back).setOnClickListener {
            finish()
        }

        val searchEditText = findViewById<EditText>(R.id.search_edit_text)
        val resetButton = findViewById<ImageView>(R.id.reset_button)

        resetButton.setOnClickListener {
            searchEditText.setText("")
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
        }


        searchEditText.doOnTextChanged { text, _, _, _ ->
            resetButton.visibility = if (text?.isNotEmpty() == true) View.VISIBLE else View.GONE
            searchQuery = text.toString()
        }
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
