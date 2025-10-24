package com.practicum.playlistmaker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate


class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
    fun goToSettingsActivity(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun goToSearchActivity(view: View) {
        val intent = Intent(this, SearchActivity::class.java)
        startActivity(intent)
    }

    fun goToMediatekaActivity(view: View) {
        val intent = Intent(this, MediatekaActivity::class.java)
        startActivity(intent)
    }


}
