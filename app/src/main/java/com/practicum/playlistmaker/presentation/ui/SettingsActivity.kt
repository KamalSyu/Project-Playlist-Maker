package com.practicum.playlistmaker.presentation.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import com.practicum.playlistmaker.App
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.model.SupportEmailIntentData
import com.practicum.playlistmaker.domain.usecase.UseCaseCreator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    @Inject
    lateinit var useCaseCreator: UseCaseCreator

    private lateinit var themeSwitcher: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        themeSwitcher = findViewById(R.id.switch_button)

        // Получаем состояние темы через use case
        val isDarkMode = useCaseCreator.createGetThemeStateUseCase()()
        themeSwitcher.isChecked = isDarkMode

        // Обработчик переключения темы
        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            (application as App).switchTheme(isChecked)
            recreate()  // Пересоздание Activity для немедленного применения темы
        }

        // Обработчики кнопок
        findViewById<View>(R.id.back).setOnClickListener { finish() }
        findViewById<View>(R.id.btnShare).setOnClickListener { shareApp() }
        findViewById<View>(R.id.supportButton).setOnClickListener { sendEmail() }
        findViewById<View>(R.id.userAgreementButton).setOnClickListener { openUserAgreement() }
    }

    // Метод для рассылки ссылки на приложение
    private fun shareApp() {
        val shareText = "Скачайте приложение: https://example.com/playlistmaker"
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, shareText)
        startActivity(Intent.createChooser(intent, getString(R.string.choose_app)))
    }

    // Метод для отправки email поддержки
    private fun sendEmail() {
        val data = SupportEmailIntentData(
            email = "support@example.com",
            subject = "Вопрос по приложению Playlist Maker",
            body = "Здравствуйте! У меня возникла проблема..."
        )

        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")  // Схема для email
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(data.email))      // Получатель
        intent.putExtra(Intent.EXTRA_SUBJECT, data.subject)         // Тема
        intent.putExtra(Intent.EXTRA_TEXT, data.body)             // Текст письма
        startActivity(intent)
    }

    // Метод для открытия пользовательского соглашения
    private fun openUserAgreement() {
        val url = getString(R.string.url_oferta)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}