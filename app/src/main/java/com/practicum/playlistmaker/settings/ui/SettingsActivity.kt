//package com.practicum.playlistmaker.settings.ui
//
//import android.content.Intent
//import android.net.Uri
//import android.os.Bundle
//import android.view.View
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.appcompat.app.AppCompatDelegate
//import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
//import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import androidx.core.view.updatePadding
//import com.google.android.material.switchmaterial.SwitchMaterial
//import com.practicum.playlistmaker.R
//import com.practicum.playlistmaker.settings.ui.view.SettingsViewModel
//import org.koin.androidx.viewmodel.ext.android.viewModel
//
///**
// * Активность для отображения и управления настройками приложения.
// * Реализует функционал:
// * - переключения темы (тёмная/светлая);
// * - отправки email в поддержку;
// * - шаринга приложения;
// * - открытия пользовательского соглашения.
// *
// * Использует ViewModel для управления состоянием UI и взаимодействия с бизнес‑логикой.
// */
//class SettingsActivity : AppCompatActivity() {
//
//
//    private val viewModel: SettingsViewModel by viewModel()
//
//    // Флаг для отслеживания пересоздания активности (например, при смене конфигурации)
//    private var isRecreating = false
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        enableEdgeToEdge()
//
//        setContentView(R.layout.activity_settings)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
//            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
//            view.updatePadding(top = statusBar.top)
//            insets
//        }
//        setupViews()
//        observeUiState()
//    }
//    /**
//     * Настраивает UI‑компоненты и обработчики кликов:
//     * - переключатель темы;
//     * - кнопка «назад»;
//     * - кнопка шаринга;
//     * - кнопка поддержки;
//     * - кнопка пользовательского соглашения.
//     */
//    private fun setupViews() {
//        val switchButton = findViewById<SwitchMaterial>(R.id.switch_button)
//
//        // Обработчик переключения темы
//        switchButton.setOnCheckedChangeListener { _, isChecked ->
//            viewModel.onThemeSwitch(isChecked)
//        }
//
//        // Кнопка «назад» — закрывает активность
//        findViewById<View>(R.id.back).setOnClickListener { finish() }
//
//        // Кнопка шаринга приложения
//        findViewById<View>(R.id.btnShare).setOnClickListener { viewModel.onShareRequested() }
//
//        // Кнопка отправки email в поддержку
//        findViewById<View>(R.id.supportButton).setOnClickListener { viewModel.onEmailRequested() }
//
//        // Кнопка открытия пользовательского соглашения
//        findViewById<View>(R.id.userAgreementButton).setOnClickListener { openUserAgreement() }
//    }
//
//    /**
//     * Подписывается на изменения состояния UI из ViewModel:
//     * - отображает индикатор загрузки;
//     * - обновляет состояние переключателя темы и применяет тему динамически.
//     */
//    private fun observeUiState() {
//        viewModel.uiState.observe(this) { state ->
//            when (state) {
//                is SettingsUiState.Loading -> {
//                    // Показываем индикатор загрузки (если есть)
//                }
//
//                is SettingsUiState.Loaded -> {
//                    // Устанавливаем состояние переключателя согласно настройкам
//                    findViewById<SwitchMaterial>(R.id.switch_button).isChecked = state.isDarkTheme
//                    // Применяем тему динамически
//                    applyThemeDynamically(state.isDarkTheme)
//                }
//            }
//            // Наблюдаем за событиями шаринга
//            viewModel.shareApp.observe(this) { intent ->
//                startActivity(Intent.createChooser(intent, getString(R.string.choose_app)))
//            }
//
//            // Наблюдаем за событиями email
//            viewModel.sendEmail.observe(this) { intent ->
//                startActivity(intent)
//            }
//        }
//    }
//
//    /**
//     * Применяет тему интерфейса динамически через AppCompatDelegate.
//     *
//     * @param isDarkMode флаг, указывающий на режим темы:
//     *   - true — тёмная тема (MODE_NIGHT_YES);
//     *   - false — светлая тема (MODE_NIGHT_NO).
//     */
//    private fun applyThemeDynamically(isDarkMode: Boolean) {
//        val mode = if (isDarkMode) MODE_NIGHT_YES else MODE_NIGHT_NO
//        AppCompatDelegate.setDefaultNightMode(mode)
//    }
//
//    /**
//     * Открывает пользовательское соглашение в браузере:
//     * - берёт URL из строковых ресурсов;
//     * - создаёт Intent с действием ACTION_VIEW;
//     * - запускает браузер для отображения страницы.
//     */
//    private fun openUserAgreement() {
//        val url = getString(R.string.url_oferta)
//        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//        startActivity(intent)
//    }
//}
