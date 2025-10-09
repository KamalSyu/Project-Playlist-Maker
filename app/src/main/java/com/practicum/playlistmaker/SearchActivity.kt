package com.practicum.playlistmaker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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
    }
}