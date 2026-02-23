package com.practicum.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import com.practicum.playlistmaker.core.usecase.UseCaseCreator
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Главный класс приложения, расширяющий Application.
 * Отвечает за инициализацию приложения и применение темы интерфейса
 * при старте (до отображения любого UI).
 *
 * Использует Hilt для внедрения зависимостей и Use Case для получения
 * сохранённого состояния темы.
 */
@HiltAndroidApp
class App : Application() {

    /**
     * Фабрика для создания Use Cases бизнес‑логики.
     * Внедряется через Dagger Hilt.
     */
    @Inject lateinit var useCaseCreator: UseCaseCreator

    override fun onCreate() {
        super.onCreate()
        // Загружаем и применяем сохранённую тему при старте приложения
        loadAndApplyTheme()
    }

    /**
     * Применяет указанный режим темы через AppCompatDelegate.
     *
     * @param isDarkMode флаг, определяющий режим темы:
     *   - true — тёмная тема (MODE_NIGHT_YES);
     *   - false — светлая тема (MODE_NIGHT_NO).
     */
    private fun applyTheme(isDarkMode: Boolean) {
        val mode = if (isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    /**
     * Загружает сохранённое состояние темы и применяет его, если оно отличается
     * от текущего режима.
     *
     * Последовательность действий:
     * 1. Создаёт Use Case `GetThemeStateUseCase` для получения состояния темы.
     * 2. Получает флаг тёмной темы (`isDarkMode`) из хранилища.
     * 3. Определяет целевой режим темы на основе флага.
     * 4. Сравнивает текущий режим темы (`AppCompatDelegate.getDefaultNightMode()`)
     *    с целевым режимом.
     * 5. Если режимы различаются, вызывает `applyTheme()` для применения новой темы.
     *
     * Это гарантирует, что:
     * - приложение запускается с сохранённой темой пользователя (а не с системной);
     * - тема применяется единообразно во всех Activity;
     * - избегаются лишние вызовы `setDefaultNightMode()`, если тема не изменилась.
     */
    private fun loadAndApplyTheme() {
        // Получаем текущее состояние темы через Use Case
        val isDarkMode = useCaseCreator.createGetThemeStateUseCase()()

        // Текущий режим темы, установленный в приложении
        val currentMode = AppCompatDelegate.getDefaultNightMode()

        // Целевой режим темы на основе сохранённых настроек
        val targetMode = if (isDarkMode) MODE_NIGHT_YES else MODE_NIGHT_NO

        // Применяем тему только если текущий режим отличается от целевого
        if (currentMode != targetMode) {
            applyTheme(isDarkMode)
        }
    }
}
