package com.practicum.playlistmaker.domain.repository
// Назначение: реализация бизнес‑правил и сценариев использования.

// Управление воспроизведением
interface PlayerRepository {
    suspend fun prepare(url: String?)     // Подготовка к воспроизведению
    suspend fun play()                    // Начать воспроизведение
    suspend fun pause()                  // Приостановить
    suspend fun stop()                   // Остановить
    suspend fun reset()                  // Сбросить состояние
    fun seekTo(position: Long)          // Перейти к позиции
    fun isPlaying(): Boolean             // Проверяет, играет ли трек
    fun getCurrentPosition(): Long        // Текущая позиция
    fun setOnCompletionListener(listener: () -> Unit) // Обработчик завершения
}