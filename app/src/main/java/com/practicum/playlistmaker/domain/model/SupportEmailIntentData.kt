package com.practicum.playlistmaker.domain.model

//Назначение: данные для отправки email через Intent.

data class SupportEmailIntentData(
    val email: String,           // Адрес получателя
    val subject: String,         // Тема письма
    val body: String             // Тело письма
)