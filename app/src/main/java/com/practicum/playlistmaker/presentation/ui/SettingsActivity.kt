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

        getThemeStateUseCase = useCaseCreator.createGetThemeStateUseCase()
        shareAppUseCase = useCaseCreator.createShareAppUseCase()
        sendSupportEmailUseCase = useCaseCreator.createSendSupportEmailUseCase()

        val isDarkMode = getThemeStateUseCase()
        themeSwitcher.isChecked = isDarkMode

        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            (application as App).switchTheme(isChecked)
            recreate()
        }
        // Обработчики кнопок
        findViewById<View>(R.id.back).setOnClickListener { finish() }
        findViewById<View>(R.id.btnShare).setOnClickListener { shareApp() }
        findViewById<View>(R.id.supportButton).setOnClickListener { sendEmail() }
        findViewById<View>(R.id.userAgreementButton).setOnClickListener { openUserAgreement() }
    }

    private fun shareApp() {
        val shareText = shareAppUseCase()
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, shareText)
        startActivity(Intent.createChooser(intent, getString(R.string.choose_app)))
    }

    private fun sendEmail() {
        val emailData = sendSupportEmailUseCase()

        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:${emailData.email}")
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
