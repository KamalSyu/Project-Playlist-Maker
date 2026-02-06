package com.practicum.playlistmaker.presentation.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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
        try {
            val shareText = shareAppUseCase()  // Теперь из strings.xml!

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // Показать ошибку
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Метод для отправки email поддержки
    private fun sendEmail() {
        try {
            val emailData = sendSupportEmailUseCase() ?: return
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(emailData.email))
                putExtra(Intent.EXTRA_SUBJECT, emailData.subject)
                putExtra(Intent.EXTRA_TEXT, emailData.body)
            }

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // Показать ошибку
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Метод для открытия пользовательского соглашения
    private fun openUserAgreement() {
        try {
            val url = getString(R.string.url_oferta)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // Можно показать Snackbar: "Не удалось открыть ссылку"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Можно показать сообщение об ошибке
        }
    }
}
