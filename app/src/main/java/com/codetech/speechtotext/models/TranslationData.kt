package com.codetech.speechtotext.models

data class TranslationData(
    val sourceLang: String,
    val targetLang: String,
    val inputText: String,
    val resultText: String,
    var isFavorite: Boolean = false
)

