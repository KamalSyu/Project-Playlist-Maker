package com.practicum.playlistmaker.search.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.practicum.playlistmaker.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MediatekaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mediateca)

        findViewById<View>(R.id.back).setOnClickListener { finish() }
    }

}