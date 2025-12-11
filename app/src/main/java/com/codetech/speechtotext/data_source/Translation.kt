package com.codetech.speechtotext.data_source

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translations")
data class Translation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val inputText: String,
    val resultText: String,
    val sourceLang: String?,
    val targetLang: String?,
    var isFavorite: Boolean = false,
    var timestamp: Long
)