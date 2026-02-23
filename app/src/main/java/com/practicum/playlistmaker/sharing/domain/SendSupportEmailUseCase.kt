package com.practicum.playlistmaker.sharing.domain

import com.practicum.playlistmaker.sharing.domain.model.SupportEmailIntentData
import com.practicum.playlistmaker.sharing.domain.provider.SupportEmailDataProvider
import com.practicum.playlistmaker.core.contract.SendSupportEmailUseCaseContract
import javax.inject.Inject

/**
 * Use Case для получения данных для отправки email в поддержку.
 * Делегирует задачу провайдеру данных и возвращает структурированную информацию,
 * необходимую для создания Intent отправки email.
 *
 * Соответствует принципу единственной ответственности: выполняет только получение данных,
 * не занимается созданием или запуском Intent.
 *
 * @param supportEmailDataProvider провайдер, предоставляющий данные для email (адрес, тема, тело письма)
 */
class SendSupportEmailUseCase @Inject constructor(
    private val supportEmailDataProvider: SupportEmailDataProvider
) : SendSupportEmailUseCaseContract {

    /**
     * Выполняет получение данных для email в поддержку.
     *
     * Последовательность действий:
     * 1. Вызывает метод `getEmailData()` у провайдера данных.
     * 2. Возвращает объект `SupportEmailIntentData`, содержащий:
     *    - email — адрес получателя;
     *    - subject — тему письма;
     *    - body — текст сообщения.
     *
     * Данные используются в UI‑слое для формирования Intent типа ACTION_SENDTO,
     * который запускает email‑клиент с предварительно заполненными полями.
     *
     * @return объект SupportEmailIntentData с данными для создания email Intent
     */
    override operator fun invoke(): SupportEmailIntentData {
        return supportEmailDataProvider.getEmailData()
    }
}
