package com.codetech.speechtotext.Helper

import android.content.Context
import android.content.SharedPreferences
import android.icu.text.Transliterator.Position
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.codetech.speechtotext.models.TranslationData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.http.POST

class SharedPrefHelper(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("translation_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val HISTORY_KEY = "history"


    fun saveTranslationToHistory(translation: TranslationData) {
        val history = getHistory().toMutableList()

        // Find the index of any existing entry that matches the translation
        val existingIndex = history.indexOfFirst {
            it.inputText == translation.inputText && it.resultText == translation.resultText
        }

        if (existingIndex != -1) {
            // If found, replace the existing entry
            history[existingIndex] = translation
        } else {
            // If not found, add the translation to the top of the list
            history.add(0, translation)
        }

        // Save the updated history list back to SharedPreferences
        sharedPreferences.edit().putString(HISTORY_KEY, gson.toJson(history)).apply()

    }


    fun getHistory(): List<TranslationData> {
        val historyJson = sharedPreferences.getString(HISTORY_KEY, "[]")
        val type = object : TypeToken<List<TranslationData>>() {}.type
        return gson.fromJson(historyJson, type) ?: emptyList()
    }

    fun removeTranslationFromHistory(translation: TranslationData) {
        val histories = getHistory().toMutableList()
        histories.removeAll { it.inputText == translation.inputText && it.resultText == translation.resultText }
        sharedPreferences.edit().putString(HISTORY_KEY, gson.toJson(histories)).apply()


    }

    fun clearHistory() {
        sharedPreferences.edit().putString(HISTORY_KEY, "[]").apply()


    }
}