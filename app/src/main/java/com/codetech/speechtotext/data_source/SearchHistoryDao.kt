package com.codetech.speechtotext.data_source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SearchHistoryDao {
    @Insert
    suspend fun insertSearchHistory(searchHistory: SearchHistory)

    @Query("SELECT * FROM search_history ORDER BY id DESC")
    suspend fun getAllSearchHistory(): List<SearchHistory>

    @Query("DELETE FROM search_history")
    suspend fun deleteAllHistory()
}