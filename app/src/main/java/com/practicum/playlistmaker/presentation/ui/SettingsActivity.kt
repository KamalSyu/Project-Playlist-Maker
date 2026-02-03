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
import com.practicum.playlistmaker.domain.usecase.GetThemeStateUseCaseContract
import com.practicum.playlistmaker.domain.usecase.SendSupportEmailUseCaseContract
import com.practicum.playlistmaker.domain.usecase.ShareAppUseCaseContract
import com.practicum.playlistmaker.domain.usecase.UseCaseCreator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject



@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    @Inject
    lateinit var useCaseCreator: UseCaseCreator

    private lateinit var themeSwitcher: SwitchMaterial

    // Use Cases через Creator
    private lateinit var getThemeStateUseCase: GetThemeStateUseCaseContract
    private lateinit var shareAppUseCase: ShareAppUseCaseContract
    private lateinit var sendSupportEmailUseCase: SendSupportEmailUseCaseContract

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        themeSwitcher = findViewById(R.id.switch_button)

        // Инициализация Use Cases
        getThemeStateUseCase = useCaseCreator.createGetThemeStateUseCase()
        shareAppUseCase = useCaseCreator.createShareAppUseCase()
        sendSupportEmailUseCase = useCaseCreator.createSendSupportEmailUseCase()


        // Получаем состояние темы через use case
        val isDarkMode = getThemeStateUseCase()
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

    // Метод для рассылки ссылки на приложение (через Use Case)
    private fun shareApp() {
        val shareText = shareAppUseCase() // Вызов Use Case
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, shareText)
        startActivity(Intent.createChooser(intent, getString(R.string.choose_app)))
    }

    // Метод для отправки email поддержки (через Use Case)
    private fun sendEmail() {
        val data = sendSupportEmailUseCase() // Вызов Use Case


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