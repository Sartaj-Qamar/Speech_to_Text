package com.codetech.speechtotext.VM

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SpeechToTextViewModel : ViewModel() {
    val translatedTextLiveData = MutableLiveData<String>()

    fun updateTranslatedText(text: String) {
        translatedTextLiveData.value = text
    }
}
