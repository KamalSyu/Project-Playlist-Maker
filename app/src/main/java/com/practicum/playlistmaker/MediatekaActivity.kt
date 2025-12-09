package com.practicum.playlistmaker

import Track
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MediatekaActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")


    override fun onCreate(savedInstanceState: Bundle?) {
        val track = intent.getSerializableExtra("track") as Track

        // Устанавливаем значения в элементы интерфейса
        findViewById<ImageView>(R.id.album).setImageResource(track.artworkUrl100) // предполагаем, что есть поле для изображения альбома
        findViewById<TextView>(R.id.textAlbumName).text = track.trackName
        findViewById<TextView>(R.id.textSingerName).text = track.artistName
        // Для кнопок можно установить слушателей событий, если нужно
        findViewById<Button>(R.id.ic_play_button).setOnClickListener { /* логика воспроизведения */ }
        findViewById<Button>(R.id.ic_button_plus).setOnClickListener { /* логика добавления в плейлист */ }
        findViewById<Button>(R.id.ic_button_like).setOnClickListener { /* логика лайка */ }
        findViewById<TextView>(R.id.time).text = track.trackTimeMillis.toString()
        findViewById<TextView>(R.id.trackTimeMillis).text = track.trackTimeMillis.toString()
        findViewById<TextView>(R.id.collectionName).text = track.collectionName
        findViewById<TextView>(R.id.releaseDate).text = track.releaseDate
        findViewById<TextView>(R.id.primaryGenreName).text = track.primaryGenreName
        findViewById<TextView>(R.id.country).text = track.country
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mediateca)

        findViewById<TextView>(R.id.back).setOnClickListener {
            finish()
        }
    }
}