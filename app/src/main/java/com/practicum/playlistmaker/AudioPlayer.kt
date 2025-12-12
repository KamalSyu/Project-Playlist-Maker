package com.practicum.playlistmaker

import Track
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.Constants.Companion.VIEW_TYPE_ALBUM

class AudioPlayer: AppCompatActivity()  {
    @SuppressLint("MissingInflatedId")

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audioplayer)

        recyclerView = findViewById(R.id.recyclerViewAudioPlayer)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        val track = intent.getSerializableExtra("track") as Track
        val trackList = ArrayList<Track>().apply { add(track) } // Добавляем трек в список

        val adapter = TrackAdapter(trackList, VIEW_TYPE_ALBUM) { track ->
        }
        recyclerView.adapter = adapter

        findViewById<TextView>(R.id.back).setOnClickListener {
            finish()
        }
    }
}