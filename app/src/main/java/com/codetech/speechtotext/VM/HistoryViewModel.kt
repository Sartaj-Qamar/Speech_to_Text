package com.codetech.speechtotext.VM

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HistoryViewModel : ViewModel() {
    private val _history = MutableLiveData<MutableList<String>>(mutableListOf())
    val history: LiveData<MutableList<String>> = _history

    fun addToHistory(word: String) {
        if (!_history.value!!.contains(word)) {
            _history.value!!.add(word)
            _history.value = _history.value // Trigger LiveData observer
        }
    }
}
