package com.practicum.playlistmaker.main.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.practicum.playlistmaker.R
import androidx.activity.enableEdgeToEdge

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Настройка edge-to-edge для всего приложения
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)

        try {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            navController = navHostFragment.navController
            bottomNavigationView = findViewById(R.id.bottom_navigation)

            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.mediatekaFragment,
                    R.id.searchFragment,
                    R.id.settingsFragment,
                    R.id.audioPlayerFragment
                    )
            )

            // Настраиваем видимость для начального экрана
            updateToolbarAndBottomNavVisibility(navController.currentDestination?.id)

            // Подписываемся на изменения навигации
            setupNavigationVisibility()

            bottomNavigationView.setupWithNavController(navController)

            Log.d("MainActivity", "Navigation setup completed successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing navigation", e)
            Toast.makeText(
                this,
                "Ошибка навигации: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Обновляет видимость тулбара и BottomNavigation для указанного экрана
     */
    private fun updateToolbarAndBottomNavVisibility(destinationId: Int?) {
        when (destinationId) {
            R.id.audioPlayerFragment -> {
                toolbar.visibility = View.GONE  // Скрываем по умолчанию
                supportActionBar?.hide()
                bottomNavigationView.visibility = View.GONE
            }
            else -> {
                // Скрываем тулбар на всех остальных экранах
                toolbar.visibility = View.GONE
                supportActionBar?.hide()
                bottomNavigationView.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Отслеживание изменений навигации для управления видимостью элементов
     */
    private fun setupNavigationVisibility() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateToolbarAndBottomNavVisibility(destination.id)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        val currentDestinationId = navController.currentDestination?.id
        if (currentDestinationId == R.id.mediatekaFragment) {
            finish()
        } else {
            super.onBackPressed()
        }
    }
    fun getToolbar(): Toolbar {
        return toolbar
    }

}
