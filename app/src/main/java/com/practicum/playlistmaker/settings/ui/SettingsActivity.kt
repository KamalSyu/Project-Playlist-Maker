package com.practicum.playlistmaker.settings.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.contract.SendSupportEmailUseCaseContract
import com.practicum.playlistmaker.core.contract.ShareAppUseCaseContract
import com.practicum.playlistmaker.core.usecase.UseCaseCreator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Активность для отображения и управления настройками приложения.
 * Реализует функционал:
 * - переключения темы (тёмная/светлая);
 * - отправки email в поддержку;
 * - шаринга приложения;
 * - открытия пользовательского соглашения.
 *
 * Использует ViewModel для управления состоянием UI и взаимодействия с бизнес‑логикой.
 */
@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    @Inject
    lateinit var useCaseCreator: UseCaseCreator

    private lateinit var viewModel: SettingsViewModel
    private lateinit var shareAppUseCase: ShareAppUseCaseContract
    private lateinit var sendSupportEmailUseCase: SendSupportEmailUseCaseContract

    // Флаг для отслеживания пересоздания активности (например, при смене конфигурации)
    private var isRecreating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupUseCases()
        setupViewModel()
        setupViews()
        observeUiState()
    }

    /**
     * Инициализирует Use Cases для шаринга и отправки email.
     */
    private fun setupUseCases() {
        shareAppUseCase = useCaseCreator.createShareAppUseCase()
        sendSupportEmailUseCase = useCaseCreator.createSendSupportEmailUseCase()
    }

    /**
     * Создаёт экземпляр ViewModel для управления состоянием UI.
     */
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
    }

    /**
     * Настраивает UI‑компоненты и обработчики кликов:
     * - переключатель темы;
     * - кнопка «назад»;
     * - кнопка шаринга;
     * - кнопка поддержки;
     * - кнопка пользовательского соглашения.
     */
    private fun setupViews() {
        val switchButton = findViewById<SwitchMaterial>(R.id.switch_button)

        // Обработчик переключения темы
        switchButton.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onThemeSwitch(isChecked)
        }

        // Кнопка «назад» — закрывает активность
        findViewById<View>(R.id.back).setOnClickListener { finish() }

        // Кнопка шаринга приложения
        findViewById<View>(R.id.btnShare).setOnClickListener { shareApp() }

        // Кнопка отправки email в поддержку
        findViewById<View>(R.id.supportButton).setOnClickListener { sendEmail() }

        // Кнопка открытия пользовательского соглашения
        findViewById<View>(R.id.userAgreementButton).setOnClickListener { openUserAgreement() }
    }

    /**
     * Подписывается на изменения состояния UI из ViewModel:
     * - отображает индикатор загрузки;
     * - обновляет состояние переключателя темы и применяет тему динамически.
     */
    private fun observeUiState() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is SettingsUiState.Loading -> {
                    // Показываем индикатор загрузки (если есть)
                }
                is SettingsUiState.Loaded -> {
                    // Устанавливаем состояние переключателя согласно настройкам
                    findViewById<SwitchMaterial>(R.id.switch_button).isChecked = state.isDarkTheme
                    // Применяем тему динамически
                    applyThemeDynamically(state.isDarkTheme)
                }
            }
        }
    }

    /**
     * Применяет тему интерфейса динамически через AppCompatDelegate.
     *
     * @param isDarkMode флаг, указывающий на режим темы:
     *   - true — тёмная тема (MODE_NIGHT_YES);
     *   - false — светлая тема (MODE_NIGHT_NO).
     */
    private fun applyThemeDynamically(isDarkMode: Boolean) {
        val mode = if (isDarkMode) MODE_NIGHT_YES else MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    /**
     * Выполняет шаринг приложения:
     * - получает текст для шаринга через Use Case;
     * - создаёт Intent для отправки текста;
     * - запускает диалог выбора приложения для шаринга.
     */
    private fun shareApp() {
        val shareText = shareAppUseCase()
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, shareText)
        startActivity(Intent.createChooser(intent, getString(R.string.choose_app)))
    }

    /**
     * Отправляет email в поддержку:
     * - получает данные для email через Use Case (адрес, тема, тело);
     * - создаёт Intent с действием ACTION_SENDTO;
     * - заполняет поля email (получатель, тема, текст);
     * - запускает email‑клиент.
     */
    private fun sendEmail() {
        val emailData = sendSupportEmailUseCase()

        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = "mailto:".toUri()
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailData.email))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailData.subject)
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailData.body)

        startActivity(emailIntent)
    }

    /**
     * Открывает пользовательское соглашение в браузере:
     * - берёт URL из строковых ресурсов;
     * - создаёт Intent с действием ACTION_VIEW;
     * - запускает браузер для отображения страницы.
     */
    private fun openUserAgreement() {
        val url = getString(R.string.url_oferta)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
