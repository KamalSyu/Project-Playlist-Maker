package com.practicum.playlistmaker

import Track
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class SearchHistory(private val sharedPreferences: SharedPreferences) {

    private val gson = Gson()
    private val maxSize = 10
    private val key = "track_history"

    // Добавление трека в историю
    fun addTrackToHistory(track: Track) {
        val json = sharedPreferences.getString(key, null)
        val type: Type = object : TypeToken<ArrayList<Track>>() {}.type
        val trackList: ArrayList<Track> = gson.fromJson(json, type) ?: ArrayList()

        // Удаляем трек с таким же trackId, если есть
        trackList.removeIf { it.trackId == track.trackId }

        // Добавляем в начало
        trackList.add(0, track)

        // Ограничение по размеру
        if (trackList.size > maxSize) {
            trackList.removeAt(trackList.size - 1)
        }

        // Сохраняем
        sharedPreferences.edit()
            .putString(key, gson.toJson(trackList))
            .apply()
    }

    // Очистка истории
    fun clearHistory() {
        sharedPreferences.edit()
            .remove(key)
            .apply()
    }

    // Получение всей истории
    fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(key, null)
        val type: Type = object : TypeToken<ArrayList<Track>>() {}.type
        val trackList: ArrayList<Track> = gson.fromJson(json, type) ?: ArrayList()
        return trackList
    }
}


//    fun addTrackToHistory(track: Track) {
//        val gson = Gson() // Создаем экземпляр Gson для работы с JSON
//        val editor = sharedPreferences.edit() // Получаем редактор для SharedPreferences
//
//        // Получаем текущий список треков из Shared Preferences
//        val json = sharedPreferences.getString(HISTORY_KEY, null)
//        val trackListType = object : TypeToken<ArrayList<Track>>() {}.type
//        val trackList: ArrayList<Track> = gson.fromJson(json, trackListType) ?: ArrayList()
//
//        // Проверяем, есть ли трек уже в истории, и удаляем его, если он там есть
//        trackList.removeIf { it.trackId == track.trackId }
//
//        // Добавляем трек в начало списка
//        trackList.add(0, track)
//
//        // Ограничиваем количество треков в истории до 10
//        if (trackList.size > 10) {
//            trackList.removeAt(trackList.size - 1)
//        }
//
//        // Сохраняем обновлённый список в Shared Preferences
//        editor.putString(HISTORY_KEY, gson.toJson(trackList))
//        editor.apply()
//    }
//
//    fun clearHistory() {
//        sharedPreferences.edit().remove(HISTORY_KEY)
//            .apply() // Удаляем ключ с историей из SharedPreferences
//    }
//}

//    private val gson = Gson()
//
//    fun saveSearchHistory(track: Track) {
//        // Получаем текущую историю поиска
//        val history = getSearchHistory()
//
//        // Проверяем, есть ли в истории трек с таким же trackId
//        if (history.any { it.trackId == track.trackId }) {
//            // Если такой трек есть, удаляем его из истории
//            history.removeAll { it.trackId == track.trackId }
//        }
//
//        // Добавляем новый трек в начало списка
//        history.add(0, track)
//
//        // Ограничиваем количество треков в истории до 10
//        if (history.size > 10) {
//            // Удаляем последний элемент, если количество треков больше 10
//            history.removeAt(10)
//        }
//
//        // Сохраняем обновлённую историю в Shared Preferences
//        sharedPreferences.edit()
//            .putString(HISTORY_KEY, gson.toJson(history))
//            .apply()
//    }
//
//
//    fun getSearchHistory(): MutableList<Track> {
//        return gson.fromJson(sharedPreferences.getString(HISTORY_KEY, "[]"), object : TypeToken<MutableList<Track>>() {}.type)
//    }
//
//    fun clearSearchHistory() {
//        sharedPreferences.edit().clear().apply()
//    }
