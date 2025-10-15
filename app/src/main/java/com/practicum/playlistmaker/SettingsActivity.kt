package com.practicum.playlistmaker

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.button.MaterialButton

class SettingsActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

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
        emailIntent.data = Uri.parse("mailto:" + getString(R.string.support_email))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
        emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_text))

        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        } else {
            Toast.makeText(this, "На устройстве не установлен почтовый клиент", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openUserAgreement() {
        val url = getString(R.string.url_oferta)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "На устройстве не найден браузер для открытия ссылки", Toast.LENGTH_SHORT).show()
        }
    }
}