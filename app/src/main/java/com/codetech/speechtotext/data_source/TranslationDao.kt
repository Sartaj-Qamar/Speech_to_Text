package com.codetech.speechtotext.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update

@Dao
interface TranslationDao {
    @Query("SELECT * FROM translations ORDER BY timestamp DESC")
    suspend fun getAllTranslations(): List<Translation>

    @Query("SELECT * FROM translations WHERE isFavorite = 1 ORDER BY timestamp DESC")
    suspend fun getFavorites(): List<Translation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslation(translation: Translation)

    @Update
    suspend fun updateTranslation(translation: Translation)


    @Query("Update translations set isFavorite = :isFavorite where timestamp = :timestamp")
    fun updateTranslationNew(timestamp: Long, isFavorite: Boolean)


    @Delete
    suspend fun deleteTranslation(translation: Translation)

    @Query("DELETE FROM translations")
    suspend fun deleteAllTranslations()
}
