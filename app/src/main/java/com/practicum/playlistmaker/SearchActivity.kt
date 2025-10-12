package com.practicum.playlistmaker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SearchActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        findViewById<TextView>(R.id.back).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            //startActivity(intent) Для выхода с экрана стоит использовать функцию finish(),
            // а не startActivity, что создаст новую активити и откроет её.
            // Принципиальная разница заключается в том, что в твоём коде вместо
            // возврата открывается и создаётся новый экран с новым состоянием,
            // а при использовании finish() закрывается текущий экран и показывается
            // предыдущий, который был открыт перед текущим.

            finish()
        }

        val searchEditText = findViewById<EditText>(R.id.search_edit_text)
        val resetButton = findViewById<ImageView>(R.id.reset_button)

        resetButton.setOnClickListener {
            searchEditText.setText("") // Очищаем текст в EditText
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Заглушка для будущих задач
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                resetButton.visibility = if (s?.isNotEmpty() == true) View.VISIBLE else View.GONE
            }

            override fun afterTextChanged(s: Editable?) {
                // Заглушка для будущих задач
            }
        })


    }
}