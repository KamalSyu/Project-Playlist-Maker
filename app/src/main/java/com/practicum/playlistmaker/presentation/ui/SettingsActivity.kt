package com.practicum.playlistmaker.presentation.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.android.material.switchmaterial.SwitchMaterial
import com.practicum.playlistmaker.App
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.usecase.GetThemeStateUseCaseContract
import com.practicum.playlistmaker.domain.usecase.SendSupportEmailUseCaseContract
import com.practicum.playlistmaker.domain.usecase.ShareAppUseCaseContract
import com.practicum.playlistmaker.domain.usecase.UseCaseCreator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    @Inject lateinit var useCaseCreator: UseCaseCreator

    private lateinit var themeSwitcher: SwitchMaterial
    private lateinit var getThemeStateUseCase: GetThemeStateUseCaseContract
    private lateinit var shareAppUseCase: ShareAppUseCaseContract
    private lateinit var sendSupportEmailUseCase: SendSupportEmailUseCaseContract

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        themeSwitcher = findViewById(R.id.switch_button)

        // Получаем Use Cases через Creator
        getThemeStateUseCase = useCaseCreator.createGetThemeStateUseCase()
        shareAppUseCase = useCaseCreator.createShareAppUseCase()
        sendSupportEmailUseCase = useCaseCreator.createSendSupportEmailUseCase()

        // Получаем текущее состояние темы через Use Case
        val isDarkMode = getThemeStateUseCase()
        themeSwitcher.isChecked = isDarkMode

        // Обработчик переключения темы
        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            (application as App).switchTheme(isChecked)
            recreate() // Пересоздание Activity для немедленного применения темы
        }

        // Обработчики кнопок
        findViewById<View>(R.id.back).setOnClickListener { finish() }
        findViewById<View>(R.id.btnShare).setOnClickListener { shareApp() }
        findViewById<View>(R.id.supportButton).setOnClickListener { sendEmail() }
        findViewById<View>(R.id.userAgreementButton).setOnClickListener { openUserAgreement() }
    }

    // Метод для рассылки ссылки на приложение
    private fun shareApp() {
        val shareText = shareAppUseCase() // Получаем текст через use case
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, shareText)
        startActivity(Intent.createChooser(intent, getString(R.string.choose_app)))
    }

    private fun sendEmail() {
        val emailData = sendSupportEmailUseCase() // Получаем данные через use case

        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:${emailData.email}") // Используем email из DTO
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailData.subject)
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailData.body)

        startActivity(emailIntent)
    }
    private fun openUserAgreement() {
        val url = getString(R.string.url_oferta)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

}
