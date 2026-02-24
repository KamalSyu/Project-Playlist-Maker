package com.practicum.playlistmaker.sharing.domain.usecase

import com.practicum.playlistmaker.core.contract.ShareAppUseCaseContract
import com.practicum.playlistmaker.sharing.domain.provider.ShareTextProvider  // Правильный импорт
import javax.inject.Inject

/**
 * Use Case для получения текста для шаринга приложения.
 * Делегирует задачу по формированию текста провайдеру данных.
 * Соответствует принципам чистой архитектуры: изолирует бизнес‑логику получения текста шаринга.
 *
 * @param shareTextProvider провайдер, отвечающий за предоставление текста для шаринга
 */
class ShareAppUseCase @Inject constructor(
    private val shareTextProvider: ShareTextProvider
) : ShareAppUseCaseContract {

    /**
     * Выполняет получение текста для шаринга приложения.
     *
     * Последовательность действий:
     * 1. Вызывает метод `getShareText()` у провайдера `shareTextProvider`.
     * 2. Возвращает полученный текст в виде строки.
     *
     * Этот метод является основным интерфейсом для получения текста шаринга
     * из других слоёв приложения (например, из ViewModel или Activity).
     *
     * @return строка с текстом для шаринга приложения, которая будет передана в Intent.
     *         Пример возвращаемого значения:
     *         "Проверьте это потрясающее приложение для создания плейлистов! [ссылка]"
     */
    override operator fun invoke(): String {
        return shareTextProvider.getShareText()
    }
}