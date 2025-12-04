package com.practicum.playlistmaker

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Получаем сохранённое состояние переключателя
        val preferences = getSharedPreferences("SettingsPref", MODE_PRIVATE)
        val isDarkMode = preferences.getBoolean("isDarkMode", false)


        findViewById<TextView>(R.id.back).setOnClickListener {
            finish()
        }

        val btnShare = findViewById<TextView>(R.id.btnShare)
        btnShare.setOnClickListener {
            shareApp()
        }

        val supportButton = findViewById<TextView>(R.id.supportButton)
        supportButton.setOnClickListener {
            sendEmail()
        }
        val userAgreementButton = findViewById<TextView>(R.id.userAgreementButton)
        userAgreementButton.setOnClickListener {
            openUserAgreement()
        }

        val themeSwitcher = findViewById<SwitchMaterial>(R.id.switch_button)
        themeSwitcher.isChecked = isDarkMode // Устанавливаем состояние переключателя

        // Добавляем слушатель на изменение состояния
        themeSwitcher.setOnCheckedChangeListener { switcher, checked ->
            (applicationContext as App).switchTheme(checked)
            with(preferences.edit()) {
                putBoolean("isDarkMode", checked)
                apply()
            }
        }
    }

    private fun shareApp() {
        val shareText = getString(R.string.share_text)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, shareText)
        startActivity(Intent.createChooser(intent, getString(R.string.choose_app)))
    }

    private fun sendEmail() {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = "mailto:".toUri()
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_email)))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
        emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_text))

        startActivity(emailIntent)
    }


    private fun openUserAgreement() {
        val url = getString(R.string.url_oferta)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
