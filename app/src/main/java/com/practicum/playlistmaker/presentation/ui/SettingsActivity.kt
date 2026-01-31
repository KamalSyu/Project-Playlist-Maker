package com.practicum.playlistmaker.presentation.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.drawable.DrawableCompat.applyTheme
import com.google.android.material.switchmaterial.SwitchMaterial
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.usecase.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    // Внедряем UseCase через Hilt
    @Inject lateinit var switchThemeUseCase: SwitchThemeUseCase
    @Inject lateinit var getThemeStateUseCase: GetThemeStateUseCase
    @Inject lateinit var shareAppUseCase: ShareAppUseCase
    @Inject lateinit var sendSupportEmailUseCase: SendSupportEmailUseCase

    private lateinit var themeSwitcher: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<View>(R.id.back).setOnClickListener { finish() }
        findViewById<View>(R.id.btnShare).setOnClickListener { shareApp() }
        findViewById<View>(R.id.supportButton).setOnClickListener { sendEmail() }
        findViewById<View>(R.id.userAgreementButton).setOnClickListener { openUserAgreement() }

        themeSwitcher = findViewById(R.id.switch_button)

        val isDarkMode = getThemeStateUseCase()
        themeSwitcher.isChecked = isDarkMode

        applyTheme(isDarkMode)

        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            Log.d("Theme", "Switch changed to: $isChecked")

            // Сохраняем новую тему
            switchThemeUseCase(isChecked)
            // Немедленно применяем к текущей активности
            applyTheme(isChecked)
            // Пересоздаём для полного обновления интерфейса
            recreate()
        }
    }
    // Метод для применения темы
    private fun applyTheme(isDarkMode: Boolean) {
        val mode = if (isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }


    private fun shareApp() {
        val shareText = shareAppUseCase()
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, shareText)
        startActivity(Intent.createChooser(intent, getString(R.string.choose_app)))
    }

    private fun sendEmail() {
        val data = sendSupportEmailUseCase()
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(data.email))
        intent.putExtra(Intent.EXTRA_SUBJECT, data.subject)
        intent.putExtra(Intent.EXTRA_TEXT, data.body)
        startActivity(intent)
    }

    private fun openUserAgreement() {
        val url = getString(R.string.url_oferta)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
