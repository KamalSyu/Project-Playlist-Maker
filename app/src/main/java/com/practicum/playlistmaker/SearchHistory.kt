package com.practicum.playlistmaker

import Track
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistory(private val context: Context) {

    private val PREFS_NAME = "search_history_pref"
    private val HISTORY_KEY = "search_history"

    private val gson = Gson()
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Получить список истории
    fun getHistory(): MutableList<Track> {
        val json = prefs.getString(HISTORY_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Track>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    // Сохранить список истории
    private fun saveHistory(history: MutableList<Track>) {
        val json = gson.toJson(history)
        prefs.edit().putString(HISTORY_KEY, json).apply()
    }

    // Добавить трек в историю
    fun addTrack(track: Track) {
        Log.d("SearchHistory", "Добавляем трек: ${track.trackName}")
        val history = getHistory()
        // Проверка уникальности по trackId
        val existsIndex = history.indexOfFirst { it.trackId == track.trackId }
        if (existsIndex != -1) {
            // Удаляем старый экземпляр
            history.removeAt(existsIndex)
        }
        // Добавляем в начало
        history.add(0, track)
        saveHistory(history)
    }

    // Очистить историю
    fun clearHistory() {
        prefs.edit().remove(HISTORY_KEY).apply()
    }
}

//class SearchHistory(private val sharedPreferences: SharedPreferences) {
//    private val gson = Gson()
//
//    companion object {
//        private const val HISTORY_KEY = "history_key"
//    }
//
//    fun saveTrack(track: Track) {
//        // Читаем текущую историю
//        val history = getTracks()
//
//        // Проверяем, есть ли трек уже в истории
//        history.removeIf { it.trackId == track.trackId }
//
//        // Добавляем новый трек в начало списка
//        history.add(0, track)
//
//        // Ограничиваем количество треков до 10
//        if (history.size > 10) {
//            history.removeAt(history.size - 1)
//        }
//
//        // Сохраняем обновлённую историю в SharedPreferences
//        sharedPreferences.edit()
//            .putString(HISTORY_KEY, gson.toJson(history))
//            .apply()
//    }
//
//    fun getTracks(): MutableList<Track> {
//        val jsonString = sharedPreferences.getString(HISTORY_KEY, null)
//        return gson.fromJson(jsonString, Array<Track>::class.java).toMutableList() ?: mutableListOf()
//    }
//
//    fun clearHistory() {
//        sharedPreferences.edit().remove(HISTORY_KEY).apply()
//    }
//}


//    // Получить текущую историю (список треков)
//    fun getHistory(): List<Track> {
//        val json = sharedPreferences.getString(HISTORY_KEY, null)
//        return if (json.isNullOrEmpty()) {
//            emptyList()
//        } else {
//            val type = object : TypeToken<List<Track>>() {}.type
//            gson.fromJson(json, type)        }
//    }
//
//    private fun saveHistory(tracks: List<Track>) {
//        val json = gson.toJson(tracks)
//        sharedPreferences.edit().putString(HISTORY_KEY, json).apply()
//    }
//
//    // Добавить трек в историю, с удалением дубликатов и ограничением по размеру
//    fun addTrack(track: Track) {
//        val currentHistory = getHistory().toMutableList()
//
//        // Проверка на существование трека по trackId
//        val existingIndex = currentHistory.indexOfFirst { it.trackId == track.trackId }
//        if (existingIndex != -1) {
//            // Удаляем существующий трек
//            currentHistory.removeAt(existingIndex)
//        }
////
////        // Удаляем трек, если уже есть в истории (по trackId)
////        currentHistory.removeAll { it.trackId == track.trackId }
//
//        // Добавляем трек в начало списка
//        currentHistory.add(0, track)
//
//        // Ограничиваем размер списка
//        if (currentHistory.size > MAX_HISTORY_SIZE) {
//            currentHistory.removeAt(currentHistory.size - 1)
////            currentHistory.subList(MAX_HISTORY_SIZE, currentHistory.size).clear()
//        }
//        // Сохраняем обновлённый список
//        saveHistory(currentHistory)
//    }
//
//    // Очистить историю полностью
//    fun clearHistory() {
//        sharedPreferences.edit().remove(HISTORY_KEY).apply()
//    }





//    // Добавление трека в историю
//    fun addTrackToHistory(track: Track) {
//        val json = sharedPreferences.getString(key, null)
//        val type: Type = object : TypeToken<ArrayList<Track>>() {}.type
//        val trackList: ArrayList<Track> = gson.fromJson(json, type) ?: ArrayList()
//
//        // Удаляем трек с таким же trackId, если есть
//        trackList.removeIf { it.trackId == track.trackId }
//
//        // Добавляем в начало
//        trackList.add(0, track)
//
//        // Ограничение по размеру
//        if (trackList.size > maxSize) {
//            trackList.removeAt(trackList.size - 1)
//        }
//
//        // Сохраняем
//        sharedPreferences.edit()
//            .putString(key, gson.toJson(trackList))
//            .apply()
//    }
//
//    // Очистка истории
//    fun clearHistory() {
//        sharedPreferences.edit()
//            .remove(key)
//            .apply()
//    }
//
//    // Получение всей истории
//    fun getHistory(): List<Track> {
//        val json = sharedPreferences.getString(key, null)
//        val type: Type = object : TypeToken<ArrayList<Track>>() {}.type
//        val trackList: ArrayList<Track> = gson.fromJson(json, type) ?: ArrayList()
//        return trackList
//    }
//}


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
